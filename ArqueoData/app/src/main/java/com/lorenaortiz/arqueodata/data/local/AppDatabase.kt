package com.lorenaortiz.arqueodata.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.lorenaortiz.arqueodata.data.local.dao.ArchaeologicalObjectDao
import com.lorenaortiz.arqueodata.data.local.dao.ArchaeologicalSiteDao
import com.lorenaortiz.arqueodata.data.local.dao.TeamMemberDao
import com.lorenaortiz.arqueodata.data.local.dao.UserDao
import com.lorenaortiz.arqueodata.data.local.entity.ArchaeologicalObjectEntity
import com.lorenaortiz.arqueodata.data.local.entity.ArchaeologicalSiteEntity
import com.lorenaortiz.arqueodata.data.local.entity.TeamMemberEntity
import com.lorenaortiz.arqueodata.data.local.entity.UserEntity
import com.lorenaortiz.arqueodata.data.local.entity.AdditionalImageEntity
import com.lorenaortiz.arqueodata.data.local.dao.AdditionalImageDao
import com.lorenaortiz.arqueodata.domain.model.UserType

/**
 * Base de datos principal de la aplicación.
 * Gestionamos el esquema de la base de datos y proporcionamos acceso a los DAOs.
 */
@Database(
    entities = [
        ArchaeologicalSiteEntity::class,
        ArchaeologicalObjectEntity::class,
        UserEntity::class,
        TeamMemberEntity::class,
        AdditionalImageEntity::class
    ],
    version = 11,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    /**
     * Proporcionamos acceso al DAO de sitios arqueológicos.
     */
    abstract fun archaeologicalSiteDao(): ArchaeologicalSiteDao

    /**
     * Proporcionamos acceso al DAO de objetos arqueológicos.
     */
    abstract fun archaeologicalObjectDao(): ArchaeologicalObjectDao

    /**
     * Proporcionamos acceso al DAO de usuarios.
     */
    abstract fun userDao(): UserDao

    /**
     * Proporcionamos acceso al DAO de miembros del equipo.
     */
    abstract fun teamMemberDao(): TeamMemberDao

    /**
     * Proporcionamos acceso al DAO de imágenes adicionales.
     */
    abstract fun additionalImageDao(): AdditionalImageDao

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Crear una nueva tabla temporal con la columna userId
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS archaeological_sites_new (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        name TEXT NOT NULL,
                        location TEXT NOT NULL,
                        description TEXT NOT NULL,
                        latitude REAL NOT NULL DEFAULT 0.0,
                        longitude REAL NOT NULL DEFAULT 0.0,
                        period TEXT NOT NULL,
                        type TEXT NOT NULL,
                        status TEXT NOT NULL DEFAULT 'ACTIVE',
                        imageUrl TEXT,
                        userId INTEGER NOT NULL DEFAULT 0
                    )
                """.trimIndent())

                // Copiar los datos de la tabla antigua a la nueva
                database.execSQL("""
                    INSERT INTO archaeological_sites_new 
                    (id, name, location, description, latitude, longitude, period, type, status, imageUrl, userId)
                    SELECT id, name, location, description, latitude, longitude, period, type, status, imageUrl, 0
                    FROM archaeological_sites
                """.trimIndent())

                // Eliminar la tabla antigua
                database.execSQL("DROP TABLE archaeological_sites")

                // Renombrar la nueva tabla
                database.execSQL("ALTER TABLE archaeological_sites_new RENAME TO archaeological_sites")

                database.execSQL(
                    "ALTER TABLE archaeological_sites ADD COLUMN lastModified INTEGER NOT NULL DEFAULT ${System.currentTimeMillis()}"
                )
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Crear la tabla de miembros del equipo
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS team_members (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        name TEXT NOT NULL,
                        role TEXT NOT NULL,
                        email TEXT NOT NULL,
                        siteId INTEGER NOT NULL,
                        FOREIGN KEY (siteId) REFERENCES archaeological_sites(id) ON DELETE CASCADE
                    )
                """.trimIndent())

                // Crear índice para siteId
                database.execSQL("CREATE INDEX IF NOT EXISTS index_team_members_siteId ON team_members(siteId)")
            }
        }

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Crear la tabla de imágenes adicionales
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS additional_images (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        objectId INTEGER NOT NULL,
                        imageUrl TEXT NOT NULL,
                        FOREIGN KEY (objectId) REFERENCES archaeological_objects(id) ON DELETE CASCADE
                    )
                """.trimIndent())

                // Crear índice para objectId
                database.execSQL("CREATE INDEX IF NOT EXISTS index_additional_images_objectId ON additional_images(objectId)")
            }
        }

        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Crear la tabla de objetos arqueológicos
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS archaeological_objects (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        name TEXT NOT NULL,
                        description TEXT NOT NULL,
                        type TEXT NOT NULL,
                        period TEXT NOT NULL,
                        material TEXT NOT NULL,
                        dimensions TEXT NOT NULL,
                        condition TEXT NOT NULL,
                        location TEXT NOT NULL,
                        notes TEXT,
                        imageUrl TEXT,
                        creatorId TEXT,
                        creatorName TEXT,
                        creatorPhotoUrl TEXT,
                        projectName TEXT,
                        siteId INTEGER NOT NULL,
                        FOREIGN KEY (siteId) REFERENCES archaeological_sites(id) ON DELETE CASCADE
                    )
                """.trimIndent())

                // Crear índice para siteId
                database.execSQL("CREATE INDEX IF NOT EXISTS index_archaeological_objects_siteId ON archaeological_objects(siteId)")

                // Crear la tabla de usuarios si no existe
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS users (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        nombre TEXT NOT NULL,
                        usuario TEXT NOT NULL,
                        email TEXT NOT NULL,
                        password TEXT NOT NULL
                    )
                """.trimIndent())
            }
        }

        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Agregar columna userType a la tabla users
                database.execSQL("ALTER TABLE users ADD COLUMN userType TEXT NOT NULL DEFAULT '${UserType.MIEMBRO.name}'")
            }
        }

        val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Crear tabla temporal con la nueva estructura
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS team_members_temp (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        userId INTEGER NOT NULL,
                        siteId INTEGER NOT NULL,
                        role TEXT NOT NULL,
                        FOREIGN KEY (siteId) REFERENCES archaeological_sites(id) ON DELETE CASCADE,
                        FOREIGN KEY (userId) REFERENCES users(id) ON DELETE CASCADE
                    )
                """.trimIndent())

                // Crear índices para la tabla temporal
                database.execSQL("CREATE INDEX IF NOT EXISTS index_team_members_temp_siteId ON team_members_temp(siteId)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_team_members_temp_userId ON team_members_temp(userId)")

                // Eliminar la tabla antigua
                database.execSQL("DROP TABLE IF EXISTS team_members")

                // Renombrar la tabla temporal
                database.execSQL("ALTER TABLE team_members_temp RENAME TO team_members")
            }
        }

        val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Agregar columna photoUrl a la tabla users
                database.execSQL("ALTER TABLE users ADD COLUMN photoUrl TEXT")
            }
        }

        val MIGRATION_8_9 = object : Migration(8, 9) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Agregar columna lastModified a la tabla archaeological_objects
                database.execSQL("ALTER TABLE archaeological_objects ADD COLUMN lastModified INTEGER NOT NULL DEFAULT ${System.currentTimeMillis()}")
            }
        }

        val MIGRATION_9_10 = object : Migration(9, 10) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Agregar columna objectId a la tabla archaeological_objects si no existe
                database.execSQL("ALTER TABLE archaeological_objects ADD COLUMN objectId TEXT NOT NULL DEFAULT ''")
            }
        }

        val MIGRATION_10_11 = object : Migration(10, 11) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Agregar columna lastModified a la tabla team_members
                database.execSQL("ALTER TABLE team_members ADD COLUMN lastModified INTEGER NOT NULL DEFAULT ${System.currentTimeMillis()}")
            }
        }
    }
} 