package com.lorenaortiz.arqueodata.di

import android.content.Context
import androidx.room.Room
import com.lorenaortiz.arqueodata.data.local.AppDatabase
import com.lorenaortiz.arqueodata.data.local.dao.AdditionalImageDao
import com.lorenaortiz.arqueodata.data.local.dao.ArchaeologicalObjectDao
import com.lorenaortiz.arqueodata.data.local.dao.ArchaeologicalSiteDao
import com.lorenaortiz.arqueodata.data.local.dao.TeamMemberDao
import com.lorenaortiz.arqueodata.data.local.dao.UserDao
import com.lorenaortiz.arqueodata.utils.ImageStorageManager
import com.lorenaortiz.arqueodata.utils.NetworkMonitor
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "arqueodata.db"
        )
        .addMigrations(
            AppDatabase.MIGRATION_1_2,
            AppDatabase.MIGRATION_2_3,
            AppDatabase.MIGRATION_3_4,
            AppDatabase.MIGRATION_4_5,
            AppDatabase.MIGRATION_5_6,
            AppDatabase.MIGRATION_6_7,
            AppDatabase.MIGRATION_7_8,
            AppDatabase.MIGRATION_8_9,
            AppDatabase.MIGRATION_9_10,
            AppDatabase.MIGRATION_10_11
        )
        .fallbackToDestructiveMigration()
        .fallbackToDestructiveMigrationOnDowngrade()
        .allowMainThreadQueries()
        .build()
    }

    @Provides
    @Singleton
    fun provideArchaeologicalObjectDao(database: AppDatabase): ArchaeologicalObjectDao {
        return database.archaeologicalObjectDao()
    }

    @Provides
    @Singleton
    fun provideAdditionalImageDao(database: AppDatabase): AdditionalImageDao {
        return database.additionalImageDao()
    }

    @Provides
    @Singleton
    fun provideArchaeologicalSiteDao(database: AppDatabase): ArchaeologicalSiteDao {
        return database.archaeologicalSiteDao()
    }

    @Provides
    @Singleton
    fun provideTeamMemberDao(database: AppDatabase): TeamMemberDao {
        return database.teamMemberDao()
    }

    @Provides
    @Singleton
    fun provideUserDao(database: AppDatabase): UserDao {
        return database.userDao()
    }

    @Provides
    @Singleton
    fun provideImageStorageManager(@ApplicationContext context: Context): ImageStorageManager {
        return ImageStorageManager(context)
    }

    @Provides
    @Singleton
    fun provideNetworkMonitor(@ApplicationContext context: Context): NetworkMonitor {
        return NetworkMonitor(context)
    }
} 