package com.sharjeel.wtmp.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.sharjeel.wtmp.model.AppUsageInfo

@Entity(tableName = "security_events")
data class SecurityEventEntity(
    @PrimaryKey val id: String,
    val type: String,
    val timestamp: Long,
    val severity: String,
    val sessionDuration: String,
    val deviceState: String,
    val evidencePath: String?,
    val accessedApps: List<AppUsageInfo>
)
