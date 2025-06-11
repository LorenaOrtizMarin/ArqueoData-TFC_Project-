package com.lorenaortiz.arqueodata.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.lorenaortiz.arqueodata.domain.model.UserType
import com.lorenaortiz.arqueodata.domain.model.User

/**
 * Entidad que representa un usuario en la base de datos local.
 * Almacenamos la información de autenticación y perfil del usuario.
 */
@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val nombre: String,
    val usuario: String,
    val email: String,
    val password: String,
    val userType: UserType,
    val photoUrl: String? = null,
    val pendingSync: Boolean = false
)

/**
 * Convertimos la entidad a su modelo de dominio correspondiente.
 */
fun UserEntity.toDomainModel(): User {
    return User(
        id = id,
        nombre = nombre,
        usuario = usuario,
        email = email,
        password = password,
        userType = userType,
        photoUrl = photoUrl
    )
}

/**
 * Convertimos el modelo de dominio a su entidad correspondiente.
 */
fun User.fromDomainModel(): UserEntity {
    return UserEntity(
        id = id,
        nombre = nombre,
        usuario = usuario,
        email = email,
        password = password,
        userType = userType,
        photoUrl = photoUrl,
        pendingSync = false
    )
} 