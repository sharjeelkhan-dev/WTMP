package com.sharjeel.wtmp.ui.screens.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sharjeel.wtmp.domain.repository.SecurityRepository
import com.sharjeel.wtmp.model.SecurityEvent
import com.sharjeel.wtmp.model.SecurityEventType
import com.sharjeel.wtmp.repository.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject

// =================================================================
// 1. STATS UI STATE DATA HOLDER
// =================================================================

/**
 * State representation for the StatsScreen displaying aggregated security metrics,
 * intrusion counts, and calculated security scores.
 */
data class StatsUiState(
    val protectedSessions: Int = 0,
    val intrusionsPrevented: Int = 0,
    val avgSessionLength: String = "00:00:00",
    val securityScore: Int = 100,
    val scoreTrend: String = "+0%",
    val activityData: List<Float> = List(7) { 0f },
    val events: List<SecurityEvent> = emptyList(),
    val totalPhotoCount: Int = 0,
    val totalEventCount: Int = 0,
    val selectedTab: Int = 0,
    val unreadActivityCount: Int = 0,
    val isLoading: Boolean = false
)

// =================================================================
// 2. STATS VIEWMODEL
// =================================================================

/**
 * ViewModel responsible for transforming raw SecurityEvent logs into
 * actionable analytics and security trends for the Stats UI.
 */
@HiltViewModel
class StatsViewModel @Inject constructor(
    repository: SecurityRepository,
    userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val _selectedTab = MutableStateFlow(0)

    // =================================================================
    // REACTIVE STATEFLOW (EVENTS TRANSFORMED TO STATS)
    // =================================================================

    val uiState: StateFlow<StatsUiState> = combine(
        repository.getAllEvents(),
        _selectedTab,
        userPreferencesRepository.lastVisitedActivityTimestamp
    ) { events, tab, lastVisited ->
        val allPhotoCount = events.count { !it.evidencePath.isNullOrBlank() }

        val filteredEvents = when (tab) {
            1 -> events.filter { isIntruderEvent(it) }
            2 -> events.filter { isUnlockEvent(it) }
            else -> events
        }

        val intrusions = events.count { isIntruderEvent(it) }
        val unlocks = events.count { isUnlockEvent(it) }
        val score = (100 - (intrusions * 2)).coerceIn(0, 100)

        val unreadCount = events.count { event ->
            (event.type == SecurityEventType.FAILED_UNLOCK || event.type == SecurityEventType.FAILED_ATTEMPT) &&
                    event.timestamp > lastVisited
        }

        StatsUiState(
            protectedSessions = unlocks,
            intrusionsPrevented = intrusions,
            avgSessionLength = formatAverageInterval(events),
            securityScore = score,
            scoreTrend = if (intrusions == 0) "+12%" else "-${(intrusions * 2).coerceAtMost(50)}%",
            activityData = computeLast7DaysActivity(events),
            events = filteredEvents,
            totalPhotoCount = allPhotoCount,
            totalEventCount = events.size,
            selectedTab = tab,
            unreadActivityCount = unreadCount,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = StatsUiState(isLoading = true)
    )

    // =================================================================
    // INTENTS & CALCULATION LOGIC
    // =================================================================

    fun setSelectedTab(tab: Int) {
        if (tab !in 0..2) return
        _selectedTab.value = tab
    }

    // =================================================================
    // PRIVATE CLASSIFICATION & ANALYTICS HELPERS
    // =================================================================

    private fun isIntruderEvent(event: SecurityEvent): Boolean {
        return event.type == SecurityEventType.FAILED_UNLOCK ||
                event.type == SecurityEventType.FAILED_ATTEMPT ||
                event.type == SecurityEventType.UNEXPECTED_UNLOCK
    }

    private fun isUnlockEvent(event: SecurityEvent): Boolean {
        return event.type == SecurityEventType.DEVICE_UNLOCKED
    }

    private fun computeLast7DaysActivity(events: List<SecurityEvent>): List<Float> {
        if (events.isEmpty()) return List(7) { 0f }

        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val startOfToday = cal.timeInMillis
        val dayMillis = 24 * 60 * 60 * 1000L
        val windowStart = startOfToday - (6 * dayMillis)

        val buckets = IntArray(7)
        for ((_, _, timestamp) in events) {
            if (timestamp >= windowStart) {
                val dayIndex = ((timestamp - windowStart) / dayMillis).toInt().coerceIn(0, 6)
                buckets[dayIndex]++
            }
        }

        val maxCount = buckets.maxOrNull()?.takeIf { it > 0 } ?: 1
        return buckets.map { (it.toFloat() / maxCount).coerceIn(0f, 1f) }
    }

    private fun formatAverageInterval(events: List<SecurityEvent>): String {
        if (events.size < 2) return "00:00:00"
        val sorted = events.map { it.timestamp }.sorted()
        var totalDiffMillis = 0L
        for (i in 1 until sorted.size) {
            totalDiffMillis += (sorted[i] - sorted[i - 1]).coerceAtLeast(0L)
        }
        val avgSeconds = (totalDiffMillis / (sorted.size - 1)) / 1000L
        val hours = avgSeconds / 3600
        val minutes = (avgSeconds % 3600) / 60
        val seconds = avgSeconds % 60
        return String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds)
    }
}
