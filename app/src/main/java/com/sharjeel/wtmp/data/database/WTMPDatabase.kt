package com.sharjeel.wtmp.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.sharjeel.wtmp.model.AppUsageConverter

/**
 * Main Room Database configuration class for WTMP application.
 */
@Database(
    entities = [SecurityEventEntity::class],
    version = 2,
    exportSchema = false
)
@TypeConverters(AppUsageConverter::class)
abstract class WTMPDatabase : RoomDatabase() {

    // =================================================================
    // DATA ACCESS OBJECTS (DAOs)
    // =================================================================

    abstract val dao: SecurityEventDao
}