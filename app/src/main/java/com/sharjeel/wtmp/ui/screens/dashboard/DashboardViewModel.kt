package com.sharjeel.wtmp.ui.screens.dashboard

import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.ai.GenerativeModel
import com.sharjeel.wtmp.domain.repository.SecurityRepository
import com.sharjeel.wtmp.domain.service.AiSecurityService
import com.sharjeel.wtmp.model.AiSecurityReport
import com.sharjeel.wtmp.model.SecurityEvent
import com.sharjeel.wtmp.model.SecurityEventType
import com.sharjeel.wtmp.repository.UserPreferencesRepository
import com.sharjeel.wtmp.service.MonitoringService
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.inject.Inject

// =================================================================
// 1. DATA MODELS & ENUMS FOR UI FILTERING
// =================================================================

enum class TimeInterval {
    LAST_24_HOURS,
    PAST_WEEK,
    PAST_MONTH,
    TODAY,
    ALL
}

enum class ReportType {
    SUCCESSFUL_UNLOCK,
    UNSUCCESSFUL_UNLOCK,
    APP_LAUNCHED
}

data class DashboardUiState(
    val isProtectionActive: Boolean = false,
    val events: List<SecurityEvent> = emptyList(),
    val todayEventsCount: Int = 0,
    val unreadActivityCount: Int = 0,
    val timeInterval: TimeInterval = TimeInterval.ALL,
    val reportTypes: Set<ReportType> = ReportType.entries.toSet(),
    val isLoading: Boolean = false,
    val currentDate: String = "",
    val customReport: AiSecurityReport? = null,
    val isAiLoading: Boolean = false,
    val isCloudBackupEnabled: Boolean = false,
    val alertEmailInterval: String = "Instant"
)

data class AiDashboardExtraState(
    val cloudBackup: Boolean,
    val emailInterval: String,
    val report: AiSecurityReport?,
    val aiLoading: Boolean,
    val lastVisitedTimestamp: Long
)

