package com.lorenaortiz.arqueodata.di

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.lorenaortiz.arqueodata.auth.AuthService
import com.lorenaortiz.arqueodata.data.local.dao.ArchaeologicalObjectDao
import com.lorenaortiz.arqueodata.data.local.dao.ArchaeologicalSiteDao
import com.lorenaortiz.arqueodata.data.local.dao.UserDao
import com.lorenaortiz.arqueodata.data.local.dao.TeamMemberDao
import com.lorenaortiz.arqueodata.data.sync.SyncManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object FirebaseModule {
    
    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth {
        return Firebase.auth
    }

    @Provides
    @Singleton
    fun provideFirestore(): FirebaseFirestore {
        return Firebase.firestore
    }

    @Provides
    @Singleton
    fun provideAuthService(firestore: FirebaseFirestore): AuthService {
        return AuthService(firestore)
    }

    @Provides
    @Singleton
    fun provideSyncManager(
        userDao: UserDao,
        siteDao: ArchaeologicalSiteDao,
        objectDao: ArchaeologicalObjectDao,
        teamMemberDao: TeamMemberDao,
        firestore: FirebaseFirestore,
        authService: AuthService
    ): SyncManager {
        return SyncManager(userDao, siteDao, objectDao, teamMemberDao, firestore, authService)
    }
} 