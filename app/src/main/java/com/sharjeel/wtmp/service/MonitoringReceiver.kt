package com.sharjeel.wtmp.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.content.ContextCompat

class MonitoringReceiver : BroadcastReceiver() {

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

    companion object {
        private const val TAG = "MonitoringReceiver"
    }
}