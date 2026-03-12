package com.utfpr.gestaofrotaapp.ui.car

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.utfpr.gestaofrotaapp.data.model.Car
import com.utfpr.gestaofrotaapp.data.repository.ApiException
import com.utfpr.gestaofrotaapp.data.repository.CarRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Estado da UI da lista de carros.
 */
sealed class CarListUiState {
    data object Loading : CarListUiState()
    data class Success(val cars: List<Car>) : CarListUiState()
    data class Error(val message: String) : CarListUiState()
}

/**
 * ViewModel da tela de lista de carros.
 * Consome GET /car via CarRepository.
 */
class CarListViewModel(
    private val repository: CarRepository = CarRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow<CarListUiState>(CarListUiState.Loading)
    val uiState: StateFlow<CarListUiState> = _uiState.asStateFlow()

    init {
        loadCars()
    }

    fun loadCars() {
        viewModelScope.launch {
            _uiState.value = CarListUiState.Loading
            repository.getCars()
                .onSuccess { cars -> _uiState.value = CarListUiState.Success(cars) }
                .onFailure { e ->
                    val msg = when (e) {
                        is ApiException -> e.message
                        else -> e.message ?: "Erro ao carregar carros"
                    }
                    _uiState.value = CarListUiState.Error(msg)
                }
        }
    }

    fun retry() = loadCars()
}
