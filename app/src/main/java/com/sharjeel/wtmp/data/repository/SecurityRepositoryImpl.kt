package com.sharjeel.wtmp.data.repository

import com.sharjeel.wtmp.data.database.SecurityEventDao
import com.sharjeel.wtmp.data.database.SecurityEventEntity
import com.sharjeel.wtmp.data.preferences.PreferenceManager
import com.sharjeel.wtmp.domain.repository.SecurityRepository
import com.sharjeel.wtmp.model.SecurityEvent
import com.sharjeel.wtmp.model.SecurityEventType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class SecurityRepositoryImpl @Inject constructor(
    private val dao: SecurityEventDao,
    private val preferenceManager: PreferenceManager
) : SecurityRepository {

    override fun getAllEvents(): Flow<List<SecurityEvent>> {
        return dao.getAllEvents().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getEventById(id: String): SecurityEvent? {
        return dao.getEventById(id)?.toDomain()
    }

    override suspend fun saveEvent(event: SecurityEvent) {
        dao.upsertEvent(event.toEntity())
    }

    override suspend fun deleteEvent(event: SecurityEvent) {
        dao.deleteEvent(event.toEntity())
    }

    override suspend fun deleteOlderThan(timestamp: Long) {
        dao.deleteOlderThan(timestamp)
    }

    // Preferences
    override val isFirstTime: Flow<Boolean> = preferenceManager.isFirstTime
    override val themeMode: Flow<String> = preferenceManager.themeMode
    override val isBiometricEnabled: Flow<Boolean> = preferenceManager.isBiometricEnabled
    override val detectionSensitivity: Flow<Float> = preferenceManager.detectionSensitivity
    override val isProtectionActive: Flow<Boolean> = preferenceManager.isProtectionActive
    override val autoDeletePeriod: Flow<Int> = preferenceManager.autoDeletePeriod
    override val isAlarmEnabled: Flow<Boolean> = preferenceManager.isAlarmEnabled
    override val isVibrationEnabled: Flow<Boolean> = preferenceManager.isVibrationEnabled

    override suspend fun setFirstTime(isFirstTime: Boolean) = preferenceManager.setFirstTime(isFirstTime)
    override suspend fun setThemeMode(mode: String) = preferenceManager.setThemeMode(mode)
    override suspend fun setBiometricEnabled(enabled: Boolean) = preferenceManager.setBiometricEnabled(enabled)
    override suspend fun setDetectionSensitivity(sensitivity: Float) = preferenceManager.setDetectionSensitivity(sensitivity)
    override suspend fun setProtectionActive(active: Boolean) = preferenceManager.setProtectionActive(active)
    override suspend fun setAutoDeletePeriod(days: Int) = preferenceManager.setAutoDeletePeriod(days)
    override suspend fun setAlarmEnabled(enabled: Boolean) = preferenceManager.setAlarmEnabled(enabled)
    override suspend fun setVibrationEnabled(enabled: Boolean) = preferenceManager.setVibrationEnabled(enabled)

    // Mappers
    private fun SecurityEventEntity.toDomain(): SecurityEvent {
        return SecurityEvent(
            id = id,
            type = SecurityEventType.valueOf(type),
            timestamp = timestamp,
            severity = com.sharjeel.wtmp.model.EventSeverity.valueOf(severity),
            sessionDuration = sessionDuration,
            deviceState = deviceState,
            evidencePath = evidencePath
        )
    }

    private fun SecurityEvent.toEntity(): SecurityEventEntity {
        return SecurityEventEntity(
            id = id,
            type = type.name,
            timestamp = timestamp,
            severity = severity.name,
            sessionDuration = sessionDuration,
            deviceState = deviceState,
            evidencePath = evidencePath
        )
    }
}
