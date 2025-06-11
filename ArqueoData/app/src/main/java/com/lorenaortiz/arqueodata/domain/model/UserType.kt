package com.lorenaortiz.arqueodata.domain.model

/**
 * Definimos los diferentes tipos de usuarios que pueden acceder al sistema.
 * Cada tipo tiene diferentes niveles de permisos y responsabilidades.
 */
enum class UserType {
    // Usuario con acceso total y control administrativo
    DIRECTOR,
    // Usuario con acceso a funcionalidades específicas del proyecto
    MIEMBRO,
    // Usuario con acceso limitado a ciertas funcionalidades
    COLABORADOR
} 