package com.lorenaortiz.arqueodata.domain.usecase

import com.lorenaortiz.arqueodata.domain.model.TeamMember
import com.lorenaortiz.arqueodata.domain.repository.TeamMemberRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Caso de uso para obtener los miembros de un equipo.
 * Proporcionamos dos formas de consulta: por sitio o por usuario.
 */
class GetTeamMembersUseCase @Inject constructor(
    private val repository: TeamMemberRepository
) {
    /**
     * Obtenemos todos los miembros de un sitio específico.
     */
    operator fun invoke(siteId: Long): Flow<List<TeamMember>> {
        return repository.getTeamMembersBySiteId(siteId)
    }

    /**
     * Obtenemos todos los sitios a los que pertenece un usuario.
     */
    operator fun invoke(userId: Long, byUserId: Boolean = true): Flow<List<Long>> {
        return repository.getSitesByUserId(userId)
    }
}

/**
 * Caso de uso para añadir un nuevo miembro al equipo.
 * Devolvemos el ID del nuevo miembro creado.
 */
class InsertTeamMemberUseCase @Inject constructor(
    private val repository: TeamMemberRepository
) {
    suspend operator fun invoke(member: TeamMember): Long {
        return repository.insertTeamMember(member)
    }
}

/**
 * Caso de uso para actualizar la información de un miembro del equipo.
 */
class UpdateTeamMemberUseCase @Inject constructor(
    private val repository: TeamMemberRepository
) {
    suspend operator fun invoke(member: TeamMember) {
        repository.updateTeamMember(member)
    }
}

/**
 * Caso de uso para eliminar un miembro del equipo.
 */
class DeleteTeamMemberUseCase @Inject constructor(
    private val repository: TeamMemberRepository
) {
    suspend operator fun invoke(member: TeamMember) {
        repository.deleteTeamMember(member)
    }
} 