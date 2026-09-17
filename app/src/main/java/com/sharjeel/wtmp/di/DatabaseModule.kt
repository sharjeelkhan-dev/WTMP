package com.sharjeel.wtmp.di

import android.content.Context
import androidx.room.Room
import com.sharjeel.wtmp.data.database.SecurityEventDao
import com.sharjeel.wtmp.data.database.WTMPDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt Module responsible for providing Room Database instances and Data Access Objects (DAOs).
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    // =================================================================
    // 1. DATABASE INSTANCE PROVIDER
    // =================================================================

    @Provides
    @Singleton
    fun provideWTMPDatabase(
        @ApplicationContext context: Context
    ): WTMPDatabase {
        return Room.databaseBuilder(
            context,
            WTMPDatabase::class.java,
            "wtmp_db"
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    // =================================================================
    // 2. DAO PROVIDERS
    // =================================================================

    @Provides
    @Singleton
    fun provideSecurityEventDao(database: WTMPDatabase): SecurityEventDao {
        return database.dao
    }
}