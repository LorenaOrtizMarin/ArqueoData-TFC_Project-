package com.lorenaortiz.arqueodata.data.repository

import com.lorenaortiz.arqueodata.data.local.dao.AdditionalImageDao
import com.lorenaortiz.arqueodata.data.local.dao.ArchaeologicalObjectDao
import com.lorenaortiz.arqueodata.data.local.entity.AdditionalImageEntity
import com.lorenaortiz.arqueodata.data.local.entity.ArchaeologicalObjectEntity
import com.lorenaortiz.arqueodata.domain.model.ArchaeologicalObject
import com.lorenaortiz.arqueodata.domain.repository.ArchaeologicalObjectRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementación del repositorio de objetos arqueológicos.
 * Gestionamos la persistencia local y la sincronización de datos.
 */
@Singleton
class ArchaeologicalObjectRepositoryImpl @Inject constructor(
    private val archaeologicalObjectDao: ArchaeologicalObjectDao,
    private val additionalImageDao: AdditionalImageDao
) : ArchaeologicalObjectRepository {
    /**
     * Obtenemos todos los objetos arqueológicos con sus imágenes adicionales.
     */
    override fun getAllObjects(): Flow<List<ArchaeologicalObject>> {
        return archaeologicalObjectDao.getAllObjects().map { entities ->
            entities.map { entity ->
                val additionalImages = additionalImageDao.getImagesForObject(entity.id).first()
                entity.toDomainModel(additionalImages.map { it.imageUrl })
            }
        }
    }

    /**
     * Recuperamos un objeto específico por su ID junto con sus imágenes adicionales.
     */
    override suspend fun getObjectById(id: Long): ArchaeologicalObject? {
        val objectEntity = archaeologicalObjectDao.getObjectById(id) ?: return null
        val additionalImages = additionalImageDao.getImagesForObject(id).first()
        return objectEntity.toDomainModel(additionalImages.map { it.imageUrl })
    }

    /**
     * Obtenemos todos los objetos asociados a un sitio específico.
     */
    override fun getObjectsBySiteId(siteId: Long): Flow<List<ArchaeologicalObject>> {
        return archaeologicalObjectDao.getObjectsBySiteId(siteId).map { entities ->
            entities.map { entity ->
                val additionalImages = additionalImageDao.getImagesForObject(entity.id).first()
                entity.toDomainModel(additionalImages.map { it.imageUrl })
            }
        }
    }

    /**
     * Filtramos objetos por tipo.
     */
    override fun filterObjectsByType(type: String): Flow<List<ArchaeologicalObject>> {
        return archaeologicalObjectDao.getObjectsByType(type).map { entities ->
            entities.map { entity ->
                val additionalImages = additionalImageDao.getImagesForObject(entity.id).first()
                entity.toDomainModel(additionalImages.map { it.imageUrl })
            }
        }
    }

    /**
     * Filtramos objetos por período histórico.
     */
    override fun filterObjectsByPeriod(period: String): Flow<List<ArchaeologicalObject>> {
        return archaeologicalObjectDao.getObjectsByPeriod(period).map { entities ->
            entities.map { entity ->
                val additionalImages = additionalImageDao.getImagesForObject(entity.id).first()
                entity.toDomainModel(additionalImages.map { it.imageUrl })
            }
        }
    }

    /**
     * Filtramos objetos por ubicación.
     */
    override fun filterObjectsByLocation(location: String): Flow<List<ArchaeologicalObject>> {
        return archaeologicalObjectDao.getObjectsByLocation(location).map { entities ->
            entities.map { entity ->
                val additionalImages = additionalImageDao.getImagesForObject(entity.id).first()
                entity.toDomainModel(additionalImages.map { it.imageUrl })
            }
        }
    }

    override suspend fun insertObject(archaeologicalObject: ArchaeologicalObject): Long {
        val objectId = archaeologicalObjectDao.insertObject(ArchaeologicalObjectEntity.fromDomainModel(archaeologicalObject))
        
        // Insertar imágenes adicionales
        if (archaeologicalObject.additionalImages.isNotEmpty()) {
            val additionalImages = archaeologicalObject.additionalImages.map { imageUrl ->
                AdditionalImageEntity(
                    objectId = objectId,
                    imageUrl = imageUrl
                )
            }
            additionalImageDao.insertImages(additionalImages)
        }
        
        return objectId
    }

    override suspend fun updateObject(archaeologicalObject: ArchaeologicalObject) {
        archaeologicalObjectDao.updateObject(ArchaeologicalObjectEntity.fromDomainModel(archaeologicalObject))
        
        // Actualizar imágenes adicionales
        additionalImageDao.deleteImagesForObject(archaeologicalObject.id)
        if (archaeologicalObject.additionalImages.isNotEmpty()) {
            val additionalImages = archaeologicalObject.additionalImages.map { imageUrl ->
                AdditionalImageEntity(
                    objectId = archaeologicalObject.id,
                    imageUrl = imageUrl
                )
            }
            additionalImageDao.insertImages(additionalImages)
        }
    }

    override suspend fun deleteObject(archaeologicalObject: ArchaeologicalObject) {
        archaeologicalObjectDao.deleteObject(ArchaeologicalObjectEntity.fromDomainModel(archaeologicalObject))
        additionalImageDao.deleteImagesForObject(archaeologicalObject.id)
    }

    override suspend fun deleteObjectsBySiteId(siteId: Long) {
        archaeologicalObjectDao.deleteObjectsBySiteId(siteId)
    }

    override fun searchObjectsByName(name: String): Flow<List<ArchaeologicalObject>> {
        return archaeologicalObjectDao.searchObjectsByName(name).map { entities ->
            entities.map { entity ->
                val additionalImages = additionalImageDao.getImagesForObject(entity.id).first()
                entity.toDomainModel(additionalImages.map { it.imageUrl })
            }
        }
    }
} 