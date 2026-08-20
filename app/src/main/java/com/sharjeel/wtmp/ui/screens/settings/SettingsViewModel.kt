package com.sharjeel.wtmp.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sharjeel.wtmp.domain.repository.SecurityRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val themeMode: String = "System Default",
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
        combine(
            repository.themeMode,
            repository.isBiometricEnabled,
            repository.detectionSensitivity
        ) { theme, bio, sens -> Triple(theme, bio, sens) },
        combine(
            repository.autoDeletePeriod,
            repository.isAlarmEnabled,
            repository.isVibrationEnabled
        ) { period, alarm, vib -> Triple(period, alarm, vib) }
    ) { t1, t2 ->
        SettingsUiState(
            themeMode = t1.first,
            isBiometricEnabled = t1.second,
            detectionSensitivity = t1.third,
            autoDeletePeriod = t2.first,
            isAlarmEnabled = t2.second,
            isVibrationEnabled = t2.third,
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
            repository.deleteOlderThan(0) // 0 means delete all
        }
    }
}
