package com.lorenaortiz.arqueodata.domain.usecase

import com.lorenaortiz.arqueodata.domain.model.ArchaeologicalObject
import com.lorenaortiz.arqueodata.domain.repository.ArchaeologicalObjectRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class FilterObjectsByPeriodUseCase @Inject constructor(
    private val repository: ArchaeologicalObjectRepository
) {
    operator fun invoke(period: String): Flow<List<ArchaeologicalObject>> {
        return repository.filterObjectsByPeriod(period)
    }
} 