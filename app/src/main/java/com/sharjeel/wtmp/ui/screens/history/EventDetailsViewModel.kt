package com.sharjeel.wtmp.ui.screens.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sharjeel.wtmp.domain.repository.SecurityRepository
import com.sharjeel.wtmp.domain.service.AiSecurityService
import com.sharjeel.wtmp.model.*
import com.google.firebase.ai.GenerativeModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EventDetailsViewModel @Inject constructor(
    private val repository: SecurityRepository,
    private val aiSecurityService: AiSecurityService,
    private val generativeModel: GenerativeModel?
) : ViewModel() {

    private val _event = MutableStateFlow<SecurityEvent?>(null)
    val event: StateFlow<SecurityEvent?> = _event.asStateFlow()

    private val _aiAnalysis = MutableStateFlow<AiEventAnalysis?>(null)
    val aiAnalysis: StateFlow<AiEventAnalysis?> = _aiAnalysis.asStateFlow()

    private val _isAiLoading = MutableStateFlow(false)
    val isAiLoading: StateFlow<Boolean> = _isAiLoading.asStateFlow()

    private val _allEvents = MutableStateFlow<List<SecurityEvent>>(emptyList())
    private var currentIndex = -1

    init {
        viewModelScope.launch {
            repository.getAllEvents().collect { events ->
                _allEvents.value = events

                // Keep the current displayed event updated if it changes in the database
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

    fun loadEvent(eventId: String) {
        viewModelScope.launch {
            val fetchedEvent = repository.getEventById(eventId)
            _event.value = fetchedEvent
            _aiAnalysis.value = null // Reset analysis
            updateCurrentIndex()
        }
    }

    fun navigateToPrevious() {
        if (_allEvents.value.isNotEmpty() && currentIndex > 0) {
            currentIndex--
            _event.value = _allEvents.value[currentIndex]
            _aiAnalysis.value = null // Reset analysis
        }
    }

    fun navigateToNext() {
        if (_allEvents.value.isNotEmpty() && currentIndex < _allEvents.value.lastIndex) {
            currentIndex++
            _event.value = _allEvents.value[currentIndex]
            _aiAnalysis.value = null // Reset analysis
        }
    }

    fun deleteEvent(event: SecurityEvent) {
        viewModelScope.launch {
            repository.deleteEvent(event)
        }
    }

    fun analyzeEvent() {
        if (generativeModel == null) return
        val event = _event.value ?: return
        viewModelScope.launch {
            _isAiLoading.value = true
            try {
                _aiAnalysis.value = aiSecurityService.analyzeEventWithVision(event)
            } catch (e: Exception) {
                // Error handled in UI
            } finally {
                _isAiLoading.value = false
            }
        }
    }

    private fun updateCurrentIndex() {
        val current = _event.value
        if (current != null && _allEvents.value.isNotEmpty()) {
            currentIndex = _allEvents.value.indexOfFirst { it.id == current.id }
        }
    }
}