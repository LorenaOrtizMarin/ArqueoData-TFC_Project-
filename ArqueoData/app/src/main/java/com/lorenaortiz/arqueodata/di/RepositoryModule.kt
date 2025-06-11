package com.lorenaortiz.arqueodata.di

import com.lorenaortiz.arqueodata.data.repository.ArchaeologicalObjectRepositoryImpl
import com.lorenaortiz.arqueodata.data.repository.TeamMemberRepositoryImpl
import com.lorenaortiz.arqueodata.data.repository.UserRepositoryImpl
import com.lorenaortiz.arqueodata.domain.repository.ArchaeologicalObjectRepository
import com.lorenaortiz.arqueodata.domain.repository.TeamMemberRepository
import com.lorenaortiz.arqueodata.domain.repository.UserRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Módulo de Dagger Hilt que proporciona las implementaciones de los repositorios.
 * Utilizamos inyección de dependencias para mantener un código limpio y testeable.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    /**
     * Proporcionamos la implementación del repositorio de objetos arqueológicos.
     * Utilizamos un singleton para mantener una única instancia en toda la aplicación.
     */
    @Binds
    @Singleton
    abstract fun bindArchaeologicalObjectRepository(
        archaeologicalObjectRepositoryImpl: ArchaeologicalObjectRepositoryImpl
    ): ArchaeologicalObjectRepository

    /**
     * Proporcionamos la implementación del repositorio de miembros del equipo.
     * Utilizamos un singleton para mantener una única instancia en toda la aplicación.
     */
    @Binds
    @Singleton
    abstract fun bindTeamMemberRepository(
        teamMemberRepositoryImpl: TeamMemberRepositoryImpl
    ): TeamMemberRepository

    /**
     * Proporcionamos la implementación del repositorio de usuarios.
     * Utilizamos un singleton para mantener una única instancia en toda la aplicación.
     */
    @Binds
    @Singleton
    abstract fun bindUserRepository(
        userRepositoryImpl: UserRepositoryImpl
    ): UserRepository
}
