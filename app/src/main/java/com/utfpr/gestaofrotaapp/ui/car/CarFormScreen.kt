package com.utfpr.gestaofrotaapp.ui.car

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.IconButton
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import com.utfpr.gestaofrotaapp.data.model.Car
import com.utfpr.gestaofrotaapp.data.model.Place
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CarFormScreen(
    modifier: Modifier = Modifier,
    initialCar: Car? = null,
    carIdForEdit: String? = null,
    onBack: () -> Unit,
    onSaved: (Car) -> Unit = {},
    viewModel: CarFormViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
        key = carIdForEdit ?: initialCar?.id ?: "new",
        factory = CarFormViewModelFactory(initialCar = initialCar, carIdForEdit = carIdForEdit)
    )
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    LaunchedEffect(initialCar, carIdForEdit) {
        if (initialCar == null && carIdForEdit == null) {
            viewModel.resetIfNew()
        }
    }

    val hasFineLocation = remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED
        )
    }
    val hasCoarseLocation = remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED
        )
    }
    val hasLocationPermission = hasFineLocation.value || hasCoarseLocation.value

    val requestLocationPermissions = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
        onResult = { result ->
            hasFineLocation.value = result[Manifest.permission.ACCESS_FINE_LOCATION] == true
            hasCoarseLocation.value = result[Manifest.permission.ACCESS_COARSE_LOCATION] == true
            val grantedNow = hasFineLocation.value || hasCoarseLocation.value
            if (!grantedNow) {
                scope.launch {
                    snackbarHostState.showSnackbar(
                        message = "Permissão de localização negada. Ative nas configurações para usar \"minha localização\".",
                        duration = SnackbarDuration.Long
                    )
                }
            }
        }
    )

    val pickVisualMedia = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            if (uri != null) {
                tryPersistReadPermission(context, uri)
                viewModel.onImageSelected(uri)
            }
        }
    )
    val pickFromDocuments = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri ->
            if (uri != null) {
                tryPersistReadPermission(context, uri)
                viewModel.onImageSelected(uri)
            }
        }
    )

    val hasCameraPermission = remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED
        )
    }
    val pendingPhotoUri = remember { mutableStateOf<Uri?>(null) }
    val takePicture = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
        onResult = { ok ->
            val uri = pendingPhotoUri.value
            if (ok && uri != null) {
                viewModel.onImageSelected(uri)
            } else {
                pendingPhotoUri.value = null
            }
        }
    )

    val requestCameraPermission = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            hasCameraPermission.value = granted
            if (granted) {
                val uri = createTempImageUri(context)
                pendingPhotoUri.value = uri
                takePicture.launch(uri)
            } else {
                scope.launch {
                    snackbarHostState.showSnackbar(
                        message = "Permissão da câmera negada. Ative nas configurações para tirar foto.",
                        duration = SnackbarDuration.Long
                    )
                }
            }
        }
    )

    LaunchedEffect(state.savedCar) {
        val saved = state.savedCar ?: return@LaunchedEffect
        onSaved(saved)
        viewModel.clearSaved()
    }

    LaunchedEffect(state.error) {
        val err = state.error ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(message = err, duration = SnackbarDuration.Long)
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(if (state.isEdit) "Editar carro" else "Novo carro") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Voltar"
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (state.isLoadingCar) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
            ImagePickerSection(
                imageUri = state.imageUri,
                imageUrl = state.imageUrl,
                isSaving = state.isSaving,
                onPickGallery = {
                    pickVisualMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                },
                onTakePhoto = {
                    if (hasCameraPermission.value) {
                        val uri = createTempImageUri(context)
                        pendingPhotoUri.value = uri
                        takePicture.launch(uri)
                    } else {
                        requestCameraPermission.launch(Manifest.permission.CAMERA)
                    }
                }
            )

            OutlinedTextField(
                value = state.name,
                onValueChange = viewModel::onNameChange,
                label = { Text("Nome") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = state.licence,
                onValueChange = viewModel::onLicenceChange,
                label = { Text("Placa") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = state.year,
                onValueChange = viewModel::onYearChange,
                label = { Text("Ano (ex: 2020/2021)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                modifier = Modifier.fillMaxWidth()
            )

            LocationSection(
                isEdit = state.isEdit,
                place = state.place,
                hasLocationPermission = hasLocationPermission,
                onRequestPermission = {
                    requestLocationPermissions.launch(
                        arrayOf(
                            Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.ACCESS_FINE_LOCATION
                        )
                    )
                },
                onPlaceChange = viewModel::onPlaceChange
            )

            Button(
                onClick = viewModel::save,
                enabled = !state.isSaving,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .semantics { contentDescription = "Salvar carro" }
            ) {
                if (state.isSaving) {
                    CircularProgressIndicator(modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
                    Spacer(modifier = Modifier.size(12.dp))
                    Text("Salvando...")
                } else {
                    Text("Salvar")
                }
            }
                }
            }
        }
    }
}

@Composable
private fun ImagePickerSection(
    imageUri: Uri?,
    imageUrl: String?,
    isSaving: Boolean,
    onPickGallery: () -> Unit,
    onTakePhoto: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            val model: Any? = imageUri ?: imageUrl
            if (model == null) {
                Text(
                    text = "Selecione uma imagem",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                SubcomposeAsyncImage(
                    model = ImageRequest.Builder(LocalContext.current).data(model).crossfade(true).build(),
                    contentDescription = "Imagem do carro",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    loading = {
                        CircularProgressIndicator(modifier = Modifier.size(32.dp))
                    }
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onPickGallery,
                enabled = !isSaving,
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Filled.Image, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.size(8.dp))
                Text("Galeria")
            }
            OutlinedButton(
                onClick = onTakePhoto,
                enabled = !isSaving,
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Filled.CameraAlt, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.size(8.dp))
                Text("Câmera")
            }
        }
    }
}

