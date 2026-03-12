package com.utfpr.gestaofrotaapp.ui.car

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.utfpr.gestaofrotaapp.data.model.Car
import com.utfpr.gestaofrotaapp.data.repository.ApiException
import com.utfpr.gestaofrotaapp.data.repository.CarRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class CarDetailUiState {
    data object Loading : CarDetailUiState()
    data class Success(val car: Car) : CarDetailUiState()
    data class Error(val message: String) : CarDetailUiState()
    data object Deleted : CarDetailUiState()
}

class CarDetailViewModel(
    private val carId: String,
    private val repository: CarRepository = CarRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow<CarDetailUiState>(CarDetailUiState.Loading)
    val uiState: StateFlow<CarDetailUiState> = _uiState.asStateFlow()

    init {
        loadCar()
    }

    fun loadCar() {
        viewModelScope.launch {
            _uiState.value = CarDetailUiState.Loading
            repository.getCarById(carId)
                .onSuccess { car -> _uiState.value = CarDetailUiState.Success(car) }
                .onFailure { e ->
                    val msg = when (e) {
                        is ApiException -> e.message
                        else -> e.message ?: "Erro ao carregar carro"
                    }
                    _uiState.value = CarDetailUiState.Error(msg)
                }
        }
    }

    private var lastDeletedCar: Car? = null

    fun deleteCar() {
        viewModelScope.launch {
            val currentCar = (_uiState.value as? CarDetailUiState.Success)?.car ?: return@launch
            lastDeletedCar = currentCar
            repository.deleteCar(carId)
                .onSuccess { _uiState.value = CarDetailUiState.Deleted }
                .onFailure { e ->
                    val msg = when (e) {
                        is ApiException -> e.message
                        else -> e.message ?: "Erro ao excluir"
                    }
                    _uiState.value = CarDetailUiState.Error(msg)
                }
        }
    }

    suspend fun undoDelete() {
        val car = lastDeletedCar ?: return
        lastDeletedCar = null
        repository.createCar(car)
            .onSuccess { _uiState.value = CarDetailUiState.Success(it) }
            .onFailure { e ->
                val msg = when (e) {
                    is ApiException -> e.message
                    else -> e.message ?: "Erro ao restaurar"
                }
                _uiState.value = CarDetailUiState.Error(msg)
            }
    }

    fun retry() = loadCar()
}

class CarDetailViewModelFactory(private val carId: String) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = CarDetailViewModel(carId) as T
}
