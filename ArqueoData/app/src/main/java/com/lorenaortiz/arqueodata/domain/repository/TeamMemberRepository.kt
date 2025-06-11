package com.lorenaortiz.arqueodata.domain.repository

import com.lorenaortiz.arqueodata.domain.model.TeamMember
import kotlinx.coroutines.flow.Flow

interface TeamMemberRepository {
    fun getAllTeamMembers(): Flow<List<TeamMember>>
    suspend fun getTeamMemberById(id: Long): TeamMember?
    fun getTeamMembersBySiteId(siteId: Long): Flow<List<TeamMember>>
    fun getSitesByUserId(userId: Long): Flow<List<Long>>
    suspend fun insertTeamMember(member: TeamMember): Long
    suspend fun updateTeamMember(member: TeamMember)
    suspend fun deleteTeamMember(member: TeamMember)
    suspend fun deleteTeamMembersBySiteId(siteId: Long)
    suspend fun deleteSitesByUserId(userId: Long)
    suspend fun processPendingTeamMembers(siteId: Long)
} 