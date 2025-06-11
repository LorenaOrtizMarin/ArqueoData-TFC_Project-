package com.lorenaortiz.arqueodata.auth

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthService @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    private val auth: FirebaseAuth = Firebase.auth

    suspend fun checkEmailExists(email: String): Boolean {
        return try {
            val signInMethods = auth.fetchSignInMethodsForEmail(email).await()
            signInMethods?.signInMethods?.isNotEmpty() == true
        } catch (e: Exception) {
            println("Error al verificar email: ${e.message}")
            false
        }
    }

    suspend fun checkUsernameExists(username: String): Boolean {
        return try {
            val query = firestore.collection("users")
                .whereEqualTo("usuario", username)
                .get()
                .await()
            !query.isEmpty
        } catch (e: Exception) {
            println("Error al verificar nombre de usuario: ${e.message}")
            false
        }
    }

    suspend fun getFirebaseUserIdByEmail(email: String): String? {
        return try {
            println("Iniciando búsqueda de usuario por email: $email")
            
            // Primero buscar en Firestore
            println("Buscando usuario en Firestore...")
            try {
                val userQuery = firestore.collection("users")
                    .whereEqualTo("email", email)
                    .limit(1)
                    .get()
                    .await()
                
                if (!userQuery.isEmpty) {
                    val userId = userQuery.documents.first().id
                    println("ID de usuario encontrado en Firestore: $userId")
                    
                    // Verificar si el usuario existe en Firebase Auth
                    println("Verificando si el usuario existe en Firebase Auth...")
                    val signInMethods = auth.fetchSignInMethodsForEmail(email).await()
                    println("Métodos de inicio de sesión encontrados: ${signInMethods?.signInMethods}")
                    
                    if (signInMethods?.signInMethods?.isNotEmpty() == true) {
                        println("Usuario verificado en Firebase Auth")
                        return userId
                    } else {
                        println("Usuario no encontrado en Firebase Auth, pero existe en Firestore")
                        // Intentar recrear el usuario en Firebase Auth
                        println("Intentando recrear usuario en Firebase Auth...")
                        try {
                            // Generar una contraseña temporal
                            val tempPassword = "TempPass123!"
                            val authResult = auth.createUserWithEmailAndPassword(email, tempPassword).await()
                            println("Usuario recreado en Firebase Auth con ID: ${authResult.user?.uid}")
                            return userId
                        } catch (e: Exception) {
                            println("Error al recrear usuario en Firebase Auth: ${e.message}")
                            // Si falla la recreación, aún así devolvemos el ID de Firestore
                            return userId
                        }
                    }
                }
                
                println("Usuario no encontrado en Firestore")
                return null
            } catch (e: Exception) {
                println("Error al buscar en Firestore: ${e.message}")
                e.printStackTrace()
                return null
            }
        } catch (e: Exception) {
            println("Error general al buscar usuario por email: ${e.message}")
            println("Stack trace completo:")
            e.printStackTrace()
            null
        }
    }

    suspend fun registerUser(
        email: String, 
        password: String, 
        verificationCode: String,
        nombre: String,
        usuario: String,
        userType: String
    ): Result<String> {
        return try {
            // Verificar si el email ya existe
            if (checkEmailExists(email)) {
                return Result.failure(Exception("El email ya está registrado"))
            }

            // Crear usuario en Firebase Auth
            val authResult = auth.createUserWithEmailAndPassword(email, password).await()
            val userId = authResult.user?.uid ?: throw Exception("No se pudo obtener el ID del usuario")

            // Guardar código de verificación en Firestore
            val verificationData = hashMapOf(
                "email" to email,
                "code" to verificationCode,
                "timestamp" to com.google.firebase.Timestamp.now()
            )
            firestore.collection("verification_codes")
                .document(userId)
                .set(verificationData)
                .await()

            // Guardar datos del usuario en Firestore
            val userData = hashMapOf(
                "email" to email,
                "nombre" to nombre,
                "usuario" to usuario,
                "userType" to userType,
                "createdAt" to com.google.firebase.Timestamp.now(),
                "emailVerified" to false
            )
            firestore.collection("users")
                .document(userId)
                .set(userData)
                .await()

            Result.success(userId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun sendVerificationCode(email: String, userId: String, code: String): Result<Unit> {
        return try {
            println("Iniciando envío de código de verificación para email: $email, userId: $userId")
            
            // Verificar que el usuario existe en Firestore
            val userDoc = firestore.collection("users")
                .document(userId)
                .get()
                .await()
            
            if (!userDoc.exists()) {
                println("Usuario no encontrado en Firestore")
                return Result.failure(Exception("Usuario no encontrado en Firestore"))
            }
            
            println("Usuario encontrado en Firestore")
            
            // Guardar código de verificación en Firestore
            val verificationData = hashMapOf(
                "email" to email,
                "code" to code,
                "timestamp" to com.google.firebase.Timestamp.now()
            )
            
            println("Guardando código de verificación en Firestore")
            firestore.collection("verification_codes")
                .document(userId)
                .set(verificationData)
                .await()
            
            println("Código de verificación guardado exitosamente")
            Result.success(Unit)
        } catch (e: Exception) {
            println("Error al enviar código de verificación: ${e.message}")
            e.printStackTrace()
            Result.failure(e)
        }
    }

    suspend fun verifyCode(userId: String, code: String): Result<Boolean> {
        return try {
            val doc = firestore.collection("verification_codes")
                .document(userId)
                .get()
                .await()

            val storedCode = doc.getString("code")
            val timestamp = doc.getLong("timestamp") ?: 0L

            // Verificar que el código no haya expirado (15 minutos)
            val isValid = storedCode == code && 
                         (System.currentTimeMillis() - timestamp) < 15 * 60 * 1000

            if (isValid) {
                // Marcar email como verificado en Firestore
                firestore.collection("users")
                    .document(userId)
                    .set(mapOf("emailVerified" to true))
                    .await()
                
                // Eliminar el código de verificación usado
                firestore.collection("verification_codes")
                    .document(userId)
                    .delete()
                    .await()
            }

            Result.success(isValid)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun loginWithFirebase(identifier: String, password: String): Result<String> {
        return try {
            println("=== INICIO PROCESO DE LOGIN ===")
            println("Identificador recibido: $identifier")
            
            // Primero intentar autenticar directamente con Firebase Auth
            try {
                println("Intentando autenticación directa con Firebase Auth")
                val email = if (identifier.contains("@")) {
                    identifier
                } else {
                    // Si es username, buscar el email en Firestore
                    println("Buscando email para usuario: $identifier")
                    val userQuery = firestore.collection("users")
                        .whereEqualTo("usuario", identifier)
                        .get()
                        .await()
                    
                    if (userQuery.isEmpty) {
                        println("No se encontró usuario en Firestore")
                        throw Exception("Usuario no encontrado")
                    }
                    
                    val userDoc = userQuery.documents.first()
                    val foundEmail = userDoc.getString("email")
                    
                    if (foundEmail == null) {
                        println("No se encontró email en el documento")
                        throw Exception("Error en datos del usuario")
                    }
                    
                    println("Email encontrado: $foundEmail")
                    foundEmail
                }
                
                println("Intentando login con email: $email")
                val authResult = auth.signInWithEmailAndPassword(email, password).await()
                val userId = authResult.user?.uid
                
                if (userId == null) {
                    println("No se pudo obtener el ID del usuario")
                    throw Exception("Error en autenticación")
                }
                
                println("Autenticación exitosa con Firebase Auth")
                println("UID obtenido: $userId")
                
                // Verificar que el usuario existe en Firestore
                val userDoc = firestore.collection("users")
                    .document(userId)
                    .get()
                    .await()
                
                if (!userDoc.exists()) {
                    println("Usuario no encontrado en Firestore")
                    throw Exception("Error en datos del usuario")
                }
                
                println("Usuario verificado en Firestore")
                println("=== LOGIN EXITOSO ===")
                Result.success(userId)
                
            } catch (e: Exception) {
                println("Error en autenticación: ${e.message}")
                println("Stack trace: ${e.stackTraceToString()}")
                throw e
            }
        } catch (e: Exception) {
            println("ERROR en proceso de login: ${e.message}")
            println("Stack trace: ${e.stackTraceToString()}")
            Result.failure(e)
        }
    }

    data class FirebaseUserData(
        val nombre: String,
        val usuario: String,
        val email: String,
        val userType: String
    )

    suspend fun getFirebaseUserData(userId: String): FirebaseUserData? {
        return try {
            val doc = firestore.collection("users")
                .document(userId)
                .get()
                .await()
            
            if (doc.exists()) {
                FirebaseUserData(
                    nombre = doc.getString("nombre") ?: "",
                    usuario = doc.getString("usuario") ?: "",
                    email = doc.getString("email") ?: "",
                    userType = doc.getString("userType") ?: "USER"
                )
            } else {
                null
            }
        } catch (e: Exception) {
            println("Error al obtener datos del usuario de Firebase: ${e.message}")
            null
        }
    }

    suspend fun getEmailByUsername(username: String): String? {
        return try {
            println("Buscando email para usuario: $username")
            val query = firestore.collection("users")
                .whereEqualTo("usuario", username)
                .get()
                .await()
            
            println("Resultados de búsqueda: ${query.documents.size}")
            if (!query.isEmpty) {
                val email = query.documents.firstOrNull()?.getString("email")
                println("Email encontrado: $email")
                email
            } else {
                println("No se encontró email para el usuario: $username")
                null
            }
        } catch (e: Exception) {
            println("Error al buscar email por nombre de usuario: ${e.message}")
            println("Stack trace: ${e.stackTraceToString()}")
            null
        }
    }

    fun isUserAuthenticated(): Boolean {
        return auth.currentUser != null
    }

    fun getCurrentFirebaseUserId(): String? {
        return auth.currentUser?.uid
    }

    fun getCurrentFirebaseUser() = auth.currentUser

    fun signOutFirebase() {
        auth.signOut()
    }
} 