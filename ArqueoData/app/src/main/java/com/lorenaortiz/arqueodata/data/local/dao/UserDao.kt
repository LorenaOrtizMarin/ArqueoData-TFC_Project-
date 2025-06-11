package com.lorenaortiz.arqueodata.data.local.dao

import androidx.room.*
import com.lorenaortiz.arqueodata.data.local.entity.UserEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO para la gestión de usuarios en la base de datos local.
 * Proporcionamos métodos para realizar operaciones CRUD y consultas específicas.
 */
@Dao
interface UserDao {
    /**
     * Obtenemos todos los usuarios almacenados.
     */
    @Query("SELECT * FROM users")
    fun getAllUsers(): Flow<List<UserEntity>>

    /**
     * Recuperamos un usuario específico por su ID.
     */
    @Query("SELECT * FROM users WHERE id = :id")
    suspend fun getUserById(id: Long): UserEntity?

    /**
     * Buscamos un usuario por su correo electrónico.
     */
    @Query("SELECT * FROM users WHERE email = :email")
    suspend fun getUserByEmail(email: String): UserEntity?

    /**
     * Insertamos un nuevo usuario o actualizamos uno existente.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity): Long

    /**
     * Actualizamos un usuario existente.
     */
    @Update
    suspend fun updateUser(user: UserEntity)

    /**
     * Eliminamos un usuario específico.
     */
    @Delete
    suspend fun deleteUser(user: UserEntity)

    /**
     * Verificamos si existe un usuario con el correo electrónico proporcionado.
     */
    @Query("SELECT EXISTS(SELECT 1 FROM users WHERE email = :email)")
    suspend fun userExists(email: String): Boolean

    /**
     * Obtenemos usuarios por tipo.
     */
    @Query("SELECT * FROM users WHERE userType = :userType")
    fun getUsersByType(userType: String): Flow<List<UserEntity>>

    @Query("SELECT * FROM users WHERE usuario = :username")
    suspend fun getUserByUsername(username: String): UserEntity?

    @Query("SELECT * FROM users WHERE email = :email OR usuario = :username")
    suspend fun getUserByEmailOrUsername(email: String, username: String): UserEntity? {
        println("Buscando usuario por email: $email o username: $username")
        return try {
            // Primero intentar buscar por email
            val userByEmail = getUserByEmail(email)
            if (userByEmail != null) {
                println("Usuario encontrado por email: ${userByEmail.id} - ${userByEmail.nombre}")
                return userByEmail
            }
            
            // Si no se encuentra por email, buscar por username
            val userByUsername = getUserByUsername(username)
            if (userByUsername != null) {
                println("Usuario encontrado por username: ${userByUsername.id} - ${userByUsername.nombre}")
                return userByUsername
            }
            
            println("Usuario no encontrado ni por email ni por username")
            null
        } catch (e: Exception) {
            println("Error al buscar usuario: ${e.message}")
            println("Stack trace: ${e.stackTraceToString()}")
            null
        }
    }

    @Query("SELECT * FROM users WHERE email = :email OR usuario = :username")
    suspend fun getUserByEmailOrUsernameInternal(email: String, username: String): UserEntity?

    @Query("SELECT * FROM users WHERE (email = :identifier OR usuario = :identifier) AND password = :password LIMIT 1")
    suspend fun login(identifier: String, password: String): UserEntity?

    @Query("SELECT * FROM users WHERE pendingSync = 1")
    suspend fun getPendingSyncUsers(): List<UserEntity>
} 