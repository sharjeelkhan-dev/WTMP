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

/**
 * Filter options for filtering security events by time periods.
 */
enum class TimeInterval {
    LAST_24_HOURS,
    PAST_WEEK,
    PAST_MONTH,
    TODAY,
    ALL
}

/**
 * Filter options for categorizing event types on the UI.
 */
enum class ReportType {
    SUCCESSFUL_UNLOCK,
    UNSUCCESSFUL_UNLOCK,
    APP_LAUNCHED
}

/**
 * Represents the complete UI state for DashboardScreen.
 * Immutable StateFlow object observed directly by Jetpack Compose.
 */
data class DashboardUiState(
    val isProtectionActive: Boolean = false,
    val events: List<SecurityEvent> = emptyList(),
    val todayEventsCount: Int = 0,
    val timeInterval: TimeInterval = TimeInterval.ALL,
    val reportTypes: Set<ReportType> = ReportType.entries.toSet(),
    val isLoading: Boolean = false,
    val currentDate: String = "",
    val customReport: AiSecurityReport? = null,
    val isAiLoading: Boolean = false
)

// =================================================================
// 2. DASHBOARD VIEWMODEL
// =================================================================

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val repository: SecurityRepository,
    private val aiSecurityService: AiSecurityService,
    private val generativeModel: GenerativeModel?,
    @ApplicationContext private val context: Context
) : ViewModel() {

    // Internal StateFlows for mutable state operations
    private val _customReport = MutableStateFlow<AiSecurityReport?>(null)
    private val _isAiLoading = MutableStateFlow(false)
    private val _timeInterval = MutableStateFlow(TimeInterval.ALL)
    private val _reportTypes = MutableStateFlow(ReportType.entries.toSet())

    /**
     * Formats current timestamp into uppercase display string (e.g., "SATURDAY, 18 JULY 2026").
     */
    private fun getFormattedCurrentDate(): String {
        return SimpleDateFormat("EEEE, d MMMM yyyy", Locale.getDefault())
            .format(Date())
            .uppercase(Locale.getDefault())
    }

    // =================================================================
    // 3. REACTIVE UI STATE COMBINER
    // =================================================================

    /**
     * Combines background flows (Repository streams + Local Filter states)
     * into a single unified DashboardUiState for reactive UI updates.
     */
    val uiState: StateFlow<DashboardUiState> = combine(
        repository.isProtectionActive,
        repository.getAllEvents(),
        _timeInterval,
        _reportTypes,
        combine(_customReport, _isAiLoading) { report, loading -> report to loading }
    ) { active, events, interval, types, aiState ->

        val (report, aiLoading) = aiState

        // Count events triggered on the current calendar day
        val todayCount = events.count { isToday(it.timestamp) }

        // Filter events based on selected TimeInterval and ReportType filters
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
            timeInterval = interval,
            reportTypes = types,
            isLoading = false,
            currentDate = getFormattedCurrentDate(),
            customReport = report,
            isAiLoading = aiLoading
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DashboardUiState(
            isLoading = true,
            currentDate = getFormattedCurrentDate()
        )
    )

    // =================================================================
    // 4. USER INTENT ACTIONS / HANDLERS
    // =================================================================

    /**
     * Updates time interval filter (e.g., Today, 24h, Week, Month).
     */
    fun setTimeInterval(interval: TimeInterval) {
        _timeInterval.value = interval
    }

    /**
     * Toggles an individual event category filter.
     * Prevents deselecting all filters by ensuring at least one remains active.
     */
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

    /**
     * Resets all filters back to default (ALL intervals & ALL report types enabled).
     */
    fun resetFilters() {
        _timeInterval.value = TimeInterval.ALL
        _reportTypes.value = ReportType.entries.toSet()
    }

    /**
     * Toggles background monitoring protection state and manages Foreground Service lifecycle.
     */
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

    /**
     * Requests custom AI security audit via Generative AI Service based on logged security events.
     */
    fun generateCustomAiReport(userPrompt: String) {
        if (generativeModel == null) return

        viewModelScope.launch {
            _isAiLoading.value = true
            try {
                val events = repository.getAllEvents().first()
                _customReport.value = aiSecurityService.generateCustomReport(userPrompt, events)
            } catch (_: Exception) {
                _customReport.value = null
            } finally {
                _isAiLoading.value = false
            }
        }
    }

    // =================================================================
    // 5. PRIVATE HELPER / DATE COMPARISON FUNCTIONS
    // =================================================================

    /**
     * Checks if given epoch timestamp falls within today's calendar day.
     */
    private fun isToday(timestamp: Long): Boolean {
        val calendar = Calendar.getInstance()
        val todayDay = calendar.get(Calendar.DAY_OF_YEAR)
        val todayYear = calendar.get(Calendar.YEAR)

        calendar.timeInMillis = timestamp
        return calendar.get(Calendar.DAY_OF_YEAR) == todayDay && calendar.get(Calendar.YEAR) == todayYear
    }

    /**
     * Checks if given timestamp occurred within last N hours.
     */
    private fun isWithinHours(timestamp: Long, hours: Int): Boolean {
        val now = System.currentTimeMillis()
        val diff = now - timestamp
        return diff >= 0 && diff <= hours.toLong() * 60 * 60 * 1000
    }

    /**
     * Checks if given timestamp occurred within last N days.
     */
    private fun isWithinDays(timestamp: Long, days: Int): Boolean {
        val now = System.currentTimeMillis()
        val diff = now - timestamp
        return diff >= 0 && diff <= days.toLong() * 24 * 60 * 60 * 1000
    }
}