package com.utfpr.gestaofrotaapp.ui.main

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.utfpr.gestaofrotaapp.data.model.Car
import com.utfpr.gestaofrotaapp.ui.car.CarDetailScreen
import com.utfpr.gestaofrotaapp.ui.car.CarFormScreen
import com.utfpr.gestaofrotaapp.ui.car.CarListScreen

private sealed interface ScreenState {
    data object List : ScreenState
    data class Detail(val carId: String) : ScreenState
    data class Form(val initialCar: Car?) : ScreenState
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    onLogout: () -> Unit,
    onAddCarClick: () -> Unit = {},
    onEditCarClick: (Car) -> Unit = {}
) {
    var screen by remember { mutableStateOf<ScreenState>(ScreenState.List) }

    when (val current = screen) {
        is ScreenState.List -> {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text("Gestão de Frota") },
                        actions = {
                            TextButton(onClick = onLogout) {
                                Text("Sair")
                            }
                        }
                    )
                }
            ) { paddingValues ->
                CarListScreen(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    onAddCarClick = { screen = ScreenState.Form(initialCar = null) },
                    onCarClick = { car -> screen = ScreenState.Detail(carId = car.id ?: car.licence) }
                )
            }
        }

        is ScreenState.Detail -> {
            CarDetailScreen(
                carId = current.carId,
                onBack = { screen = ScreenState.List },
                onEditClick = { car -> screen = ScreenState.Form(initialCar = car) },
                onDeleted = { screen = ScreenState.List }
            )
        }

        is ScreenState.Form -> {
            CarFormScreen(
                initialCar = current.initialCar,
                onBack = { screen = ScreenState.List },
                onSaved = { screen = ScreenState.List }
            )
        }
    }
}
