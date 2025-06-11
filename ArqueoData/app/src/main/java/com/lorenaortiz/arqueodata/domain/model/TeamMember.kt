package com.lorenaortiz.arqueodata.domain.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.lorenaortiz.arqueodata.data.local.entity.UserEntity

/**
 * Representamos la relación entre un usuario y un sitio arqueológico.
 * Gestionamos los roles y permisos de los miembros del equipo en cada sitio.
 */
@Entity(
    tableName = "team_members",
    foreignKeys = [
        ForeignKey(
            entity = ArchaeologicalSite::class,
            parentColumns = ["id"],
            childColumns = ["siteId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("siteId"), Index("userId")]
)
data class TeamMember(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    // ID del usuario miembro del equipo
    val userId: Long,
    // ID del sitio arqueológico al que pertenece
    val siteId: Long,
    // Rol o función del miembro en el sitio
    val role: String
) 