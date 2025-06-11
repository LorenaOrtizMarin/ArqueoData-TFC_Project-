package com.lorenaortiz.arqueodata.data.repository

import com.lorenaortiz.arqueodata.data.local.dao.UserDao
import com.lorenaortiz.arqueodata.data.local.entity.UserEntity
import com.lorenaortiz.arqueodata.data.local.entity.fromDomainModel
import com.lorenaortiz.arqueodata.data.local.entity.toDomainModel
import com.lorenaortiz.arqueodata.domain.model.User
import com.lorenaortiz.arqueodata.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementación del repositorio de usuarios.
 * Gestionamos la persistencia local y la autenticación de usuarios.
 */
@Singleton
class UserRepositoryImpl @Inject constructor(
    private val userDao: UserDao
) : UserRepository {

    /**
     * Obtenemos todos los usuarios registrados.
     */
    override fun getAllUsers(): Flow<List<User>> {
        return userDao.getAllUsers().map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    /**
     * Recuperamos un usuario específico por su ID.
     */
    override suspend fun getUserById(id: Long): User? {
        return userDao.getUserById(id)?.toDomainModel()
    }

    /**
     * Buscamos un usuario por su correo electrónico.
     */
    override suspend fun getUserByEmail(email: String): User? {
        return userDao.getUserByEmail(email)?.toDomainModel()
    }

    /**
     * Insertamos un nuevo usuario o actualizamos uno existente.
     */
    override suspend fun insertUser(user: User): Long {
        return userDao.insertUser(user.fromDomainModel())
    }

    /**
     * Actualizamos un usuario existente.
     */
    override suspend fun updateUser(user: User) {
        userDao.updateUser(user.fromDomainModel())
    }

    /**
     * Eliminamos un usuario específico.
     */
    override suspend fun deleteUser(user: User) {
        userDao.deleteUser(user.fromDomainModel())
    }

    /**
     * Verificamos si existe un usuario con el correo electrónico proporcionado.
     */
    override suspend fun userExists(email: String): Boolean {
        return userDao.userExists(email)
    }

    /**
     * Obtenemos usuarios por tipo.
     */
    override fun getUsersByType(userType: String): Flow<List<User>> {
        return userDao.getUsersByType(userType).map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    /**
     * Autenticamos a un usuario con su identificador y contraseña.
     */
    override suspend fun login(identifier: String, password: String): User? {
        return userDao.login(identifier, password)?.toDomainModel()
    }

    /**
     * Obtenemos los usuarios pendientes de sincronización.
     */
    override suspend fun getPendingSyncUsers(): List<User> {
        return userDao.getPendingSyncUsers().map { it.toDomainModel() }
    }
} 