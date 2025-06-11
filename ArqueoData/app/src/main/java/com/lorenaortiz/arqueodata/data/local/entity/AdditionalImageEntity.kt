package com.lorenaortiz.arqueodata.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Entidad que representa una imagen adicional en la base de datos local.
 * Almacenamos las imágenes adicionales asociadas a objetos arqueológicos.
 */
@Entity(
    tableName = "additional_images",
    foreignKeys = [
        ForeignKey(
            entity = ArchaeologicalObjectEntity::class,
            parentColumns = ["id"],
            childColumns = ["objectId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("objectId")]
)
data class AdditionalImageEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val objectId: Long,
    val imageUrl: String,
    val lastModified: Long = System.currentTimeMillis()
)
