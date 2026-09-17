package com.sharjeel.wtmp.ui.screens.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.ai.GenerativeModel
import com.sharjeel.wtmp.domain.repository.SecurityRepository
import com.sharjeel.wtmp.domain.service.AiSecurityService
import com.sharjeel.wtmp.model.AiEventAnalysis
import com.sharjeel.wtmp.model.SecurityEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
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

    // Loading indicator state for AI analysis network call
    private val _isAiLoading = MutableStateFlow(false)
    val isAiLoading: StateFlow<Boolean> = _isAiLoading.asStateFlow()

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
            _aiAnalysis.value = null // Reset previous AI analysis state
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
            _aiAnalysis.value = null // Reset AI analysis for new item
        }
    }

    /**
     * Navigates to the next report log in chronological sequence.
     */
    fun navigateToNext() {
        if (_allEvents.value.isNotEmpty() && currentIndex < _allEvents.value.lastIndex) {
            currentIndex++
            _event.value = _allEvents.value[currentIndex]
            _aiAnalysis.value = null // Reset AI analysis for new item
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
     * Triggers AI Vision feature to analyze captured intruder image using Gemini AI SDK.
     */
    fun analyzeEvent() {
        if (generativeModel == null) return
        val event = _event.value ?: return

        viewModelScope.launch {
            _isAiLoading.value = true
            try {
                _aiAnalysis.value = aiSecurityService.analyzeEventWithVision(event)
            } catch (_: Exception) {
                // Failure handled safely in UI
            } finally {
                _isAiLoading.value = false
            }
        }
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