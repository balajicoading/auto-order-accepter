package com.autoaccept.app

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Path
import android.graphics.PixelFormat
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.ImageReader
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.util.DisplayMetrics
import android.view.accessibility.AccessibilityEvent
import android.view.WindowManager
import kotlinx.coroutines.*

class OrderAccessibilityService : AccessibilityService() {

    private var loopJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    private var mediaProjection: MediaProjection? = null
    private var virtualDisplay: VirtualDisplay? = null
    private var imageReader: ImageReader? = null

    companion object {
        var instance: OrderAccessibilityService? = null
        var projectionResultCode: Int? = null
        var projectionData: Intent? = null
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
        startForegroundService()
        checkAndStartLoop()
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
    }

    override fun onInterrupt() {
    }

    override fun onDestroy() {
        loopJob?.cancel()
        scope.cancel()
        stopForegroundService()
        cleanupMediaProjection()
        instance = null
        super.onDestroy()
    }

    private fun startForegroundService() {
        val intent = Intent(this, KeepAliveService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
    }

    private fun stopForegroundService() {
        val intent = Intent(this, KeepAliveService::class.java)
        stopService(intent)
    }

    private fun checkAndStartLoop() {
        if (Prefs.isEnabled(this)) {
            startLoop()
        } else {
            stopLoop()
        }
    }

    private fun startLoop() {
        loopJob?.cancel()
        loopJob = scope.launch {
            while (isActive) {
                try {
                    processScreenshot()
                } catch (e: Exception) {
                }
                delay(900)
            }
        }
    }

    private fun stopLoop() {
        loopJob?.cancel()
        loopJob = null
    }

    fun refreshLoop() {
        checkAndStartLoop()
    }

    private suspend fun processScreenshot() {
        val bitmap = captureScreenshot() ?: return

        val distances = OcrUtils.readDistances(bitmap)
        bitmap.recycle()

        if (distances == null) return

        val (pickup, drop) = distances
        val total = pickup + drop
        val threshold = Prefs.getThreshold(this)

        withContext(Dispatchers.Main) {
            if (total <= threshold) {
                performSwipe(
                    Prefs.getStartX(this@OrderAccessibilityService),
                    Prefs.getStartY(this@OrderAccessibilityService),
                    Prefs.getEndX(this@OrderAccessibilityService),
                    Prefs.getEndY(this@OrderAccessibilityService),
                    Prefs.getDuration(this@OrderAccessibilityService)
                )
            } else {
                performTap(
                    Prefs.getCloseX(this@OrderAccessibilityService),
                    Prefs.getCloseY(this@OrderAccessibilityService)
                )
            }
        }
    }

    private suspend fun captureScreenshot(): Bitmap? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            captureScreenshotAndroid13Plus()
        } else {
            captureScreenshotAndroid7to12()
        }
    }

    private suspend fun captureScreenshotAndroid13Plus(): Bitmap? {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) return null

        return suspendCancellableCoroutine { continuation ->
            takeScreenshot(
                android.view.Display.DEFAULT_DISPLAY,
                application.mainExecutor,
                object : TakeScreenshotCallback {
                    override fun onSuccess(screenshot: ScreenshotResult) {
                        val bitmap = Bitmap.wrapHardwareBuffer(
                            screenshot.hardwareBuffer,
                            screenshot.colorSpace
                        )?.copy(Bitmap.Config.ARGB_8888, false)
                        screenshot.hardwareBuffer.close()
                        continuation.resume(bitmap) {}
                    }

                    override fun onFailure(errorCode: Int) {
                        continuation.resume(null) {}
                    }
                }
            )
        }
    }

    private fun captureScreenshotAndroid7to12(): Bitmap? {
        if (projectionResultCode == null || projectionData == null) {
            return null
        }

        try {
            if (mediaProjection == null) {
                val projectionManager = getSystemService(MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
                mediaProjection = projectionManager.getMediaProjection(projectionResultCode!!, projectionData!!)
            }

            if (imageReader == null) {
                val wm = getSystemService(WINDOW_SERVICE) as WindowManager
                val metrics = DisplayMetrics()
                wm.defaultDisplay.getMetrics(metrics)

                imageReader = ImageReader.newInstance(
                    metrics.widthPixels,
                    metrics.heightPixels,
                    PixelFormat.RGBA_8888,
                    2
                )

                virtualDisplay = mediaProjection?.createVirtualDisplay(
                    "AutoAccept",
                    metrics.widthPixels,
                    metrics.heightPixels,
                    metrics.densityDpi,
                    DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                    imageReader?.surface,
                    null,
                    null
                )
            }

            val image = imageReader?.acquireLatestImage()
            if (image != null) {
                val planes = image.planes
                val buffer = planes[0].buffer
                val pixelStride = planes[0].pixelStride
                val rowStride = planes[0].rowStride
                val rowPadding = rowStride - pixelStride * image.width

                val bitmap = Bitmap.createBitmap(
                    image.width + rowPadding / pixelStride,
                    image.height,
                    Bitmap.Config.ARGB_8888
                )
                bitmap.copyPixelsFromBuffer(buffer)
                image.close()
                return bitmap
            }
        } catch (e: Exception) {
        }

        return null
    }

    private fun cleanupMediaProjection() {
        virtualDisplay?.release()
        imageReader?.close()
        mediaProjection?.stop()
        virtualDisplay = null
        imageReader = null
        mediaProjection = null
    }

    fun performSwipe(startX: Int, startY: Int, endX: Int, endY: Int, durationMs: Long) {
        val path = Path().apply {
            moveTo(startX.toFloat(), startY.toFloat())
            lineTo(endX.toFloat(), endY.toFloat())
        }

        val gestureBuilder = GestureDescription.Builder()
        gestureBuilder.addStroke(
            GestureDescription.StrokeDescription(path, 0, durationMs)
        )

        dispatchGesture(gestureBuilder.build(), null, null)
    }

    fun performTap(x: Int, y: Int) {
        val path = Path().apply {
            moveTo(x.toFloat(), y.toFloat())
        }

        val gestureBuilder = GestureDescription.Builder()
        gestureBuilder.addStroke(
            GestureDescription.StrokeDescription(path, 0, 1)
        )

        dispatchGesture(gestureBuilder.build(), null, null)
    }
}
