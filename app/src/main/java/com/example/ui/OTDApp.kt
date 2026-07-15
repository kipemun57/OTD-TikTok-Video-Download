package com.example.ui

import android.app.Activity
import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.viewinterop.AndroidView
import android.widget.VideoView
import android.widget.MediaController
import coil.compose.AsyncImage
import com.example.ads.AdManager
import com.example.ads.BannerAdView
import com.example.data.DownloadItem
import com.example.network.TikWmVideoData
import com.example.service.DownloadStateTracker
import com.example.service.DownloadService
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File

enum class Screen {
    SPLASH, HOME, DOWNLOADS, FEEDBACK, PRIVACY, LANGUAGE, SETTINGS, ABOUT
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OTDApp(viewModel: MainViewModel) {
    val context = LocalContext.current
    val activity = context as? Activity
    val coroutineScope = rememberCoroutineScope()

    // Preferences & Settings from ViewModel
    val currentThemeMode by viewModel.themeMode.collectAsStateWithLifecycle()
    val currentLanguage by viewModel.language.collectAsStateWithLifecycle()
    val searchResults by viewModel.searchResults.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()

    var currentScreen by remember { mutableStateOf(Screen.SPLASH) }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    // Download state observation
    val activeDownloadStatus by DownloadStateTracker.downloadStatus.collectAsStateWithLifecycle()

    // Modal Bottom Sheet control for download options
    var showBottomSheet by remember { mutableStateOf(false) }
    var selectedVideoDataForSheet by remember { mutableStateOf<TikWmVideoData?>(null) }

    // Video preview dialog control
    var showPreviewDialogUrl by remember { mutableStateOf<String?>(null) }

    // Splash screen timer
    if (currentScreen == Screen.SPLASH) {
        LaunchedEffect(Unit) {
            delay(2000)
            // Trigger App Open Ad before showing Home
            if (activity != null) {
                AdManager.showAppOpenAd(activity) {
                    currentScreen = Screen.HOME
                }
            } else {
                currentScreen = Screen.HOME
            }
        }
    }

    // App background/resume observer for App Open Ads (4+ hour frequency cap)
    LaunchedEffect(currentScreen) {
        if (currentScreen != Screen.SPLASH && activity != null) {
            if (AdManager.shouldShowAppOpenOnResume()) {
                AdManager.showAppOpenAd(activity)
            }
        }
    }

    if (currentScreen == Screen.SPLASH) {
        SplashScreen()
    } else {
        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                ModalDrawerSheet(
                    modifier = Modifier.width(300.dp),
                    drawerContainerColor = MaterialTheme.colorScheme.surface
                ) {
                    DrawerHeader(currentLanguage)
                    Spacer(modifier = Modifier.height(12.dp))

                    DrawerNavigationItem(
                        label = L10n.getString("home", currentLanguage),
                        icon = Icons.Default.Home,
                        isSelected = currentScreen == Screen.HOME,
                        onClick = {
                            currentScreen = Screen.HOME
                            coroutineScope.launch { drawerState.close() }
                        }
                    )

                    DrawerNavigationItem(
                        label = L10n.getString("downloads", currentLanguage),
                        icon = Icons.Default.Download,
                        isSelected = currentScreen == Screen.DOWNLOADS,
                        onClick = {
                            // Show ad check on navigating to Downloads
                            if (activity != null) {
                                AdManager.showDownloadsPageInterstitial(activity) {
                                    currentScreen = Screen.DOWNLOADS
                                    coroutineScope.launch { drawerState.close() }
                                }
                            } else {
                                currentScreen = Screen.DOWNLOADS
                                coroutineScope.launch { drawerState.close() }
                            }
                        }
                    )

                    DrawerNavigationItem(
                        label = L10n.getString("feedback", currentLanguage),
                        icon = Icons.Default.Feedback,
                        isSelected = currentScreen == Screen.FEEDBACK,
                        onClick = {
                            currentScreen = Screen.FEEDBACK
                            coroutineScope.launch { drawerState.close() }
                        }
                    )

                    DrawerNavigationItem(
                        label = L10n.getString("privacy_policy", currentLanguage),
                        icon = Icons.Default.Security,
                        isSelected = currentScreen == Screen.PRIVACY,
                        onClick = {
                            currentScreen = Screen.PRIVACY
                            coroutineScope.launch { drawerState.close() }
                        }
                    )

                    DrawerNavigationItem(
                        label = L10n.getString("language", currentLanguage),
                        icon = Icons.Default.Language,
                        isSelected = currentScreen == Screen.LANGUAGE,
                        onClick = {
                            currentScreen = Screen.LANGUAGE
                            coroutineScope.launch { drawerState.close() }
                        }
                    )

                    DrawerNavigationItem(
                        label = L10n.getString("settings", currentLanguage),
                        icon = Icons.Default.Settings,
                        isSelected = currentScreen == Screen.SETTINGS,
                        onClick = {
                            currentScreen = Screen.SETTINGS
                            coroutineScope.launch { drawerState.close() }
                        }
                    )

                    DrawerNavigationItem(
                        label = L10n.getString("rate_app", currentLanguage),
                        icon = Icons.Default.Star,
                        isSelected = false,
                        onClick = {
                            Utils.rateApp(context)
                            coroutineScope.launch { drawerState.close() }
                        }
                    )

                    DrawerNavigationItem(
                        label = L10n.getString("share_app", currentLanguage),
                        icon = Icons.Default.Share,
                        isSelected = false,
                        onClick = {
                            Utils.shareAppLink(context)
                            coroutineScope.launch { drawerState.close() }
                        }
                    )

                    DrawerNavigationItem(
                        label = L10n.getString("about", currentLanguage),
                        icon = Icons.Default.Info,
                        isSelected = currentScreen == Screen.ABOUT,
                        onClick = {
                            currentScreen = Screen.ABOUT
                            coroutineScope.launch { drawerState.close() }
                        }
                    )
                }
            }
        ) {
            Scaffold(
                topBar = {
                    CenterAlignedTopAppBar(
                        title = {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = L10n.getString("app_title", currentLanguage),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 20.sp
                                )
                                Text(
                                    text = L10n.getString("app_subtitle", currentLanguage),
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        },
                        navigationIcon = {
                            IconButton(onClick = { coroutineScope.launch { drawerState.open() } }) {
                                Icon(imageVector = Icons.Default.Menu, contentDescription = "Menu")
                            }
                        },
                        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                            containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(4.dp)
                        )
                    )
                },
                bottomBar = {
                    // Display persistent Adaptive Banner Ads under Home, Downloads, Settings, Privacy Policy
                    if (currentScreen == Screen.HOME || currentScreen == Screen.DOWNLOADS ||
                        currentScreen == Screen.SETTINGS || currentScreen == Screen.PRIVACY
                    ) {
                        Surface(
                            tonalElevation = 8.dp,
                            modifier = Modifier.navigationBarsPadding()
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                BannerAdView()
                            }
                        }
                    }
                }
            ) { paddingValues ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    when (currentScreen) {
                        Screen.HOME -> HomeScreenContent(
                            viewModel = viewModel,
                            currentLanguage = currentLanguage,
                            activeDownloadStatus = activeDownloadStatus,
                            onStartDownloadWithOptions = { videoData ->
                                selectedVideoDataForSheet = videoData
                                showBottomSheet = true
                            },
                            onPlayPreview = { url ->
                                showPreviewDialogUrl = url
                            }
                        )
                        Screen.DOWNLOADS -> DownloadsScreenContent(
                            searchResults = searchResults,
                            searchQuery = searchQuery,
                            onSearchQueryChanged = { viewModel.updateSearchQuery(it) },
                            currentLanguage = currentLanguage,
                            onPlayLocalFile = { path ->
                                showPreviewDialogUrl = path
                            },
                            onShareFile = { path ->
                                Utils.shareVideoFile(context, path)
                            },
                            onDeleteFile = { item ->
                                viewModel.deleteDownload(item)
                            }
                        )
                        Screen.FEEDBACK -> FeedbackScreenContent(currentLanguage)
                        Screen.PRIVACY -> PrivacyPolicyScreenContent(currentLanguage)
                        Screen.LANGUAGE -> LanguageScreenContent(
                            currentLanguage = currentLanguage,
                            onLanguageSelected = { viewModel.setLanguage(it) }
                        )
                        Screen.SETTINGS -> SettingsScreenContent(
                            viewModel = viewModel,
                            currentLanguage = currentLanguage
                        )
                        Screen.ABOUT -> AboutScreenContent(currentLanguage)
                        else -> {}
                    }
                }
            }
        }
    }

    // Video Player Dialog overlay for playing both online previews and offline local files
    showPreviewDialogUrl?.let { url ->
        VideoPlayerDialog(videoUrl = url, onDismiss = { showPreviewDialogUrl = null })
    }

    // Bottom Sheet for choosing download options & quality before starting the process
    if (showBottomSheet && selectedVideoDataForSheet != null) {
        val videoData = selectedVideoDataForSheet!!
        ModalBottomSheet(
            onDismissRequest = { showBottomSheet = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ) {
            QualitySelectionSheetContent(
                videoData = videoData,
                currentLanguage = currentLanguage,
                onQualitySelected = { selectedRes, selectedUrl ->
                    showBottomSheet = false
                    if (activity != null) {
                        // Check interstitial ad threshold before starting download (Rule: Every 3rd download, frequency limited)
                        AdManager.incrementDownloadAndCheckAd(activity) {
                            val intent = Intent(context, DownloadService::class.java).apply {
                                putExtra("download_url", selectedUrl)
                                putExtra("title", videoData.title ?: "OTD_Video")
                                putExtra("thumbnail_url", videoData.cover ?: "")
                                putExtra("duration", videoData.duration ?: 0)
                                putExtra("resolution", selectedRes)
                            }
                            context.startService(intent)
                        }
                    } else {
                        val intent = Intent(context, DownloadService::class.java).apply {
                            putExtra("download_url", selectedUrl)
                            putExtra("title", videoData.title ?: "OTD_Video")
                            putExtra("thumbnail_url", videoData.cover ?: "")
                            putExtra("duration", videoData.duration ?: 0)
                            putExtra("resolution", selectedRes)
                        }
                        context.startService(intent)
                    }
                }
            )
        }
    }
}

