package com.lorenaortiz.arqueodata.data.mapper

import com.lorenaortiz.arqueodata.data.local.entity.ArchaeologicalSiteEntity
import com.lorenaortiz.arqueodata.domain.model.ArchaeologicalSite

/**
 * Convertimos una entidad de sitio arqueológico a su modelo de dominio.
 * Transformamos los datos de la capa de datos a la capa de dominio.
 */
fun ArchaeologicalSiteEntity.toArchaeologicalSite(): ArchaeologicalSite {
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

/**
 * Convertimos un modelo de dominio de sitio arqueológico a su entidad.
 * Transformamos los datos de la capa de dominio a la capa de datos.
 */
fun ArchaeologicalSite.toArchaeologicalSiteEntity(): ArchaeologicalSiteEntity {
    return ArchaeologicalSiteEntity(
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