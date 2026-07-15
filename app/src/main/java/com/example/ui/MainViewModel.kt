package com.example.ui

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.DownloadItem
import com.example.data.DownloadRepository
import com.example.network.RetrofitClient
import com.example.network.TikWmVideoData
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File

@OptIn(FlowPreview::class, kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val sharedPrefs = application.getSharedPreferences("otd_prefs", Context.MODE_PRIVATE)

    private val repository: DownloadRepository

    init {
        val database = AppDatabase.getDatabase(application)
        repository = DownloadRepository(database.downloadDao())
    }

    // Settings States
    private val _themeMode = MutableStateFlow(sharedPrefs.getString("theme_mode", "auto") ?: "auto")
    val themeMode: StateFlow<String> = _themeMode

    private val _language = MutableStateFlow(
        L10n.Language.values().firstOrNull { it.code == sharedPrefs.getString("language_code", "en") }
            ?: L10n.Language.ENGLISH
    )
    val language: StateFlow<L10n.Language> = _language

    private val _notificationsEnabled = MutableStateFlow(sharedPrefs.getBoolean("notifications_enabled", true))
    val notificationsEnabled: StateFlow<Boolean> = _notificationsEnabled

    private val _autoSaveEnabled = MutableStateFlow(sharedPrefs.getBoolean("auto_save_enabled", true))
    val autoSaveEnabled: StateFlow<Boolean> = _autoSaveEnabled

    private val _downloadLocation = MutableStateFlow(sharedPrefs.getString("download_location", "OTD Folder") ?: "OTD Folder")
    val downloadLocation: StateFlow<String> = _downloadLocation

    // Download History Lists
    val allDownloads: StateFlow<List<DownloadItem>> = repository.allDownloads
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    val searchResults: StateFlow<List<DownloadItem>> = _searchQuery
        .debounce(300)
        .flatMapLatest { query ->
            if (query.isEmpty()) {
                repository.allDownloads
            } else {
                repository.searchDownloads(query)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Video Analysis state
    sealed class AnalysisState {
        object Idle : AnalysisState()
        object Loading : AnalysisState()
        data class Success(val videoData: TikWmVideoData) : AnalysisState()
        data class Error(val message: String) : AnalysisState()
    }

    private val _analysisState = MutableStateFlow<AnalysisState>(AnalysisState.Idle)
    val analysisState: StateFlow<AnalysisState> = _analysisState

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setThemeMode(mode: String) {
        _themeMode.value = mode
        sharedPrefs.edit().putString("theme_mode", mode).apply()
    }

    fun setLanguage(lang: L10n.Language) {
        _language.value = lang
        sharedPrefs.edit().putString("language_code", lang.code).apply()
    }

    fun toggleNotifications(enabled: Boolean) {
        _notificationsEnabled.value = enabled
        sharedPrefs.edit().putBoolean("notifications_enabled", enabled).apply()
    }

    fun toggleAutoSave(enabled: Boolean) {
        _autoSaveEnabled.value = enabled
        sharedPrefs.edit().putBoolean("auto_save_enabled", enabled).apply()
    }

    fun setDownloadLocation(location: String) {
        _downloadLocation.value = location
        sharedPrefs.edit().putString("download_location", location).apply()
    }

    fun clearCache(context: Context) {
        viewModelScope.launch {
            try {
                context.cacheDir.deleteRecursively()
                context.externalCacheDir?.deleteRecursively()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // TikTok URL Analysis
    fun analyzeTikTokUrl(url: String) {
        if (url.isBlank() || !url.contains("tiktok.com")) {
            _analysisState.value = AnalysisState.Error("Please enter a valid TikTok URL.")
            return
        }

        _analysisState.value = AnalysisState.Loading

        viewModelScope.launch {
            try {
                val response = RetrofitClient.tikWmService.analyzeVideo(url)
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null && body.code == 0 && body.data != null) {
                        _analysisState.value = AnalysisState.Success(body.data)
                    } else {
                        val errMsg = body?.msg ?: "Video private or not found."
                        _analysisState.value = AnalysisState.Error(errMsg)
                    }
                } else {
                    _analysisState.value = AnalysisState.Error("Server returned code ${response.code()}")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _analysisState.value = AnalysisState.Error("Network Error. Please try again.")
            }
        }
    }

    fun clearAnalysis() {
        _analysisState.value = AnalysisState.Idle
    }

    fun deleteDownload(item: DownloadItem) {
        viewModelScope.launch {
            try {
                val file = File(item.localFilePath)
                if (file.exists()) {
                    file.delete()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            repository.deleteDownload(item)
        }
    }
}
