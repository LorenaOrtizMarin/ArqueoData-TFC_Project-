package com.lorenaortiz.arqueodata.domain.usecase

import com.lorenaortiz.arqueodata.domain.model.ArchaeologicalObject
import com.lorenaortiz.arqueodata.domain.repository.ArchaeologicalObjectRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Caso de uso para obtener todos los objetos arqueológicos del sistema.
 * Proporcionamos un flujo de datos que se actualiza automáticamente cuando hay cambios.
 */
class GetAllObjectsUseCase @Inject constructor(
    private val repository: ArchaeologicalObjectRepository
) {
    operator fun invoke(): Flow<List<ArchaeologicalObject>> {
        return repository.getAllObjects()
    }
} 