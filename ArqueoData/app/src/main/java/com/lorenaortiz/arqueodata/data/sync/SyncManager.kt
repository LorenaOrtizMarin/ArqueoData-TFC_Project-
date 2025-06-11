package com.lorenaortiz.arqueodata.data.sync

import com.google.firebase.firestore.FirebaseFirestore
import com.lorenaortiz.arqueodata.auth.AuthService
import com.lorenaortiz.arqueodata.data.local.dao.ArchaeologicalSiteDao
import com.lorenaortiz.arqueodata.data.local.dao.UserDao
import com.lorenaortiz.arqueodata.data.local.dao.ArchaeologicalObjectDao
import com.lorenaortiz.arqueodata.data.local.dao.TeamMemberDao
import com.lorenaortiz.arqueodata.data.local.entity.ArchaeologicalSiteEntity
import com.lorenaortiz.arqueodata.data.local.entity.UserEntity
import com.lorenaortiz.arqueodata.data.local.entity.ArchaeologicalObjectEntity
import com.lorenaortiz.arqueodata.data.local.entity.TeamMemberEntity
import com.lorenaortiz.arqueodata.domain.model.UserType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Gestor de sincronización entre la base de datos local y Firestore.
 * Coordinamos la sincronización de datos entre el almacenamiento local y la nube.
 */
