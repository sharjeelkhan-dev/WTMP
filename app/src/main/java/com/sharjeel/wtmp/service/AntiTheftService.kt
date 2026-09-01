package com.sharjeel.wtmp.service

import android.accessibilityservice.AccessibilityService
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.graphics.PixelFormat
import android.os.Build
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import com.sharjeel.wtmp.MainActivity
import com.sharjeel.wtmp.domain.repository.SecurityRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

@AndroidEntryPoint
class AntiTheftService : AccessibilityService() {

    @Inject
    lateinit var repository: SecurityRepository

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    @Volatile
    private var isAntiTheftActive = false

    @Volatile
    private var isBypassingInterception = false
    
    @Volatile
    private var isBiometricCheckActive = false

    private var windowManager: WindowManager? = null
    private var overlayView: View? = null
    private var isOverlayShowing = false

    private val authReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == ACTION_AUTHENTICATED) {
                Log.d("AntiTheftService", "User Authenticated -> Releasing Lock")
                isBiometricCheckActive = false
                showSystemPowerMenu()
            }
        }
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        Log.d("AntiTheftService", "Service Connected")
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager

        val filter = IntentFilter(ACTION_AUTHENTICATED)
        ContextCompat.registerReceiver(
            this,
            authReceiver,
            filter,
            ContextCompat.RECEIVER_NOT_EXPORTED
        )

        serviceScope.launch {
            repository.isAntiTheftEnabled
                .distinctUntilChanged()
                .collect { enabled ->
                    isAntiTheftActive = enabled
                }
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        if (!isAntiTheftActive) return
        
        // 1. Silent Kiosk: Block Back, Home, and Recents while biometric prompt is showing
        if (isBiometricCheckActive && event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            val packageName = event.packageName?.toString() ?: ""
            if (packageName != "com.sharjeel.wtmp") {
                // If intruder tries to swipe home or switch apps, force them back
                launchLockScreenActivity()
            }
        }

        if (isOverlayShowing || isBypassingInterception) return

        // 2. Intercept Power Menu
        if (event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            val packageName = event.packageName?.toString() ?: ""
            val className = event.className?.toString() ?: ""

            if (isSystemPowerMenu(packageName, className)) {
                Log.d("AntiTheftService", "Power Menu Intercepted!")
                isBiometricCheckActive = true
                dismissSystemPowerDialog()
                showTouchShieldAndLock()
            }
        }
    }

    private fun showSystemPowerMenu() {
        serviceScope.launch {
            isBypassingInterception = true
            removeLockOverlay()
            
            // Wait for MainActivity to fully finish so it doesn't overlap
            delay(600.milliseconds)

            // Trigger the native system power menu
            val success = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                performGlobalAction(GLOBAL_ACTION_POWER_DIALOG)
            } else {
                false
            }
            Log.d("AntiTheftService", "Power Dialog Show Success: $success")

            // Keep bypass active for 10 seconds to allow user interaction
            delay(10.seconds)
            isBypassingInterception = false
        }
    }

    private fun dismissSystemPowerDialog() {
        // Dismiss the system power dialog instantly
        performGlobalAction(GLOBAL_ACTION_BACK)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            performGlobalAction(GLOBAL_ACTION_DISMISS_NOTIFICATION_SHADE)
        }
        performGlobalAction(GLOBAL_ACTION_HOME)
    }

    private fun showTouchShieldAndLock() {
        if (isOverlayShowing) return

        try {
            val params = WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or 
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED, // Essential for Lock Screen
                PixelFormat.TRANSLUCENT
            ).apply {
                gravity = Gravity.FILL
            }

            val transparentView = FrameLayout(this).apply {
                setBackgroundColor(Color.TRANSPARENT)
            }

            overlayView = transparentView
            windowManager?.addView(overlayView, params)
            isOverlayShowing = true

            launchLockScreenActivity()

            // Remove overlay quickly to let activity handle input
            serviceScope.launch {
                delay(300.milliseconds)
                removeLockOverlay()
            }

        } catch (e: Exception) {
            Log.e("AntiTheftService", "Overlay failed", e)
        }
    }

    fun removeLockOverlay() {
        overlayView?.let {
            try {
                windowManager?.removeView(it)
            } catch (_: Exception) {}
        }
        overlayView = null
        isOverlayShowing = false
    }

    private fun isSystemPowerMenu(packageName: String, className: String): Boolean {
        // Restricted list to prevent false triggers in other apps
        val powerPackages = setOf("android", "com.android.systemui", "com.samsung.android.app.poweroff")
        
        val isPowerPackage = powerPackages.contains(packageName)
        
        val powerClasses = setOf(
            "com.android.server.policy.GlobalActions",
            "com.android.systemui.globalactions.GlobalActionsDialog",
            "com.android.systemui.globalactions.GlobalActionsDialogLite",
            "com.samsung.android.app.poweroff.PowerOffActivity",
            "Shutdown",
            "Restart",
            "PowerUI"
        )

        val isPowerClass = powerClasses.any { className.contains(it, ignoreCase = true) }
        
        return isPowerPackage && (isPowerClass || className.contains("Dialog", ignoreCase = true))
    }

    private fun launchLockScreenActivity() {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_ACTIVITY_SINGLE_TOP or
                    Intent.FLAG_ACTIVITY_NO_ANIMATION or
                    Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS
            putExtra("TRIGGER_ANTI_THEFT_LOCK", true)
        }
        startActivity(intent)
    }

    override fun onInterrupt() {}

    override fun onDestroy() {
        super.onDestroy()
        try { unregisterReceiver(authReceiver) } catch (e: Exception) {}
        removeLockOverlay()
        serviceScope.cancel()
    }

    companion object {
        const val ACTION_AUTHENTICATED = "com.sharjeel.wtmp.ACTION_AUTHENTICATED"
    }
}
