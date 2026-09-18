package com.sharjeel.wtmp

import android.app.Application
import android.content.pm.ApplicationInfo
import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.appcheck.appCheck
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory
import com.google.firebase.initialize
import dagger.hilt.android.HiltAndroidApp

// =================================================================
// APPLICATION CLASS (HILT ENTRY POINT & FIREBASE INITIALIZATION)
// =================================================================

/**
 * Custom Application class managing:
 * - Hilt Dependency Injection lifecycle container.
 * - Safe Firebase SDK initialization.
 * - Conditional Firebase App Check Debug Provider setup for dev builds.
 */
@HiltAndroidApp
class WTMPApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        setupFirebase()
    }

    // =================================================================
    // SETUP & INITIALIZATION HELPERS
    // =================================================================

    /**
     * Initializes Firebase App SDK safely and configures Debug App Check
     * provider if running on a debug build variant.
     */
    private fun setupFirebase() {
        try {
            Firebase.initialize(context = this)

            val isDebuggable = (applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
            if (isDebuggable && FirebaseApp.getApps(this).isNotEmpty()) {
                Firebase.appCheck.installAppCheckProviderFactory(
                    DebugAppCheckProviderFactory.getInstance()
                )
                Log.d(TAG, "Firebase App Check Debug Provider installed successfully.")
            }
        } catch (e: Exception) {
            Log.e(
                TAG,
                "Firebase initialization failed. Ensure google-services.json is present in the app module.",
                e
            )
        }
    }

    companion object {
        private const val TAG = "WTMPApplication"
    }
}