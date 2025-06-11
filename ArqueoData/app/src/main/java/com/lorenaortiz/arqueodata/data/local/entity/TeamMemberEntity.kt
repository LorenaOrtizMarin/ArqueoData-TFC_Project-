package com.lorenaortiz.arqueodata.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.lorenaortiz.arqueodata.domain.model.TeamMember

/**
 * Entidad que representa un miembro del equipo en la base de datos local.
 * Gestionamos la relación entre usuarios y sitios arqueológicos.
 */
@Entity(
    tableName = "team_members",
    foreignKeys = [
        ForeignKey(
            entity = ArchaeologicalSiteEntity::class,
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
data class TeamMemberEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: Long,
    val siteId: Long,
    val role: String,
    val lastModified: Long = System.currentTimeMillis()
) {
    /**
     * Convertimos la entidad a su modelo de dominio correspondiente.
     */
    fun toDomainModel(): TeamMember {
        return TeamMember(
            id = id,
            userId = userId,
            siteId = siteId,
            role = role
        )
    }

    companion object {
        /**
         * Creamos una entidad a partir de un modelo de dominio.
         */
        fun fromDomainModel(teamMember: TeamMember): TeamMemberEntity {
            return TeamMemberEntity(
                id = teamMember.id,
                userId = teamMember.userId,
                siteId = teamMember.siteId,
                role = teamMember.role
            )
        }
    }
} 