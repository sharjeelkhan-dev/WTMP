package com.sharjeel.wtmp.service

import android.app.admin.DeviceAdminReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

/**
 * Receiver class handling Android Device Administration privileges
 * for anti-theft operations and security monitoring.
 */
class AntiTheftAdminReceiver : DeviceAdminReceiver() {

    // =================================================================
    // DEVICE ADMIN CALLBACKS
    // =================================================================

    override fun onEnabled(context: Context, intent: Intent) {
        super.onEnabled(context, intent)
        Log.d(TAG, "Device Admin Enabled successfully.")
    }

    override fun onDisabled(context: Context, intent: Intent) {
        super.onDisabled(context, intent)
        Log.d(TAG, "Device Admin Disabled.")
    }

    // =================================================================
    // CONSTANTS
    // =================================================================

    companion object {
        private const val TAG = "AntiTheftAdmin"
    }
}