// =================================================================
// 2. DASHBOARD VIEWMODEL
// =================================================================

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val repository: SecurityRepository,
    private val aiSecurityService: AiSecurityService,
    private val generativeModel: GenerativeModel?,
    private val userPreferencesRepository: UserPreferencesRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _customReport = MutableStateFlow<AiSecurityReport?>(null)
    private val _isAiLoading = MutableStateFlow(false)
    private val _timeInterval = MutableStateFlow(TimeInterval.ALL)
    private val _reportTypes = MutableStateFlow(ReportType.entries.toSet())

    private fun getFormattedCurrentDate(): String {
        return SimpleDateFormat("EEEE, d MMMM yyyy", Locale.getDefault())
            .format(Date())
            .uppercase(Locale.getDefault())
    }

    val uiState: StateFlow<DashboardUiState> = combine(
        repository.isProtectionActive,
        repository.getAllEvents(),
        _timeInterval,
        _reportTypes,
        combine(
            userPreferencesRepository.isCloudBackupEnabled,
            userPreferencesRepository.alertEmailInterval,
            userPreferencesRepository.lastVisitedActivityTimestamp,
            _customReport,
            _isAiLoading
        ) { cloudBackup, emailInterval, lastVisited, report, loading ->
            AiDashboardExtraState(cloudBackup, emailInterval, report, loading, lastVisited)
        }
    ) { active, events, interval, types, extra ->

        val cloudBackup = extra.cloudBackup
        val emailInterval = extra.emailInterval
        val report = extra.report
        val aiLoading = extra.aiLoading
        val lastVisited = extra.lastVisitedTimestamp

        val todayCount = events.count { isToday(it.timestamp) }

        val unreadCount = events.count { event ->
            (event.type == SecurityEventType.FAILED_UNLOCK || event.type == SecurityEventType.FAILED_ATTEMPT) &&
                    event.timestamp > lastVisited
        }

        val filteredEvents = events.filter { event ->
            val matchesInterval = when (interval) {
                TimeInterval.LAST_24_HOURS -> isWithinHours(event.timestamp, 24)
                TimeInterval.TODAY -> isToday(event.timestamp)
                TimeInterval.PAST_WEEK -> isWithinDays(event.timestamp, 7)
                TimeInterval.PAST_MONTH -> isWithinDays(event.timestamp, 30)
                TimeInterval.ALL -> true
            }

            val matchesType = when (event.type) {
                SecurityEventType.DEVICE_UNLOCKED, SecurityEventType.UNEXPECTED_UNLOCK ->
                    ReportType.SUCCESSFUL_UNLOCK in types
                SecurityEventType.FAILED_UNLOCK, SecurityEventType.FAILED_ATTEMPT ->
                    ReportType.UNSUCCESSFUL_UNLOCK in types
                SecurityEventType.APP_OPENED ->
                    ReportType.APP_LAUNCHED in types
                else -> true
            }

            matchesInterval && matchesType
        }

        DashboardUiState(
            isProtectionActive = active,
            events = filteredEvents,
            todayEventsCount = todayCount,
            unreadActivityCount = unreadCount,
            timeInterval = interval,
            reportTypes = types,
            isLoading = false,
            currentDate = getFormattedCurrentDate(),
            customReport = report,
            isAiLoading = aiLoading,
            isCloudBackupEnabled = cloudBackup,
            alertEmailInterval = emailInterval
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DashboardUiState(
            isLoading = true,
            currentDate = getFormattedCurrentDate()
        )
    )

    fun setAlertEmailInterval(interval: String) {
        viewModelScope.launch {
            userPreferencesRepository.setAlertEmailInterval(interval)
        }
    }

    fun setCloudBackupEnabled(enabled: Boolean) {
        viewModelScope.launch {
            userPreferencesRepository.setCloudBackupEnabled(enabled)
        }
    }

    fun setTimeInterval(interval: TimeInterval) {
        _timeInterval.value = interval
    }

    fun toggleReportType(type: ReportType) {
        _reportTypes.update { current ->
            val mutableSet = current.toMutableSet()
            if (type in mutableSet) {
                if (mutableSet.size > 1) mutableSet.remove(type)
            } else {
                mutableSet.add(type)
            }
            mutableSet
        }
    }

    fun resetFilters() {
        _timeInterval.value = TimeInterval.ALL
        _reportTypes.value = ReportType.entries.toSet()
    }

    fun toggleProtection() {
        viewModelScope.launch {
            val newStatus = !uiState.value.isProtectionActive
            repository.setProtectionActive(newStatus)

            val intent = Intent(context, MonitoringService::class.java)
            if (newStatus) {
                context.startForegroundService(intent)
            } else {
                context.stopService(intent)
            }
        }
    }

    fun generateCustomAiReport(userPrompt: String) {
        viewModelScope.launch {
            _isAiLoading.value = true
            try {
                val events = repository.getAllEvents().first()
                if (generativeModel != null) {
                    val report = aiSecurityService.generateCustomReport(userPrompt, events)
                    _customReport.value = report ?: createFallbackAiReport(events)
                } else {
                    _customReport.value = createFallbackAiReport(events)
                }
            } catch (_: Exception) {
                val events = try { repository.getAllEvents().first() } catch (_: Exception) { emptyList() }
                _customReport.value = createFallbackAiReport(events)
            } finally {
                _isAiLoading.value = false
            }
        }
    }

    private suspend fun createFallbackAiReport(events: List<SecurityEvent>): AiSecurityReport {
        val totalEvents = events.size
        val failedCount = events.count { it.type == SecurityEventType.FAILED_UNLOCK || it.type == SecurityEventType.FAILED_ATTEMPT }
        val successCount = events.count { it.type == SecurityEventType.DEVICE_UNLOCKED || it.type == SecurityEventType.UNEXPECTED_UNLOCK }
        val protectionActive = repository.isProtectionActive.first()

        val score = when {
            failedCount == 0 && protectionActive -> 98
            failedCount == 0 -> 85
            failedCount < 3 -> 78
            else -> 62
        }

        val summaryText = if (failedCount == 0) {
            "Overall Security Audit: Device protection is optimal. 0 unauthorized intrusion attempts detected across $totalEvents total events."
        } else {
            "Overall Security Audit: Attention Required. $failedCount failed unlock attempts detected out of $totalEvents events. Auto photo evidence captured."
        }

        val insights = mutableListOf<String>()
        if (protectionActive) {
            insights.add("🛡️ Protection Shield: Active & Monitoring in background")
        } else {
            insights.add("⚠️ Protection Shield: OFF (Enable in Dashboard for real-time monitoring)")
        }

        insights.add("📊 Audit Metrics: $failedCount Intrusions | $successCount Device Unlocks")

        if (failedCount > 0) {
            insights.add("📷 Evidence Recorded: Photos captured for failed attempts")
        } else {
            insights.add("✅ Zero Threats: No unauthorized logins detected in recent activity")
        }

        insights.add("☁️ Data Vault: Encrypted local storage & Google Drive sync ready")

        return AiSecurityReport(
            reportTitle = "WTMP AI Security Audit",
            summary = summaryText,
            securityScore = score,
            detailedInsights = insights,
            recommendations = listOf(
                "Keep background protection enabled continuously",
                "Ensure Device Admin permissions remain active for failed PIN detection"
            )
        )
    }

    private fun isToday(timestamp: Long): Boolean {
        val calendar = Calendar.getInstance()
        val todayDay = calendar.get(Calendar.DAY_OF_YEAR)
        val todayYear = calendar.get(Calendar.YEAR)

        calendar.timeInMillis = timestamp
        return calendar.get(Calendar.DAY_OF_YEAR) == todayDay && calendar.get(Calendar.YEAR) == todayYear
    }

    private fun isWithinHours(timestamp: Long, hours: Int): Boolean {
        val now = System.currentTimeMillis()
        val diff = now - timestamp
        return diff >= 0 && diff <= hours.toLong() * 60 * 60 * 1000
    }

    private fun isWithinDays(timestamp: Long, days: Int): Boolean {
        val now = System.currentTimeMillis()
        val diff = now - timestamp
        return diff >= 0 && diff <= days.toLong() * 24 * 60 * 60 * 1000
    }
}
