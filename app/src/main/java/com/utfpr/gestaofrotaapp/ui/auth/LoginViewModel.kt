package com.utfpr.gestaofrotaapp.ui.auth

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.utfpr.gestaofrotaapp.core.utils.formatPhoneForApi
import com.utfpr.gestaofrotaapp.data.firebase.AuthDataSource
import com.utfpr.gestaofrotaapp.data.firebase.VerificationResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class LoginUiState(
    val phoneNumber: String = "",
    val code: String = "",
    val verificationId: String? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val codeSent: Boolean = false,
    val loginSuccess: Boolean = false
)

class LoginViewModel(
    private val authDataSource: AuthDataSource = AuthDataSource()
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun updatePhoneNumber(phone: String) {
        _uiState.value = _uiState.value.copy(phoneNumber = phone, errorMessage = null)
    }

    fun updateCode(code: String) {
        _uiState.value = _uiState.value.copy(code = code, errorMessage = null)
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    fun sendCode(activity: Activity) {
        val digits = _uiState.value.phoneNumber.trim()
        if (digits.isBlank()) {
            _uiState.value = _uiState.value.copy(errorMessage = "Informe o número de telefone")
            return
        }
        val phoneE164 = formatPhoneForApi(digits)
        if (digits.length < 13) {
            _uiState.value = _uiState.value.copy(errorMessage = "Número incompleto (ex: 55 11 91234-5678)")
            return
        }

        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
        authDataSource.sendPhoneVerificationCode(activity, phoneE164) { result ->
            result.fold(
                onSuccess = { verificationResult ->
                    viewModelScope.launch {
                        if (verificationResult.hasCredential) {
                            verificationResult.credential?.let { credential ->
                                authDataSource.signInWithCredential(credential).fold(
                                    onSuccess = {
                                        _uiState.value = _uiState.value.copy(
                                            isLoading = false,
                                            loginSuccess = true
                                        )
                                    },
                                    onFailure = { e ->
                                        _uiState.value = _uiState.value.copy(
                                            isLoading = false,
                                            errorMessage = e.message ?: "Erro ao autenticar"
                                        )
                                    }
                                )
                            } ?: run {
                                _uiState.value = _uiState.value.copy(isLoading = false)
                            }
                        } else if (verificationResult.needsCode) {
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                codeSent = true,
                                verificationId = verificationResult.verificationId
                            )
                        } else {
                            _uiState.value = _uiState.value.copy(isLoading = false)
                        }
                    }
                },
                onFailure = { e ->
                    viewModelScope.launch {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = e.message ?: "Falha ao enviar código"
                        )
                    }
                }
            )
        }
    }

    fun verifyCode() {
        val id = _uiState.value.verificationId
        val code = _uiState.value.code.trim()
        if (id.isNullOrBlank() || code.isBlank()) {
            _uiState.value = _uiState.value.copy(errorMessage = "Informe o código recebido")
            return
        }

        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
        viewModelScope.launch {
            authDataSource.signInWithPhoneCredential(id, code).fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        loginSuccess = true
                    )
                },
                onFailure = { e ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = e.message ?: "Código inválido"
                    )
                }
            )
        }
    }

    fun resetCodeSent() {
        _uiState.value = _uiState.value.copy(
            codeSent = false,
            verificationId = null,
            code = "",
            errorMessage = null
        )
    }
}
