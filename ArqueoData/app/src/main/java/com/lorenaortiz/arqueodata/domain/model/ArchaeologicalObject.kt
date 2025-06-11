package com.lorenaortiz.arqueodata.domain.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Representamos un objeto arqueológico en nuestra base de datos.
 * Mantenemos una relación con el sitio arqueológico al que pertenece.
 */
@Entity(
    tableName = "archaeological_objects",
    foreignKeys = [
        ForeignKey(
            entity = ArchaeologicalSite::class,
            parentColumns = ["id"],
            childColumns = ["siteId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("siteId")]
)
data class ArchaeologicalObject(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    // Identificador único del objeto en el sistema
    val objectId: String,
    // Nombre descriptivo del objeto
    val name: String,
    // Descripción detallada del objeto
    val description: String,
    // Tipo de objeto arqueológico
    val type: String,
    // Período histórico al que pertenece
    val period: String,
    // Material del que está hecho
    val material: String,
    // Dimensiones del objeto
    val dimensions: String,
    // Estado de conservación
    val condition: String,
    // Ubicación específica donde se encontró
    val location: String,
    // Notas adicionales sobre el objeto
    val notes: String? = null,
    // URL de la imagen principal
    val imageUrl: String? = null,
    // Lista de URLs de imágenes adicionales
    val additionalImages: List<String> = emptyList(),
    // ID del creador del registro
    val creatorId: String? = null,
    // Nombre del creador
    val creatorName: String? = null,
    // URL de la foto del creador
    val creatorPhotoUrl: String? = null,
    // Nombre del proyecto asociado
    val projectName: String? = null,
    // ID del sitio arqueológico al que pertenece
    val siteId: Long
) 