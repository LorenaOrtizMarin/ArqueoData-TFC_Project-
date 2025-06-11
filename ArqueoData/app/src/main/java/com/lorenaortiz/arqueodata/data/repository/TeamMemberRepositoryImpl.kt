package com.lorenaortiz.arqueodata.data.repository

import com.lorenaortiz.arqueodata.data.local.dao.TeamMemberDao
import com.lorenaortiz.arqueodata.data.local.entity.TeamMemberEntity
import com.lorenaortiz.arqueodata.domain.model.TeamMember
import com.lorenaortiz.arqueodata.domain.repository.TeamMemberRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementación del repositorio de miembros del equipo.
 * Gestionamos la persistencia local y las relaciones entre usuarios y sitios.
 */
@Singleton
class TeamMemberRepositoryImpl @Inject constructor(
    private val teamMemberDao: TeamMemberDao
) : TeamMemberRepository {

    /**
     * Obtenemos todos los miembros del equipo registrados.
     */
    override fun getAllTeamMembers(): Flow<List<TeamMember>> {
        return teamMemberDao.getAllTeamMembers().map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    /**
     * Recuperamos un miembro específico por su ID.
     */
    override suspend fun getTeamMemberById(id: Long): TeamMember? {
        return teamMemberDao.getTeamMemberById(id)?.toDomainModel()
    }

    /**
     * Obtenemos todos los miembros asociados a un sitio específico.
     */
    override fun getTeamMembersBySiteId(siteId: Long): Flow<List<TeamMember>> {
        return teamMemberDao.getTeamMembersBySiteId(siteId).map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    /**
     * Obtenemos todos los sitios asociados a un usuario específico.
     */
    override fun getSitesByUserId(userId: Long): Flow<List<Long>> {
        return teamMemberDao.getSitesByUserId(userId).map { entities ->
            entities.map { it.siteId }
        }
    }

    /**
     * Insertamos un nuevo miembro o actualizamos uno existente.
     */
    override suspend fun insertTeamMember(teamMember: TeamMember): Long {
        return teamMemberDao.insertTeamMember(TeamMemberEntity.fromDomainModel(teamMember))
    }

    /**
     * Actualizamos un miembro existente.
     */
    override suspend fun updateTeamMember(teamMember: TeamMember) {
        teamMemberDao.updateTeamMember(TeamMemberEntity.fromDomainModel(teamMember))
    }

    /**
     * Eliminamos un miembro específico.
     */
    override suspend fun deleteTeamMember(teamMember: TeamMember) {
        teamMemberDao.deleteTeamMember(TeamMemberEntity.fromDomainModel(teamMember))
    }

    /**
     * Eliminamos todos los miembros asociados a un sitio.
     */
    override suspend fun deleteTeamMembersBySiteId(siteId: Long) {
        teamMemberDao.deleteTeamMembersBySiteId(siteId)
    }

    /**
     * Eliminamos todos los sitios asociados a un usuario.
     */
    override suspend fun deleteSitesByUserId(userId: Long) {
        teamMemberDao.deleteSitesByUserId(userId)
    }

    override suspend fun processPendingTeamMembers(siteId: Long) {
        // Este método se implementará en el ViewModel ya que necesita acceso al UserDao
    }
} 