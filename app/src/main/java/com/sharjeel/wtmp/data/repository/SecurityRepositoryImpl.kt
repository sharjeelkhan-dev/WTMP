package com.sharjeel.wtmp.data.repository

import com.sharjeel.wtmp.data.database.SecurityEventDao
import com.sharjeel.wtmp.data.database.SecurityEventEntity
import com.sharjeel.wtmp.domain.repository.SecurityRepository
import com.sharjeel.wtmp.model.SecurityEvent
import com.sharjeel.wtmp.model.SecurityEventType
import com.sharjeel.wtmp.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class SecurityRepositoryImpl @Inject constructor(
    private val dao: SecurityEventDao,
    private val userPreferencesRepository: UserPreferencesRepository
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

    // Preferences Delegations
    override val isFirstTime: Flow<Boolean> = userPreferencesRepository.hasCompletedOnboarding.map { !it }
    override val themeMode: Flow<String> = userPreferencesRepository.themeMode
    override val isBiometricEnabled: Flow<Boolean> = userPreferencesRepository.isBiometricEnabled
    override val detectionSensitivity: Flow<Float> = userPreferencesRepository.detectionSensitivity
    override val autoDeletePeriod: Flow<Int> = userPreferencesRepository.autoDeletePeriod
    override val isAlarmEnabled: Flow<Boolean> = userPreferencesRepository.isAlarmEnabled
    override val isVibrationEnabled: Flow<Boolean> = userPreferencesRepository.isVibrationEnabled

    override val isProtectionActive: Flow<Boolean> = userPreferencesRepository.isBiometricEnabled

    override suspend fun setFirstTime(isFirstTime: Boolean) = userPreferencesRepository.setOnboardingCompleted()
    override suspend fun setThemeMode(mode: String) = userPreferencesRepository.setThemeMode(mode)
    override suspend fun setBiometricEnabled(enabled: Boolean) = userPreferencesRepository.setBiometricEnabled(enabled)
    override suspend fun setDetectionSensitivity(sensitivity: Float) = userPreferencesRepository.setDetectionSensitivity(sensitivity)
    override suspend fun setProtectionActive(active: Boolean) = userPreferencesRepository.setBiometricEnabled(active)
    override suspend fun setAutoDeletePeriod(days: Int) = userPreferencesRepository.setAutoDeletePeriod(days)
    override suspend fun setAlarmEnabled(enabled: Boolean) = userPreferencesRepository.setAlarmEnabled(enabled)
    override suspend fun setVibrationEnabled(enabled: Boolean) = userPreferencesRepository.setVibrationEnabled(enabled)

    // Mappers
    private fun SecurityEventEntity.toDomain(): SecurityEvent {
        return SecurityEvent(
            id = id,
            type = SecurityEventType.valueOf(type),
            timestamp = timestamp,
            severity = com.sharjeel.wtmp.model.EventSeverity.valueOf(severity),
            sessionDuration = sessionDuration,
            deviceState = deviceState,
            evidencePath = evidencePath,
            accessedApps = accessedApps
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
            evidencePath = evidencePath,
            accessedApps = accessedApps
        )
    }
}