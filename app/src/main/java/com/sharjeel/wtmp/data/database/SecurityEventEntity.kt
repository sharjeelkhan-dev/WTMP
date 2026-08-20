package com.sharjeel.wtmp.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "security_events")
data class SecurityEventEntity(
    @PrimaryKey val id: String,
    val type: String,
    val timestamp: Long,
    val severity: String,
    val sessionDuration: String,
    val deviceState: String,
    val evidencePath: String?
)
