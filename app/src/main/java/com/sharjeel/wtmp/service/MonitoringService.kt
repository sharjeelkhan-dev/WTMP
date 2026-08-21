package com.sharjeel.wtmp.service

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.admin.DeviceAdminReceiver
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ServiceInfo
import android.graphics.ImageFormat
import android.hardware.camera2.*
import android.media.ImageReader
import android.media.RingtoneManager
import android.os.Build
import android.os.Handler
import android.os.HandlerThread
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import com.sharjeel.wtmp.domain.repository.SecurityRepository
import com.sharjeel.wtmp.model.AppUsageInfo
import com.sharjeel.wtmp.model.EventSeverity
import com.sharjeel.wtmp.model.SecurityEvent
import com.sharjeel.wtmp.model.SecurityEventType
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Locale
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

@AndroidEntryPoint
class MonitoringService : LifecycleService() {

    @Inject
    lateinit var repository: SecurityRepository

    private var isReceiverRegistered = false
    @Volatile private var isProcessingEvent = false

    private val securityReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            lifecycleScope.launch {
                val isActive = repository.isProtectionActive.first()
                if (!isActive) {
                    Log.d(TAG, "Protection is OFF. Ignoring event: ${intent.action}")
                    return@launch
                }

                when (intent.action) {
                    Intent.ACTION_USER_PRESENT -> {
                        Log.d(TAG, "Device unlocked successfully.")
                        handleSecurityEvent(isFailedAttempt = false)
                    }
                    ACTION_FAILED_UNLOCK -> {
                        Log.d(TAG, "Failed unlock attempt captured via broadcast.")
                        showFailedAttemptNotification()
                        handleSecurityEvent(isFailedAttempt = true)
                    }
                }
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        startForegroundService()
        registerSecurityReceivers()

        if (intent?.action == ACTION_FAILED_UNLOCK) {
            lifecycleScope.launch {
                if (repository.isProtectionActive.first()) {
                    showFailedAttemptNotification()
                    handleSecurityEvent(isFailedAttempt = true)
                }
            }
        } else if (intent?.action == ACTION_START_IF_ACTIVE) {
            lifecycleScope.launch {
                val isActive = repository.isProtectionActive.first()
                if (!isActive) {
                    stopForeground(STOP_FOREGROUND_REMOVE)
                    stopSelf()
                }
            }
        }

        observeProtectionStatus()
        return START_STICKY
    }

    private fun observeProtectionStatus() {
        lifecycleScope.launch {
            repository.isProtectionActive.collect { isActive ->
                if (!isActive) {
                    unregisterSecurityReceivers()
                    stopForeground(STOP_FOREGROUND_REMOVE)
                    stopSelf()
                }
            }
        }
    }

    private fun registerSecurityReceivers() {
        if (!isReceiverRegistered) {
            val filter = IntentFilter().apply {
                addAction(Intent.ACTION_USER_PRESENT)
                addAction(ACTION_FAILED_UNLOCK)
            }

            ContextCompat.registerReceiver(
                this,
                securityReceiver,
                filter,
                ContextCompat.RECEIVER_EXPORTED
            )

            isReceiverRegistered = true
            Log.d(TAG, "Security receivers dynamically registered.")
        }
    }

    private fun unregisterSecurityReceivers() {
        if (isReceiverRegistered) {
            try {
                unregisterReceiver(securityReceiver)
            } catch (e: Exception) {
                Log.e(TAG, "Receiver unregister error", e)
            }
            isReceiverRegistered = false
        }
    }

