package com.sharjeel.wtmp.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PreferenceManager @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    private object PreferencesKeys {
        val IS_FIRST_TIME = booleanPreferencesKey("is_first_time")
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val IS_BIOMETRIC_ENABLED = booleanPreferencesKey("is_biometric_enabled")
        val DETECTION_SENSITIVITY = floatPreferencesKey("detection_sensitivity")
        val IS_PROTECTION_ACTIVE = booleanPreferencesKey("is_protection_active")
        val AUTO_DELETE_PERIOD = intPreferencesKey("auto_delete_period")
        val IS_ALARM_ENABLED = booleanPreferencesKey("is_alarm_enabled")
        val IS_VIBRATION_ENABLED = booleanPreferencesKey("is_vibration_enabled")
    }

    val isFirstTime: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[PreferencesKeys.IS_FIRST_TIME] ?: true
    }

    val themeMode: Flow<String> = dataStore.data.map { preferences ->
        preferences[PreferencesKeys.THEME_MODE] ?: "System"
    }

    val isBiometricEnabled: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[PreferencesKeys.IS_BIOMETRIC_ENABLED] ?: false
    }

    val detectionSensitivity: Flow<Float> = dataStore.data.map { preferences ->
        preferences[PreferencesKeys.DETECTION_SENSITIVITY] ?: 0.5f
    }

    val isProtectionActive: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[PreferencesKeys.IS_PROTECTION_ACTIVE] ?: false
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

    suspend fun setFirstTime(isFirstTime: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.IS_FIRST_TIME] = isFirstTime
        }
    }

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

    suspend fun setProtectionActive(active: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.IS_PROTECTION_ACTIVE] = active
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
}
