package com.utfpr.gestaofrotaapp.ui.car

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.utfpr.gestaofrotaapp.data.model.Car
import com.utfpr.gestaofrotaapp.data.model.Place
import com.utfpr.gestaofrotaapp.data.repository.ApiException
import com.utfpr.gestaofrotaapp.data.repository.CarRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CarFormState(
    val isEdit: Boolean = false,
    val id: String? = null,
    val name: String = "",
    val licence: String = "",
    val year: String = "",
    val imageUrl: String? = null,
    val imageUri: Uri? = null,
    val place: Place = Place(lat = -23.5505, long = -46.6333),
    val isSaving: Boolean = false,
    val error: String? = null,
    val savedCar: Car? = null
)

class CarFormViewModel(
    private val repository: CarRepository = CarRepository(),
    initialCar: Car? = null
) : ViewModel() {

    private val _state = MutableStateFlow(
        if (initialCar == null) {
            CarFormState()
        } else {
            CarFormState(
                isEdit = true,
                id = initialCar.id,
                name = initialCar.name,
                licence = initialCar.licence,
                year = initialCar.year,
                imageUrl = initialCar.imageUrl,
                place = initialCar.place
            )
        }
    )
    val state: StateFlow<CarFormState> = _state.asStateFlow()

    fun onNameChange(value: String) = _state.update { it.copy(name = value, error = null) }
    fun onLicenceChange(value: String) = _state.update { it.copy(licence = value, error = null) }
    fun onYearChange(value: String) = _state.update { it.copy(year = value, error = null) }
    fun onImageSelected(uri: Uri) = _state.update { it.copy(imageUri = uri, error = null) }
    fun onPlaceChange(place: Place) = _state.update { it.copy(place = place, error = null) }

    fun clearSaved() = _state.update { it.copy(savedCar = null) }

    fun resetIfNew() {
        if (!_state.value.isEdit) {
            _state.value = CarFormState()
        }
    }

    fun save() {
        val current = _state.value
        val id = current.id
        val name = current.name.trim()
        val licence = current.licence.trim()
        val year = current.year.trim()

        if (name.isBlank() || licence.isBlank() || year.isBlank()) {
            _state.update { it.copy(error = "Preencha nome, placa e ano") }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isSaving = true, error = null) }

            val result = if (!current.isEdit) {
                val imageUri = current.imageUri
                if (imageUri == null) {
                    Result.failure(ApiException(-1, "Selecione uma imagem"))
                } else {
                    repository.createCarWithImage(
                        imageUri = imageUri,
                        year = year,
                        name = name,
                        licence = licence,
                        place = current.place
                    )
                }
            } else {
                val editId = id ?: return@launch _state.update {
                    it.copy(isSaving = false, error = "ID inválido")
                }

                val imageUri = current.imageUri
                if (imageUri != null) {
                    repository.updateCarWithImage(
                        id = editId,
                        imageUri = imageUri,
                        year = year,
                        name = name,
                        licence = licence,
                        place = current.place
                    )
                } else {
                    val imageUrl = current.imageUrl ?: ""
                    repository.updateCar(
                        id = editId,
                        car = Car(
                            id = editId,
                            imageUrl = imageUrl,
                            year = year,
                            name = name,
                            licence = licence,
                            place = current.place
                        )
                    )
                }
            }

            result
                .onSuccess { car ->
                    _state.update { it.copy(isSaving = false, savedCar = car) }
                }
                .onFailure { e ->
                    val msg = when (e) {
                        is ApiException -> e.message
                        else -> e.message ?: "Erro ao salvar"
                    }
                    _state.update { it.copy(isSaving = false, error = msg) }
                }
        }
    }
}

class CarFormViewModelFactory(private val initialCar: Car?) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        CarFormViewModel(initialCar = initialCar) as T
}