@Singleton
class SyncManager @Inject constructor(
    private val userDao: UserDao,
    private val siteDao: ArchaeologicalSiteDao,
    private val objectDao: ArchaeologicalObjectDao,
    private val teamMemberDao: TeamMemberDao,
    private val firestore: FirebaseFirestore,
    private val authService: AuthService
) {
    suspend fun syncUsers() {
        try {
            println("=== INICIO SINCRONIZACIÓN DE USUARIOS ===")
            
            // Verificar autenticación
            if (!authService.isUserAuthenticated()) {
                println("Error: Usuario no autenticado en Firebase")
                return
            }

            // 1. Obtener usuarios de Firestore
            println("Buscando usuarios en Firestore...")
            val firestoreUsers = firestore.collection("users")
                .get()
                .await()
            
            println("Usuarios encontrados en Firestore: ${firestoreUsers.documents.size}")
            
            // 2. Sincronizar usuarios de Firestore con la base de datos local
            for (document in firestoreUsers.documents) {
                try {
                    val userData = document.data
                    if (userData != null) {
                        println("Procesando usuario de Firestore: ${document.id}")
                        val userId = document.id.toLong()
                        val existingUser = withContext(Dispatchers.IO) {
                            userDao.getUserById(userId)
                        }
                        
                        val user = UserEntity(
                            id = userId,
                            nombre = userData["nombre"] as String,
                            usuario = userData["usuario"] as String,
                            email = userData["email"] as String,
                            password = userData["password"] as String,
                            userType = UserType.valueOf(userData["userType"] as String),
                            photoUrl = userData["photoUrl"] as? String,
                            pendingSync = false
                        )
                        
                        withContext(Dispatchers.IO) {
                            if (existingUser == null) {
                                userDao.insertUser(user)
                                println("Nuevo usuario ${user.id} insertado desde Firestore")
                            } else {
                                userDao.updateUser(user)
                                println("Usuario ${user.id} actualizado desde Firestore")
                            }
                        }
                    }
                } catch (e: Exception) {
                    println("Error sincronizando usuario desde Firestore: ${e.message}")
                    println("Stack trace: ${e.stackTraceToString()}")
                }
            }
            
            // 3. Obtener usuarios locales pendientes de sincronización
            val pendingUsers = withContext(Dispatchers.IO) {
                userDao.getPendingSyncUsers()
            }
            println("Usuarios locales pendientes de sincronización: ${pendingUsers.size}")
            
            // 4. Sincronizar usuarios locales con Firestore
            for (user in pendingUsers) {
                try {
                    println("Intentando sincronizar usuario local: ${user.id}")
                    val userMap = mapOf(
                        "id" to user.id,
                        "nombre" to user.nombre,
                        "usuario" to user.usuario,
                        "email" to user.email,
                        "password" to user.password,
                        "userType" to user.userType.name,
                        "photoUrl" to user.photoUrl
                    )
                    
                    println("Datos del usuario a guardar: $userMap")
                    
                    firestore.collection("users")
                        .document(user.id.toString())
                        .set(userMap)
                        .await()
                    
                    withContext(Dispatchers.IO) {
                        userDao.updateUser(user.copy(pendingSync = false))
                    }
                    
                    println("Usuario ${user.id} sincronizado con Firestore exitosamente")
                } catch (e: Exception) {
                    println("Error sincronizando usuario ${user.id}: ${e.message}")
                    println("Stack trace: ${e.stackTraceToString()}")
                }
            }
            
            println("=== SINCRONIZACIÓN DE USUARIOS COMPLETADA ===")
        } catch (e: Exception) {
            println("Error en sincronización de usuarios: ${e.message}")
            println("Stack trace: ${e.stackTraceToString()}")
        }
    }

    suspend fun syncSites(userId: Long) {
        try {
            println("=== INICIO SINCRONIZACIÓN DE SITIOS ===")
            println("Usuario ID: $userId")
            
            // Verificar autenticación
            if (!authService.isUserAuthenticated()) {
                println("Error: Usuario no autenticado en Firebase")
                return
            }

            // 1. Obtener yacimientos locales
            val localSites = withContext(Dispatchers.IO) {
                siteDao.getAllSites(userId).first()
            }
            println("Yacimientos locales encontrados: ${localSites.size}")
            
            // 2. Sincronizar yacimientos locales con Firestore
            for (site in localSites) {
                try {
                    println("Intentando sincronizar yacimiento local: ${site.id}")
                    val currentTime = System.currentTimeMillis()
                    val siteMap = mapOf(
                        "id" to site.id,
                        "name" to site.name,
                        "location" to site.location,
                        "description" to site.description,
                        "latitude" to site.latitude,
                        "longitude" to site.longitude,
                        "period" to site.period,
                        "type" to site.type,
                        "status" to site.status,
                        "imageUrl" to site.imageUrl,
                        "userId" to site.userId,
                        "lastModified" to currentTime
                    )
                    
                    println("Datos del yacimiento a guardar: $siteMap")
                    
                    firestore.collection("sites")
                        .document(site.id.toString())
                        .set(siteMap)
                        .await()
                    
                    // Actualizar lastModified en la base de datos local
                    withContext(Dispatchers.IO) {
                        siteDao.updateSite(site.copy(lastModified = currentTime))
                    }
                    
                    println("Yacimiento ${site.id} sincronizado con Firestore exitosamente")
                } catch (e: Exception) {
                    println("Error sincronizando yacimiento ${site.id}: ${e.message}")
                    println("Stack trace: ${e.stackTraceToString()}")
                }
            }
            
            println("=== SINCRONIZACIÓN DE SITIOS COMPLETADA ===")
        } catch (e: Exception) {
            println("Error en sincronización de sitios: ${e.message}")
            println("Stack trace: ${e.stackTraceToString()}")
        }
    }

    /**
     * Eliminamos un sitio de Firestore.
     */
    suspend fun deleteSiteFromFirestore(siteId: Long) {
        try {
            firestore.collection("sites")
                .document(siteId.toString())
                .delete()
                .await()
            println("Yacimiento $siteId eliminado de Firestore")
        } catch (e: Exception) {
            println("Error eliminando yacimiento $siteId de Firestore: ${e.message}")
        }
    }

    /**
     * Sincronizamos los objetos arqueológicos de un sitio específico.
     */
    suspend fun syncObjects(siteId: Long) {
        try {
            // Verificar autenticación
            if (!authService.isUserAuthenticated()) {
                println("Error: Usuario no autenticado en Firebase")
                return
            }

            println("Iniciando sincronización de objetos para sitio $siteId...")
            
            // 1. Obtener objetos locales
            val localObjects = withContext(Dispatchers.IO) {
                objectDao.getAllObjects().first()
            }
            println("Objetos locales encontrados: ${localObjects.size}")
            
            // 2. Sincronizar objetos locales con Firestore
            for (obj in localObjects) {
                try {
                    val objectMap = mapOf(
                        "id" to obj.id,
                        "name" to obj.name,
                        "description" to obj.description,
                        "type" to obj.type,
                        "period" to obj.period,
                        "material" to obj.material,
                        "dimensions" to obj.dimensions,
                        "condition" to obj.condition,
                        "location" to obj.location,
                        "notes" to obj.notes,
                        "imageUrl" to obj.imageUrl,
                        "creatorId" to obj.creatorId,
                        "creatorName" to obj.creatorName,
                        "creatorPhotoUrl" to obj.creatorPhotoUrl,
                        "projectName" to obj.projectName,
                        "siteId" to obj.siteId,
                        "userId" to authService.getCurrentFirebaseUserId(),
                        "lastModified" to System.currentTimeMillis()
                    )
                    
                    firestore.collection("objects")
                        .document(obj.id.toString())
                        .set(objectMap)
                        .await()
                    
                    println("Objeto ${obj.id} sincronizado con Firestore")
                } catch (e: Exception) {
                    println("Error sincronizando objeto ${obj.id}: ${e.message}")
                }
            }
            
            // 3. Obtener objetos de Firestore
            val firestoreObjects = firestore.collection("objects")
                .whereEqualTo("userId", authService.getCurrentFirebaseUserId())
                .get()
                .await()
            
            println("Objetos encontrados en Firestore: ${firestoreObjects.documents.size}")
            
            // 4. Sincronizar objetos de Firestore con la base de datos local
            for (document in firestoreObjects.documents) {
                try {
                    val objectData = document.data
                    if (objectData != null) {
                        val objectId = document.id.toLong()
                        val existingObject = withContext(Dispatchers.IO) {
                            objectDao.getObjectById(objectId)
                        }
                        
                        // Solo actualizar si el objeto no existe localmente
                        if (existingObject == null) {
                            val obj = ArchaeologicalObjectEntity(
                                id = objectId,
                                objectId = objectData["objectId"] as String,
                                name = objectData["name"] as String,
                                description = objectData["description"] as String,
                                type = objectData["type"] as String,
                                period = objectData["period"] as String,
                                material = objectData["material"] as String,
                                dimensions = objectData["dimensions"] as String,
                                condition = objectData["condition"] as String,
                                location = objectData["location"] as String,
                                notes = objectData["notes"] as? String,
                                imageUrl = objectData["imageUrl"] as? String,
                                creatorId = objectData["creatorId"] as? String,
                                creatorName = objectData["creatorName"] as? String,
                                creatorPhotoUrl = objectData["creatorPhotoUrl"] as? String,
                                projectName = objectData["projectName"] as? String,
                                siteId = (objectData["siteId"] as Number).toLong()
                            )
                            
                            withContext(Dispatchers.IO) {
                                objectDao.insertObject(obj)
                                println("Nuevo objeto ${obj.id} insertado desde Firestore")
                            }
                        }
                    }
                } catch (e: Exception) {
                    println("Error sincronizando objeto desde Firestore: ${e.message}")
                }
            }
            
            println("Sincronización de objetos completada")
        } catch (e: Exception) {
            println("Error en sincronización de objetos: ${e.message}")
        }
    }

    /**
     * Sincronizamos los miembros del equipo de un sitio específico.
     */
    suspend fun syncTeamMembers(siteId: Long) {
        try {
            println("Iniciando sincronización de miembros del equipo para sitio $siteId...")
            
            // 1. Obtener miembros locales
            val localMembers = withContext(Dispatchers.IO) {
                teamMemberDao.getTeamMembersBySiteId(siteId).first()
            }
            println("Miembros locales encontrados: ${localMembers.size}")
            
            // 2. Sincronizar miembros locales con Firestore
            for (member in localMembers) {
                try {
                    val memberMap = mapOf(
                        "id" to member.id,
                        "siteId" to member.siteId,
                        "userId" to member.userId,
                        "role" to member.role,
                        "lastModified" to System.currentTimeMillis()
                    )
                    
                    firestore.collection("team_members")
                        .document(member.id.toString())
                        .set(memberMap)
                        .await()
                    
                    println("Miembro ${member.id} sincronizado con Firestore")
                } catch (e: Exception) {
                    println("Error sincronizando miembro ${member.id}: ${e.message}")
                }
            }
            
            // 3. Obtener miembros de Firestore
            val firestoreMembers = firestore.collection("team_members")
                .whereEqualTo("siteId", siteId)
                .get()
                .await()
            
            println("Miembros encontrados en Firestore: ${firestoreMembers.documents.size}")
            
            // 4. Sincronizar miembros de Firestore con la base de datos local
            for (document in firestoreMembers.documents) {
                try {
                    val memberData = document.data
                    if (memberData != null) {
                        val memberId = document.id.toLong()
                        val existingMember = withContext(Dispatchers.IO) {
                            teamMemberDao.getTeamMemberById(memberId)
                        }
                        val lastModified = memberData["lastModified"] as? Long ?: 0L
                        
                        // Solo actualizar si el miembro no existe localmente o si la versión de Firestore es más reciente
                        if (existingMember == null || existingMember.lastModified < lastModified) {
                            val member = TeamMemberEntity(
                                id = memberId,
                                siteId = (memberData["siteId"] as Number).toLong(),
                                userId = (memberData["userId"] as Number).toLong(),
                                role = memberData["role"] as String,
                                lastModified = lastModified
                            )
                            
                            withContext(Dispatchers.IO) {
                                if (existingMember == null) {
                                    teamMemberDao.insertTeamMember(member)
                                    println("Nuevo miembro ${member.id} insertado desde Firestore")
                                } else {
                                    teamMemberDao.updateTeamMember(member)
                                    println("Miembro ${member.id} actualizado desde Firestore")
                                }
                            }
                        }
                    }
                } catch (e: Exception) {
                    println("Error sincronizando miembro desde Firestore: ${e.message}")
                }
            }
            
            println("Sincronización de miembros del equipo completada para sitio $siteId")
        } catch (e: Exception) {
            println("Error en sincronización de miembros del equipo: ${e.message}")
        }
    }

    /**
     * Eliminamos un objeto de Firestore.
     */
    suspend fun deleteObjectFromFirestore(objectId: Long) {
        try {
            firestore.collection("objects")
                .document(objectId.toString())
                .delete()
                .await()
            println("Objeto $objectId eliminado de Firestore")
        } catch (e: Exception) {
            println("Error eliminando objeto $objectId de Firestore: ${e.message}")
        }
    }

    /**
     * Eliminamos un miembro del equipo de Firestore.
     */
    suspend fun deleteTeamMemberFromFirestore(memberId: Long) {
        try {
            firestore.collection("team_members")
                .document(memberId.toString())
                .delete()
                .await()
            println("Miembro $memberId eliminado de Firestore")
        } catch (e: Exception) {
            println("Error eliminando miembro $memberId de Firestore: ${e.message}")
        }
    }
} 
