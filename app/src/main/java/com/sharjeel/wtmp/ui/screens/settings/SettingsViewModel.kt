package com.sharjeel.wtmp.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sharjeel.wtmp.domain.repository.SecurityRepository
import com.sharjeel.wtmp.model.SecurityEventType
import com.sharjeel.wtmp.repository.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

// =================================================================
// 1. UI STATE DATA HOLDER
// =================================================================

/**
 * Data class representing the immutable state for the Settings screen.
 */
data class SettingsUiState(
    val themeMode: String = "System",
    val isBiometricEnabled: Boolean = false,
    val detectionSensitivity: Float = 0.5f,
    val autoDeletePeriod: Int = 30,
    val isAlarmEnabled: Boolean = true,
    val isVibrationEnabled: Boolean = true,
    val isAntiTheftEnabled: Boolean = false,
    val unreadActivityCount: Int = 0,
    val alertEmailInterval: String = "Instant",
    val isLoading: Boolean = false
)

// =================================================================
// 2. SETTINGS VIEWMODEL
// =================================================================

/**
 * SettingsViewModel manages application settings streams from SecurityRepository
 * and exposes user preferences as a unified StateFlow to the UI.
 */
@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val repository: SecurityRepository,
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    // =================================================================
    // STATE OBSERVER (Combine DataStore/Repository Flows)
    // =================================================================

    val uiState: StateFlow<SettingsUiState> = combine(
        combine(
            repository.themeMode,
            repository.isBiometricEnabled,
            repository.detectionSensitivity,
            repository.autoDeletePeriod,
            repository.isAlarmEnabled,
            repository.isVibrationEnabled,
            repository.isAntiTheftEnabled
        ) { flows: Array<Any?> -> flows },
        combine(
            repository.getAllEvents(),
            userPreferencesRepository.lastVisitedActivityTimestamp,
            userPreferencesRepository.alertEmailInterval
        ) { events, lastVisited, emailInterval ->
            val unread = events.count {
                (it.type == SecurityEventType.FAILED_UNLOCK || it.type == SecurityEventType.FAILED_ATTEMPT) &&
                        it.timestamp > lastVisited
            }
            Pair(unread, emailInterval)
        }
    ) { settingsArray, extra ->
        SettingsUiState(
            themeMode = settingsArray[0] as String,
            isBiometricEnabled = settingsArray[1] as Boolean,
            detectionSensitivity = settingsArray[2] as Float,
            autoDeletePeriod = settingsArray[3] as Int,
            isAlarmEnabled = settingsArray[4] as Boolean,
            isVibrationEnabled = settingsArray[5] as Boolean,
            isAntiTheftEnabled = settingsArray[6] as Boolean,
            unreadActivityCount = extra.first,
            alertEmailInterval = extra.second,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SettingsUiState(isLoading = true)
    )

    // =================================================================
    // USER INTENTS / PREFERENCE UPDATES
    // =================================================================

    fun updateTheme(mode: String) {
        viewModelScope.launch {
            repository.setThemeMode(mode)
        }
    }

    fun updateBiometric(enabled: Boolean) {
        viewModelScope.launch {
            repository.setBiometricEnabled(enabled)
        }
    }

    fun updateSensitivity(sensitivity: Float) {
        viewModelScope.launch {
            repository.setDetectionSensitivity(sensitivity)
        }
    }

    fun updateAutoDeletePeriod(days: Int) {
        viewModelScope.launch {
            repository.setAutoDeletePeriod(days)
        }
    }

    fun updateAlarm(enabled: Boolean) {
        viewModelScope.launch {
            repository.setAlarmEnabled(enabled)
        }
    }

    fun updateVibration(enabled: Boolean) {
        viewModelScope.launch {
            repository.setVibrationEnabled(enabled)
        }
    }

    fun updateAntiTheft(enabled: Boolean) {
        viewModelScope.launch {
            repository.setAntiTheftEnabled(enabled)
        }
    }

    /**
     * Clears all recorded security logs and captured evidence up to current timestamp.
     */
    fun clearData() {
        viewModelScope.launch {
            repository.deleteOlderThan(System.currentTimeMillis())
        }
    }
}
