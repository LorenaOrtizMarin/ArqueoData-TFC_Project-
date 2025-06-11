package com.lorenaortiz.arqueodata.domain.repository

import com.lorenaortiz.arqueodata.domain.model.ArchaeologicalSite
import kotlinx.coroutines.flow.Flow

interface ArchaeologicalSiteRepository {
    fun getAllSites(userId: Long): Flow<List<ArchaeologicalSite>>
    suspend fun getSiteById(id: Long, userId: Long): ArchaeologicalSite?
    suspend fun insertSite(site: ArchaeologicalSite): Long
    suspend fun updateSite(site: ArchaeologicalSite)
    suspend fun deleteSite(site: ArchaeologicalSite)
    fun getSitesByType(type: String, userId: Long): Flow<List<ArchaeologicalSite>>
    fun searchSites(query: String, userId: Long): Flow<List<ArchaeologicalSite>>
    fun getSitesByStatus(status: String, userId: Long): Flow<List<ArchaeologicalSite>>
} 