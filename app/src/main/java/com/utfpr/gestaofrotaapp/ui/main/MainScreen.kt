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
import com.utfpr.gestaofrotaapp.ui.car.CarListScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    onLogout: () -> Unit,
    onAddCarClick: () -> Unit = {},
    onEditCarClick: (Car) -> Unit = {}
) {
    var selectedCarId by remember { mutableStateOf<String?>(null) }

    if (selectedCarId == null) {
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
                onAddCarClick = onAddCarClick,
                onCarClick = { car -> selectedCarId = car.id ?: car.licence }
            )
        }
    } else {
        CarDetailScreen(
            carId = selectedCarId!!,
            onBack = { selectedCarId = null },
            onEditClick = onEditCarClick,
            onDeleted = { selectedCarId = null }
        )
    }
}