    private fun startForegroundService() {
        val notification = createServiceNotification()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            try {
                startForeground(
                    NOTIFICATION_ID,
                    notification,
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE or ServiceInfo.FOREGROUND_SERVICE_TYPE_CAMERA
                )
            } catch (_: Exception) {
                try {
                    startForeground(
                        NOTIFICATION_ID,
                        notification,
                        ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
                    )
                } catch (_: Exception) {
                    startForeground(NOTIFICATION_ID, notification)
                }
            }
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    private fun createNotificationChannels() {
        val notificationManager = getSystemService(NotificationManager::class.java)

        val serviceChannel = NotificationChannel(
            CHANNEL_ID,
            "WTMP Protection Service",
            NotificationManager.IMPORTANCE_LOW
        )

        val alertChannel = NotificationChannel(
            ALERT_CHANNEL_ID,
            "WTMP Security Alerts",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "High priority notifications for failed unlock attempts"
            enableVibration(true)
        }

        notificationManager?.createNotificationChannel(serviceChannel)
        notificationManager?.createNotificationChannel(alertChannel)
    }

    private fun createServiceNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("WTMP Security Monitoring Active")
            .setContentText("Listening for device unlock events...")
            .setSmallIcon(com.sharjeel.wtmp.R.drawable.webcam_icon)
            .setLargeIcon(android.graphics.BitmapFactory.decodeResource(resources, com.sharjeel.wtmp.R.drawable.webcam_icon))
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .build()
    }

    private fun showFailedAttemptNotification() {
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        val launchIntent = packageManager.getLaunchIntentForPackage(packageName)?.apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            launchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val alertNotification = NotificationCompat.Builder(this, ALERT_CHANNEL_ID)
            .setContentTitle("Failed Unlock Attempt Detected")
            .setContentText("Another failed Attempt to unlock the Device")
            .setSmallIcon(com.sharjeel.wtmp.R.drawable.webcam_icon)
            .setLargeIcon(android.graphics.BitmapFactory.decodeResource(resources, com.sharjeel.wtmp.R.drawable.webcam_icon))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify((System.currentTimeMillis() % 10000).toInt(), alertNotification)
    }

    private fun handleSecurityEvent(isFailedAttempt: Boolean) {
        if (isProcessingEvent) return
        isProcessingEvent = true

        lifecycleScope.launch {
            val eventStartTime = System.currentTimeMillis()
            try {
                delay(300.milliseconds)

                var evidencePath: String? = null
                var retries = 2

                while (retries > 0 && evidencePath == null) {
                    evidencePath = capturePhotoInternal()
                    if (evidencePath == null) {
                        retries--
                        delay(400.milliseconds)
                    }
                }

                val eventType = if (isFailedAttempt) {
                    SecurityEventType.FAILED_UNLOCK
                } else {
                    SecurityEventType.DEVICE_UNLOCKED
                }

                val initialEvent = SecurityEvent(
                    type = eventType,
                    timestamp = eventStartTime,
                    severity = if (isFailedAttempt) EventSeverity.HIGH else EventSeverity.MEDIUM,
                    evidencePath = evidencePath,
                    deviceState = if (isFailedAttempt) "Unlock Failed" else "Device Unlocked",
                    accessedApps = emptyList()
                )

                withContext(Dispatchers.IO) {
                    repository.saveEvent(initialEvent)
                    Log.d(TAG, "Initial security event saved: $initialEvent")

                    if (repository.isAlarmEnabled.first()) {
                        playAlarm()
                    }
                    if (repository.isVibrationEnabled.first()) {
                        vibrate()
                    }
                }

                // Wait and update apps launched after unlock
                if (!isFailedAttempt) {
                    lifecycleScope.launch(Dispatchers.IO) {
                        delay(25.seconds)
                        val apps = getLaunchedAppsAfterUnlock(eventStartTime)
                        if (apps.isNotEmpty()) {
                            val updatedEvent = initialEvent.copy(accessedApps = apps)
                            repository.saveEvent(updatedEvent)
                            Log.d(TAG, "Updated event with ${apps.size} apps")
                        }
                    }
                }

            } catch (e: Exception) {
                Log.e(TAG, "Error executing security event processing", e)
            } finally {
                isProcessingEvent = false
            }
        }
    }

