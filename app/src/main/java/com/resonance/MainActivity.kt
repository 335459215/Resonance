package com.resonance

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.resonance.data.repository.EmbyRepository
import com.resonance.data.repository.CloudStorageManager
import com.resonance.ui.AdaptiveMainScreen
import com.resonance.ui.ServerConfig
import com.resonance.ui.ServerType
import com.resonance.ui.settings.SettingsScreen
import com.resonance.ui.settings.ServerManagementScreen
import com.resonance.ui.theme.EmbyPlayerTheme
import com.resonance.ui.adaptation.ForwardEliteApp
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import com.resonance.ui.mobile.MediaItem
import com.resonance.ui.mobile.MediaType

sealed class Screen {
    object Home : Screen()
    object Servers : Screen()
    object Settings : Screen()
    object Search : Screen()
    object ServerManagement : Screen()
    object AddServer : Screen()
}

class MainActivity : ComponentActivity() {
    private lateinit var embyRepository: EmbyRepository
    private lateinit var cloudStorageManager: CloudStorageManager
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize repositories
        embyRepository = EmbyRepository(applicationContext)
        cloudStorageManager = CloudStorageManager(applicationContext)
        
        setContent {
            EmbyPlayerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppContent(embyRepository, cloudStorageManager)
                }
            }
        }
    }
}

