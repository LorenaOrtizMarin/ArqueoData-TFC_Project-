package com.lorenaortiz.arqueodata.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lorenaortiz.arqueodata.auth.AuthService
import com.lorenaortiz.arqueodata.data.local.dao.UserDao
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class ForgotPasswordUiState {
    object Idle : ForgotPasswordUiState()
    object Loading : ForgotPasswordUiState()
    data class EmailSent(val email: String) : ForgotPasswordUiState()
    data class Error(val message: String) : ForgotPasswordUiState()
    data class VerificationCodeSent(val code: String) : ForgotPasswordUiState()
}

@HiltViewModel
class ForgotPasswordViewModel @Inject constructor(
    private val authService: AuthService,
    private val userDao: UserDao
) : ViewModel() {
    private val _uiState = MutableStateFlow<ForgotPasswordUiState>(ForgotPasswordUiState.Idle)
    val uiState: StateFlow<ForgotPasswordUiState> = _uiState

    private var verificationCode: String? = null
    private var verificationEmail: String? = null

    fun sendVerificationCode(email: String) {
        viewModelScope.launch {
            _uiState.value = ForgotPasswordUiState.Loading
            try {
                println("Iniciando proceso de recuperación de contraseña para email: $email")
                
                // Obtener el ID de Firebase para el email
                println("Buscando ID de Firebase para el email: $email")
                val firebaseUserId = authService.getFirebaseUserIdByEmail(email)
                if (firebaseUserId == null) {
                    println("No se encontró el ID de Firebase para el email: $email")
                    _uiState.value = ForgotPasswordUiState.Error("Usuario no encontrado en Firebase. Por favor, verifica que el email esté correctamente registrado.")
                    return@launch
                }
                println("ID de Firebase encontrado: $firebaseUserId")

                // Verificar si el email existe en la base de datos local
                val user = userDao.getUserByEmail(email)
                if (user == null) {
                    println("Email no encontrado en base de datos local: $email")
                    _uiState.value = ForgotPasswordUiState.Error("El email no está registrado en la aplicación")
                    return@launch
                }
                println("Email encontrado en base de datos local: $email")

                // Generar nuevo código de verificación
                val code = (1000..9999).random().toString()
                verificationCode = code
                verificationEmail = email
                println("Nuevo código generado: $code")

                // Enviar código de verificación
                println("Intentando enviar código de verificación a Firebase")
                val result = authService.sendVerificationCode(email, firebaseUserId, code)
                
                if (result.isSuccess) {
                    println("Código enviado exitosamente: $code")
                    _uiState.value = ForgotPasswordUiState.VerificationCodeSent(code)
                } else {
                    val errorMessage = result.exceptionOrNull()?.message ?: "Error desconocido"
                    println("Error al enviar código: $errorMessage")
                    _uiState.value = ForgotPasswordUiState.Error("Error al enviar código de verificación: $errorMessage")
                }
            } catch (e: Exception) {
                println("Excepción al enviar código de verificación: ${e.message}")
                e.printStackTrace()
                _uiState.value = ForgotPasswordUiState.Error("Error al enviar código de verificación: ${e.message}")
            }
        }
    }

    fun getVerificationCode(): String? {
        return verificationCode
    }

    fun getVerificationEmail(): String? {
        return verificationEmail
    }

    fun resetState() {
        _uiState.value = ForgotPasswordUiState.Idle
    }
} 