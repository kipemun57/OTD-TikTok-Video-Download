package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "download_history")
data class DownloadItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val videoUrl: String,
    val title: String,
    val thumbnailUrl: String,
    val localFilePath: String,
    val duration: Int, // in seconds
    val fileSize: Long, // in bytes
    val downloadDate: Long = System.currentTimeMillis(),
    val resolution: String
)
