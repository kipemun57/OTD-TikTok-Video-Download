package com.example.service

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

object DownloadStateTracker {
    sealed class DownloadStatus {
        object Idle : DownloadStatus()
        data class Downloading(
            val videoUrl: String,
            val title: String,
            val progress: Int, // 0 to 100
            val speed: String, // e.g. "1.5 MB/s"
            val remainingTime: String, // e.g. "12s"
            val downloadedBytes: Long,
            val totalBytes: Long
        ) : DownloadStatus()
        data class Success(val localPath: String, val title: String, val size: Long) : DownloadStatus()
        data class Failed(val error: String) : DownloadStatus()
    }

    private val _downloadStatus = MutableStateFlow<DownloadStatus>(DownloadStatus.Idle)
    val downloadStatus: StateFlow<DownloadStatus> = _downloadStatus

    fun updateStatus(status: DownloadStatus) {
        _downloadStatus.value = status
    }
}
