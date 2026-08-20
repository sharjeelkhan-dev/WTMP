package com.sharjeel.wtmp.utils

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import kotlin.coroutines.resume

class CameraHelper(private val context: Context) {

    private var imageCapture: ImageCapture? = null

    suspend fun capturePhoto(lifecycleOwner: LifecycleOwner): String? = withContext(Dispatchers.Main) {
        if (!PermissionUtils.hasCameraPermission(context)) {
            Log.e(TAG, "Camera permission missing")
            return@withContext null
        }

        val cameraProvider = getCameraProvider() ?: return@withContext null
        
        imageCapture = ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
            .build()

        val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA

        try {
            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(
                lifecycleOwner,
                cameraSelector,
                imageCapture
            )
        } catch (exc: Exception) {
            Log.e(TAG, "Use case binding failed", exc)
            return@withContext null
        }

        takePicture()
    }

    private suspend fun getCameraProvider(): ProcessCameraProvider? = suspendCancellableCoroutine { continuation ->
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener({
            try {
                continuation.resume(cameraProviderFuture.get())
            } catch (e: Exception) {
                Log.e(TAG, "Error getting camera provider", e)
                continuation.resume(null)
            }
        }, ContextCompat.getMainExecutor(context))
    }

    private suspend fun takePicture(): String? = suspendCancellableCoroutine { continuation ->
        val imageCapture = imageCapture ?: run {
            continuation.resume(null)
            return@suspendCancellableCoroutine
        }

        val evidenceDir = File(context.filesDir, "evidence")
        if (!evidenceDir.exists()) {
            evidenceDir.mkdirs()
        }

        val name = SimpleDateFormat(FILENAME_FORMAT, Locale.US)
            .format(System.currentTimeMillis())
        val photoFile = File(evidenceDir, "$name.jpg")

        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(context),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    Log.e(TAG, "Photo capture failed: ${exc.message}", exc)
                    continuation.resume(null)
                }

                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    val savedUri = Uri.fromFile(photoFile)
                    Log.d(TAG, "Photo capture succeeded: $savedUri")
                    continuation.resume(photoFile.absolutePath)
                }
            }
        )
    }

    companion object {
        private const val TAG = "CameraHelper"
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
    }
}
