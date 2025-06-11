package com.lorenaortiz.arqueodata.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lorenaortiz.arqueodata.data.local.dao.UserDao
import com.lorenaortiz.arqueodata.data.local.entity.toDomainModel
import com.lorenaortiz.arqueodata.data.sync.SyncManager
import com.lorenaortiz.arqueodata.domain.model.TeamMember
import com.lorenaortiz.arqueodata.domain.model.User
import com.lorenaortiz.arqueodata.domain.repository.TeamMemberRepository
import com.lorenaortiz.arqueodata.domain.usecase.GetTeamMembersUseCase
import com.lorenaortiz.arqueodata.domain.usecase.InsertTeamMemberUseCase
import com.lorenaortiz.arqueodata.domain.usecase.UpdateTeamMemberUseCase
import com.lorenaortiz.arqueodata.domain.usecase.DeleteTeamMemberUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TeamMemberViewModel @Inject constructor(
    private val userDao: UserDao,
    private val getTeamMembersUseCase: GetTeamMembersUseCase,
    private val insertTeamMemberUseCase: InsertTeamMemberUseCase,
    private val updateTeamMemberUseCase: UpdateTeamMemberUseCase,
    private val deleteTeamMemberUseCase: DeleteTeamMemberUseCase,
    private val syncManager: SyncManager,
    private val teamMemberRepository: TeamMemberRepository
) : ViewModel() {

    private val _teamMembers = MutableStateFlow<List<TeamMember>>(emptyList())
    val teamMembers: StateFlow<List<TeamMember>> = _teamMembers

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _memberSiteIds = MutableStateFlow<List<Long>>(emptyList())
    val memberSiteIds: StateFlow<List<Long>> = _memberSiteIds

    private val _pendingTeamMembers = MutableStateFlow<List<Pair<String, String>>>(emptyList())
    val pendingTeamMembers: StateFlow<List<Pair<String, String>>> = _pendingTeamMembers

    fun loadSitesByUserId(userId: Long) {
        viewModelScope.launch {
            try {
                getTeamMembersUseCase(userId, true)
                    .catch { e ->
                        _error.value = "Error al cargar sitios del usuario: ${e.message}"
                    }
                    .collect { siteIds ->
                        _memberSiteIds.value = siteIds
                        _error.value = null
                    }
            } catch (e: Exception) {
                _error.value = "Error al cargar sitios del usuario: ${e.message}"
            }
        }
    }

    fun getTeamMembersBySiteId(siteId: Long) {
        viewModelScope.launch {
            try {
                getTeamMembersUseCase(siteId)
                    .catch { e ->
                        _error.value = "Error al cargar miembros del equipo: ${e.message}"
                    }
                    .collect { members ->
                        _teamMembers.value = members
                        _error.value = null
                    }
            } catch (e: Exception) {
                _error.value = "Error al cargar miembros del equipo: ${e.message}"
            }
        }
    }

    fun addTeamMember(userIdentifier: String, role: String, siteId: Long) {
        viewModelScope.launch {
            try {
                println("Intentando añadir miembro: $userIdentifier con rol: $role al sitio: $siteId")
                
                if (userIdentifier.isBlank()) {
                    _error.value = "El identificador del usuario no puede estar vacío"
                    return@launch
                }
                
                if (role.isBlank()) {
                    _error.value = "El rol no puede estar vacío"
                    return@launch
                }
                
                // Buscar el usuario por nombre de usuario o email
                val user = userDao.getUserByEmailOrUsername(userIdentifier, userIdentifier)
                if (user == null) {
                    println("Usuario no encontrado: $userIdentifier")
                    _error.value = "Usuario no encontrado. Por favor, verifica que el email o nombre de usuario sea correcto"
                    return@launch
                }
                println("Usuario encontrado: ${user.id} - ${user.nombre}")

                // Verificar si el usuario ya es miembro del equipo
                val existingMember = _teamMembers.value.find { it.userId == user.id }
                if (existingMember != null) {
                    println("El usuario ya es miembro del equipo")
                    _error.value = "El usuario ya es miembro del equipo con el rol: ${existingMember.role}"
                    return@launch
                }

                // Crear el nuevo miembro del equipo
                val newMember = TeamMember(
                    userId = user.id,
                    siteId = siteId,
                    role = role
                )
                println("Creando nuevo miembro: ${newMember.userId} - ${newMember.role}")
                
                insertTeamMemberUseCase(newMember)
                println("Miembro añadido exitosamente")
                
                // Sincronizar con Firebase
                syncManager.syncTeamMembers(siteId)
                println("Sincronización completada")
                
                // Actualizar la lista de miembros
                getTeamMembersBySiteId(siteId)
                _error.value = null
            } catch (e: Exception) {
                println("Error al agregar miembro del equipo: ${e.message}")
                println("Stack trace: ${e.stackTraceToString()}")
                _error.value = "Error al agregar miembro del equipo: ${e.message}"
            }
        }
    }

    fun insertTeamMember(teamMember: TeamMember) {
        viewModelScope.launch {
            try {
                insertTeamMemberUseCase(teamMember)
                // Sincronizar después de insertar
                syncManager.syncTeamMembers(teamMember.siteId)
                getTeamMembersBySiteId(teamMember.siteId)
            } catch (e: Exception) {
                _error.value = e.message ?: "Error al insertar miembro del equipo"
            }
        }
    }

    fun updateTeamMember(teamMember: TeamMember) {
        viewModelScope.launch {
            try {
                updateTeamMemberUseCase(teamMember)
                // Sincronizar después de actualizar
                syncManager.syncTeamMembers(teamMember.siteId)
                getTeamMembersBySiteId(teamMember.siteId)
            } catch (e: Exception) {
                _error.value = e.message ?: "Error al actualizar miembro del equipo"
            }
        }
    }

    fun deleteTeamMember(teamMember: TeamMember) {
        viewModelScope.launch {
            try {
                deleteTeamMemberUseCase(teamMember)
                // Sincronizar después de eliminar
                syncManager.syncTeamMembers(teamMember.siteId)
                getTeamMembersBySiteId(teamMember.siteId)
            } catch (e: Exception) {
                _error.value = "Error al eliminar miembro: ${e.message}"
            }
        }
    }

    fun addPendingTeamMember(userIdentifier: String, role: String) {
        viewModelScope.launch {
            try {
                // Buscar el usuario por nombre de usuario o email
                val user = userDao.getUserByEmailOrUsername(userIdentifier, userIdentifier)
                if (user == null) {
                    _error.value = "Usuario no encontrado"
                    return@launch
                }

                // Agregar a la lista de miembros pendientes
                _pendingTeamMembers.value = _pendingTeamMembers.value + (userIdentifier to role)
                _error.value = null
            } catch (e: Exception) {
                _error.value = "Error al agregar miembro pendiente: ${e.message}"
            }
        }
    }

    fun processPendingTeamMembers(siteId: Long) {
        viewModelScope.launch {
            try {
                _pendingTeamMembers.value.forEach { (userIdentifier, role) ->
                    val user = userDao.getUserByEmailOrUsername(userIdentifier, userIdentifier)
                    if (user != null) {
                        val newMember = TeamMember(
                            userId = user.id,
                            siteId = siteId,
                            role = role
                        )
                        insertTeamMemberUseCase(newMember)
                    }
                }
                // Limpiar la lista de miembros pendientes
                _pendingTeamMembers.value = emptyList()
                getTeamMembersBySiteId(siteId)
            } catch (e: Exception) {
                _error.value = "Error al procesar miembros pendientes: ${e.message}"
            }
        }
    }

    suspend fun getUserInfo(userId: Long): User? {
        return userDao.getUserById(userId)?.toDomainModel()
    }
} 