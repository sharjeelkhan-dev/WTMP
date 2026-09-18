package com.sharjeel.wtmp.utils

import android.Manifest
import android.app.AppOpsManager
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Process
import androidx.core.content.ContextCompat
import com.sharjeel.wtmp.service.AdminReceiver

// =================================================================
// PERMISSION UTILS (Device Admin & System Permissions Manager)
// =================================================================

/**
 * Utility object for checking Android system runtime permissions,
 * AppOps usage statistics, and Device Administrator activation status.
 */
object PermissionUtils {

    // =================================================================
    // DEVICE ADMIN CHECK
    // =================================================================

    /**
     * Checks whether the application is currently granted Device Administrator rights.
     */
    fun isAdminActive(context: Context): Boolean {
        val devicePolicyManager = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        val adminComponent = ComponentName(context, AdminReceiver::class.java)
        return devicePolicyManager.isAdminActive(adminComponent)
    }

    // =================================================================
    // INDIVIDUAL PERMISSION CHECKS
    // =================================================================

    /**
     * Checks if runtime CAMERA permission is granted.
     */
    fun hasCameraPermission(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Checks if PACKAGE_USAGE_STATS permission is allowed via AppOpsManager.
     */
    fun hasUsageStatsPermission(context: Context): Boolean {
        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            appOps.unsafeCheckOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                Process.myUid(),
                context.packageName
            )
        } else {
            @Suppress("DEPRECATION")
            appOps.checkOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                Process.myUid(),
                context.packageName
            )
        }
        return mode == AppOpsManager.MODE_ALLOWED
    }

    /**
     * Checks POST_NOTIFICATIONS permission for Android 13+ (API level 33+).
     */
    fun hasNotificationPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    /**
     * Checks FOREGROUND_SERVICE permission for Android 9+ (API level 28+).
     */
    fun hasForegroundServicePermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.FOREGROUND_SERVICE
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    // =================================================================
    // AGGREGATED PERMISSION CHECKS
    // =================================================================

    /**
     * Verifies if all required runtime permissions and AppOps usage stats are fully granted.
     */
    fun hasAllPermissions(context: Context): Boolean {
        return getRequiredPermissions().all {
            ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        } && hasUsageStatsPermission(context)
    }

    /**
     * Dynamically builds the list of required manifest permissions based on Android API level.
     */
    fun getRequiredPermissions(): Array<String> {
        val permissions = mutableListOf(
            Manifest.permission.CAMERA
        )

        // Android 13 (Tiramisu) - API 33
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.POST_NOTIFICATIONS)
        }

        // Android 9 (Pie) - API 28
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            permissions.add(Manifest.permission.FOREGROUND_SERVICE)
        }

        // Android 14 (Upside Down Cake) - API 34+ Specific Foreground Types
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            permissions.add(Manifest.permission.FOREGROUND_SERVICE_CAMERA)
            permissions.add(Manifest.permission.FOREGROUND_SERVICE_SPECIAL_USE)
        }

        return permissions.toTypedArray()
    }
}