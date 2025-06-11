package com.lorenaortiz.arqueodata.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lorenaortiz.arqueodata.data.local.dao.UserDao
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class ResetPasswordUiState {
    object Idle : ResetPasswordUiState()
    object Loading : ResetPasswordUiState()
    object CodeVerified : ResetPasswordUiState()
    object PasswordReset : ResetPasswordUiState()
    data class Error(val message: String) : ResetPasswordUiState()
}

@HiltViewModel
class ResetPasswordViewModel @Inject constructor(
    private val userDao: UserDao
) : ViewModel() {
    private val _uiState = MutableStateFlow<ResetPasswordUiState>(ResetPasswordUiState.Idle)
    val uiState: StateFlow<ResetPasswordUiState> = _uiState

    private var emailToReset: String? = null

    fun setEmailToReset(email: String) {
        emailToReset = email
    }

    fun verifyCode(code: String) {
        viewModelScope.launch {
            _uiState.value = ResetPasswordUiState.Loading
            try {
                // TODO: Implementar verificación real del código
                kotlinx.coroutines.delay(1500) // Simulación de llamada
                if (code.length == 6) {
                    _uiState.value = ResetPasswordUiState.CodeVerified
                } else {
                    _uiState.value = ResetPasswordUiState.Error("Código inválido")
                }
            } catch (e: Exception) {
                _uiState.value = ResetPasswordUiState.Error("Error al verificar el código: ${e.message}")
            }
        }
    }

    fun resetPassword(newPassword: String, confirmPassword: String) {
        viewModelScope.launch {
            _uiState.value = ResetPasswordUiState.Loading
            try {
                // Validaciones
                if (newPassword.isBlank()) {
                    _uiState.value = ResetPasswordUiState.Error("La contraseña es requerida")
                    return@launch
                }
                if (newPassword != confirmPassword) {
                    _uiState.value = ResetPasswordUiState.Error("Las contraseñas no coinciden")
                    return@launch
                }
                if (newPassword.length < 6) {
                    _uiState.value = ResetPasswordUiState.Error("La contraseña debe tener al menos 6 caracteres")
                    return@launch
                }

                // Obtener el usuario por email
                val email = emailToReset ?: run {
                    _uiState.value = ResetPasswordUiState.Error("No se encontró el email para restablecer")
                    return@launch
                }

                val user = userDao.getUserByEmail(email)
                if (user == null) {
                    _uiState.value = ResetPasswordUiState.Error("Usuario no encontrado")
                    return@launch
                }

                // Actualizar la contraseña
                val updatedUser = user.copy(password = newPassword)
                userDao.updateUser(updatedUser)

                _uiState.value = ResetPasswordUiState.PasswordReset
            } catch (e: Exception) {
                _uiState.value = ResetPasswordUiState.Error("Error al restablecer la contraseña: ${e.message}")
            }
        }
    }

    fun resetState() {
        _uiState.value = ResetPasswordUiState.Idle
    }
} 