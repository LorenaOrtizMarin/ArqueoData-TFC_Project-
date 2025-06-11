package com.lorenaortiz.arqueodata.presentation.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lorenaortiz.arqueodata.domain.model.ArchaeologicalObject
import com.lorenaortiz.arqueodata.domain.model.UserType
import com.lorenaortiz.arqueodata.domain.usecase.*
import com.lorenaortiz.arqueodata.utils.ImageStorageManager
import com.lorenaortiz.arqueodata.data.sync.SyncManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ArchaeologicalObjectViewModel @Inject constructor(
    private val insertObjectUseCase: InsertObjectUseCase,
    private val updateObjectUseCase: UpdateObjectUseCase,
    private val deleteObjectUseCase: DeleteObjectUseCase,
    private val getObjectByIdUseCase: GetObjectByIdUseCase,
    private val getObjectsBySiteIdUseCase: GetObjectsBySiteIdUseCase,
    private val getAllObjectsUseCase: GetAllObjectsUseCase,
    private val searchObjectsByNameUseCase: SearchObjectsByNameUseCase,
    private val filterObjectsByTypeUseCase: FilterObjectsByTypeUseCase,
    private val filterObjectsByPeriodUseCase: FilterObjectsByPeriodUseCase,
    private val filterObjectsByLocationUseCase: FilterObjectsByLocationUseCase,
    private val imageStorageManager: ImageStorageManager,
    private val syncManager: SyncManager
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState>(UiState.Initial)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private var currentUserId: Long = -1L
    private var currentUserType: UserType? = null
    private var currentUserName: String? = null
    private var currentUserPhotoUrl: String? = null

    fun setCurrentUser(userId: Long, userType: UserType, userName: String? = null, userPhotoUrl: String? = null) {
        currentUserId = userId
        currentUserType = userType
        currentUserName = userName
        currentUserPhotoUrl = userPhotoUrl
    }

    fun getCurrentUserId(): Long = currentUserId
    fun getCurrentUserName(): String? = currentUserName
    fun getCurrentUserPhotoUrl(): String? = currentUserPhotoUrl

    fun insertObject(archaeologicalObject: ArchaeologicalObject) {
        viewModelScope.launch {
            try {
                _uiState.value = UiState.Loading
                
                // Procesar la imagen principal si existe
                var imageUrl = archaeologicalObject.imageUrl
                if (imageUrl != null && imageUrl.startsWith("content://")) {
                    imageUrl = imageStorageManager.saveImage(Uri.parse(imageUrl), "object_main_${System.currentTimeMillis()}")
                }

                // Procesar imágenes adicionales
                val additionalImages = archaeologicalObject.additionalImages.mapNotNull { url ->
                    if (url.startsWith("content://")) {
                        imageStorageManager.saveImage(Uri.parse(url), "object_additional_${System.currentTimeMillis()}")
                    } else if (!url.startsWith("file://")) {
                        // Si la URL no es content:// ni file://, asumimos que es una ruta relativa
                        imageStorageManager.getImageFile(url)?.absolutePath?.let { "file://$it" }
                    } else {
                        url
                    }
                }

                val objectWithProcessedImages = archaeologicalObject.copy(
                    imageUrl = imageUrl,
                    additionalImages = additionalImages
                )

                val objectId = insertObjectUseCase(objectWithProcessedImages)
                
                // Sincronizar el objeto recién creado
                syncManager.syncObjects(archaeologicalObject.siteId)
                
                _uiState.value = UiState.Success
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "Error desconocido")
            }
        }
    }

    fun updateObject(archaeologicalObject: ArchaeologicalObject) {
        viewModelScope.launch {
            try {
                _uiState.value = UiState.Loading
                
                // Procesar la imagen principal si es nueva
                var imageUrl = archaeologicalObject.imageUrl
                if (imageUrl != null && imageUrl.startsWith("content://")) {
                    // Eliminar imagen anterior si existe
                    getObjectByIdUseCase(archaeologicalObject.id)?.imageUrl?.let { oldUrl ->
                        if (oldUrl.startsWith("file://")) {
                            imageStorageManager.deleteImage(oldUrl.substringAfter("file://"))
                        }
                    }
                    imageUrl = imageStorageManager.saveImage(Uri.parse(imageUrl), "object_main_${System.currentTimeMillis()}")
                } else if (imageUrl != null && !imageUrl.startsWith("file://")) {
                    // Si la URL no es content:// ni file://, asumimos que es una ruta relativa
                    imageUrl = imageStorageManager.getImageFile(imageUrl)?.absolutePath?.let { "file://$it" }
                }

                // Procesar imágenes adicionales
                val additionalImages = archaeologicalObject.additionalImages.mapNotNull { url ->
                    if (url.startsWith("content://")) {
                        imageStorageManager.saveImage(Uri.parse(url), "object_additional_${System.currentTimeMillis()}")
                    } else if (!url.startsWith("file://")) {
                        // Si la URL no es content:// ni file://, asumimos que es una ruta relativa
                        imageStorageManager.getImageFile(url)?.absolutePath?.let { "file://$it" }
                    } else {
                        url
                    }
                }

                val objectWithProcessedImages = archaeologicalObject.copy(
                    imageUrl = imageUrl,
                    additionalImages = additionalImages
                )

                updateObjectUseCase(objectWithProcessedImages)
                _uiState.value = UiState.Success
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "Error desconocido")
            }
        }
    }

    fun deleteObject(archaeologicalObject: ArchaeologicalObject) {
        viewModelScope.launch {
            try {
                _uiState.value = UiState.Loading
                
                // Verificar si el usuario es director
                if (currentUserType != UserType.DIRECTOR) {
                    _uiState.value = UiState.Error("Solo los directores pueden eliminar objetos")
                    return@launch
                }
                
                // Eliminar imágenes asociadas
                
                // Eliminar imagen principal
                archaeologicalObject.imageUrl?.let { url ->
                    if (url.startsWith("file://")) {
                        imageStorageManager.deleteImage(url.substringAfter("file://"))
                    }
                }
                
                // Eliminar imágenes adicionales
                archaeologicalObject.additionalImages.forEach { url ->
                    if (url.startsWith("file://")) {
                        imageStorageManager.deleteImage(url.substringAfter("file://"))
                    }
                }

                deleteObjectUseCase(archaeologicalObject)
                _uiState.value = UiState.Success
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "Error desconocido")
            }
        }
    }

    fun getObjectById(id: Long) {
        viewModelScope.launch {
            try {
                val archaeologicalObject = getObjectByIdUseCase(id)
                _uiState.value = UiState.ObjectLoaded(archaeologicalObject)
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "Error al cargar el objeto")
            }
        }
    }

    fun getObjectsBySiteId(siteId: Long) {
        viewModelScope.launch {
            try {
                getObjectsBySiteIdUseCase(siteId)
                    .collect { objects ->
                        _uiState.value = UiState.ObjectsLoaded(objects)
                    }
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "Error al cargar los objetos")
            }
        }
    }

    fun getAllObjects() {
        viewModelScope.launch {
            try {
                getAllObjectsUseCase()
                    .collect { objects ->
                        _uiState.value = UiState.ObjectsLoaded(objects)
                    }
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "Error al cargar los objetos")
            }
        }
    }

    fun searchObjectsByName(name: String) {
        viewModelScope.launch {
            try {
                searchObjectsByNameUseCase(name)
                    .collect { objects ->
                        _uiState.value = UiState.ObjectsLoaded(objects)
                    }
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "Error al buscar objetos")
            }
        }
    }

    fun filterObjectsByType(type: String) {
        viewModelScope.launch {
            try {
                filterObjectsByTypeUseCase(type)
                    .collect { objects ->
                        _uiState.value = UiState.ObjectsLoaded(objects)
                    }
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "Error al filtrar objetos")
            }
        }
    }

    fun filterObjectsByPeriod(period: String) {
        viewModelScope.launch {
            try {
                filterObjectsByPeriodUseCase(period)
                    .collect { objects ->
                        _uiState.value = UiState.ObjectsLoaded(objects)
                    }
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "Error al filtrar objetos")
            }
        }
    }

    fun filterObjectsByLocation(location: String) {
        viewModelScope.launch {
            try {
                filterObjectsByLocationUseCase(location)
                    .collect { objects ->
                        _uiState.value = UiState.ObjectsLoaded(objects)
                    }
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "Error al filtrar objetos")
            }
        }
    }

    sealed class UiState {
        object Initial : UiState()
        object Success : UiState()
        data class Error(val message: String) : UiState()
        data class ObjectLoaded(val archaeologicalObject: ArchaeologicalObject?) : UiState()
        data class ObjectsLoaded(val objects: List<ArchaeologicalObject>) : UiState()
        object Loading : UiState()
    }
} 