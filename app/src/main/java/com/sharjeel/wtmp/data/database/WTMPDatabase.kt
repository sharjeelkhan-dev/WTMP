package com.sharjeel.wtmp.data.database

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [SecurityEventEntity::class],
    version = 1,
    exportSchema = false
)
abstract class WTMPDatabase : RoomDatabase() {
    abstract val dao: SecurityEventDao
}
