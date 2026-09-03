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
    private var isBiometricCheckInProgress = false

    private var windowManager: WindowManager? = null
    private var overlayView: View? = null
    private var isOverlayShowing = false

    private val authReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                ACTION_AUTHENTICATED -> {
                    Log.d("AntiTheftService", "User Authenticated -> Releasing Lock and showing Power Menu")
                    isBiometricCheckInProgress = false
                    showSystemPowerMenu()
                }
                ACTION_RESET_STATE -> {
                    Log.d("AntiTheftService", "Anti-Theft State Reset")
                    isBiometricCheckInProgress = false
                    isBypassingInterception = false
                    removeLockOverlay()
                }
            }
        }
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        Log.d("AntiTheftService", "Service Connected")
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager

        val filter = IntentFilter().apply {
            addAction(ACTION_AUTHENTICATED)
            addAction(ACTION_RESET_STATE)
        }
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
                    if (!enabled) {
                        isBiometricCheckInProgress = false
                        isBypassingInterception = false
                    }
                }
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        if (!isAntiTheftActive) return

        // 1. Silent Kiosk: Prevent app switching while biometric prompt is active
        if (isBiometricCheckInProgress && event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            val pkg = event.packageName?.toString() ?: ""
            // Whitelist system UI and our app
            if (pkg != "com.sharjeel.wtmp" && pkg != "com.android.systemui" && pkg != "android") {
                Log.d("AntiTheftService", "Blocking app switch to $pkg during auth")
                launchLockScreenActivity()
            }
        }

        if (isOverlayShowing || isBypassingInterception) return

        // 2. Power Menu Interception
        if (event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            val packageName = event.packageName?.toString() ?: ""
            val className = event.className?.toString() ?: ""

            if (isSystemPowerMenu(packageName, className)) {
                Log.d("AntiTheftService", "Power Menu Intercepted!")
                isBiometricCheckInProgress = true
                dismissSystemPowerDialog()
                showTouchShieldAndLock()
            }
        }
    }

    private fun showSystemPowerMenu() {
        serviceScope.launch {
            isBypassingInterception = true
            removeLockOverlay()

            // Wait for MainActivity to fully finish
            delay(500.milliseconds)

            // Trigger the native system power menu
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val success = performGlobalAction(GLOBAL_ACTION_POWER_DIALOG)
                Log.d("AntiTheftService", "Power Dialog Action Success: $success")
            }

            // Keep bypass active for interaction
            delay(10.seconds)
            isBypassingInterception = false
        }
    }

    private fun dismissSystemPowerDialog() {
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
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED,
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

            serviceScope.launch {
                delay(400.milliseconds)
                removeLockOverlay()
            }

        } catch (e: Exception) {
            Log.e("AntiTheftService", "Shield failed", e)
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
        // High-precision detection to avoid false triggers with other dialogs
        val powerPackages = setOf("android", "com.android.systemui", "com.samsung.android.app.poweroff")
        if (!powerPackages.contains(packageName)) return false

        val powerClasses = setOf(
            "com.android.server.policy.GlobalActions",
            "com.android.systemui.globalactions.GlobalActionsDialog",
            "com.android.systemui.globalactions.GlobalActionsDialogLite",
            "com.samsung.android.app.poweroff.PowerOffActivity",
            "com.android.internal.policy.impl.GlobalActions",
            "Shutdown",
            "Restart",
            "PowerUI"
        )

        // Class must be in the list OR explicitly contain Shutdown/Restart
        return powerClasses.any { className.contains(it, ignoreCase = true) }
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
        try { unregisterReceiver(authReceiver) } catch (_: Exception) {}
        removeLockOverlay()
        serviceScope.cancel()
    }

    companion object {
        const val ACTION_AUTHENTICATED = "com.sharjeel.wtmp.ACTION_AUTHENTICATED"
        const val ACTION_RESET_STATE = "com.sharjeel.wtmp.ACTION_RESET_STATE"
    }
}
