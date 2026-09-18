package com.sharjeel.wtmp.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository handling user settings and app preferences using Jetpack DataStore.
 */
@Singleton
class UserPreferencesRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {

    // =================================================================
    // 1. DATASTORE KEYS CONFIGURATION
    // =================================================================

    private object PreferencesKeys {
        val HAS_COMPLETED_ONBOARDING = booleanPreferencesKey("has_completed_onboarding")
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val IS_BIOMETRIC_ENABLED = booleanPreferencesKey("is_biometric_enabled")
        val DETECTION_SENSITIVITY = floatPreferencesKey("detection_sensitivity")
        val AUTO_DELETE_PERIOD = intPreferencesKey("auto_delete_period")
        val IS_ALARM_ENABLED = booleanPreferencesKey("is_alarm_enabled")
        val IS_VIBRATION_ENABLED = booleanPreferencesKey("is_vibration_enabled")
        val IS_ANTI_THEFT_ENABLED = booleanPreferencesKey("is_anti_theft_enabled")
    }

    // =================================================================
    // 2. ONBOARDING PREFERENCES
    // =================================================================

    val hasCompletedOnboarding: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[PreferencesKeys.HAS_COMPLETED_ONBOARDING] ?: false
    }

    suspend fun setOnboardingCompleted() {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.HAS_COMPLETED_ONBOARDING] = true
        }
    }

    // =================================================================
    // 3. SETTINGS READ FLOWS
    // =================================================================

    val themeMode: Flow<String> = dataStore.data.map { preferences ->
        preferences[PreferencesKeys.THEME_MODE] ?: "System"
    }

    val isBiometricEnabled: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[PreferencesKeys.IS_BIOMETRIC_ENABLED] ?: false
    }

    val detectionSensitivity: Flow<Float> = dataStore.data.map { preferences ->
        preferences[PreferencesKeys.DETECTION_SENSITIVITY] ?: 0.5f
    }

    val autoDeletePeriod: Flow<Int> = dataStore.data.map { preferences ->
        preferences[PreferencesKeys.AUTO_DELETE_PERIOD] ?: 30
    }

    val isAlarmEnabled: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[PreferencesKeys.IS_ALARM_ENABLED] ?: true
    }

    val isVibrationEnabled: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[PreferencesKeys.IS_VIBRATION_ENABLED] ?: true
    }

    val isAntiTheftEnabled: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[PreferencesKeys.IS_ANTI_THEFT_ENABLED] ?: false
    }

    // =================================================================
    // 4. SETTINGS WRITE UPDATERS
    // =================================================================

    suspend fun setThemeMode(mode: String) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.THEME_MODE] = mode
        }
    }

    suspend fun setBiometricEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.IS_BIOMETRIC_ENABLED] = enabled
        }
    }

    suspend fun setDetectionSensitivity(sensitivity: Float) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.DETECTION_SENSITIVITY] = sensitivity
        }
    }

    suspend fun setAutoDeletePeriod(days: Int) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.AUTO_DELETE_PERIOD] = days
        }
    }

    suspend fun setAlarmEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.IS_ALARM_ENABLED] = enabled
        }
    }

    suspend fun setVibrationEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.IS_VIBRATION_ENABLED] = enabled
        }
    }

    suspend fun setAntiTheftEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.IS_ANTI_THEFT_ENABLED] = enabled
        }
    }
}