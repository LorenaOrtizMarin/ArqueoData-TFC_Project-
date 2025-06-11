package com.lorenaortiz.arqueodata.data.local.dao

import androidx.room.*
import com.lorenaortiz.arqueodata.data.local.entity.ArchaeologicalObjectEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO para la gestión de objetos arqueológicos en la base de datos local.
 * Proporcionamos métodos para realizar operaciones CRUD y consultas específicas.
 */
@Dao
interface ArchaeologicalObjectDao {
    /**
     * Obtenemos todos los objetos arqueológicos almacenados.
     */
    @Query("SELECT * FROM archaeological_objects")
    fun getAllObjects(): Flow<List<ArchaeologicalObjectEntity>>

    /**
     * Recuperamos un objeto específico por su ID.
     */
    @Query("SELECT * FROM archaeological_objects WHERE id = :id")
    suspend fun getObjectById(id: Long): ArchaeologicalObjectEntity?

    /**
     * Obtenemos todos los objetos asociados a un sitio específico.
     */
    @Query("SELECT * FROM archaeological_objects WHERE siteId = :siteId")
    fun getObjectsBySiteId(siteId: Long): Flow<List<ArchaeologicalObjectEntity>>

    /**
     * Filtramos objetos por tipo.
     */
    @Query("SELECT * FROM archaeological_objects WHERE type = :type")
    fun getObjectsByType(type: String): Flow<List<ArchaeologicalObjectEntity>>

    /**
     * Filtramos objetos por período histórico.
     */
    @Query("SELECT * FROM archaeological_objects WHERE period = :period")
    fun getObjectsByPeriod(period: String): Flow<List<ArchaeologicalObjectEntity>>

    /**
     * Filtramos objetos por ubicación.
     */
    @Query("SELECT * FROM archaeological_objects WHERE location = :location")
    fun getObjectsByLocation(location: String): Flow<List<ArchaeologicalObjectEntity>>

    /**
     * Insertamos un nuevo objeto o actualizamos uno existente.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertObject(archaeologicalObject: ArchaeologicalObjectEntity): Long

    /**
     * Actualizamos un objeto existente.
     */
    @Update
    suspend fun updateObject(archaeologicalObject: ArchaeologicalObjectEntity)

    /**
     * Eliminamos un objeto específico.
     */
    @Delete
    suspend fun deleteObject(archaeologicalObject: ArchaeologicalObjectEntity)

    /**
     * Eliminamos todos los objetos asociados a un sitio.
     */
    @Query("DELETE FROM archaeological_objects WHERE siteId = :siteId")
    suspend fun deleteObjectsBySiteId(siteId: Long)

    /**
     * Buscamos objetos por nombre usando coincidencia parcial.
     */
    @Query("SELECT * FROM archaeological_objects WHERE name LIKE '%' || :name || '%'")
    fun searchObjectsByName(name: String): Flow<List<ArchaeologicalObjectEntity>>
} 