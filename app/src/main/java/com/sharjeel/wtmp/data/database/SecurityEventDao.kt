package com.sharjeel.wtmp.data.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object (DAO) for managing security event entities in Room DB.
 */
@Dao
interface SecurityEventDao {

    // =================================================================
    // 1. INSERT / UPDATE OPERATIONS
    // =================================================================

    @Upsert
    suspend fun upsertEvent(event: SecurityEventEntity)

    // =================================================================
    // 2. READ / QUERY OPERATIONS
    // =================================================================

    @Query("SELECT * FROM security_events ORDER BY timestamp DESC")
    fun getAllEvents(): Flow<List<SecurityEventEntity>>

    @Query("SELECT * FROM security_events WHERE id = :id")
    suspend fun getEventById(id: String): SecurityEventEntity?

    // =================================================================
    // 3. DELETE OPERATIONS
    // =================================================================

    @Delete
    suspend fun deleteEvent(event: SecurityEventEntity)

    @Query("DELETE FROM security_events WHERE timestamp < :timestamp")
    suspend fun deleteOlderThan(timestamp: Long)
}