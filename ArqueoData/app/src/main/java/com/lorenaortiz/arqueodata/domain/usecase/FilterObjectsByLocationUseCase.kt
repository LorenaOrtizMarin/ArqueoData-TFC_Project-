package com.lorenaortiz.arqueodata.domain.usecase

import com.lorenaortiz.arqueodata.domain.model.ArchaeologicalObject
import com.lorenaortiz.arqueodata.domain.repository.ArchaeologicalObjectRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class FilterObjectsByLocationUseCase @Inject constructor(
    private val repository: ArchaeologicalObjectRepository
) {
    operator fun invoke(location: String): Flow<List<ArchaeologicalObject>> {
        return repository.filterObjectsByLocation(location)
    }
} 