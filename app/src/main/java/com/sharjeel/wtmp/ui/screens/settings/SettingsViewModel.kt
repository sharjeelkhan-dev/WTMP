package com.sharjeel.wtmp.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sharjeel.wtmp.domain.repository.SecurityRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val themeMode: String = "System",
    val isBiometricEnabled: Boolean = false,
    val detectionSensitivity: Float = 0.5f,
    val autoDeletePeriod: Int = 30,
    val isAlarmEnabled: Boolean = true,
    val isVibrationEnabled: Boolean = true,
    val isLoading: Boolean = false
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val repository: SecurityRepository
) : ViewModel() {

    val uiState: StateFlow<SettingsUiState> = combine(
        repository.themeMode,
        repository.isBiometricEnabled,
        repository.detectionSensitivity,
        repository.autoDeletePeriod,
        repository.isAlarmEnabled,
        repository.isVibrationEnabled
    ) { flows: Array<Any?> ->
        SettingsUiState(
            themeMode = flows[0] as String,
            isBiometricEnabled = flows[1] as Boolean,
            detectionSensitivity = flows[2] as Float,
            autoDeletePeriod = flows[3] as Int,
            isAlarmEnabled = flows[4] as Boolean,
            isVibrationEnabled = flows[5] as Boolean,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SettingsUiState(isLoading = true)
    )

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

    fun clearData() {
        viewModelScope.launch {
            repository.deleteOlderThan(System.currentTimeMillis())
        }
    }
}