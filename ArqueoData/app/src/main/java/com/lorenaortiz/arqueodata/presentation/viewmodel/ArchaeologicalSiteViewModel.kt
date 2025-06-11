package com.lorenaortiz.arqueodata.presentation.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lorenaortiz.arqueodata.domain.model.ArchaeologicalSite
import com.lorenaortiz.arqueodata.domain.model.TeamMember
import com.lorenaortiz.arqueodata.domain.repository.ArchaeologicalSiteRepository
import com.lorenaortiz.arqueodata.domain.repository.TeamMemberRepository
import com.lorenaortiz.arqueodata.utils.ImageStorageManager
import com.lorenaortiz.arqueodata.utils.NetworkMonitor
import com.lorenaortiz.arqueodata.data.sync.SyncManager
import com.lorenaortiz.arqueodata.data.local.dao.UserDao
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class ArchaeologicalSiteViewModel @Inject constructor(
    private val repository: ArchaeologicalSiteRepository,
    private val teamMemberRepository: TeamMemberRepository,
    private val imageStorageManager: ImageStorageManager,
    private val networkMonitor: NetworkMonitor,
    private val syncManager: SyncManager,
    private val userDao: UserDao
) : ViewModel() {

    private val _sites = MutableStateFlow<List<ArchaeologicalSite>>(emptyList())
    val sites: StateFlow<List<ArchaeologicalSite>> = _sites

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _updateComplete = MutableStateFlow(false)
    val updateComplete: StateFlow<Boolean> = _updateComplete

    private val _lastCreatedSite = MutableStateFlow<ArchaeologicalSite?>(null)
    val lastCreatedSite: StateFlow<ArchaeologicalSite?> = _lastCreatedSite.asStateFlow()

    fun resetUpdateComplete() {
        _updateComplete.value = false
    }

    var currentUserId: Long = -1
        private set

    fun setCurrentUserId(userId: Long) {
        if (userId != currentUserId) {
            currentUserId = userId
            loadSites()
        }
    }

    fun loadSites() {
        if (currentUserId == -1L) {
            _error.value = "No hay usuario autenticado"
            return
        }
        
        viewModelScope.launch {
            try {
                repository.getAllSites(currentUserId)
                    .catch { e ->
                        _error.value = "Error al cargar sitios: ${e.message}"
                    }
                    .collect { sites ->
                        _sites.value = sites
                        _error.value = null
                    }
            } catch (e: Exception) {
                _error.value = "Error al cargar sitios: ${e.message}"
            }
        }
    }

    fun addSite(site: ArchaeologicalSite) {
        if (currentUserId == -1L) {
            _error.value = "No hay usuario autenticado"
            return
        }
        
        viewModelScope.launch {
            try {
                val siteWithUserId = site.copy(userId = currentUserId)
                val siteId = repository.insertSite(siteWithUserId)
                
                // Añadir automáticamente al director como miembro del equipo
                val teamMember = TeamMember(
                    userId = currentUserId,
                    siteId = siteId,
                    role = "Director"
                )
                teamMemberRepository.insertTeamMember(teamMember)
                
                loadSites()
            } catch (e: Exception) {
                _error.value = "Error al agregar sitio: ${e.message}"
            }
        }
    }

    fun updateSite(site: ArchaeologicalSite) {
        println("DEBUG: ViewModel - Iniciando updateSite para sitio ID: ${site.id}")
        _updateComplete.value = false
        
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // 1. Verificar permisos
                if (!hasPermissionToModify(site)) {
                    println("DEBUG: ViewModel - Error: Usuario no tiene permisos")
                    withContext(Dispatchers.Main) {
                        _error.value = "No tienes permisos para modificar este sitio"
                        _updateComplete.value = true
                    }
                    return@launch
                }

                // 2. Actualizar en el repositorio
                println("DEBUG: ViewModel - Llamando a repository.updateSite")
                repository.updateSite(site)
                println("DEBUG: ViewModel - Sitio actualizado en repositorio")

                // 3. Recargar la lista de sitios
                println("DEBUG: ViewModel - Recargando lista de sitios")
                withContext(Dispatchers.Main) {
                    repository.getAllSites(currentUserId)
                        .catch { e ->
                            println("DEBUG: ViewModel - Error al cargar sitios: ${e.message}")
                            _error.value = "Error al cargar sitios: ${e.message}"
                        }
                        .collect { sites ->
                            println("DEBUG: ViewModel - Sitios recargados: ${sites.size}")
                            _sites.value = sites
                            _error.value = null
                            _updateComplete.value = true
                            println("DEBUG: ViewModel - Actualización completada")
                        }
                }
            } catch (e: Exception) {
                println("DEBUG: ViewModel - Error en updateSite: ${e.message}")
                println("DEBUG: ViewModel - Stack trace: ${e.stackTraceToString()}")
                withContext(Dispatchers.Main) {
                    _error.value = "Error al actualizar el sitio: ${e.message}"
                    _updateComplete.value = true
                }
            }
        }
    }

    fun deleteSite(site: ArchaeologicalSite) {
        if (currentUserId == -1L) {
            _error.value = "No hay usuario autenticado"
            return
        }

        if (site.userId != currentUserId) {
            _error.value = "No tienes permiso para eliminar este proyecto"
            return
        }
        
        viewModelScope.launch {
            try {
                repository.deleteSite(site)
                loadSites()
            } catch (e: Exception) {
                _error.value = "Error al eliminar sitio: ${e.message}"
            }
        }
    }

    private fun hasPermissionToModify(site: ArchaeologicalSite): Boolean {
        if (currentUserId == -1L) {
            println("DEBUG: ViewModel - No hay usuario autenticado")
            return false
        }

        if (site.userId != currentUserId) {
            println("DEBUG: ViewModel - Usuario no tiene permisos para modificar el sitio ID: ${site.id}")
            return false
        }

        return true
    }

    fun canEditSite(site: ArchaeologicalSite): Boolean {
        return site.userId == currentUserId
    }

    fun searchSites(query: String) {
        if (currentUserId == -1L) {
            _error.value = "No hay usuario autenticado"
            return
        }
        
        viewModelScope.launch {
            try {
                repository.searchSites(query, currentUserId)
                    .catch { e ->
                        _error.value = "Error al buscar sitios: ${e.message}"
                    }
                    .collect { sites ->
                        _sites.value = sites
                        _error.value = null
                    }
            } catch (e: Exception) {
                _error.value = "Error al buscar sitios: ${e.message}"
            }
        }
    }

    fun clearSites() {
        _sites.value = emptyList()
        _error.value = null
    }

    fun getSitesByStatus(status: String) {
        if (currentUserId == -1L) {
            _error.value = "No hay usuario autenticado"
            return
        }
        
        viewModelScope.launch {
            try {
                repository.getSitesByStatus(status, currentUserId)
                    .catch { e ->
                        _error.value = "Error al filtrar sitios: ${e.message}"
                    }
                    .collect { sites ->
                        _sites.value = sites
                        _error.value = null
                    }
            } catch (e: Exception) {
                _error.value = "Error al filtrar sitios: ${e.message}"
            }
        }
    }

    fun saveSiteImage(context: Context, uri: Uri): String? {
        return try {
            val savedPath = imageStorageManager.saveImage(uri, "site_main_${System.currentTimeMillis()}")
            savedPath?.let {
                if (!it.startsWith("file://")) "file://$it" else it
            }
        } catch (e: Exception) {
            _error.value = "Error al guardar la imagen: ${e.message}"
            null
        }
    }

    fun deleteSiteImage(imageUrl: String?) {
        if (imageUrl != null && imageUrl.startsWith("file://")) {
            imageStorageManager.deleteImage(imageUrl.substringAfter("file://"))
        }
    }

    fun createSite(site: ArchaeologicalSite, teamMembers: List<Pair<String, String>>) {
        viewModelScope.launch {
            try {
                val siteWithUserId = site.copy(userId = currentUserId)
                val siteId = repository.insertSite(siteWithUserId)
                
                // Añadir automáticamente al director como miembro del equipo
                val director = TeamMember(
                    userId = currentUserId,
                    siteId = siteId,
                    role = "Director"
                )
                teamMemberRepository.insertTeamMember(director)

                // Procesar los miembros del equipo
                teamMembers.forEach { (identifier, role) ->
                    // Buscar el usuario por nombre de usuario o email
                    val user = userDao.getUserByEmailOrUsername(identifier, identifier)
                    if (user != null) {
                        val member = TeamMember(
                            userId = user.id,
                            siteId = siteId,
                            role = role
                        )
                        teamMemberRepository.insertTeamMember(member)
                    }
                }
                
                // Obtener el sitio recién creado con su ID
                val createdSite = repository.getSiteById(siteId, currentUserId)
                if (createdSite != null) {
                    _lastCreatedSite.value = createdSite
                }
                
                loadSites()
            } catch (e: Exception) {
                _error.value = "Error al crear sitio: ${e.message}"
            }
        }
    }
} 