package com.example.data

import kotlinx.coroutines.flow.Flow

class DownloadRepository(private val downloadDao: DownloadDao) {
    val allDownloads: Flow<List<DownloadItem>> = downloadDao.getAllDownloads()

    fun searchDownloads(query: String): Flow<List<DownloadItem>> {
        return downloadDao.searchDownloads("%$query%")
    }

    suspend fun insertDownload(item: DownloadItem): Long {
        return downloadDao.insertDownload(item)
    }

    suspend fun deleteDownload(item: DownloadItem) {
        downloadDao.deleteDownload(item)
    }

    suspend fun deleteDownloadById(id: Long) {
        downloadDao.deleteDownloadById(id)
    }
}