@SuppressLint("MissingPermission")
@Composable
private fun LocationSection(
    isEdit: Boolean,
    place: Place,
    hasLocationPermission: Boolean,
    onRequestPermission: () -> Unit,
    onPlaceChange: (Place) -> Unit
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val latLng = LatLng(place.lat, place.long)
    val markerState = remember { MarkerState(position = latLng) }
    LaunchedEffect(latLng) { markerState.position = latLng }

    LaunchedEffect(markerState) {
        snapshotFlow { markerState.position }
            .map { pos -> Place(lat = pos.latitude, long = pos.longitude) }
            .distinctUntilChanged()
            .collect { onPlaceChange(it) }
    }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(latLng, 14f)
    }

    val hasCenteredFromLocation = remember { mutableStateOf(false) }
    val fusedClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    LaunchedEffect(hasLocationPermission) {
        if (hasLocationPermission && !isEdit && !hasCenteredFromLocation.value) {
            runCatching {
                val location = fusedClient.lastLocation.await()
                if (location != null) {
                    val pos = LatLng(location.latitude, location.longitude)
                    markerState.position = pos
                    onPlaceChange(Place(lat = pos.latitude, long = pos.longitude))
                    cameraPositionState.move(CameraUpdateFactory.newLatLngZoom(pos, 15f))
                    hasCenteredFromLocation.value = true
                }
            }
        }
    }

    val uiSettings = MapUiSettings(
        myLocationButtonEnabled = hasLocationPermission,
        zoomControlsEnabled = true
    )
    val properties = MapProperties(
        isMyLocationEnabled = hasLocationPermission
    )

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Localização", style = MaterialTheme.typography.titleMedium)
            if (!hasLocationPermission) {
                TextButton(onClick = onRequestPermission) {
                    Icon(Icons.Filled.MyLocation, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.size(8.dp))
                    Text("Permitir")
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
                .clip(RoundedCornerShape(12.dp))
        ) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                properties = properties,
                uiSettings = uiSettings,
                onMapClick = { clicked ->
                    markerState.position = clicked
                    onPlaceChange(Place(lat = clicked.latitude, long = clicked.longitude))
                }
            ) {
                Marker(
                    state = markerState,
                    title = "Posição do carro",
                    draggable = true
                )
            }
        }

        Text(
            text = "Lat: ${place.lat}  Long: ${place.long}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        if (hasLocationPermission) {
            TextButton(
                onClick = {
                    val pos = markerState.position
                    scope.launch {
                        cameraPositionState.move(CameraUpdateFactory.newLatLngZoom(pos, 15f))
                    }
                }
            ) {
                Icon(Icons.Filled.MyLocation, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.size(8.dp))
                Text("Centralizar na localização")
            }
        }
    }
}

private fun createTempImageUri(context: Context): Uri {
    val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
    val dir = File(context.cacheDir, "images").apply { mkdirs() }
    val file = File(dir, "car_$timeStamp.jpg")
    return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
}

private fun tryPersistReadPermission(context: Context, uri: Uri) {
    runCatching {
        context.contentResolver.takePersistableUriPermission(
            uri,
            android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
        )
    }
}

