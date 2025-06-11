package com.lorenaortiz.arqueodata.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.lorenaortiz.arqueodata.data.local.dao.ArchaeologicalSiteDao
import com.lorenaortiz.arqueodata.data.local.entity.ArchaeologicalSiteEntity
import com.lorenaortiz.arqueodata.data.mapper.toArchaeologicalSite
import com.lorenaortiz.arqueodata.data.mapper.toArchaeologicalSiteEntity
import com.lorenaortiz.arqueodata.data.sync.SyncManager
import com.lorenaortiz.arqueodata.domain.model.ArchaeologicalSite
import com.lorenaortiz.arqueodata.domain.repository.ArchaeologicalSiteRepository
import com.lorenaortiz.arqueodata.utils.NetworkMonitor
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementación del repositorio de sitios arqueológicos.
 * Gestionamos la persistencia local y la sincronización de datos.
 */
@Singleton
class ArchaeologicalSiteRepositoryImpl @Inject constructor(
    private val siteDao: ArchaeologicalSiteDao,
    private val firestore: FirebaseFirestore,
    private val syncManager: SyncManager,
    private val networkMonitor: NetworkMonitor
) : ArchaeologicalSiteRepository {

    /**
     * Obtenemos todos los sitios arqueológicos accesibles para un usuario.
     */
    override fun getAllSites(userId: Long): Flow<List<ArchaeologicalSite>> {
        return siteDao.getAllSites(userId).map { entities ->
            entities.map { it.toArchaeologicalSite() }
        }
    }

    /**
     * Recuperamos un sitio específico por su ID.
     */
    override suspend fun getSiteById(id: Long, userId: Long): ArchaeologicalSite? {
        return siteDao.getSiteById(id, userId)?.toArchaeologicalSite()
    }

    /**
     * Insertamos un nuevo sitio o actualizamos uno existente.
     */
    override suspend fun insertSite(site: ArchaeologicalSite): Long {
        val entity = site.toArchaeologicalSiteEntity()
        val id = siteDao.insertSite(entity)
        if (networkMonitor.isOnline().first()) {
            syncManager.syncSites(site.userId)
        }
        return id
    }

    /**
     * Actualizamos un sitio existente.
     */
    override suspend fun updateSite(site: ArchaeologicalSite) {
        println("DEBUG: Repository - Iniciando updateSite para sitio ID: ${site.id}")
        
        try {
            // 1. Actualizar en la base de datos local
            println("DEBUG: Repository - Actualizando en base de datos local")
            val entity = site.toArchaeologicalSiteEntity()
            siteDao.updateSite(entity)
            println("DEBUG: Repository - Sitio actualizado en base de datos local")
            
            // 2. Sincronizar con Firestore si hay conexión
            if (networkMonitor.isOnline().first()) {
                println("DEBUG: Repository - Dispositivo en línea, sincronizando con Firestore")
                syncManager.syncSites(site.userId)
                println("DEBUG: Repository - Sincronización completada")
            } else {
                println("DEBUG: Repository - Dispositivo sin conexión, omitiendo sincronización")
            }
            
            println("DEBUG: Repository - Actualización completada exitosamente")
        } catch (e: Exception) {
            println("DEBUG: Repository - Error en updateSite: ${e.message}")
            println("DEBUG: Repository - Stack trace: ${e.stackTraceToString()}")
            throw e
        }
    }

    /**
     * Eliminamos un sitio específico.
     */
    override suspend fun deleteSite(site: ArchaeologicalSite) {
        siteDao.deleteSite(site.toArchaeologicalSiteEntity())
        if (networkMonitor.isOnline().first()) {
            syncManager.deleteSiteFromFirestore(site.id)
        }
    }

    /**
     * Filtramos sitios por tipo.
     */
    override fun getSitesByType(type: String, userId: Long): Flow<List<ArchaeologicalSite>> {
        return siteDao.getSitesByType(type, userId).map { entities ->
            entities.map { it.toArchaeologicalSite() }
        }
    }

    /**
     * Buscamos sitios por nombre o descripción.
     */
    override fun searchSites(query: String, userId: Long): Flow<List<ArchaeologicalSite>> {
        return siteDao.searchSites(query, userId).map { entities ->
            entities.map { it.toArchaeologicalSite() }
        }
    }

    /**
     * Filtramos sitios por estado.
     */
    override fun getSitesByStatus(status: String, userId: Long): Flow<List<ArchaeologicalSite>> {
        return siteDao.getSitesByStatus(status, userId).map { entities ->
            entities.map { it.toArchaeologicalSite() }
        }
    }
} 