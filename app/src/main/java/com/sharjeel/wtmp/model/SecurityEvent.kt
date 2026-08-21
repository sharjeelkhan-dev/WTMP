package com.sharjeel.wtmp.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.UUID

enum class SecurityEventType(val title: String) {
    DEVICE_UNLOCKED("Device Unlocked"),
    FAILED_UNLOCK("Failed Unlock Attempt"),
    UNEXPECTED_UNLOCK("Unexpected Unlock"),
    FAILED_ATTEMPT("Failed Attempt"),
    APP_OPENED("App Opened"),
    POWER_CONNECTED("Power Connected")
}

enum class EventSeverity {
    LOW, MEDIUM, HIGH
}

data class AppUsageInfo(
    val packageName: String,
    val appName: String,
    val iconUri: String? = null,
    val launchedTimestamp: Long
)

@Entity(tableName = "security_events")
@TypeConverters(AppUsageConverter::class)
data class SecurityEvent(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val type: SecurityEventType,
    val timestamp: Long,
    val severity: EventSeverity = EventSeverity.LOW,
    val sessionDuration: String = "0m",
    val deviceState: String = "Locked",
    val evidencePath: String? = null,
    val accessedApps: List<AppUsageInfo> = emptyList()
)

class AppUsageConverter {
    private val gson = Gson()

    @TypeConverter
    fun fromAppUsageList(value: List<AppUsageInfo>?): String {
        return gson.toJson(value ?: emptyList<AppUsageInfo>())
    }

    @TypeConverter
    fun toAppUsageList(value: String?): List<AppUsageInfo> {
        if (value.isNullOrEmpty()) return emptyList()
        val listType = object : TypeToken<List<AppUsageInfo>>() {}.type
        return gson.fromJson(value, listType)
    }
}