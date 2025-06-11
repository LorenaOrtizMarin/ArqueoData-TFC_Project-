package com.lorenaortiz.arqueodata.domain.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Representamos un sitio arqueológico en nuestra base de datos.
 * Almacenamos información detallada sobre la ubicación y características del sitio.
 */
@Entity(tableName = "archaeological_sites")
data class ArchaeologicalSite(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    // Nombre del sitio arqueológico
    val name: String,
    // Ubicación general del sitio
    val location: String,
    // Descripción detallada del sitio
    val description: String,
    // Coordenada de latitud
    val latitude: Double,
    // Coordenada de longitud
    val longitude: Double,
    // Período histórico del sitio
    val period: String,
    // Tipo de sitio arqueológico
    val type: String,
    // Estado actual del sitio
    val status: String,
    // URL de la imagen principal del sitio
    val imageUrl: String? = null,
    // ID del usuario propietario del registro
    val userId: Long // ID del usuario propietario
)

/**
 * Definimos los posibles estados en los que puede encontrarse un sitio arqueológico.
 */
enum class SiteStatus {
    // Sitio en activo y siendo investigado
    ACTIVE,
    // Sitio inactivo temporalmente
    INACTIVE,
    // Sitio bajo investigación actual
    UNDER_INVESTIGATION,
    // Sitio protegido por su importancia
    PROTECTED
} 