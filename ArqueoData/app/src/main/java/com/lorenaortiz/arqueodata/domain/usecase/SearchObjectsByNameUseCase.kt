package com.lorenaortiz.arqueodata.domain.usecase

import com.lorenaortiz.arqueodata.domain.model.ArchaeologicalObject
import com.lorenaortiz.arqueodata.domain.repository.ArchaeologicalObjectRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Caso de uso para buscar objetos arqueológicos por nombre.
 * Proporcionamos un flujo de resultados que se actualiza en tiempo real.
 */
class SearchObjectsByNameUseCase @Inject constructor(
    private val repository: ArchaeologicalObjectRepository
) {
    operator fun invoke(name: String): Flow<List<ArchaeologicalObject>> {
        return repository.searchObjectsByName(name)
    }
} 