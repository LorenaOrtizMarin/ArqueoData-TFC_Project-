package com.lorenaortiz.arqueodata.presentation.viewmodel

import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lorenaortiz.arqueodata.auth.AuthService
import com.lorenaortiz.arqueodata.data.local.dao.UserDao
import com.lorenaortiz.arqueodata.data.local.entity.UserEntity
import com.lorenaortiz.arqueodata.domain.model.User
import com.lorenaortiz.arqueodata.domain.model.UserType
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class RegisterUiState {
    object Initial : RegisterUiState()
    object Loading : RegisterUiState()
    data class Success(val userId: String) : RegisterUiState()
    data class Error(val message: String) : RegisterUiState()
}

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val authService: AuthService,
    private val userDao: UserDao,
    @ApplicationContext private val context: Context
) : ViewModel() {
    private val _uiState = MutableStateFlow<RegisterUiState>(RegisterUiState.Initial)
    val uiState: StateFlow<RegisterUiState> = _uiState

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUserFlow: StateFlow<User?> = _currentUser

    private var currentUserId: Long = -1
    private var currentUser: User? = null
    private var verificationCode: String? = null
    private var verificationEmail: String? = null

    private val prefs: SharedPreferences = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)

    private fun saveCurrentUserId(id: Long) {
        prefs.edit().putLong("current_user_id", id).apply()
    }

    fun register(nombre: String, usuario: String, email: String, password: String, userType: UserType) {
        viewModelScope.launch {
            _uiState.value = RegisterUiState.Loading
            try {
                // Verificar si el email ya existe
                if (authService.checkEmailExists(email)) {
                    _uiState.value = RegisterUiState.Error("El email ya está registrado")
                    return@launch
                }

                // Verificar si el nombre de usuario ya existe
                if (authService.checkUsernameExists(usuario)) {
                    _uiState.value = RegisterUiState.Error("El nombre de usuario ya está en uso")
                    return@launch
                }

                // Solo generar nuevo código si es un email diferente o no hay código
                if (verificationEmail != email || verificationCode == null) {
                    val code = (1000..9999).random().toString()
                    verificationCode = code
                    verificationEmail = email
                    println("Nuevo código generado para registro: $code")
                } else {
                    println("Usando código existente para registro: $verificationCode")
                }

                // Intentar registrar en Firebase
                val firebaseResult = authService.registerUser(
                    email = email,
                    password = password,
                    verificationCode = verificationCode!!,
                    nombre = nombre,
                    usuario = usuario,
                    userType = userType.name
                )
                if (firebaseResult.isFailure) {
                    val error = firebaseResult.exceptionOrNull()
                    val errorMessage = when (error?.message) {
                        "The email address is already in use by another account." -> "El email ya está registrado"
                        "The password is invalid or the user does not have a password." -> "La contraseña no es correcta"
                        "The user account has been disabled by an administrator." -> "La cuenta ha sido deshabilitada"
                        "There is no user record corresponding to this identifier." -> "No existe una cuenta con este email"
                        else -> error?.message ?: "Error al registrar usuario"
                    }
                    _uiState.value = RegisterUiState.Error(errorMessage)
                    return@launch
                }

                // Guardar en Room
                val newUser = UserEntity(
                    nombre = nombre,
                    usuario = usuario,
                    email = email,
                    password = password,
                    userType = userType,
                    photoUrl = null,
                    pendingSync = false
                )
                val userId = userDao.insertUser(newUser)
                currentUserId = userId
                saveCurrentUserId(userId)
                currentUser = User(
                    id = userId,
                    nombre = nombre,
                    usuario = usuario,
                    email = email,
                    password = password,
                    userType = userType,
                    photoUrl = null
                )
                _currentUser.value = currentUser

                _uiState.value = RegisterUiState.Success(userId.toString())
            } catch (e: Exception) {
                val errorMessage = when (e.message) {
                    "The email address is already in use by another account." -> "El email ya está registrado por otra cuenta"
                    "The password is invalid or the user does not have a password." -> "La contraseña es inválida"
                    "The user account has been disabled by an administrator." -> "La cuenta ha sido deshabilitada por un administrador"
                    "There is no user record corresponding to this identifier." -> "No existe una cuenta con este email"
                    else -> "Error al registrar usuario: ${e.message}"
                }
                _uiState.value = RegisterUiState.Error(errorMessage)
            }
        }
    }

    fun getVerificationCode(): String? {
        return verificationCode
    }

    fun getVerificationEmail(): String? {
        return verificationEmail
    }
} 