package com.lorenaortiz.arqueodata.di

import android.content.Context
import com.google.firebase.firestore.FirebaseFirestore
import com.lorenaortiz.arqueodata.auth.AuthService
import com.lorenaortiz.arqueodata.data.local.dao.ArchaeologicalSiteDao
import com.lorenaortiz.arqueodata.data.repository.ArchaeologicalSiteRepositoryImpl
import com.lorenaortiz.arqueodata.data.sync.SyncManager
import com.lorenaortiz.arqueodata.domain.repository.ArchaeologicalSiteRepository
import com.lorenaortiz.arqueodata.utils.NetworkMonitor
import com.lorenaortiz.arqueodata.utils.SessionManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    
    @Provides
    @Singleton
    fun provideArchaeologicalSiteRepository(
        dao: ArchaeologicalSiteDao,
        firestore: FirebaseFirestore,
        syncManager: SyncManager,
        networkMonitor: NetworkMonitor
    ): ArchaeologicalSiteRepository {
        return ArchaeologicalSiteRepositoryImpl(dao, firestore, syncManager, networkMonitor)
    }

    @Provides
    @Singleton
    fun provideSessionManager(
        @ApplicationContext context: Context,
        authService: AuthService
    ): SessionManager {
        return SessionManager(context, authService)
    }
} 