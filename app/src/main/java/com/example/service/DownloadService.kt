package com.example.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Environment
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.data.AppDatabase
import com.example.data.DownloadItem
import com.example.data.DownloadRepository
import kotlinx.coroutines.*
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

class DownloadService : Service() {

    private val serviceJob = Job()
    private val serviceScope = CoroutineScope(Dispatchers.IO + serviceJob)

    private val okHttpClient = OkHttpClient()

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val url = intent?.getStringExtra("download_url")
        val title = intent?.getStringExtra("title") ?: "OTD_Video_${System.currentTimeMillis()}"
        val thumbnailUrl = intent?.getStringExtra("thumbnail_url") ?: ""
        val duration = intent?.getIntExtra("duration", 0) ?: 0
        val resolution = intent?.getStringExtra("resolution") ?: "HD"

        if (url != null) {
            startForeground(NOTIFICATION_ID, createNotification(title, 0, "0 KB/s", "Starting..."))
            serviceScope.launch {
                executeDownload(url, title, thumbnailUrl, duration, resolution)
            }
        } else {
            stopSelf()
        }

        return START_NOT_STICKY
    }

    private suspend fun executeDownload(
        url: String,
        title: String,
        thumbnailUrl: String,
        duration: Int,
        resolution: String
    ) {
        val sanitizedTitle = title.replace(Regex("[\\\\/:*?\"<>|]"), "_")
        val fileName = "${sanitizedTitle}.mp4"

        // Fallback to internal storage if external downloads directory is unavailable
        val downloadFolder = try {
            val publicDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val otdDir = File(publicDir, "OTD")
            if (!otdDir.exists()) otdDir.mkdirs()
            otdDir
        } catch (e: Exception) {
            val fallback = File(getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), "OTD")
            if (!fallback.exists()) fallback.mkdirs()
            fallback
        }

        var targetFile = File(downloadFolder, fileName)
        if (targetFile.exists()) {
            val baseName = sanitizedTitle
            var counter = 1
            var uniqueFileName = "${baseName}_$counter.mp4"
            var uniqueFile = File(downloadFolder, uniqueFileName)
            while (uniqueFile.exists()) {
                counter++
                uniqueFileName = "${baseName}_$counter.mp4"
                uniqueFile = File(downloadFolder, uniqueFileName)
            }
            targetFile = uniqueFile
        }

        try {
            val request = Request.Builder().url(url).build()
            val response = okHttpClient.newCall(request).execute()

            if (!response.isSuccessful) {
                throw Exception("Server returned code ${response.code}")
            }

            val body = response.body ?: throw Exception("Response body is empty")
            val totalBytes = body.contentLength()
            val inputStream: InputStream = body.byteStream()
            val outputStream = FileOutputStream(targetFile)

            val buffer = ByteArray(8192)
            var bytesRead: Int
            var downloadedBytes: Long = 0
            val startTime = System.currentTimeMillis()
            var lastUpdateTime = startTime

            DownloadStateTracker.updateStatus(
                DownloadStateTracker.DownloadStatus.Downloading(
                    videoUrl = url,
                    title = title,
                    progress = 0,
                    speed = "0 KB/s",
                    remainingTime = "Calculating...",
                    downloadedBytes = 0,
                    totalBytes = totalBytes
                )
            )

            while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                outputStream.write(buffer, 0, bytesRead)
                downloadedBytes += bytesRead

                val currentTime = System.currentTimeMillis()
                if (currentTime - lastUpdateTime > 500) {
                    val progress = if (totalBytes > 0) ((downloadedBytes * 100) / totalBytes).toInt() else 0
                    val speedBytesPerSec = (downloadedBytes * 1000) / (currentTime - startTime + 1)
                    val speedText = formatSpeed(speedBytesPerSec)

                    val remainingTimeText = if (totalBytes > 0 && speedBytesPerSec > 0) {
                        val remainingBytes = totalBytes - downloadedBytes
                        val remainingSecs = remainingBytes / speedBytesPerSec
                        formatRemainingTime(remainingSecs)
                    } else {
                        "Unknown"
                    }

                    DownloadStateTracker.updateStatus(
                        DownloadStateTracker.DownloadStatus.Downloading(
                            videoUrl = url,
                            title = title,
                            progress = progress,
                            speed = speedText,
                            remainingTime = remainingTimeText,
                            downloadedBytes = downloadedBytes,
                            totalBytes = totalBytes
                        )
                    )

                    updateNotification(title, progress, speedText, remainingTimeText)
                    lastUpdateTime = currentTime
                }
            }

            outputStream.flush()
            outputStream.close()
            inputStream.close()

            // Insert into local history database
            val database = AppDatabase.getDatabase(this@DownloadService)
            val repository = DownloadRepository(database.downloadDao())
            repository.insertDownload(
                DownloadItem(
                    videoUrl = url,
                    title = title,
                    thumbnailUrl = thumbnailUrl,
                    localFilePath = targetFile.absolutePath,
                    duration = duration,
                    fileSize = targetFile.length(),
                    resolution = resolution
                )
            )

            DownloadStateTracker.updateStatus(
                DownloadStateTracker.DownloadStatus.Success(
                    localPath = targetFile.absolutePath,
                    title = title,
                    size = targetFile.length()
                )
            )

            showCompletionNotification(title)

        } catch (e: Exception) {
            e.printStackTrace()
            DownloadStateTracker.updateStatus(
                DownloadStateTracker.DownloadStatus.Failed(
                    e.message ?: "Unknown error"
                )
            )
            showFailureNotification(title, e.message ?: "Download failed")
        } finally {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                stopForeground(STOP_FOREGROUND_REMOVE)
            } else {
                @Suppress("DEPRECATION")
                stopForeground(true)
            }
            stopSelf()
        }
    }

    private fun formatSpeed(bytesPerSec: Long): String {
        return when {
            bytesPerSec >= 1024 * 1024 -> String.format("%.2f MB/s", bytesPerSec / (1024.0 * 1024.0))
            bytesPerSec >= 1024 -> String.format("%.1f KB/s", bytesPerSec / 1024.0)
            else -> "$bytesPerSec B/s"
        }
    }

    private fun formatRemainingTime(seconds: Long): String {
        return when {
            seconds >= 3600 -> String.format("%dh %dm %ds", seconds / 3600, (seconds % 3600) / 60, seconds % 60)
            seconds >= 60 -> String.format("%dm %ds", seconds / 60, seconds % 60)
            else -> "${seconds}s"
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "OTD Video Downloader",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows progress of downloading videos"
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(
        title: String,
        progress: Int,
        speed: String,
        remaining: String
    ): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Downloading: $title")
            .setContentText("Progress: $progress% | Speed: $speed | Remaining: $remaining")
            .setSmallIcon(android.R.drawable.stat_sys_download)
            .setProgress(100, progress, false)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .build()
    }

    private fun updateNotification(title: String, progress: Int, speed: String, remaining: String) {
        val notification = createNotification(title, progress, speed, remaining)
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(NOTIFICATION_ID, notification)
    }

    private fun showCompletionNotification(title: String) {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Download Completed")
            .setContentText("Successfully downloaded $title")
            .setSmallIcon(android.R.drawable.stat_sys_download_done)
            .setAutoCancel(true)
            .build()
        manager.notify(System.currentTimeMillis().toInt(), notification)
    }

    private fun showFailureNotification(title: String, error: String) {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Download Failed")
            .setContentText("Error downloading $title: $error")
            .setSmallIcon(android.R.drawable.stat_notify_error)
            .setAutoCancel(true)
            .build()
        manager.notify(System.currentTimeMillis().toInt(), notification)
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceJob.cancel()
    }

    companion object {
        private const val CHANNEL_ID = "otd_download_channel"
        private const val NOTIFICATION_ID = 4554
    }
}
