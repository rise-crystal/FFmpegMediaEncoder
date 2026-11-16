package com.milkywayy.ffmpegmediaencoder.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import androidx.core.app.NotificationCompat
import com.milkywayy.ffmpegmediaencoder.MainActivity
import com.milkywayy.ffmpegmediaencoder.data.ConversionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class EncoderService : Service() {

    companion object {
        const val CHANNEL_ID = "ffmpeg_encoder_channel"
        const val NOTIFICATION_ID = 1001
        const val ACTION_START = "com.milkywayy.ffmpegmediaencoder.action.START"
        const val ACTION_CANCEL = "com.milkywayy.ffmpegmediaencoder.action.CANCEL"

        fun start(context: Context) {
            val intent = Intent(context, EncoderService::class.java).apply {
                action = ACTION_START
            }
            context.startForegroundService(intent)
        }

        fun stop(context: Context) {
            val intent = Intent(context, EncoderService::class.java).apply {
                action = ACTION_CANCEL
            }
            context.startService(intent)
        }
    }

    private val serviceScope = CoroutineScope(Dispatchers.Main + Job())
    private var wakeLock: PowerManager.WakeLock? = null
    private var conversionJob: Job? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_CANCEL -> {
                ConversionManager.cancelConversion()
                stopForegroundService()
                stopSelf()
                return START_NOT_STICKY
            }
            ACTION_START -> {
                startForegroundWithNotification()
                acquireWakeLock()
                startConversion()
            }
        }
        return START_NOT_STICKY
    }

    private fun startForegroundWithNotification() {
        val notification = buildNotification("Menyiapkan konversi...", 0, 0, 0)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC)
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    private fun acquireWakeLock() {
        if (wakeLock == null) {
            val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
            wakeLock = powerManager.newWakeLock(
                PowerManager.PARTIAL_WAKE_LOCK,
                "FFmpegMediaEncoder::ConversionWakeLock"
            ).apply {
                setReferenceCounted(false)
                acquire(2 * 60 * 60 * 1000L) // Safety timeout: 2 hours
            }
        }
    }

    private fun releaseWakeLock() {
        try {
            if (wakeLock?.isHeld == true) {
                wakeLock?.release()
            }
        } catch (_: Exception) {}
        wakeLock = null
    }

    private fun startConversion() {
        if (conversionJob?.isActive == true) return

        // Observe progress to update notification
        serviceScope.launch {
            ConversionManager.totalProgress.collectLatest { progress ->
                if (ConversionManager.isConverting.value) {
                    val current = ConversionManager.currentFileIndex.value
                    val total = ConversionManager.totalFiles.value
                    val currentFile = ConversionManager.queue.value.getOrNull(current - 1)?.inputFileName ?: ""
                    updateNotification(currentFile, progress, current, total)
                }
            }
        }

        conversionJob = serviceScope.launch(Dispatchers.IO) {
            try {
                ConversionManager.runQueue(applicationContext)
            } finally {
                stopForegroundService()
                releaseWakeLock()
                stopSelf()
            }
        }
    }

    private fun updateNotification(fileName: String, progress: Int, current: Int, total: Int) {
        val notification = buildNotification(fileName, progress, current, total)
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(NOTIFICATION_ID, notification)
    }

    private fun buildNotification(fileName: String, progress: Int, current: Int, total: Int): Notification {
        val openAppIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pendingOpenApp = PendingIntent.getActivity(
            this, 0, openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val cancelIntent = Intent(this, EncoderService::class.java).apply {
            action = ACTION_CANCEL
        }
        val pendingCancel = PendingIntent.getService(
            this, 1, cancelIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val title = if (total > 0) "Mengonversi Media ($current/$total)" else "Mengonversi Media..."
        val contentText = if (fileName.isNotEmpty()) "$fileName ($progress%)" else "Memproses..."

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(contentText)
            .setSmallIcon(android.R.drawable.stat_sys_upload)
            .setProgress(100, progress, progress == 0)
            .setContentIntent(pendingOpenApp)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Batalkan", pendingCancel)
            .build()
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "FFmpeg Media Encoder",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Notifikasi proses konversi video & audio"
            setShowBadge(false)
        }
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(channel)
    }

    private fun stopForegroundService() {
        stopForeground(STOP_FOREGROUND_REMOVE)
    }

    override fun onDestroy() {
        super.onDestroy()
        releaseWakeLock()
        serviceScope.cancel()
    }
}
