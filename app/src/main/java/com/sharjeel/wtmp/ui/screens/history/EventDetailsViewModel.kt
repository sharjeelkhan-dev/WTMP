package com.sharjeel.wtmp.ui.screens.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.ai.GenerativeModel
import com.sharjeel.wtmp.domain.repository.SecurityRepository
import com.sharjeel.wtmp.domain.service.AiSecurityService
import com.sharjeel.wtmp.model.AiEventAnalysis
import com.sharjeel.wtmp.model.SecurityEvent
import com.sharjeel.wtmp.model.SecurityEventType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

// =================================================================
// EVENT DETAILS VIEWMODEL
// =================================================================

/**
 * EventDetailsViewModel manages screen state, pagination, deletion,
 * and Gemini AI vision analysis for a single security report log.
 */
@HiltViewModel
class EventDetailsViewModel @Inject constructor(
    private val repository: SecurityRepository,
    private val aiSecurityService: AiSecurityService,
    private val generativeModel: GenerativeModel?
) : ViewModel() {

    // =================================================================
    // 1. STATE HOLDERS (Private Mutable + Public Read-Only StateFlows)
    // =================================================================

    // Currently selected security event log
    private val _event = MutableStateFlow<SecurityEvent?>(null)
    val event: StateFlow<SecurityEvent?> = _event.asStateFlow()

    // AI vision analysis result generated for the current event
    private val _aiAnalysis = MutableStateFlow<AiEventAnalysis?>(null)
    val aiAnalysis: StateFlow<AiEventAnalysis?> = _aiAnalysis.asStateFlow()

    // Loading indicator state for AI analysis call
    private val _isAiLoading = MutableStateFlow(false)
    val isAiLoading: StateFlow<Boolean> = _isAiLoading.asStateFlow()

    // Optional error/status message when live vision analysis is unavailable or fails
    private val _aiError = MutableStateFlow<String?>(null)
    val aiError: StateFlow<String?> = _aiError.asStateFlow()

    // Internal cache of all recorded security events for pagination support
    private val _allEvents = MutableStateFlow<List<SecurityEvent>>(emptyList())
    private var currentIndex = -1

    // =================================================================
    // 2. INITIALIZATION & REACTIVE DATA STREAMING
    // =================================================================

    init {
        // Collect real-time updates from Room Database repository
        viewModelScope.launch {
            repository.getAllEvents().collect { events ->
                _allEvents.value = events

                // Keep currently displayed event updated if modified in database
                val current = _event.value
                if (current != null) {
                    val updated = events.find { it.id == current.id }
                    if (updated != null && updated != current) {
                        _event.value = updated
                    }
                }

                updateCurrentIndex()
            }
        }
    }

    // =================================================================
    // 3. USER ACTIONS & PAGINATION INTENTS
    // =================================================================

    /**
     * Loads specific event details by ID from local database repository.
     */
    fun loadEvent(eventId: String) {
        viewModelScope.launch {
            val fetchedEvent = repository.getEventById(eventId)
            _event.value = fetchedEvent
            _aiAnalysis.value = null
            _aiError.value = null
            updateCurrentIndex()
        }
    }

    /**
     * Navigates to the previous report log in chronological sequence.
     */
    fun navigateToPrevious() {
        if (_allEvents.value.isNotEmpty() && currentIndex > 0) {
            currentIndex--
            _event.value = _allEvents.value[currentIndex]
            _aiAnalysis.value = null
            _aiError.value = null
        }
    }

    /**
     * Navigates to the next report log in chronological sequence.
     */
    fun navigateToNext() {
        if (_allEvents.value.isNotEmpty() && currentIndex < _allEvents.value.lastIndex) {
            currentIndex++
            _event.value = _allEvents.value[currentIndex]
            _aiAnalysis.value = null
            _aiError.value = null
        }
    }

    /**
     * Removes selected security event record from local repository.
     */
    fun deleteEvent(event: SecurityEvent) {
        viewModelScope.launch {
            repository.deleteEvent(event)
        }
    }

    /**
     * Triggers real AI Vision analysis for the captured event image via AiSecurityService.
     * Falls back strictly to event-log-based metadata without fabricating visual details.
     */
    fun analyzeEvent() {
        val currentEvent = _event.value ?: return

        viewModelScope.launch {
            _isAiLoading.value = true
            _aiError.value = null
            try {
                if (generativeModel != null) {
                    val liveAnalysis = aiSecurityService.analyzeEventWithVision(currentEvent)
                    if (liveAnalysis != null) {
                        _aiAnalysis.value = liveAnalysis
                    } else {
                        val reason = "Vision model returned no result."
                        _aiError.value = reason
                        _aiAnalysis.value = createEventLogFallbackAnalysis(currentEvent, reason)
                    }
                } else {
                    val reason = "GenerativeModel is null."
                    _aiError.value = reason
                    _aiAnalysis.value = createEventLogFallbackAnalysis(currentEvent, reason)
                }
            } catch (e: Exception) {
                val reason = e.localizedMessage?.takeIf { it.isNotBlank() }
                    ?: e::class.java.simpleName
                _aiError.value = reason
                _aiAnalysis.value = createEventLogFallbackAnalysis(currentEvent, reason)
            } finally {
                _isAiLoading.value = false
            }
        }
    }

    /**
     * Builds a grounded fallback strictly from the SecurityEvent record fields.
     * Never fabricates physical appearance, clothing, room details, or timestamps not in the event.
     */
    private fun createEventLogFallbackAnalysis(
        event: SecurityEvent,
        reason: String
    ): AiEventAnalysis {
        val isUnlockSuccess =
            event.type == SecurityEventType.DEVICE_UNLOCKED ||
                    event.type == SecurityEventType.UNEXPECTED_UNLOCK

        val formattedTime = SimpleDateFormat("MMM dd, yyyy 'at' hh:mm:ss a", Locale.getDefault())
            .format(Date(event.timestamp))

        val hasEvidenceFile = !event.evidencePath.isNullOrBlank()
        val evidenceNote = if (hasEvidenceFile) {
            "Evidence image path is attached at ${event.evidencePath}, but live vision did not return a result ($reason)."
        } else {
            "No local evidence image path is attached to this event record ($reason)."
        }

        val stateDetail = event.deviceState.takeIf { it.isNotBlank() }
            ?: if (isUnlockSuccess) "Device unlock event recorded" else "Failed unlock attempt recorded"

        val explanationText = if (isUnlockSuccess) {
            "A device unlock event ($stateDetail) was logged on $formattedTime. $evidenceNote"
        } else {
            "A failed unlock event ($stateDetail) was logged on $formattedTime. $evidenceNote"
        }

        val riskText = when (event.type) {
            SecurityEventType.FAILED_UNLOCK -> "High"
            SecurityEventType.UNEXPECTED_UNLOCK -> "Medium"
            SecurityEventType.DEVICE_UNLOCKED -> "Low"
            else -> "Medium"
        }

        val categoryText = when (event.type) {
            SecurityEventType.FAILED_UNLOCK -> "Failed Unlock Event"
            SecurityEventType.UNEXPECTED_UNLOCK -> "Unexpected Unlock Event"
            SecurityEventType.DEVICE_UNLOCKED -> "Device Unlock Event"
            else -> "Security Event"
        }

        val recommendationText = if (isUnlockSuccess) {
            "Confirm you initiated the unlock at $formattedTime."
        } else {
            "Verify whether you attempted to unlock the device at $formattedTime and check security settings if unrecognized."
        }

        val subjectDescriptionText = if (hasEvidenceFile) {
            "Live vision result unavailable for ${event.evidencePath} ($reason)."
        } else {
            "No captured subject image is available in this event log."
        }

        return AiEventAnalysis(
            explanation = explanationText,
            riskLevel = riskText,
            category = categoryText,
            recommendation = recommendationText,
            intruderDescription = subjectDescriptionText
        )
    }

    // =================================================================
    // 4. PRIVATE HELPER FUNCTIONS
    // =================================================================

    /**
     * Synchronizes current item index position relative to complete cached list.
     */
    private fun updateCurrentIndex() {
        val current = _event.value
        if (current != null && _allEvents.value.isNotEmpty()) {
            currentIndex = _allEvents.value.indexOfFirst { it.id == current.id }
        }
    }
}