// Visual layout of Drawer Header with gorgeous custom gradient background
@Composable
fun DrawerHeader(language: L10n.Language) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(170.dp)
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF1F1F1F),
                        Color(0xFF121212)
                    )
                )
            ),
        contentAlignment = Alignment.BottomStart
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(Color(0xFF00F2FE), Color(0xFFF35588))
                        ),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Download,
                    contentDescription = "OTD Logo",
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "OTD",
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp,
                color = Color.White
            )
            Text(
                text = L10n.getString("app_subtitle", language),
                fontSize = 12.sp,
                color = Color.LightGray
            )
        }
    }
}

@Composable
fun DrawerNavigationItem(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    NavigationDrawerItem(
        label = { Text(text = label, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
        selected = isSelected,
        onClick = onClick,
        icon = { Icon(imageVector = icon, contentDescription = label) },
        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
        colors = NavigationDrawerItemDefaults.colors(
            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
            selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
            selectedTextColor = MaterialTheme.colorScheme.onPrimaryContainer
        )
    )
}

// Complete Splash Screen containing high-contrast typography and subtle spinner loaders matching the Sleek theme
@Composable
fun SplashScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            androidx.compose.foundation.Image(
                painter = androidx.compose.ui.res.painterResource(id = com.example.R.drawable.otd_logo_1784069491943),
                contentDescription = "OTD Main Logo",
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "OTD",
                fontSize = 38.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = 2.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "TikTok Video Downloader",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(48.dp))
            CircularProgressIndicator(
                color = MaterialTheme.colorScheme.primary,
                strokeWidth = 3.dp,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}

// Complete Home screen containing primary download action field, clipboard integration, and real-time analyzer states
@Composable
fun HomeScreenContent(
    viewModel: MainViewModel,
    currentLanguage: L10n.Language,
    activeDownloadStatus: DownloadStateTracker.DownloadStatus,
    onStartDownloadWithOptions: (TikWmVideoData) -> Unit,
    onPlayPreview: (String) -> Unit
) {
    val context = LocalContext.current
    var inputUrl by remember { mutableStateOf("") }
    val analysisState by viewModel.analysisState.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            // Main Input Panel following the Sleek Interface theme
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                ),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = L10n.getString("paste_hint", currentLanguage),
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    TextField(
                        value = inputUrl,
                        onValueChange = { inputUrl = it },
                        placeholder = { Text(L10n.getString("paste_hint", currentLanguage), color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp, bottomStart = 0.dp, bottomEnd = 0.dp),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                            focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                            unfocusedIndicatorColor = MaterialTheme.colorScheme.outline,
                            disabledIndicatorColor = Color.Transparent
                        ),
                        singleLine = true,
                        trailingIcon = {
                            if (inputUrl.isNotEmpty()) {
                                IconButton(onClick = { inputUrl = "" }) {
                                    Icon(imageVector = Icons.Default.Close, contentDescription = "Clear", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = {
                                val pasted = Utils.getClipboardText(context)
                                if (pasted.isNotEmpty()) {
                                    inputUrl = pasted
                                } else {
                                    Toast.makeText(context, "Clipboard empty!", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.weight(1f).height(44.dp),
                            shape = CircleShape,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        ) {
                            Icon(imageVector = Icons.Default.ContentPaste, contentDescription = "Paste", modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(L10n.getString("btn_paste", currentLanguage), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }

                        Button(
                            onClick = {
                                inputUrl = ""
                                viewModel.clearAnalysis()
                            },
                            modifier = Modifier.weight(1f).height(44.dp),
                            shape = CircleShape,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer,
                                contentColor = MaterialTheme.colorScheme.onErrorContainer
                            )
                        ) {
                            Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear", modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(L10n.getString("btn_clear", currentLanguage), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }

                    Button(
                        onClick = { viewModel.analyzeTikTokUrl(inputUrl) },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        Icon(imageVector = Icons.Default.YoutubeSearchedFor, contentDescription = "Analyze", modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(L10n.getString("btn_analyze", currentLanguage), fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
                }
            }
        }

        // Handle Video Analysis State Changes (MVVM Reactive design)
        item {
            AnimatedContent(
                targetState = analysisState,
                transitionSpec = { fadeIn(tween(250)) togetherWith fadeOut(tween(250)) },
                label = "analysis_state_anim"
            ) { state ->
                when (state) {
                    is MainViewModel.AnalysisState.Loading -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                CircularProgressIndicator()
                                Spacer(modifier = Modifier.height(12.dp))
                                Text("Analyzing video metrics. Please wait...", fontSize = 13.sp)
                            }
                        }
                    }
                    is MainViewModel.AnalysisState.Error -> {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ErrorOutline,
                                    contentDescription = "Error",
                                    tint = MaterialTheme.colorScheme.onErrorContainer
                                )
                                Text(
                                    text = state.message,
                                    color = MaterialTheme.colorScheme.onErrorContainer,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                    is MainViewModel.AnalysisState.Success -> {
                        VideoPreviewCard(
                            videoData = state.videoData,
                            currentLanguage = currentLanguage,
                            onPlayPreview = onPlayPreview,
                            onDownloadClicked = onStartDownloadWithOptions
                        )
                    }
                    else -> {}
                }
            }
        }

        // Active Foreground Download Tracker
        item {
            AnimatedVisibility(
                visible = activeDownloadStatus !is DownloadStateTracker.DownloadStatus.Idle,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                ActiveDownloadCard(
                    status = activeDownloadStatus,
                    currentLanguage = currentLanguage,
                    onDismiss = { viewModel.clearAnalysis(); DownloadStateTracker.updateStatus(DownloadStateTracker.DownloadStatus.Idle) }
                )
            }
        }
    }
}

// Beautiful Material Design 3 Video Preview Card following the Sleek Interface design theme
@Composable
fun VideoPreviewCard(
    videoData: TikWmVideoData,
    currentLanguage: L10n.Language,
    onPlayPreview: (String) -> Unit,
    onDownloadClicked: (TikWmVideoData) -> Unit
) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            // Video Thumbnail with aspect-ratio 16:9, Play button overlay, and duration badge
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
                    .background(Color.Black)
                    .clickable { videoData.play?.let(onPlayPreview) },
                contentAlignment = Alignment.Center
            ) {
                // Background thumbnail image
                AsyncImage(
                    model = videoData.cover ?: videoData.origin_cover,
                    contentDescription = "Thumbnail",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    alpha = 0.8f
                )
                
                // Play Button Overlay
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.9f), shape = CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Play Preview",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }

                // Duration Badge
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(8.dp)
                        .background(Color.Black.copy(alpha = 0.6f), shape = RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "${videoData.duration ?: 0}s",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Description and Download trigger block
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = videoData.title ?: "TikTok Video",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "@${videoData.author?.unique_id ?: "unknown"}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "•",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "1080p Full HD",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium
                    )
                    val sizeMb = (videoData.size ?: 0L) / (1024f * 1024f)
                    if (sizeMb > 0f) {
                        Text(
                            text = "•",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = String.format("%.1f MB", sizeMb),
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Button(
                    onClick = { onDownloadClicked(videoData) },
                    modifier = Modifier.fillMaxWidth().height(44.dp),
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.DownloadForOffline,
                        contentDescription = "Download Video",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Download Video",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}

// Display available resolutions and estimated file sizes in sheet
@Composable
fun QualitySelectionSheetContent(
    videoData: TikWmVideoData,
    currentLanguage: L10n.Language,
    onQualitySelected: (String, String) -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(24.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text(
                text = "Choose Quality / Format",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Each option downloads clean, watermark-free videos.",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            HorizontalDivider()

            val standardSizeMb = (videoData.size ?: 0L) / (1024f * 1024f)
            val hdSizeMb = (videoData.hd_size ?: 0L) / (1024f * 1024f)

            val options = mutableListOf<Triple<String, String, String>>()

            if (videoData.hdplay != null) {
                options.add(Triple("Full HD (1080p)", String.format("%.1f MB", hdSizeMb), videoData.hdplay))
            }
            if (videoData.play != null) {
                val bestSizeText = if (hdSizeMb > 0f) String.format("%.1f MB", hdSizeMb) else "Auto"
                options.add(Triple("HD (720p)", bestSizeText, videoData.play))
                options.add(Triple("SD (540p)", String.format("%.1f MB", standardSizeMb * 0.8f), videoData.play))
                options.add(Triple("Mobile (480p)", String.format("%.1f MB", standardSizeMb * 0.6f), videoData.play))
                options.add(Triple("Low (360p)", String.format("%.1f MB", standardSizeMb * 0.4f), videoData.play))
            }

            if (options.isEmpty()) {
                Text("No download qualities available.", color = MaterialTheme.colorScheme.error)
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(options) { opt ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { onQualitySelected(opt.first, opt.third) }
                                .background(MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp))
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.HighQuality, contentDescription = opt.first, tint = MaterialTheme.colorScheme.primary)
                                Text(opt.first, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                            }
                            Text(
                                text = opt.second,
                                fontWeight = FontWeight.Medium,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

// Active Foreground Download Tracker UI Card following the Sleek Interface theme
@Composable
fun ActiveDownloadCard(
    status: DownloadStateTracker.DownloadStatus,
    currentLanguage: L10n.Language,
    onDismiss: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            when (status) {
                is DownloadStateTracker.DownloadStatus.Downloading -> {
                    Text(
                        text = "Downloading: ${status.title}",
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 15.sp
                    )
                    LinearProgressIndicator(
                        progress = { status.progress / 100f },
                        modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("${status.progress}%", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
                        Text(status.speed, color = MaterialTheme.colorScheme.primary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Text("ETA: ${status.remainingTime}", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                is DownloadStateTracker.DownloadStatus.Success -> {
                    val context = LocalContext.current
                    Text(
                        text = "Download Completed Successfully",
                        color = Color(0xFF00C853),
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Text(status.title, maxLines = 1, overflow = TextOverflow.Ellipsis, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
 
                    Spacer(modifier = Modifier.height(4.dp))
 
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { Utils.openVideoFile(context, status.localPath) },
                            shape = CircleShape,
                            modifier = Modifier.weight(1f).height(40.dp)
                        ) {
                            Text("Open", fontWeight = FontWeight.Bold)
                        }
                        Button(
                            onClick = { Utils.shareVideoFile(context, status.localPath) },
                            shape = CircleShape,
                            modifier = Modifier.weight(1f).height(40.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer, contentColor = MaterialTheme.colorScheme.onSecondaryContainer)
                        ) {
                            Text("Share", fontWeight = FontWeight.Bold)
                        }
                        OutlinedButton(
                            onClick = onDismiss,
                            shape = CircleShape,
                            modifier = Modifier.weight(1.2f).height(40.dp)
                        ) {
                            Text("Another", fontWeight = FontWeight.Bold)
                        }
                    }
                }
                is DownloadStateTracker.DownloadStatus.Failed -> {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Icon(imageVector = Icons.Default.Error, contentDescription = "Error", tint = MaterialTheme.colorScheme.error)
                        Text("Download Failed", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                    }
                    Text(status.error, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Button(
                        onClick = onDismiss,
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.errorContainer, contentColor = MaterialTheme.colorScheme.onErrorContainer),
                        modifier = Modifier.fillMaxWidth().height(40.dp)
                    ) {
                        Text("Retry / Dismiss", fontWeight = FontWeight.Bold)
                    }
                }
                else -> {}
            }
        }
    }
}

// Complete Downloads history card containing SQLite search filter and direct action keys
@Composable
fun DownloadsScreenContent(
    searchResults: List<DownloadItem>,
    searchQuery: String,
    onSearchQueryChanged: (String) -> Unit,
    currentLanguage: L10n.Language,
    onPlayLocalFile: (String) -> Unit,
    onShareFile: (String) -> Unit,
    onDeleteFile: (DownloadItem) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChanged,
            placeholder = { Text(L10n.getString("search_hint", currentLanguage)) },
            leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = "Search") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        )

        if (searchResults.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.DownloadForOffline,
                        contentDescription = "No Downloads",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.size(72.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = L10n.getString("no_downloads", currentLanguage),
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(searchResults) { item ->
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Async load thumbnail from sqlite cache or public cache
                                AsyncImage(
                                    model = item.thumbnailUrl,
                                    contentDescription = item.title,
                                    modifier = Modifier
                                        .size(70.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(Color.DarkGray),
                                    contentScale = ContentScale.Crop
                                )

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = item.title,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = String.format("%.1f MB", item.fileSize / (1024f * 1024f)),
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = "Saved path: .../Download/OTD",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = { onPlayLocalFile(item.localFilePath) },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.PlayArrow, contentDescription = "Play", modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Open", fontSize = 12.sp)
                                }

                                Button(
                                    onClick = { onShareFile(item.localFilePath) },
                                    modifier = Modifier.weight(1.1f),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer, contentColor = MaterialTheme.colorScheme.onSecondaryContainer)
                                ) {
                                    Icon(imageVector = Icons.Default.Share, contentDescription = "Share", modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Share", fontSize = 12.sp)
                                }

                                IconButton(
                                    onClick = { onDeleteFile(item) },
                                    colors = IconButtonDefaults.iconButtonColors(contentColor = MaterialTheme.colorScheme.error)
                                ) {
                                    Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// Complete Feedback Screen layout
@Composable
fun FeedbackScreenContent(currentLanguage: L10n.Language) {
    val context = LocalContext.current
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "We Love Your Feedback",
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Let us know about errors, improvements, or features you'd love us to build next.",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        item {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Name") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )
        }

        item {
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )
        }

        item {
            OutlinedTextField(
                value = message,
                onValueChange = { message = it },
                label = { Text("Message") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 4,
                maxLines = 6,
                shape = RoundedCornerShape(12.dp)
            )
        }

        item {
            Button(
                onClick = {
                    if (name.isBlank() || email.isBlank() || message.isBlank()) {
                        Toast.makeText(context, "Please complete all fields.", Toast.LENGTH_SHORT).show()
                    } else {
                        Utils.sendFeedbackEmail(context, name, email, message)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Submit Feedback")
            }
        }
    }
}

// Complete Professional Privacy Policy Layout
@Composable
fun PrivacyPolicyScreenContent(currentLanguage: L10n.Language) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "Privacy Policy",
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp
            )
            HorizontalDivider()
        }

        item {
            PrivacySection(
                title = "1. Data Collection",
                body = "OTD Downloader does not collect or store personal identifiers or tracking info. Your link analyses are performed securely directly using external CDNs."
            )
        }

        item {
            PrivacySection(
                title = "2. Downloaded Files",
                body = "Files you download are saved directly locally on your device in the public /Download/OTD directory. We never upload files to our servers."
            )
        }

        item {
            PrivacySection(
                title = "3. Advertising & Cookies",
                body = "We use Google AdMob SDK to serve banner, interstitial, and app-open ads. These networks might gather location or device attributes to serve personalized campaigns."
            )
        }

        item {
            PrivacySection(
                title = "4. Permissions Requested",
                body = "We request internet access to analyze links and storage access solely to save videos down to public folders."
            )
        }
    }
}

@Composable
fun PrivacySection(title: String, body: String) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(4.dp))
            Text(body, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

// Complete Language Selection Panel supporting immediate local translation bindings
@Composable
fun LanguageScreenContent(
    currentLanguage: L10n.Language,
    onLanguageSelected: (L10n.Language) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Switch Language",
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp
        )
        Text(
            text = "The UI elements will update immediately upon selection.",
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(L10n.Language.values()) { lang ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onLanguageSelected(lang) }
                        .background(
                            if (currentLanguage == lang) MaterialTheme.colorScheme.primaryContainer
                            else MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp)
                        )
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(lang.displayName, fontWeight = FontWeight.SemiBold)
                    RadioButton(
                        selected = currentLanguage == lang,
                        onClick = { onLanguageSelected(lang) }
                    )
                }
            }
        }
    }
}

// Settings Screen UI Card Panel
@Composable
fun SettingsScreenContent(
    viewModel: MainViewModel,
    currentLanguage: L10n.Language
) {
    val context = LocalContext.current
    val currentThemeMode by viewModel.themeMode.collectAsStateWithLifecycle()
    val notificationsEnabled by viewModel.notificationsEnabled.collectAsStateWithLifecycle()
    val autoSaveEnabled by viewModel.autoSaveEnabled.collectAsStateWithLifecycle()
    val downloadLocation by viewModel.downloadLocation.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Application Settings",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )
            HorizontalDivider()
        }

        // Theme Options
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f))
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Theme Appearance", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.primary)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("light", "dark", "auto").forEach { mode ->
                            Button(
                                onClick = { viewModel.setThemeMode(mode) },
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (currentThemeMode == mode) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                    contentColor = if (currentThemeMode == mode) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            ) {
                                Text(mode.replaceFirstChar { if (it.isLowerCase()) it.titlecase(java.util.Locale.getDefault()) else it.toString() }, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }

        // Notifications switch
        item {
            SettingsToggleCard(
                title = "Notifications",
                subtitle = "Receive foreground service status metrics",
                checked = notificationsEnabled,
                onCheckedChange = { viewModel.toggleNotifications(it) }
            )
        }

        // Auto Save downloads switch
        item {
            SettingsToggleCard(
                title = "Auto Save Downloads",
                subtitle = "Automatically index file inputs directly",
                checked = autoSaveEnabled,
                onCheckedChange = { viewModel.toggleAutoSave(it) }
            )
        }

        // Download directory location
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Download Location", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text(downloadLocation, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Icon(imageVector = Icons.Default.Folder, contentDescription = "Folder")
                }
            }
        }

        // Cache Management
        item {
            Button(
                onClick = {
                    viewModel.clearCache(context)
                    Toast.makeText(context, "Application cache cleared successfully!", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.errorContainer, contentColor = MaterialTheme.colorScheme.onErrorContainer),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Clear Temporary Cache")
            }
        }

        item {
            Text(
                text = "App Version: 1.0.0 (Build 4)",
                fontSize = 11.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun SettingsToggleCard(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text(subtitle, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Switch(checked = checked, onCheckedChange = onCheckedChange)
        }
    }
}

// Complete About Page
@Composable
fun AboutScreenContent(currentLanguage: L10n.Language) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(70.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xFF00F2FE), Color(0xFFF35588))
                    ),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Download,
                contentDescription = "Logo",
                tint = Color.White,
                modifier = Modifier.size(36.dp)
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "OTD",
            fontWeight = FontWeight.ExtraBold,
            fontSize = 28.sp
        )
        Text(
            text = "Version 1.0.0",
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Premium Watermark-free TikTok Downloader.\nDesigned and developed utilizing modern Android architecture paradigms.",
            textAlign = TextAlign.Center,
            fontSize = 13.sp,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "Developer: triqet@gmail.com",
            fontWeight = FontWeight.SemiBold,
            fontSize = 13.sp
        )
        Text(
            text = "Copyright © 2026 OTD Inc. All Rights Reserved.",
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// Reusable Video Player dialog leveraging Android's native VideoView & media controller
@Composable
fun VideoPlayerDialog(videoUrl: String, onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(450.dp),
            shape = RoundedCornerShape(24.dp),
            color = Color.Black
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                AndroidView(
                    modifier = Modifier.fillMaxSize(),
                    factory = { context ->
                        VideoView(context).apply {
                            val mediaController = MediaController(context)
                            mediaController.setAnchorView(this)
                            setMediaController(mediaController)
                            setVideoPath(videoUrl)
                            setOnPreparedListener { start() }
                        }
                    }
                )
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(12.dp)
                        .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                ) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                }
            }
        }
    }
}
