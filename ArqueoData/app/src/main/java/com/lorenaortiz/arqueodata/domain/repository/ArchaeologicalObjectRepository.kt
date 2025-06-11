package com.lorenaortiz.arqueodata.domain.repository

import com.lorenaortiz.arqueodata.domain.model.ArchaeologicalObject
import kotlinx.coroutines.flow.Flow

/**
 * Interfaz que define las operaciones que podemos realizar con los objetos arqueológicos.
 * Gestionamos la persistencia y recuperación de datos de objetos arqueológicos.
 */
interface ArchaeologicalObjectRepository {
    // Obtenemos todos los objetos asociados a un sitio específico
    fun getObjectsBySiteId(siteId: Long): Flow<List<ArchaeologicalObject>>
    
    // Recuperamos un objeto específico por su ID
    suspend fun getObjectById(id: Long): ArchaeologicalObject?
    
    // Insertamos un nuevo objeto y devolvemos su ID
    suspend fun insertObject(archaeologicalObject: ArchaeologicalObject): Long
    
    // Actualizamos un objeto existente
    suspend fun updateObject(archaeologicalObject: ArchaeologicalObject)
    
    // Eliminamos un objeto específico
    suspend fun deleteObject(archaeologicalObject: ArchaeologicalObject)
    
    // Eliminamos todos los objetos asociados a un sitio
    suspend fun deleteObjectsBySiteId(siteId: Long)
    
    // Obtenemos todos los objetos del sistema
    fun getAllObjects(): Flow<List<ArchaeologicalObject>>
    
    // Buscamos objetos por nombre
    fun searchObjectsByName(name: String): Flow<List<ArchaeologicalObject>>
    
    // Filtramos objetos por tipo
    fun filterObjectsByType(type: String): Flow<List<ArchaeologicalObject>>
    
    // Filtramos objetos por período
    fun filterObjectsByPeriod(period: String): Flow<List<ArchaeologicalObject>>
    
    // Filtramos objetos por ubicación
    fun filterObjectsByLocation(location: String): Flow<List<ArchaeologicalObject>>
} 