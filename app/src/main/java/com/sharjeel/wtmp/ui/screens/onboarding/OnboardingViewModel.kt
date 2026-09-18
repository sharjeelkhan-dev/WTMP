package com.sharjeel.wtmp.ui.screens.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sharjeel.wtmp.repository.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

// =================================================================
// ONBOARDING VIEWMODEL
// =================================================================

/**
 * OnboardingViewModel handles user interactions on the Onboarding flow
 * and updates application-level preference states.
 */
@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    // =================================================================
    // USER INTENTS / ACTIONS
    // =================================================================

    /**
     * Marks the onboarding flow as completed in DataStore / SharedPreferences
     * so that the user is directly routed to the Dashboard on subsequent app launches.
     */
    fun completeOnboarding() {
        viewModelScope.launch {
            userPreferencesRepository.setOnboardingCompleted()
        }
    }
}