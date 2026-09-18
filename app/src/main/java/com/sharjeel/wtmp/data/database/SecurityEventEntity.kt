package com.sharjeel.wtmp.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.sharjeel.wtmp.model.AppUsageInfo

/**
 * Room Entity representing a security event log in the database.
 */
@Entity(tableName = "security_events")
data class SecurityEventEntity(

    // =================================================================
    // 1. PRIMARY KEY & CORE EVENT METADATA
    // =================================================================

    @PrimaryKey
    val id: String,

    val type: String,
    val timestamp: Long,
    val severity: String,

    // =================================================================
    // 2. SESSION & DEVICE STATE DATA
    // =================================================================

    val sessionDuration: String,
    val deviceState: String,

    // =================================================================
    // 3. MEDIA EVIDENCE & APP USAGE (OPTIONAL / COMPLEX DATA)
    // =================================================================

    val evidencePath: String?,
    val accessedApps: List<AppUsageInfo>
)