package com.sharjeel.wtmp.data.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface SecurityEventDao {
    @Upsert
    suspend fun upsertEvent(event: SecurityEventEntity)

    @Delete
    suspend fun deleteEvent(event: SecurityEventEntity)

    @Query("SELECT * FROM security_events ORDER BY timestamp DESC")
    fun getAllEvents(): Flow<List<SecurityEventEntity>>

    @Query("SELECT * FROM security_events WHERE id = :id")
    suspend fun getEventById(id: String): SecurityEventEntity?

    @Query("DELETE FROM security_events WHERE timestamp < :timestamp")
    suspend fun deleteOlderThan(timestamp: Long)
}
