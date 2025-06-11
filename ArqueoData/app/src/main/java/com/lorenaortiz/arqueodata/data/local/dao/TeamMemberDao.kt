package com.lorenaortiz.arqueodata.data.local.dao

import androidx.room.*
import com.lorenaortiz.arqueodata.data.local.entity.TeamMemberEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO para la gestión de miembros del equipo en la base de datos local.
 * Proporcionamos métodos para realizar operaciones CRUD y consultas específicas.
 */
@Dao
interface TeamMemberDao {
    /**
     * Obtenemos todos los miembros del equipo almacenados.
     */
    @Query("SELECT * FROM team_members")
    fun getAllTeamMembers(): Flow<List<TeamMemberEntity>>

    /**
     * Recuperamos un miembro específico por su ID.
     */
    @Query("SELECT * FROM team_members WHERE id = :id")
    suspend fun getTeamMemberById(id: Long): TeamMemberEntity?

    /**
     * Obtenemos todos los miembros asociados a un sitio específico.
     */
    @Query("SELECT * FROM team_members WHERE siteId = :siteId")
    fun getTeamMembersBySiteId(siteId: Long): Flow<List<TeamMemberEntity>>

    /**
     * Obtenemos todos los sitios asociados a un usuario específico.
     */
    @Query("SELECT * FROM team_members WHERE userId = :userId")
    fun getSitesByUserId(userId: Long): Flow<List<TeamMemberEntity>>

    /**
     * Insertamos un nuevo miembro o actualizamos uno existente.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTeamMember(teamMember: TeamMemberEntity): Long

    /**
     * Actualizamos un miembro existente.
     */
    @Update
    suspend fun updateTeamMember(teamMember: TeamMemberEntity)

    /**
     * Eliminamos un miembro específico.
     */
    @Delete
    suspend fun deleteTeamMember(teamMember: TeamMemberEntity)

    /**
     * Eliminamos todos los miembros asociados a un sitio.
     */
    @Query("DELETE FROM team_members WHERE siteId = :siteId")
    suspend fun deleteTeamMembersBySiteId(siteId: Long)

    /**
     * Eliminamos todos los sitios asociados a un usuario.
     */
    @Query("DELETE FROM team_members WHERE userId = :userId")
    suspend fun deleteSitesByUserId(userId: Long)
} 