@Composable
fun AppContent(
    embyRepository: EmbyRepository,
    cloudStorageManager: CloudStorageManager
) {
    var currentScreen by remember { mutableStateOf<Screen>(Screen.Home) }
    var servers by remember { mutableStateOf(listOf<ServerConfig>()) }
    var selectedServer by remember { mutableStateOf<ServerConfig?>(null) }
    
    LaunchedEffect(Unit) {
        // Load servers from repository
        servers = listOf(
            ServerConfig(
                id = "1",
                name = "Local Emby",
                url = "http://localhost:8096",
                type = ServerType.EMBY,
                isDefault = true
            ),
            ServerConfig(
                id = "2",
                name = "115 Cloud",
                url = "https://www.115.com",
                type = ServerType.CLOUD_115
            ),
            ServerConfig(
                id = "3",
                name = "Local Music",
                url = "http://localhost:8096",
                type = ServerType.EMBY
            )
        )
        // Set default server
        selectedServer = servers.firstOrNull { it.isDefault }
    }
    
    when (currentScreen) {
        Screen.Home -> {
            AdaptiveMainScreen(
                embyRepository = embyRepository,
                selectedServer = selectedServer,
                servers = servers,
                onServerSelect = {
                    selectedServer = it
                },
                onMediaClick = { mediaItem ->
                    // Handle media item click
                    if (mediaItem is MediaItem) {
                        when (mediaItem.mediaType) {
                            MediaType.MUSIC -> {
                                // Play music via strm file
                                embyRepository.playMedia(mediaItem.id) {
                                    // Handle music playback URL
                                }
                            }
                            else -> {
                                // Play video via regular playback
                                embyRepository.playMedia(mediaItem.id) {
                                    // Handle video playback URL
                                }
                            }
                        }
                    }
                },
                onSearchClick = {
                    currentScreen = Screen.Search
                },
                onServersClick = {
                    currentScreen = Screen.Servers
                },
                onSettingsClick = {
                    currentScreen = Screen.Settings
                }
            )
        }
        Screen.Servers -> {
            ServerManagementScreen(
                servers = servers,
                embyRepository = embyRepository,
                cloudStorageManager = cloudStorageManager,
                onServerAdd = {
                    currentScreen = Screen.AddServer
                },
                onServerEdit = {
                    // Handle server edit
                },
                onServerDelete = {
                    // Handle server delete
                },
                onServerSelect = {
                    selectedServer = it
                    currentScreen = Screen.Home
                },
                onBackClick = {
                    currentScreen = Screen.Home
                }
            )
        }
        Screen.Settings -> {
            SettingsScreen(
                embyRepository = embyRepository,
                cloudStorageManager = cloudStorageManager,
                onNavigateBack = {
                    currentScreen = Screen.Home
                },
                onNavigateToServerManagement = {
                    currentScreen = Screen.Servers
                }
            )
        }
        Screen.Search -> {
            SearchScreen(
                onBackClick = {
                    currentScreen = Screen.Home
                }
            )
        }
        Screen.AddServer -> {
            // 添加服务器页面待实现
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "添加服务器",
                        style = MaterialTheme.typography.headlineSmall
                    )
                    Button(onClick = {
                        currentScreen = Screen.Servers
                    }) {
                        Text("返回")
                    }
                }
            }
        }
        Screen.ServerManagement -> {
            ServerManagementScreen(
                servers = servers,
                embyRepository = embyRepository,
                cloudStorageManager = cloudStorageManager,
                onServerAdd = {
                    currentScreen = Screen.AddServer
                },
                onServerEdit = {
                    // Handle server edit
                },
                onServerDelete = {
                    // Handle server delete
                },
                onServerSelect = {
                    selectedServer = it
                    currentScreen = Screen.Home
                },
                onBackClick = {
                    currentScreen = Screen.Home
                }
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SearchScreen(
    onBackClick: () -> Unit
) {
    var searchQuery by remember { mutableStateOf(TextFieldValue("")) }
    var showResults by remember { mutableStateOf(false) }
    
    // 模拟搜索历史
    val searchHistory = remember {
        listOf(
            "复仇者联盟",
            "权力的游戏",
            "周杰伦",
            "科幻电影",
            "流行音乐"
        )
    }
    
    // 模拟最近添加内容
    val recentlyAdded = remember {
        listOf(
            MediaItem(
                id = "1",
                title = "复仇者联盟：终局之战",
                subtitle = "2023",
                mediaType = MediaType.MOVIE
            ),
            MediaItem(
                id = "2",
                title = "周杰伦：最伟大的作品",
                subtitle = "2022",
                mediaType = MediaType.MUSIC
            ),
            MediaItem(
                id = "3",
                title = "权力的游戏：前传",
                subtitle = "2023",
                mediaType = MediaType.SERIES
            )
        )
    }
    
    // 模拟推荐内容
    val recommendedContent = remember {
        listOf(
            MediaItem(
                id = "4",
                title = "银河护卫队3",
                subtitle = "2023",
                mediaType = MediaType.MOVIE
            ),
            MediaItem(
                id = "5",
                title = "Taylor Swift: The Eras Tour",
                subtitle = "2023",
                mediaType = MediaType.MUSIC
            ),
            MediaItem(
                id = "6",
                title = "三体",
                subtitle = "2023",
                mediaType = MediaType.SERIES
            )
        )
    }
    
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // 搜索栏
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "返回")
            }
            
            OutlinedTextField(
                value = searchQuery,
                onValueChange = {
                    searchQuery = it
                    showResults = it.text.isNotEmpty()
                },
                placeholder = { Text("搜索电影、剧集、音乐...") },
                leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = "搜索") },
                trailingIcon = {
                    if (searchQuery.text.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = TextFieldValue("") }) {
                            Icon(Icons.Outlined.Clear, contentDescription = "清除")
                        }
                    }
                },
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp),
                shape = RoundedCornerShape(20.dp)
            )
        }
        
        if (showResults) {
            // 搜索结果（模拟）
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Text(
                        text = "搜索结果",
                        style = MaterialTheme.typography.headlineSmall
                    )
                }
                items(recommendedContent) {
                    SearchResultItem(mediaItem = it)
                }
            }
        } else {
            // 搜索历史 + 最近添加 + 推荐
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // 搜索历史
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "搜索历史",
                            style = MaterialTheme.typography.titleMedium
                        )
                        TextButton(onClick = { /* 清除历史 */ }) {
                            Text("清除")
                        }
                    }
                    
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        searchHistory.forEach { query ->
                            FilterChip(
                                selected = false,
                                onClick = {
                                    searchQuery = TextFieldValue(query)
                                    showResults = true
                                },
                                label = { Text(query) },
                                leadingIcon = {
                                    Icon(
                                        Icons.Outlined.History,
                                        contentDescription = "历史",
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            )
                        }
                    }
                }
                
                // 最近添加
                item {
                    Text(
                        text = "最近添加",
                        style = MaterialTheme.typography.titleMedium
                    )
                    
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        recentlyAdded.forEach { item ->
                            SearchResultItem(mediaItem = item)
                        }
                    }
                }
                
                // 推荐内容
                item {
                    Text(
                        text = "为你推荐",
                        style = MaterialTheme.typography.titleMedium
                    )
                    
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        recommendedContent.forEach { item ->
                            SearchResultItem(mediaItem = item)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SearchResultItem(
    mediaItem: MediaItem
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable(onClick = { /* 处理点击 */ }),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 媒体图标
        Surface(
            modifier = Modifier.size(56.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            shape = RoundedCornerShape(8.dp)
        ) {
            Box(modifier = Modifier.padding(all = 8.dp)) {
                Icon(
                    when (mediaItem.mediaType) {
                        MediaType.MOVIE -> Icons.Outlined.Movie
                        MediaType.SERIES -> Icons.Outlined.Tv
                        MediaType.MUSIC -> Icons.Outlined.MusicNote
                        else -> Icons.Outlined.VideoLibrary
                    },
                    contentDescription = mediaItem.mediaType.name,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
        
        // 媒体信息
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = mediaItem.title,
                style = MaterialTheme.typography.bodyLarge
            )
            mediaItem.subtitle?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        // 媒体类型图标
        Icon(
            when (mediaItem.mediaType) {
                MediaType.MOVIE -> Icons.Outlined.Movie
                MediaType.SERIES -> Icons.Outlined.Tv
                MediaType.MUSIC -> Icons.Outlined.MusicNote
                else -> Icons.Outlined.VideoLibrary
            },
            contentDescription = mediaItem.mediaType.name,
            modifier = Modifier.size(20.dp)
        )
    }
}