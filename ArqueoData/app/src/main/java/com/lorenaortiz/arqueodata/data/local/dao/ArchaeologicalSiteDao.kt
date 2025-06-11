package com.lorenaortiz.arqueodata.data.local.dao

import androidx.room.*
import com.lorenaortiz.arqueodata.data.local.entity.ArchaeologicalSiteEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO para la gestión de sitios arqueológicos en la base de datos local.
 * Proporcionamos métodos para realizar operaciones CRUD y consultas específicas.
 */
@Dao
interface ArchaeologicalSiteDao {
    /**
     * Obtenemos todos los sitios arqueológicos almacenados.
     */
    @Query("""
        SELECT DISTINCT s.* FROM archaeological_sites s
        LEFT JOIN team_members tm ON s.id = tm.siteId
        WHERE s.userId = :userId OR tm.userId = :userId
    """)
    fun getAllSites(userId: Long): Flow<List<ArchaeologicalSiteEntity>>

    /**
     * Recuperamos un sitio específico por su ID.
     */
    @Query("SELECT * FROM archaeological_sites WHERE id = :siteId AND userId = :userId")
    fun getSiteById(siteId: Long, userId: Long): ArchaeologicalSiteEntity?

    /**
     * Insertamos un nuevo sitio o actualizamos uno existente.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSite(site: ArchaeologicalSiteEntity): Long

    /**
     * Actualizamos un sitio existente.
     */
    @Update
    suspend fun updateSite(site: ArchaeologicalSiteEntity)

    /**
     * Eliminamos un sitio específico.
     */
    @Delete
    suspend fun deleteSite(site: ArchaeologicalSiteEntity)

    /**
     * Filtramos sitios por tipo.
     */
    @Query("""
        SELECT DISTINCT s.* FROM archaeological_sites s
        LEFT JOIN team_members tm ON s.id = tm.siteId
        WHERE (s.userId = :userId OR tm.userId = :userId) AND s.type = :type
    """)
    fun getSitesByType(type: String, userId: Long): Flow<List<ArchaeologicalSiteEntity>>

    /**
     * Filtramos sitios por período histórico.
     */
    @Query("""
        SELECT DISTINCT s.* FROM archaeological_sites s
        LEFT JOIN team_members tm ON s.id = tm.siteId
        WHERE (s.userId = :userId OR tm.userId = :userId) 
        AND (s.name LIKE '%' || :query || '%' OR s.description LIKE '%' || :query || '%')
    """)
    fun searchSites(query: String, userId: Long): Flow<List<ArchaeologicalSiteEntity>>

    /**
     * Filtramos sitios por ubicación.
     */
    @Query("""
        SELECT DISTINCT s.* FROM archaeological_sites s
        LEFT JOIN team_members tm ON s.id = tm.siteId
        WHERE (s.userId = :userId OR tm.userId = :userId) AND s.status = :status
    """)
    fun getSitesByStatus(status: String, userId: Long): Flow<List<ArchaeologicalSiteEntity>>
} 