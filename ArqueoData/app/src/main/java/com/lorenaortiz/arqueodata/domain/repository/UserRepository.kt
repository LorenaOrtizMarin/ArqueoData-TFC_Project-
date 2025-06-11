package com.lorenaortiz.arqueodata.domain.repository

import com.lorenaortiz.arqueodata.domain.model.User
import kotlinx.coroutines.flow.Flow

/**
 * Interfaz que define las operaciones que podemos realizar con los usuarios.
 * Gestionamos la persistencia y recuperación de datos de usuarios.
 */
interface UserRepository {
    // Obtenemos todos los usuarios registrados
    fun getAllUsers(): Flow<List<User>>
    
    // Recuperamos un usuario específico por su ID
    suspend fun getUserById(id: Long): User?
    
    // Buscamos un usuario por su correo electrónico
    suspend fun getUserByEmail(email: String): User?
    
    // Insertamos un nuevo usuario o actualizamos uno existente
    suspend fun insertUser(user: User): Long
    
    // Actualizamos un usuario existente
    suspend fun updateUser(user: User)
    
    // Eliminamos un usuario específico
    suspend fun deleteUser(user: User)
    
    // Verificamos si existe un usuario con el correo electrónico proporcionado
    suspend fun userExists(email: String): Boolean
    
    // Obtenemos usuarios por tipo
    fun getUsersByType(userType: String): Flow<List<User>>
    
    // Autenticamos a un usuario con su identificador y contraseña
    suspend fun login(identifier: String, password: String): User?
    
    // Obtenemos los usuarios pendientes de sincronización
    suspend fun getPendingSyncUsers(): List<User>
} 