    private suspend fun getLaunchedAppsAfterUnlock(startTime: Long): List<AppUsageInfo> = withContext(Dispatchers.IO) {
        val usageStatsManager = getSystemService(USAGE_STATS_SERVICE) as? UsageStatsManager
            ?: return@withContext emptyList()

        val endTime = System.currentTimeMillis()
        Log.d(TAG, "Querying usage events from $startTime to $endTime")
        val usageEvents = usageStatsManager.queryEvents(startTime, endTime)
        val appList = mutableListOf<AppUsageInfo>()

        val event = UsageEvents.Event()
        while (usageEvents.hasNextEvent()) {
            usageEvents.getNextEvent(event)
            if (event.eventType == UsageEvents.Event.ACTIVITY_RESUMED) {
                val pkgName = event.packageName
                if (pkgName != packageName && !pkgName.contains("launcher", ignoreCase = true)) {
                    try {
                        val appInfo = packageManager.getApplicationInfo(pkgName, 0)
                        val appName = packageManager.getApplicationLabel(appInfo).toString()
                        appList.add(
                            AppUsageInfo(
                                packageName = pkgName,
                                appName = appName,
                                launchedTimestamp = event.timeStamp
                            )
                        )
                    } catch (e: Exception) {
                        Log.e(TAG, "Could not fetch app label for $pkgName", e)
                    }
                }
            }
        }
        return@withContext appList.distinctBy { it.packageName }
    }

