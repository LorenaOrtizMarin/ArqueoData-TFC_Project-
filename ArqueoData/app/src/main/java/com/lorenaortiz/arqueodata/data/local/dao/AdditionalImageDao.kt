package com.lorenaortiz.arqueodata.data.local.dao

import androidx.room.*
import com.lorenaortiz.arqueodata.data.local.entity.AdditionalImageEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO para la gestión de imágenes adicionales en la base de datos local.
 * Proporcionamos métodos para realizar operaciones CRUD y consultas específicas.
 */
@Dao
interface AdditionalImageDao {
    /**
     * Obtenemos todas las imágenes adicionales almacenadas.
     */
    @Query("SELECT * FROM additional_images")
    fun getAllImages(): Flow<List<AdditionalImageEntity>>

    /**
     * Recuperamos las imágenes asociadas a un objeto específico.
     */
    @Query("SELECT * FROM additional_images WHERE objectId = :objectId")
    fun getImagesForObject(objectId: Long): Flow<List<AdditionalImageEntity>>

    /**
     * Insertamos una nueva imagen o actualizamos una existente.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertImage(image: AdditionalImageEntity): Long

    /**
     * Insertamos múltiples imágenes de una vez.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertImages(images: List<AdditionalImageEntity>)

    /**
     * Actualizamos una imagen existente.
     */
    @Update
    suspend fun updateImage(image: AdditionalImageEntity)

    /**
     * Eliminamos una imagen específica.
     */
    @Delete
    suspend fun deleteImage(image: AdditionalImageEntity)

    /**
     * Eliminamos todas las imágenes asociadas a un objeto.
     */
    @Query("DELETE FROM additional_images WHERE objectId = :objectId")
    suspend fun deleteImagesForObject(objectId: Long)
}
