package com.lorenaortiz.arqueodata.domain.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Representamos un usuario en nuestra base de datos.
 * Almacenamos la información personal y de autenticación del usuario.
 */
@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    // Nombre completo del usuario
    val nombre: String,
    // Nombre de usuario para inicio de sesión
    val usuario: String,
    // Correo electrónico del usuario
    val email: String,
    // Contraseña encriptada
    val password: String,
    // Tipo de usuario (DIRECTOR, MIEMBRO, COLABORADOR)
    val userType: UserType,
    // URL de la foto de perfil
    val photoUrl: String? = null
) 