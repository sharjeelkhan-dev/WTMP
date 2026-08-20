package com.sharjeel.wtmp.ui.screens.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sharjeel.wtmp.domain.repository.SecurityRepository
import com.sharjeel.wtmp.model.SecurityEvent
import com.sharjeel.wtmp.model.SecurityEventType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import javax.inject.Inject

data class StatsUiState(
    val protectedSessions: Int = 0,
    val intrusionsPrevented: Int = 0,
    val avgSessionLength: String = "00:00:00",
    val securityScore: Int = 85,
    val scoreTrend: String = "+0%",
    val activityData: List<Float> = emptyList(),
    val isLoading: Boolean = false
)

@HiltViewModel
class StatsViewModel @Inject constructor(
    private val repository: SecurityRepository
) : ViewModel() {

    val uiState: StateFlow<StatsUiState> = repository.getAllEvents().map { events ->
        calculateStats(events)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = StatsUiState(isLoading = true)
    )

    fun refreshStats() {
        // Stats are reactive, but this could trigger a manual refresh if needed
    }

    private fun calculateStats(events: List<SecurityEvent>): StatsUiState {
        val intrusions = events.count { 
            it.type == SecurityEventType.UNEXPECTED_UNLOCK || 
            it.type == SecurityEventType.FAILED_ATTEMPT 
        }
        
        // Mock calculations for demo purposes
        return StatsUiState(
            protectedSessions = 128, // In a real app, track sessions separately
            intrusionsPrevented = intrusions,
            avgSessionLength = "08:24:15",
            securityScore = (100 - (intrusions * 2)).coerceIn(0, 100),
            scoreTrend = "+12%",
            activityData = listOf(0.8f, 0.4f, 0.6f, 0.2f, 0.7f, 0.3f, 0.9f),
            isLoading = false
        )
    }
}
