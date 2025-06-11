package com.lorenaortiz.arqueodata.domain.usecase

import com.lorenaortiz.arqueodata.domain.model.ArchaeologicalObject
import com.lorenaortiz.arqueodata.domain.repository.ArchaeologicalObjectRepository
import javax.inject.Inject

/**
 * Caso de uso para insertar un nuevo objeto arqueológico.
 * Devolvemos el ID del objeto recién creado para su posterior referencia.
 */
class InsertObjectUseCase @Inject constructor(
    private val repository: ArchaeologicalObjectRepository
) {
    suspend operator fun invoke(archaeologicalObject: ArchaeologicalObject): Long {
        return repository.insertObject(archaeologicalObject)
    }
} 