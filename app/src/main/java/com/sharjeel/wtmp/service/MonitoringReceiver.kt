package com.sharjeel.wtmp.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.content.ContextCompat

/**
 * BroadcastReceiver responsible for intercepting system boot triggers and user present events
 * to restore security monitoring services automatically.
 */
class MonitoringReceiver : BroadcastReceiver() {

    // =================================================================
    // BROADCAST RECEIVER CALLBACK
    // =================================================================

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Intent.ACTION_BOOT_COMPLETED -> {
                Log.d(TAG, "Boot completed received. Starting MonitoringService if active.")
                val serviceIntent = Intent(context, MonitoringService::class.java).apply {
                    action = MonitoringService.ACTION_START_IF_ACTIVE
                }
                // Safe helper method for all Android API versions
                ContextCompat.startForegroundService(context, serviceIntent)
            }
            Intent.ACTION_USER_PRESENT -> {
                Log.d(TAG, "User present received.")
                // Handled dynamically by MonitoringService
            }
        }
    }

    // =================================================================
    // CONSTANTS & TAG
    // =================================================================

    companion object {
        private const val TAG = "MonitoringReceiver"
    }
}