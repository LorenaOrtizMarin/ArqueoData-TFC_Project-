package com.lorenaortiz.arqueodata.domain.usecase

import com.lorenaortiz.arqueodata.domain.model.ArchaeologicalObject
import com.lorenaortiz.arqueodata.domain.repository.ArchaeologicalObjectRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetObjectsBySiteIdUseCase @Inject constructor(
    private val repository: ArchaeologicalObjectRepository
) {
    operator fun invoke(siteId: Long): Flow<List<ArchaeologicalObject>> {
        return repository.getObjectsBySiteId(siteId)
    }
} 