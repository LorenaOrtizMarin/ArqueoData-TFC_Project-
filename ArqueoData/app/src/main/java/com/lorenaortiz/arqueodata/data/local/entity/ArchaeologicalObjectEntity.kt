package com.lorenaortiz.arqueodata.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.lorenaortiz.arqueodata.domain.model.ArchaeologicalObject

/**
 * Entidad que representa un objeto arqueológico en la base de datos local.
 * Almacenamos la información detallada del objeto y su relación con el sitio al que pertenece.
 */
@Entity(
    tableName = "archaeological_objects",
    foreignKeys = [
        ForeignKey(
            entity = ArchaeologicalSiteEntity::class,
            parentColumns = ["id"],
            childColumns = ["siteId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("siteId")]
)
data class ArchaeologicalObjectEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val objectId: String,
    val name: String,
    val description: String,
    val type: String,
    val period: String,
    val material: String,
    val dimensions: String,
    val condition: String,
    val location: String,
    val notes: String? = null,
    val imageUrl: String? = null,
    val creatorId: String? = null,
    val creatorName: String? = null,
    val creatorPhotoUrl: String? = null,
    val projectName: String? = null,
    val siteId: Long,
    val lastModified: Long = System.currentTimeMillis()
) {
    /**
     * Convertimos la entidad a su modelo de dominio correspondiente.
     */
    fun toDomainModel(additionalImages: List<String> = emptyList()): ArchaeologicalObject {
        return ArchaeologicalObject(
            id = id,
            objectId = objectId,
            siteId = siteId,
            name = name,
            description = description,
            type = type,
            material = material,
            period = period,
            dimensions = dimensions,
            condition = condition,
            location = location,
            notes = notes,
            imageUrl = imageUrl,
            additionalImages = additionalImages,
            creatorId = creatorId,
            creatorName = creatorName,
            creatorPhotoUrl = creatorPhotoUrl,
            projectName = projectName
        )
    }

    companion object {
        /**
         * Creamos una entidad a partir de un modelo de dominio.
         */
        fun fromDomainModel(archaeologicalObject: ArchaeologicalObject): ArchaeologicalObjectEntity {
            return ArchaeologicalObjectEntity(
                id = archaeologicalObject.id,
                objectId = archaeologicalObject.objectId,
                siteId = archaeologicalObject.siteId,
                name = archaeologicalObject.name,
                description = archaeologicalObject.description,
                type = archaeologicalObject.type,
                material = archaeologicalObject.material,
                period = archaeologicalObject.period,
                dimensions = archaeologicalObject.dimensions,
                condition = archaeologicalObject.condition,
                location = archaeologicalObject.location,
                notes = archaeologicalObject.notes,
                imageUrl = archaeologicalObject.imageUrl,
                creatorId = archaeologicalObject.creatorId,
                creatorName = archaeologicalObject.creatorName,
                creatorPhotoUrl = archaeologicalObject.creatorPhotoUrl,
                projectName = archaeologicalObject.projectName
            )
        }
    }
} 