    @SuppressLint("MissingPermission")
    private suspend fun capturePhotoInternal(): String? = suspendCancellableCoroutine { continuation ->
        try {
            val cameraManager = getSystemService(CAMERA_SERVICE) as CameraManager
            val cameraId = cameraManager.cameraIdList.firstOrNull { id ->
                val characteristics = cameraManager.getCameraCharacteristics(id)
                characteristics.get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_FRONT
            } ?: cameraManager.cameraIdList.firstOrNull()

            if (cameraId == null) {
                Log.e(TAG, "No camera found on device")
                if (continuation.isActive) continuation.resume(null)
                return@suspendCancellableCoroutine
            }

            val backgroundThread = HandlerThread("CameraBackgroundThread_${System.currentTimeMillis()}").apply { start() }
            val backgroundHandler = Handler(backgroundThread.looper)

            val imageReader = ImageReader.newInstance(640, 480, ImageFormat.JPEG, 1)

            val mediaDir = getExternalFilesDir(null)?.let {
                File(it, "WTMP_Captures").apply { mkdirs() }
            }
            val outputDirectory = if (mediaDir != null && mediaDir.exists()) mediaDir else filesDir
            val photoFile = File(
                outputDirectory,
                "IMG_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(System.currentTimeMillis())}.jpg"
            )

            imageReader.setOnImageAvailableListener({ reader ->
                var image: android.media.Image? = null
                try {
                    image = reader.acquireLatestImage()
                    if (image != null) {
                        val buffer = image.planes[0].buffer
                        val bytes = ByteArray(buffer.remaining())
                        buffer.get(bytes)
                        FileOutputStream(photoFile).use { it.write(bytes) }
                        Log.d(TAG, "Camera2 photo saved successfully at: ${photoFile.absolutePath}")
                        if (continuation.isActive) continuation.resume(photoFile.absolutePath)
                    } else {
                        if (continuation.isActive) continuation.resume(null)
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to save image", e)
                    if (continuation.isActive) continuation.resume(null)
                } finally {
                    image?.close()
                    imageReader.close()
                    backgroundThread.quitSafely()
                }
            }, backgroundHandler)

            cameraManager.openCamera(cameraId, object : CameraDevice.StateCallback() {
                override fun onOpened(camera: CameraDevice) {
                    try {
                        val captureBuilder = camera.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE).apply {
                            addTarget(imageReader.surface)
                            set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE)
                            set(CaptureRequest.JPEG_ORIENTATION, 270)
                        }

                        @Suppress("DEPRECATION")
                        camera.createCaptureSession(
                            listOf(imageReader.surface),
                            object : CameraCaptureSession.StateCallback() {
                                override fun onConfigureFailed(session: CameraCaptureSession) {
                                    camera.close()
                                    imageReader.close()
                                    if (continuation.isActive) continuation.resume(null)
                                    backgroundThread.quitSafely()
                                }

                                override fun onConfigured(session: CameraCaptureSession) {
                                    try {
                                        session.capture(captureBuilder.build(), object : CameraCaptureSession.CaptureCallback() {
                                            override fun onCaptureCompleted(
                                                session: CameraCaptureSession,
                                                request: CaptureRequest,
                                                result: TotalCaptureResult
                                            ) {
                                                super.onCaptureCompleted(session, request, result)
                                                camera.close()
                                            }
                                        }, backgroundHandler)
                                    } catch (e: Exception) {
                                        Log.e(TAG, "Capture session execution failed", e)
                                        camera.close()
                                        imageReader.close()
                                        if (continuation.isActive) continuation.resume(null)
                                        backgroundThread.quitSafely()
                                    }
                                }
                            },
                            backgroundHandler
                        )
                    } catch (e: Exception) {
                        Log.e(TAG, "Session creation failed", e)
                        camera.close()
                        imageReader.close()
                        if (continuation.isActive) continuation.resume(null)
                        backgroundThread.quitSafely()
                    }
                }

                override fun onDisconnected(camera: CameraDevice) {
                    camera.close()
                    imageReader.close()
                    if (continuation.isActive) continuation.resume(null)
                    backgroundThread.quitSafely()
                }

                override fun onError(camera: CameraDevice, error: Int) {
                    camera.close()
                    imageReader.close()
                    Log.e(TAG, "Camera device open error: $error")
                    if (continuation.isActive) continuation.resume(null)
                    backgroundThread.quitSafely()
                }
            }, backgroundHandler)

        } catch (e: Exception) {
            Log.e(TAG, "Fatal Camera2 background exception", e)
            if (continuation.isActive) continuation.resume(null)
        }
    }

    private fun playAlarm() {
        try {
            val notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val r = RingtoneManager.getRingtone(applicationContext, notification)
            r.play()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to play alarm", e)
        }
    }

    private fun vibrate() {
        try {
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = getSystemService(VIBRATOR_MANAGER_SERVICE) as VibratorManager
                vibratorManager.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                getSystemService(VIBRATOR_SERVICE) as Vibrator
            }

            vibrator.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE))
        } catch (e: Exception) {
            Log.e(TAG, "Vibration failed", e)
        }
    }

    override fun onDestroy() {
        unregisterSecurityReceivers()
        super.onDestroy()
    }

    companion object {
        private const val TAG = "MonitoringService"
        private const val CHANNEL_ID = "monitoring_channel"
        private const val ALERT_CHANNEL_ID = "wtmp_alert_channel"
        private const val NOTIFICATION_ID = 101

        const val ACTION_START_IF_ACTIVE = "com.sharjeel.wtmp.action.START_IF_ACTIVE"
        const val ACTION_FAILED_UNLOCK = "com.sharjeel.wtmp.action.FAILED_UNLOCK"
    }
}

class AdminReceiver : DeviceAdminReceiver() {
    @Deprecated("Deprecated in Java")
    override fun onPasswordFailed(context: Context, intent: Intent) {
        super.onPasswordFailed(context, intent)
        Log.d("AdminReceiver", "Device Admin detected failed password attempt")

        val broadcastIntent = Intent(MonitoringService.ACTION_FAILED_UNLOCK).apply {
            setPackage(context.packageName)
        }
        context.sendBroadcast(broadcastIntent)

        val serviceIntent = Intent(context, MonitoringService::class.java).apply {
            action = MonitoringService.ACTION_FAILED_UNLOCK
        }
        try {
            ContextCompat.startForegroundService(context, serviceIntent)
        } catch (e: Exception) {
            Log.e("AdminReceiver", "Failed starting foreground service", e)
        }
    }
}