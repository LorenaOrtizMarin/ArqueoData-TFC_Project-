package com.lorenaortiz.arqueodata.presentation.viewmodel

import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lorenaortiz.arqueodata.data.local.dao.UserDao
import com.lorenaortiz.arqueodata.data.local.entity.UserEntity
import com.lorenaortiz.arqueodata.domain.model.User
import com.lorenaortiz.arqueodata.domain.model.UserType
import com.lorenaortiz.arqueodata.utils.ImageStorageManager
import com.lorenaortiz.arqueodata.data.sync.SyncManager
import com.lorenaortiz.arqueodata.auth.AuthService
import com.lorenaortiz.arqueodata.auth.GoogleAuthUiClient
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val userDao: UserDao,
    private val syncManager: SyncManager,
    private val authService: AuthService,
    @ApplicationContext private val context: Context
) : ViewModel() {
    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Initial)
    val uiState: StateFlow<AuthUiState> = _uiState

    private val prefs: SharedPreferences = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)

    private var currentUserId: Long = -1
    private var currentUser: User? = null

    // Nuevo StateFlow para el usuario actual
    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUserFlow: StateFlow<User?> = _currentUser

    private var verificationCode: String? = null
    private var verificationEmail: String? = null

    init {
        val savedId = getSavedUserId()
        if (savedId != -1L) {
            currentUserId = savedId
            loadCurrentUser()
        }
        // Intentar sincronizar al iniciar
        syncPendingUsers()
    }

    private fun saveCurrentUserId(id: Long) {
        prefs.edit().putLong("current_user_id", id).apply()
    }

    private fun getSavedUserId(): Long {
        return prefs.getLong("current_user_id", -1)
    }

    private fun clearCurrentUserId() {
        prefs.edit().remove("current_user_id").apply()
    }

    fun register(nombre: String, usuario: String, email: String, password: String, userType: UserType) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            try {
                // Verificar si el email ya existe
                if (authService.checkEmailExists(email)) {
                    _uiState.value = AuthUiState.Error("El email ya está registrado")
                    return@launch
                }

                // Verificar si el nombre de usuario ya existe
                if (authService.checkUsernameExists(usuario)) {
                    _uiState.value = AuthUiState.Error("El nombre de usuario ya está en uso")
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
                    _uiState.value = AuthUiState.Error(errorMessage)
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

                _uiState.value = AuthUiState.Success(userId.toString())
            } catch (e: Exception) {
                val errorMessage = when (e.message) {
                    "The email address is already in use by another account." -> "El email ya está registrado por otra cuenta"
                    "The password is invalid or the user does not have a password." -> "La contraseña es inválida"
                    "The user account has been disabled by an administrator." -> "La cuenta ha sido deshabilitada por un administrador"
                    "There is no user record corresponding to this identifier." -> "No existe una cuenta con este email"
                    else -> "Error al registrar usuario: ${e.message}"
                }
                _uiState.value = AuthUiState.Error(errorMessage)
            }
        }
    }

    fun login(identifier: String, password: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            
            try {
                // Intentar primero con Firebase
                val firebaseResult = authService.loginWithFirebase(identifier, password)
                if (firebaseResult.isSuccess) {
                    val firebaseUserId = firebaseResult.getOrNull()
                    if (firebaseUserId != null) {
                        // Buscar el usuario en la base de datos local
                        var user = if (identifier.contains("@")) {
                            userDao.getUserByEmail(identifier)
                        } else {
                            userDao.getUserByUsername(identifier)
                        }
                        
                        // Si no existe en local, obtener datos de Firebase y crear usuario local
                        if (user == null) {
                            val firebaseUser = authService.getFirebaseUserData(firebaseUserId)
                            if (firebaseUser != null) {
                                val newUser = UserEntity(
                                    nombre = firebaseUser.nombre,
                                    usuario = firebaseUser.usuario,
                                    email = firebaseUser.email,
                                    password = password,
                                    userType = UserType.valueOf(firebaseUser.userType),
                                    photoUrl = null,
                                    pendingSync = false
                                )
                                val userId = userDao.insertUser(newUser)
                                user = userDao.getUserById(userId)
                            }
                        }
                        
                        if (user != null) {
                            currentUserId = user.id
                            saveCurrentUserId(user.id)
                            currentUser = User(
                                id = user.id,
                                nombre = user.nombre,
                                usuario = user.usuario,
                                email = user.email,
                                password = user.password,
                                userType = user.userType,
                                photoUrl = user.photoUrl
                            )
                            _currentUser.value = currentUser
                            _uiState.value = AuthUiState.Success(user.id.toString())
                        } else {
                            _uiState.value = AuthUiState.Error("No se pudo obtener la información del usuario")
                        }
                    } else {
                        _uiState.value = AuthUiState.Error("Error al obtener el ID del usuario de Firebase")
                    }
                } else {
                    // Si falla Firebase, intentar con la base de datos local
                    val user = if (identifier.contains("@")) {
                        userDao.getUserByEmail(identifier)
                    } else {
                        userDao.getUserByUsername(identifier)
                    }
                    
                    if (user != null && user.password == password) {
                        currentUserId = user.id
                        saveCurrentUserId(user.id)
                        currentUser = User(
                            id = user.id,
                            nombre = user.nombre,
                            usuario = user.usuario,
                            email = user.email,
                            password = user.password,
                            userType = user.userType,
                            photoUrl = user.photoUrl
                        )
                        _currentUser.value = currentUser
                        _uiState.value = AuthUiState.Success(user.id.toString())
                    } else {
                        _uiState.value = AuthUiState.Error("Credenciales incorrectas")
                    }
                }
            } catch (e: Exception) {
                // Si hay error de conexión, intentar con la base de datos local
                try {
                    val user = if (identifier.contains("@")) {
                        userDao.getUserByEmail(identifier)
                    } else {
                        userDao.getUserByUsername(identifier)
                    }
                    
                    if (user != null && user.password == password) {
                        currentUserId = user.id
                        saveCurrentUserId(user.id)
                        currentUser = User(
                            id = user.id,
                            nombre = user.nombre,
                            usuario = user.usuario,
                            email = user.email,
                            password = user.password,
                            userType = user.userType,
                            photoUrl = user.photoUrl
                        )
                        _currentUser.value = currentUser
                        _uiState.value = AuthUiState.Success(user.id.toString())
                    } else {
                        _uiState.value = AuthUiState.Error("Credenciales incorrectas")
                    }
                } catch (e2: Exception) {
                    _uiState.value = AuthUiState.Error("Error al iniciar sesión: ${e2.message}")
                }
            }
        }
    }

    // Nuevo método para cargar el usuario actual desde la base de datos
    fun loadCurrentUser() {
        viewModelScope.launch {
            if (currentUserId != -1L) {
                val userEntity = userDao.getUserById(currentUserId)
                val user = userEntity?.let {
                    User(
                        id = it.id,
                        nombre = it.nombre,
                        usuario = it.usuario,
                        email = it.email,
                        password = it.password,
                        userType = it.userType,
                        photoUrl = it.photoUrl
                    )
                }
                currentUser = user
                _currentUser.value = user
            } else {
                currentUser = null
                _currentUser.value = null
            }
        }
    }

    fun getCurrentUser(): User? = currentUser

    fun logout() {
        // Cerrar sesión en Firebase para email/password si hay usuario autenticado
        val firebaseUser = authService.getCurrentFirebaseUser()
        if (firebaseUser != null && firebaseUser.providerData.any { it.providerId == "password" }) {
            authService.signOutFirebase()
        }
        
        currentUserId = -1
        currentUser = null
        _currentUser.value = null
        clearCurrentUserId()
        _uiState.value = AuthUiState.Initial // Emitir estado inicial para que la UI reaccione
    }

    fun updateProfile(
        nombre: String,
        usuario: String,
        photoUri: android.net.Uri?,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                if (currentUserId == -1L) {
                    onError("No hay usuario autenticado")
                    return@launch
                }
                val userEntity = userDao.getUserById(currentUserId)
                if (userEntity == null) {
                    onError("Usuario no encontrado")
                    return@launch
                }

                // Procesar la imagen seleccionada usando ImageStorageManager
                var newPhotoUrl: String? = userEntity.photoUrl
                if (photoUri != null) {
                    val imageStorageManager = ImageStorageManager(context.applicationContext)
                    newPhotoUrl = imageStorageManager.saveImage(photoUri, "profile_${currentUserId}")
                    
                    // Si hay una imagen anterior, eliminarla
                    userEntity.photoUrl?.let { oldPath ->
                        imageStorageManager.deleteImage(oldPath)
                    }
                }

                val updatedUser = userEntity.copy(
                    nombre = nombre,
                    usuario = usuario,
                    photoUrl = newPhotoUrl
                )
                userDao.updateUser(updatedUser)
                // Actualizar el usuario en memoria y el StateFlow
                currentUser = User(
                    id = updatedUser.id,
                    nombre = updatedUser.nombre,
                    usuario = updatedUser.usuario,
                    email = updatedUser.email,
                    password = updatedUser.password,
                    userType = updatedUser.userType,
                    photoUrl = updatedUser.photoUrl
                )
                _currentUser.value = currentUser
                onSuccess()
            } catch (e: Exception) {
                onError("Error al actualizar el perfil: ${e.message}")
            }
        }
    }

    fun syncPendingUsers() {
        viewModelScope.launch {
            try {
                println("Iniciando sincronización de usuarios...")
                syncManager.syncUsers()
                println("Sincronización completada")
            } catch (e: Exception) {
                println("Error en sincronización: ${e.message}")
                e.printStackTrace()
            }
        }
    }

    fun sendVerificationCode(email: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            try {
                println("Verificando email en base de datos local: $email")
                // Verificar si el email existe en la base de datos
                val user = userDao.getUserByEmail(email)
                if (user == null) {
                    println("Email no encontrado en base de datos local")
                    _uiState.value = AuthUiState.Error("El email no está registrado en la aplicación")
                    return@launch
                }
                println("Email encontrado en base de datos local")

                // Generar nuevo código de verificación
                val code = (1000..9999).random().toString()
                verificationCode = code
                verificationEmail = email
                println("Nuevo código generado: $code")

                // Obtener el ID de Firebase para el email
                val firebaseUserId = authService.getFirebaseUserIdByEmail(email)
                if (firebaseUserId == null) {
                    _uiState.value = AuthUiState.Error("Usuario no encontrado en Firebase")
                    return@launch
                }

                // Enviar código de verificación
                println("Intentando enviar código de verificación a Firebase")
                val result = authService.sendVerificationCode(email, firebaseUserId, code)
                
                if (result.isSuccess) {
                    println("Código enviado exitosamente: $code")
                    _uiState.value = AuthUiState.VerificationCodeSent(code)
                } else {
                    val errorMessage = result.exceptionOrNull()?.message ?: "Error desconocido"
                    println("Error al enviar código: $errorMessage")
                    _uiState.value = AuthUiState.Error("Error al enviar código de verificación: $errorMessage")
                }
            } catch (e: Exception) {
                println("Excepción al enviar código de verificación: ${e.message}")
                e.printStackTrace()
                _uiState.value = AuthUiState.Error("Error al enviar código de verificación: ${e.message}")
            }
        }
    }

    fun verifyCode(code: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            try {
                println("Código ingresado: $code")
                println("Código almacenado: $verificationCode")
                
                if (verificationCode != null) {
                    val trimmedCode = code.trim()
                    val trimmedStoredCode = verificationCode!!.trim()
                    println("Comparando códigos - Ingresado: '$trimmedCode', Almacenado: '$trimmedStoredCode'")
                    
                    if (trimmedCode == trimmedStoredCode) {
                        _uiState.value = AuthUiState.VerificationSuccess
                    } else {
                        _uiState.value = AuthUiState.Error("Código de verificación incorrecto")
                    }
                } else {
                    _uiState.value = AuthUiState.Error("No hay código de verificación disponible")
                }
            } catch (e: Exception) {
                _uiState.value = AuthUiState.Error("Error al verificar el código: ${e.message}")
            }
        }
    }

    fun getVerificationCode(): String? {
        return verificationCode
    }

    fun resendVerificationCode(email: String) {
        // Forzar generación de nuevo código
        verificationCode = null
        sendVerificationCode(email)
    }

    fun checkSyncStatus() {
        viewModelScope.launch {
            try {
                val pendingUsers = userDao.getPendingSyncUsers()
                println("Usuarios pendientes de sincronización: ${pendingUsers.size}")
                pendingUsers.forEach { user ->
                    println("Usuario pendiente: ${user.email}")
                }
            } catch (e: Exception) {
                println("Error al verificar estado de sincronización: ${e.message}")
            }
        }
    }

    fun clearState() {
        _uiState.value = AuthUiState.Initial
    }

    fun signOut(context: Context, onComplete: () -> Unit) {
        viewModelScope.launch {
            try {
                // Cerrar sesión en Google y Firebase
                val googleAuthUiClient = GoogleAuthUiClient(context)
                googleAuthUiClient.signOut {
                    // Limpiar datos locales
                    clearCurrentUserId()
                    currentUserId = -1
                    currentUser = null
                    _currentUser.value = null
                    _uiState.value = AuthUiState.Initial
                    onComplete()
                }
            } catch (e: Exception) {
                println("Error al cerrar sesión: ${e.message}")
                // Aún así, intentamos limpiar el estado local
                clearCurrentUserId()
                currentUserId = -1
                currentUser = null
                _currentUser.value = null
                _uiState.value = AuthUiState.Initial
                onComplete()
            }
        }
    }

    fun loginWithGoogle(
        uid: String,
        email: String,
        displayName: String,
        photoUrl: String?,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                // Comprueba si el usuario ya existe en Room
                var user = userDao.getUserByEmail(email)
                
                val isNewUser = user == null

                if (isNewUser) {
                    // Si no existe, créalo (temporalmente como MIEMBRO, se actualizará después)
                    val newUser = UserEntity(
                        nombre = displayName,
                        usuario = email, // Puedes cambiar esto si quieres pedir un username personalizado
                        email = email,
                        password = "", // No hay password, es Google
                        userType = UserType.MIEMBRO, // Tipo temporal
                        photoUrl = photoUrl,
                        pendingSync = false
                    )
                    val userId = userDao.insertUser(newUser)
                    user = userDao.getUserById(userId)
                }

                // Guarda el usuario como actual
                currentUserId = user!!.id
                saveCurrentUserId(user.id)
                currentUser = User(
                    id = user.id,
                    nombre = user.nombre,
                    usuario = user.usuario,
                    email = user.email,
                    password = user.password,
                    userType = user.userType,
                    photoUrl = user.photoUrl
                )
                _currentUser.value = currentUser
                
                if (isNewUser) {
                    // Si es nuevo usuario, notifica para navegar a la selección de tipo de usuario
                    _uiState.value = AuthUiState.NavigateToUserTypeSelection
                } else {
                    // Si ya existe, inicia sesión normalmente
                    onSuccess()
                }

            } catch (e: Exception) {
                onError("Error al iniciar sesión con Google: ${e.message}")
            }
        }
    }

    fun updateUserType(userType: UserType, onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                if (currentUser != null) {
                    val updatedUserEntity = userDao.getUserById(currentUser!!.id)?.copy(
                        userType = userType,
                        pendingSync = true // Marcar para sincronización si es necesario
                    )
                    if (updatedUserEntity != null) {
                        userDao.updateUser(updatedUserEntity)
                        // Actualizar el usuario en memoria y el StateFlow
                        currentUser = currentUser!!.copy(userType = userType)
                        _currentUser.value = currentUser
                        // Intentar sincronizar inmediatamente
                        syncPendingUsers()
                        onSuccess()
                    } else {
                         println("Error: Usuario actual no encontrado en la base de datos local.")
                    }
                } else {
                    println("Error: No hay usuario actual para actualizar el tipo.")
                }
            } catch (e: Exception) {
                println("Error al actualizar el tipo de usuario: ${e.message}")
                // Manejar el error apropiadamente, quizás mostrando un mensaje al usuario
            }
        }
    }
}

sealed class AuthUiState {
    object Initial : AuthUiState()
    object Loading : AuthUiState()
    data class Success(val userId: String) : AuthUiState()
    data class Error(val message: String) : AuthUiState()
    data class VerificationCodeSent(val code: String) : AuthUiState()
    object VerificationSuccess : AuthUiState()
    object NavigateToUserTypeSelection : AuthUiState()
} 