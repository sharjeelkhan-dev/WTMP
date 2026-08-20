package com.sharjeel.wtmp.ui.screens.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sharjeel.wtmp.domain.repository.SecurityRepository
import com.sharjeel.wtmp.model.SecurityEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EventDetailsViewModel @Inject constructor(
    private val repository: SecurityRepository
) : ViewModel() {

    private val _event = MutableStateFlow<SecurityEvent?>(null)
    val event: StateFlow<SecurityEvent?> = _event.asStateFlow()

    private val _allEvents = MutableStateFlow<List<SecurityEvent>>(emptyList())
    private var currentIndex = -1

    init {
        viewModelScope.launch {
            repository.getAllEvents().collect { events ->
                _allEvents.value = events
                updateCurrentIndex()
            }
        }
    }

    fun loadEvent(eventId: String) {
        viewModelScope.launch {
            val fetchedEvent = repository.getEventById(eventId)
            _event.value = fetchedEvent
            updateCurrentIndex()
        }
    }

    fun navigateToPrevious() {
        if (_allEvents.value.isNotEmpty() && currentIndex > 0) {
            currentIndex--
            _event.value = _allEvents.value[currentIndex]
        }
    }

    fun navigateToNext() {
        if (_allEvents.value.isNotEmpty() && currentIndex < _allEvents.value.lastIndex) {
            currentIndex++
            _event.value = _allEvents.value[currentIndex]
        }
    }

    fun deleteEvent(event: SecurityEvent) {
        viewModelScope.launch {
            repository.deleteEvent(event)
        }
    }

    private fun updateCurrentIndex() {
        val current = _event.value
        if (current != null && _allEvents.value.isNotEmpty()) {
            currentIndex = _allEvents.value.indexOfFirst { it.id == current.id }
        }
    }
}