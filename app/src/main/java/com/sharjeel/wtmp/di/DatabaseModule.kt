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

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideWTMPDatabase(
        @ApplicationContext context: Context
    ): WTMPDatabase {
        return Room.databaseBuilder(
            context,
            WTMPDatabase::class.java,
            "wtmp_db"
        ).build()
    }

    @Provides
    @Singleton
    fun provideSecurityEventDao(database: WTMPDatabase): SecurityEventDao {
        return database.dao
    }
}
