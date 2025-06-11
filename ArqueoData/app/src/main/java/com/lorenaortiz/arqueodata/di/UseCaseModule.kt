package com.lorenaortiz.arqueodata.di

import com.lorenaortiz.arqueodata.domain.repository.ArchaeologicalObjectRepository
import com.lorenaortiz.arqueodata.domain.repository.TeamMemberRepository
import com.lorenaortiz.arqueodata.domain.usecase.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object UseCaseModule {
    @Provides
    @Singleton
    fun provideGetAllObjectsUseCase(
        repository: ArchaeologicalObjectRepository
    ): GetAllObjectsUseCase {
        return GetAllObjectsUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideGetObjectByIdUseCase(
        repository: ArchaeologicalObjectRepository
    ): GetObjectByIdUseCase {
        return GetObjectByIdUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideGetObjectsBySiteIdUseCase(
        repository: ArchaeologicalObjectRepository
    ): GetObjectsBySiteIdUseCase {
        return GetObjectsBySiteIdUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideInsertObjectUseCase(
        repository: ArchaeologicalObjectRepository
    ): InsertObjectUseCase {
        return InsertObjectUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideUpdateObjectUseCase(
        repository: ArchaeologicalObjectRepository
    ): UpdateObjectUseCase {
        return UpdateObjectUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideDeleteObjectUseCase(
        repository: ArchaeologicalObjectRepository
    ): DeleteObjectUseCase {
        return DeleteObjectUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideSearchObjectsByNameUseCase(
        repository: ArchaeologicalObjectRepository
    ): SearchObjectsByNameUseCase {
        return SearchObjectsByNameUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideFilterObjectsByTypeUseCase(
        repository: ArchaeologicalObjectRepository
    ): FilterObjectsByTypeUseCase {
        return FilterObjectsByTypeUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideFilterObjectsByPeriodUseCase(
        repository: ArchaeologicalObjectRepository
    ): FilterObjectsByPeriodUseCase {
        return FilterObjectsByPeriodUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideFilterObjectsByLocationUseCase(
        repository: ArchaeologicalObjectRepository
    ): FilterObjectsByLocationUseCase {
        return FilterObjectsByLocationUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideGetTeamMembersUseCase(
        repository: TeamMemberRepository
    ): GetTeamMembersUseCase {
        return GetTeamMembersUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideInsertTeamMemberUseCase(
        repository: TeamMemberRepository
    ): InsertTeamMemberUseCase {
        return InsertTeamMemberUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideUpdateTeamMemberUseCase(
        repository: TeamMemberRepository
    ): UpdateTeamMemberUseCase {
        return UpdateTeamMemberUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideDeleteTeamMemberUseCase(
        repository: TeamMemberRepository
    ): DeleteTeamMemberUseCase {
        return DeleteTeamMemberUseCase(repository)
    }
} 