package com.lorenaortiz.arqueodata.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.lorenaortiz.arqueodata.domain.model.ArchaeologicalSite

/**
 * Entidad que representa un sitio arqueológico en la base de datos local.
 * Almacenamos la información básica del sitio y su relación con el usuario propietario.
 */
@Entity(tableName = "archaeological_sites")
data class ArchaeologicalSiteEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val location: String,
    val description: String,
    val latitude: Double,
    val longitude: Double,
    val period: String,
    val type: String,
    val status: String,
    val imageUrl: String? = null,
    val userId: Long,
    val lastModified: Long = System.currentTimeMillis()
) {
    /**
     * Convertimos la entidad a su modelo de dominio correspondiente.
     */
    fun toDomainModel(): ArchaeologicalSite {
        return ArchaeologicalSite(
            id = id,
            name = name,
            location = location,
            description = description,
            latitude = latitude,
            longitude = longitude,
            period = period,
            type = type,
            status = status,
            imageUrl = imageUrl,
            userId = userId
        )
    }

    companion object {
        /**
         * Creamos una entidad a partir de un modelo de dominio.
         */
        fun fromDomainModel(site: ArchaeologicalSite): ArchaeologicalSiteEntity {
            return ArchaeologicalSiteEntity(
                id = site.id,
                name = site.name,
                location = site.location,
                description = site.description,
                latitude = site.latitude,
                longitude = site.longitude,
                period = site.period,
                type = site.type,
                status = site.status,
                imageUrl = site.imageUrl,
                userId = site.userId
            )
        }
    }
} 