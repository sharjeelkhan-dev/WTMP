package com.sharjeel.wtmp.domain.repository

import com.sharjeel.wtmp.model.SecurityEvent
import kotlinx.coroutines.flow.Flow

/**
 * Domain Repository interface defining contract for Security Events management
 * and User Preference operations.
 */
interface SecurityRepository {

    // =================================================================
    // 1. SECURITY EVENT OPERATIONS
    // =================================================================

    fun getAllEvents(): Flow<List<SecurityEvent>>

    suspend fun getEventById(id: String): SecurityEvent?

    suspend fun saveEvent(event: SecurityEvent)

    suspend fun deleteEvent(event: SecurityEvent)

    suspend fun deleteOlderThan(timestamp: Long)

    // =================================================================
    // 2. PREFERENCE READ STREAMS (FLOWS)
    // =================================================================

    val isFirstTime: Flow<Boolean>
    val themeMode: Flow<String>
    val isBiometricEnabled: Flow<Boolean>
    val detectionSensitivity: Flow<Float>
    val isProtectionActive: Flow<Boolean>
    val autoDeletePeriod: Flow<Int>
    val isAlarmEnabled: Flow<Boolean>
    val isVibrationEnabled: Flow<Boolean>
    val isAntiTheftEnabled: Flow<Boolean>

    // =================================================================
    // 3. PREFERENCE WRITE OPERATIONS (SETTERS)
    // =================================================================

    suspend fun setFirstTime(isFirstTime: Boolean)

    suspend fun setThemeMode(mode: String)

    suspend fun setBiometricEnabled(enabled: Boolean)

    suspend fun setDetectionSensitivity(sensitivity: Float)

    suspend fun setProtectionActive(active: Boolean)

    suspend fun setAutoDeletePeriod(days: Int)

    suspend fun setAlarmEnabled(enabled: Boolean)

    suspend fun setVibrationEnabled(enabled: Boolean)

    suspend fun setAntiTheftEnabled(enabled: Boolean)
}