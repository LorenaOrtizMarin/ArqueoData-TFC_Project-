package com.lorenaortiz.arqueodata.domain.usecase

import com.lorenaortiz.arqueodata.domain.model.ArchaeologicalObject
import com.lorenaortiz.arqueodata.domain.repository.ArchaeologicalObjectRepository
import javax.inject.Inject

class GetObjectByIdUseCase @Inject constructor(
    private val repository: ArchaeologicalObjectRepository
) {
    suspend operator fun invoke(objectId: Long): ArchaeologicalObject? {
        return repository.getObjectById(objectId)
    }
} 