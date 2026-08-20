package com.sharjeel.wtmp.domain.repository

import com.sharjeel.wtmp.model.SecurityEvent
import kotlinx.coroutines.flow.Flow

interface SecurityRepository {
    // Events
    fun getAllEvents(): Flow<List<SecurityEvent>>
    suspend fun getEventById(id: String): SecurityEvent?
    suspend fun saveEvent(event: SecurityEvent)
    suspend fun deleteEvent(event: SecurityEvent)
    suspend fun deleteOlderThan(timestamp: Long)

    // Preferences
    val isFirstTime: Flow<Boolean>
    val themeMode: Flow<String>
    val isBiometricEnabled: Flow<Boolean>
    val detectionSensitivity: Flow<Float>
    val isProtectionActive: Flow<Boolean>
    val autoDeletePeriod: Flow<Int>
    val isAlarmEnabled: Flow<Boolean>
    val isVibrationEnabled: Flow<Boolean>

    suspend fun setFirstTime(isFirstTime: Boolean)
    suspend fun setThemeMode(mode: String)
    suspend fun setBiometricEnabled(enabled: Boolean)
    suspend fun setDetectionSensitivity(sensitivity: Float)
    suspend fun setProtectionActive(active: Boolean)
    suspend fun setAutoDeletePeriod(days: Int)
    suspend fun setAlarmEnabled(enabled: Boolean)
    suspend fun setVibrationEnabled(enabled: Boolean)
}
