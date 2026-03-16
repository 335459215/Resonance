package com.resonance.ui.mobile

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.outlined.Sort
import androidx.compose.material.icons.automirrored.outlined.ViewList
import androidx.compose.material.icons.automirrored.outlined.VolumeUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.ui.unit.lerp
import androidx.compose.ui.util.lerp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.foundation.border
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.resonance.data.repository.EmbyRepository
import com.resonance.ui.components.*
import com.resonance.ui.model.MenuItem
import com.resonance.ui.repository.MenuRepository
import com.resonance.ui.ServerConfig
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

data class MediaItem(
    val id: String,
    val title: String,
    val subtitle: String? = null,
    val posterUrl: String? = null,
    val backdropUrl: String? = null,
    val overview: String? = null,
    val year: Int? = null,
    val rating: Float? = null,
    val duration: Long? = null,
    val progress: Float = 0f,
    val isNew: Boolean = false,
    val isFavorite: Boolean = false,
    val genres: List<String> = emptyList(),
    val mediaType: MediaType = MediaType.MOVIE
)

enum class MediaType {
    MOVIE, SERIES, EPISODE, MUSIC, PHOTO
}

enum class ViewMode {
    GRID, LIST, COMPACT
}

enum class SortOrder {
    DATE_ADDED, NAME, RELEASE_DATE, RATING, DURATION
}

// Media library data class
data class MediaLibrary(
    val id: String,
    val name: String,
    val type: String,
    val icon: ImageVector
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun MainScreen(
    embyRepository: EmbyRepository,
    menuRepository: MenuRepository,
    selectedServer: ServerConfig?,
    servers: List<ServerConfig>,
    onServerSelect: (ServerConfig) -> Unit,
    onMediaClick: (MediaItem) -> Unit = {},
    onMediaLongClick: (MediaItem) -> Unit = {},
    onSearchClick: () -> Unit = {},
    onServersClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {}
) {
    var selectedNavItem by remember { mutableStateOf(0) } // 0: Home, 1: Servers, 2: Settings
    var viewMode by remember { mutableStateOf(ViewMode.GRID) }
    var showSearch by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var menus by remember { mutableStateOf<List<MenuItem>>(emptyList()) }
    var isLoadingMenus by remember { mutableStateOf(true) }
    var menuError by remember { mutableStateOf<String?>(null) }
    var showServerSelection by remember { mutableStateOf(false) }
    
    // 下拉刷新相关状态
    var isRefreshing by remember { mutableStateOf(false) }
    var refreshProgress by remember { mutableStateOf(0f) }
    val refreshThreshold = 100.dp
    val refreshThresholdPx = with(LocalDensity.current) { refreshThreshold.toPx() }
    val coroutineScope = rememberCoroutineScope()
    
    // Load data when screen is initialized
    LaunchedEffect(Unit) {
        // Load recent items
        embyRepository.loadRecentItems()
        
        // Load favorite items
        embyRepository.loadFavoriteItems()
        
        // Load media items from default folder
        embyRepository.loadMediaItems("")
        
        // Load menus from repository
        menuRepository.getMenus(null) { response ->
            isLoadingMenus = false
            if (response.success) {
                menus = response.menus
            } else {
                menuError = response.error
                // Use default menus if loading fails
                menus = listOf(
                    MenuItem("1", "媒体库", "VideoLibrary", "/library", 1, null, null, true, 1),
                    MenuItem("2", "网络", "CloudQueue", "/network", 1, null, null, true, 2),
                    MenuItem("3", "最近", "History", "/recent", 1, null, null, true, 3),
                    MenuItem("4", "收藏", "FavoriteBorder", "/favorites", 1, null, null, true, 4),
                    MenuItem("5", "设置", "Settings", "/settings", 1, null, null, true, 5),
                    MenuItem("6", "精英版", "Star", "/forward-elite", 1, null, "elite_access", true, 6)
                )
            }
        }
    }
    
    // Get default menus for fallback
    fun getDefaultMenus(): List<MenuItem> {
        return listOf(
            MenuItem("1", "媒体库", "VideoLibrary", "/library", 1, null, null, true, 1),
            MenuItem("2", "网络", "CloudQueue", "/network", 1, null, null, true, 2),
            MenuItem("3", "最近", "History", "/recent", 1, null, null, true, 3),
            MenuItem("4", "收藏", "FavoriteBorder", "/favorites", 1, null, null, true, 4),
            MenuItem("5", "设置", "Settings", "/settings", 1, null, null, true, 5),
            MenuItem("6", "精英版", "Star", "/forward-elite", 1, null, "elite_access", true, 6)
        )
    }
    
    // Get icon from menu icon name
    fun getIconFromName(iconName: String): ImageVector {
        return when (iconName) {
            "VideoLibrary" -> Icons.Outlined.VideoLibrary
            "CloudQueue" -> Icons.Outlined.CloudQueue
            "History" -> Icons.Outlined.History
            "FavoriteBorder" -> Icons.Outlined.FavoriteBorder
            "Settings" -> Icons.Outlined.Settings
            "Star" -> Icons.Outlined.Star
            else -> Icons.Outlined.MoreVert
        }
    }
    
    // Convert menus to tab items
    val tabItems = menus.mapIndexed { index, menu ->
        TabItem(
            icon = getIconFromName(menu.icon),
            label = menu.name,
            badge = if (menu.path == "/recent") 3 else 0
        )
    }
    
    // Refresh function
    fun refreshMenus() {
        if (isRefreshing) return
        
        isRefreshing = true
        refreshProgress = 1f
        
        menuRepository.refreshMenus(null) { response ->
            coroutineScope.launch {
                // Simulate network delay for better user experience
                delay(1000)
                
                if (response.success) {
                    menus = response.menus
                    menuError = null
                } else {
                    menuError = response.error
                }
                
                isRefreshing = false
                refreshProgress = 0f
            }
        }
    }
    
    // Handle drag state for pull-to-refresh
    val draggableState = rememberDraggableState {
        if (!isRefreshing) {
            refreshProgress = (refreshProgress + it / refreshThresholdPx).coerceIn(0f, 1.5f)
        }
    }
    
    // Handle drag end for pull-to-refresh
    fun handleDragEnd() {
        if (refreshProgress > 1f) {
            refreshMenus()
        } else {
            refreshProgress = 0f
        }
    }
    
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // Main content with padding for floating nav
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 80.dp) // Space for floating nav
                .draggable(
                    state = draggableState,
                    orientation = Orientation.Vertical,
                    onDragStopped = { velocity ->
                        handleDragEnd()
                    }
                )
        ) {
            // Pull-to-refresh indicator
            AnimatedVisibility(
                visible = refreshProgress > 0f,
                enter = fadeIn() + slideInVertically(),
                exit = fadeOut() + slideOutVertically()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 20.dp)
                        .align(Alignment.TopCenter)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        if (isRefreshing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(32.dp),
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "刷新中...",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .rotate(refreshProgress * 180f)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "刷新",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "下拉刷新",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
            
            // Main content
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .offset(y = if (refreshProgress > 0f) (refreshProgress * 60).dp else 0.dp)
            ) {
                // Home screen content
                HomeScreenContent(
                    embyRepository = embyRepository,
                    selectedServer = selectedServer,
                    servers = servers,
                    onServerSelect = onServerSelect,
                    onSearchClick = onSearchClick,
                    onMediaClick = onMediaClick,
                    onMediaLongClick = onMediaLongClick
                )
            }
        }
        
        // Left floating elements
        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 16.dp)
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                when (selectedNavItem) {
                    0 -> {
                        // Home page left floating elements
                        FloatingActionButton(
                            onClick = { /* 快速操作 */ },
                            modifier = Modifier.size(56.dp),
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        ) {
                            Icon(Icons.Outlined.PlayCircle, contentDescription = "快速播放")
                        }
                        FloatingActionButton(
                            onClick = { /* 收藏 */ },
                            modifier = Modifier.size(56.dp),
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                        ) {
                            Icon(Icons.Outlined.Favorite, contentDescription = "收藏")
                        }
                    }
                    1 -> {
                        // Servers page left floating elements
                        FloatingActionButton(
                            onClick = { /* 添加服务器 */ },
                            modifier = Modifier.size(56.dp),
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        ) {
                            Icon(Icons.Outlined.Add, contentDescription = "添加服务器")
                        }
                    }
                    2 -> {
                        // Settings page left floating elements
                        FloatingActionButton(
                            onClick = { /* 重置设置 */ },
                            modifier = Modifier.size(56.dp),
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        ) {
                            Icon(Icons.Outlined.Restore, contentDescription = "重置设置")
                        }
                    }
                }
            }
        }
        
        // Right floating elements
        Box(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 16.dp)
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                when (selectedNavItem) {
                    0 -> {
                        // Home page right floating elements
                        FloatingActionButton(
                            onClick = { /* 筛选 */ },
                            modifier = Modifier.size(56.dp),
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                            contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                        ) {
                            Icon(Icons.Outlined.FilterList, contentDescription = "筛选")
                        }
                        FloatingActionButton(
                            onClick = { /* 排序 */ },
                            modifier = Modifier.size(56.dp),
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                            contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                        ) {
                            Icon(Icons.AutoMirrored.Outlined.Sort, contentDescription = "排序")
                        }
                    }
                    1 -> {
                        // Servers page right floating elements
                        FloatingActionButton(
                            onClick = { /* 刷新服务器列表 */ },
                            modifier = Modifier.size(56.dp),
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                            contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                        ) {
                            Icon(Icons.Outlined.Refresh, contentDescription = "刷新")
                        }
                    }
                    2 -> {
                        // Settings page right floating elements
                        FloatingActionButton(
                            onClick = { /* 关于 */ },
                            modifier = Modifier.size(56.dp),
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                            contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                        ) {
                            Icon(Icons.Outlined.Info, contentDescription = "关于")
                        }
                    }
                }
            }
        }
        
        // Floating navigation bar
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 20.dp)
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .height(60.dp),
                shape = RoundedCornerShape(30.dp),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 8.dp,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Home button
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clickable(onClick = {
                                selectedNavItem = 0
                            })
                            .padding(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Home,
                            contentDescription = "首页",
                            tint = if (selectedNavItem == 0) 
                                MaterialTheme.colorScheme.primary 
                            else 
                                MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "首页",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (selectedNavItem == 0) 
                                MaterialTheme.colorScheme.primary 
                            else 
                                MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    // Servers button
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clickable(onClick = {
                                onServersClick()
                            })
                            .padding(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Dns,
                            contentDescription = "服务器",
                            tint = if (selectedNavItem == 1) 
                                MaterialTheme.colorScheme.primary 
                            else 
                                MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "服务器",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (selectedNavItem == 1) 
                                MaterialTheme.colorScheme.primary 
                            else 
                                MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    // Settings button
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clickable(onClick = {
                                onSettingsClick()
                            })
                            .padding(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Settings,
                            contentDescription = "设置",
                            tint = if (selectedNavItem == 2) 
                                MaterialTheme.colorScheme.primary 
                            else 
                                MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "设置",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (selectedNavItem == 2) 
                                MaterialTheme.colorScheme.primary 
                            else 
                                MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun HomeScreenContent(
    embyRepository: EmbyRepository,
    selectedServer: ServerConfig?,
    servers: List<ServerConfig>,
    onServerSelect: (ServerConfig) -> Unit,
    onSearchClick: () -> Unit,
    onMediaClick: (MediaItem) -> Unit,
    onMediaLongClick: (MediaItem) -> Unit
) {
    var viewMode by remember { mutableStateOf(ViewMode.GRID) }
    var showServerSelection by remember { mutableStateOf(false) }
    
    // Media server libraries (simulated)
    val mediaLibraries = remember {
        listOf(
            MediaLibrary("1", "电影库", "电影", Icons.Outlined.Movie),
            MediaLibrary("2", "剧集库", "剧集", Icons.Outlined.Tv),
            MediaLibrary("3", "音乐库", "音乐", Icons.Outlined.MusicNote),
            MediaLibrary("4", "照片库", "照片", Icons.Outlined.PhotoLibrary)
        )
    }
    var selectedLibrary by remember { mutableStateOf<String?>(null) }
    

    
    val sampleMedia = remember {
        (1..20).map { i ->
            MediaItem(
                id = i.toString(),
                title = "媒体项目 $i",
                subtitle = if (i % 3 == 0) "第1季" else "${2020 + i % 5}",
                posterUrl = null,
                progress = if (i % 4 == 0) (i % 100) / 100f else 0f,
                isNew = i % 7 == 0,
                isFavorite = i % 5 == 0,
                rating = (7..9).random() + (0..9).random() / 10f,
                genres = listOf("动作", "科幻").take((1..2).random()),
                mediaType = when (i % 4) {
                    0 -> MediaType.MUSIC
                    1 -> MediaType.SERIES
                    2 -> MediaType.MOVIE
                    else -> MediaType.MOVIE
                }
            )
        }
    }
    
    // 轮播数据 - 从媒体服务器获取（模拟）
    val carouselItems = remember {
        listOf(
            CarouselItem(
                id = "1",
                title = "热门剧集：权力的游戏",
                subtitle = "史诗级奇幻剧集",
                imageUrl = null,
                mediaId = "1"
            ),
            CarouselItem(
                id = "2",
                title = "新片上映：复仇者联盟",
                subtitle = "超级英雄大片",
                imageUrl = null,
                mediaId = "2"
            ),
            CarouselItem(
                id = "3",
                title = "精选音乐：周杰伦新专辑",
                subtitle = "华语流行音乐",
                imageUrl = null,
                mediaId = "3"
            )
        )
    }
    
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // 顶部栏：服务器选择 + 标题 + 搜索按钮
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 服务器选择按钮
            Button(
                onClick = { showServerSelection = true },
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .padding(end = 12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        Icons.Outlined.Dns,
                        contentDescription = "服务器",
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = selectedServer?.name ?: "选择服务器",
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }
            
            // 搜索按钮
            IconButton(onClick = onSearchClick) {
                Icon(Icons.Outlined.Search, contentDescription = "搜索")
            }
        }
        
        // 媒体内容轮播组件
        MediaCarousel(
            items = carouselItems,
            onItemClick = { item ->
                // 查找对应的媒体项目并触发点击事件
                sampleMedia.find { it.id == item.mediaId }?.let {
                    onMediaClick(it)
                }
            }
        )
        
        // Media server libraries
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(vertical = 16.dp)
        ) {
            items(mediaLibraries) { library ->
                Surface(
                    modifier = Modifier
                        .clickable(onClick = {
                            selectedLibrary = if (selectedLibrary == library.id) null else library.id
                        })
                        .padding(8.dp),
                    shape = RoundedCornerShape(20.dp),
                    color = if (selectedLibrary == library.id) 
                        MaterialTheme.colorScheme.primaryContainer 
                    else 
                        MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = library.icon,
                            contentDescription = library.name,
                            tint = if (selectedLibrary == library.id) 
                                MaterialTheme.colorScheme.onPrimaryContainer 
                            else 
                                MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = library.name,
                            style = MaterialTheme.typography.labelMedium,
                            color = if (selectedLibrary == library.id) 
                                MaterialTheme.colorScheme.onPrimaryContainer 
                            else 
                                MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
        
        // 视图模式切换
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.End
        ) {
            IconButton(onClick = {
                viewMode = when (viewMode) {
                    ViewMode.GRID -> ViewMode.LIST
                    ViewMode.LIST -> ViewMode.COMPACT
                    ViewMode.COMPACT -> ViewMode.GRID
                }
            }) {
                Icon(
                    when (viewMode) {
                        ViewMode.GRID -> Icons.Outlined.GridView
                        ViewMode.LIST -> Icons.AutoMirrored.Outlined.ViewList
                        ViewMode.COMPACT -> Icons.Outlined.ViewAgenda
                    },
                    "视图"
                )
            }
        }
        
        // 媒体内容显示
        val filteredMedia = sampleMedia.filter {
            selectedLibrary?.let {libraryId ->
                val selectedLib = mediaLibraries.find { it.id == libraryId }
                selectedLib?.let {lib ->
                    when (lib.type) {
                        "电影" -> it.mediaType == MediaType.MOVIE
                        "剧集" -> it.mediaType == MediaType.SERIES
                        "音乐" -> it.mediaType == MediaType.MUSIC
                        "照片" -> it.mediaType == MediaType.PHOTO
                        else -> true
                    }
                } ?: true
            } ?: true
        }
        
        when (viewMode) {
            ViewMode.GRID -> MediaGridView(
                media = filteredMedia,
                onMediaClick = onMediaClick,
                onMediaLongClick = onMediaLongClick
            )
            ViewMode.LIST -> MediaListView(
                media = filteredMedia,
                onMediaClick = onMediaClick,
                onMediaLongClick = onMediaLongClick
            )
            ViewMode.COMPACT -> MediaCompactView(
                media = filteredMedia,
                onMediaClick = onMediaClick,
                onMediaLongClick = onMediaLongClick
            )
        }
    }
    
    // 服务器选择对话框
    if (showServerSelection) {
        AlertDialog(
            onDismissRequest = { showServerSelection = false },
            title = { Text("选择媒体服务器") },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    servers.forEach {server ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onServerSelect(server)
                                    showServerSelection = false
                                }
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(server.name)
                                Text(
                                    server.url,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            if (server.id == selectedServer?.id) {
                                Icon(Icons.Default.Check, contentDescription = "已选择")
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showServerSelection = false }) {
                    Text("取消")
                }
            }
        )
    }
}

data class TabItem(
    val icon: ImageVector,
    val label: String,
    val badge: Int
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LibraryTab(
    embyRepository: EmbyRepository,
    viewMode: ViewMode,
    onViewModeChange: (ViewMode) -> Unit,
    onMediaClick: (MediaItem) -> Unit,
    onMediaLongClick: (MediaItem) -> Unit,
    onSearchClick: () -> Unit
) {
    var selectedCategory by remember { mutableStateOf(0) }
    var sortOrder by remember { mutableStateOf(SortOrder.DATE_ADDED) }
    var showSortMenu by remember { mutableStateOf(false) }
    
    val categories = listOf("全部", "电影", "剧集", "音乐", "照片")
    
    val sampleMedia = remember {
        (1..20).map { i ->
            MediaItem(
                id = i.toString(),
                title = "媒体项目 $i",
                subtitle = if (i % 3 == 0) "第1季" else "${2020 + i % 5}",
                posterUrl = null,
                progress = if (i % 4 == 0) (i % 100) / 100f else 0f,
                isNew = i % 7 == 0,
                isFavorite = i % 5 == 0,
                rating = (7..9).random() + (0..9).random() / 10f,
                genres = listOf("动作", "科幻").take((1..2).random()),
                mediaType = if (i % 3 == 0) MediaType.SERIES else MediaType.MOVIE
            )
        }
    }
    
    // 轮播数据
    val carouselItems = remember {
        listOf(
            CarouselItem(
                id = "1",
                title = "热门剧集：权力的游戏",
                subtitle = "史诗级奇幻剧集",
                imageUrl = null,
                mediaId = "1"
            ),
            CarouselItem(
                id = "2",
                title = "新片上映：复仇者联盟",
                subtitle = "超级英雄大片",
                imageUrl = null,
                mediaId = "2"
            ),
            CarouselItem(
                id = "3",
                title = "精选剧集：绝命毒师",
                subtitle = "获奖剧情剧集",
                imageUrl = null,
                mediaId = "3"
            )
        )
    }
    
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        TopAppBar(
            title = "媒体库",
            actions = {
                IconButton(onClick = onSearchClick) {
                    Icon(Icons.Outlined.Search, "搜索")
                }
                IconButton(onClick = { showSortMenu = true }) {
                    Icon(Icons.AutoMirrored.Outlined.Sort, "排序")
                }
                IconButton(onClick = { 
                    onViewModeChange(
                        when (viewMode) {
                            ViewMode.GRID -> ViewMode.LIST
                            ViewMode.LIST -> ViewMode.COMPACT
                            ViewMode.COMPACT -> ViewMode.GRID
                        }
                    )
                }) {
                    Icon(
                        when (viewMode) {
                            ViewMode.GRID -> Icons.Outlined.GridView
                            ViewMode.LIST -> Icons.AutoMirrored.Outlined.ViewList
                            ViewMode.COMPACT -> Icons.Outlined.ViewAgenda
                        },
                        "视图"
                    )
                }
            }
        )
        
        // 媒体内容轮播组件
        MediaCarousel(
            items = carouselItems,
            onItemClick = { item ->
                // 查找对应的媒体项目并触发点击事件
                sampleMedia.find { it.id == item.mediaId }?.let {
                    onMediaClick(it)
                }
            }
        )
        
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(categories.indices.toList()) { index ->
                FilterChip(
                    selected = selectedCategory == index,
                    onClick = { selectedCategory = index },
                    label = { Text(categories[index]) },
                    leadingIcon = if (selectedCategory == index) {
                        {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    } else null
                )
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        when (viewMode) {
            ViewMode.GRID -> MediaGridView(
                media = sampleMedia,
                onMediaClick = onMediaClick,
                onMediaLongClick = onMediaLongClick
            )
            ViewMode.LIST -> MediaListView(
                media = sampleMedia,
                onMediaClick = onMediaClick,
                onMediaLongClick = onMediaLongClick
            )
            ViewMode.COMPACT -> MediaCompactView(
                media = sampleMedia,
                onMediaClick = onMediaClick,
                onMediaLongClick = onMediaLongClick
            )
        }
    }
    
    DropdownMenu(
        expanded = showSortMenu,
        onDismissRequest = { showSortMenu = false }
    ) {
        SortOrder.values().forEach { order ->
            DropdownMenuItem(
                text = { 
                    Text(
                        when (order) {
                            SortOrder.DATE_ADDED -> "添加日期"
                            SortOrder.NAME -> "名称"
                            SortOrder.RELEASE_DATE -> "发布日期"
                            SortOrder.RATING -> "评分"
                            SortOrder.DURATION -> "时长"
                        }
                    )
                },
                onClick = {
                    sortOrder = order
                    showSortMenu = false
                },
                trailingIcon = {
                    if (sortOrder == order) {
                        Icon(Icons.Default.Check, null)
                    }
                }
            )
        }
    }
}

// 轮播项数据类
data class CarouselItem(
    val id: String,
    val title: String,
    val subtitle: String,
    val imageUrl: String?,
    val mediaId: String
)

// 媒体内容轮播组件
@Composable
fun MediaCarousel(
    items: List<CarouselItem>,
    onItemClick: (CarouselItem) -> Unit
) {
    var currentIndex by remember { mutableStateOf(0) }
    val coroutineScope = rememberCoroutineScope()
    
    // 自动播放
    LaunchedEffect(currentIndex) {
        coroutineScope.launch {
            delay(5000) // 5秒切换一次
            currentIndex = (currentIndex + 1) % items.size
        }
    }
    
    Box(
        modifier = Modifier
            .height(200.dp)
            .fillMaxWidth()
    ) {
        // 轮播内容
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            items.forEachIndexed { index, item ->
                AnimatedVisibility(
                    visible = index == currentIndex,
                    enter = fadeIn(animationSpec = tween(1000)),
                    exit = fadeOut(animationSpec = tween(1000))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clickable {
                                onItemClick(item)
                            }
                    ) {
                        // 背景
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                        )
                        
                        // 内容
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(24.dp)
                                .align(Alignment.CenterStart)
                        ) {
                            Text(
                                text = item.title,
                                style = MaterialTheme.typography.headlineSmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = item.subtitle,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                            Button(
                                onClick = {
                                    onItemClick(item)
                                },
                                modifier = Modifier.padding(top = 16.dp)
                            ) {
                                Text("立即观看")
                            }
                        }
                    }
                }
            }
        }
        
        // 指示器
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items.forEachIndexed { index, _ ->
                Box(
                    modifier = Modifier
                        .size(if (index == currentIndex) 24.dp else 8.dp)
                        .background(
                            color = if (index == currentIndex) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            },
                            shape = RoundedCornerShape(4.dp)
                        )
                        .clickable {
                            currentIndex = index
                        }
                )
            }
        }
        
        // 左右切换按钮
        IconButton(
            onClick = {
                currentIndex = (currentIndex - 1 + items.size) % items.size
            },
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 16.dp)
                .background(
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                    shape = CircleShape
                )
        ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, "上一个")
        }
        
        IconButton(
            onClick = {
                currentIndex = (currentIndex + 1) % items.size
            },
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 16.dp)
                .background(
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                    shape = CircleShape
                )
        ) {
            Icon(Icons.AutoMirrored.Filled.ArrowForward, "下一个")
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MediaGridView(
    media: List<MediaItem>,
    onMediaClick: (MediaItem) -> Unit,
    onMediaLongClick: (MediaItem) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 120.dp),
        contentPadding = PaddingValues(12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(media, key = { it.id }) { item ->
            MediaCard(
                item = item,
                onClick = { onMediaClick(item) },
                onLongClick = { onMediaLongClick(item) },
                modifier = Modifier.animateItem()
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MediaListView(
    media: List<MediaItem>,
    onMediaClick: (MediaItem) -> Unit,
    onMediaLongClick: (MediaItem) -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(media, key = { it.id }) { item ->
            MediaListItem(
                item = item,
                onClick = { onMediaClick(item) },
                onLongClick = { onMediaLongClick(item) },
                modifier = Modifier.animateItem()
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MediaCompactView(
    media: List<MediaItem>,
    onMediaClick: (MediaItem) -> Unit,
    onMediaLongClick: (MediaItem) -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        items(media, key = { it.id }) { item ->
            MediaCompactItem(
                item = item,
                onClick = { onMediaClick(item) },
                onLongClick = { onMediaLongClick(item) },
                modifier = Modifier.animateItem()
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MediaCard(
    item: MediaItem,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isFocused by remember { mutableStateOf(false) }
    
    val scale by animateFloatAsState(
        targetValue = if (isFocused) 1.05f else 1f,
        animationSpec = tween(200),
        label = "scale"
    )
    
    Card(
        modifier = modifier
            .aspectRatio(2f / 3f)
            .scale(scale)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
            .onFocusChanged { isFocused = it.isFocused },

        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isFocused) 8.dp else 2.dp
        )
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            PosterImage(
                url = item.posterUrl,
                modifier = Modifier.fillMaxSize()
            )
            
            if (item.isNew) {
                NewBadge(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                )
            }
            
            if (item.isFavorite) {
                Icon(
                    Icons.Default.Favorite,
                    contentDescription = "已收藏",
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(8.dp)
                        .size(20.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            
            if (item.progress > 0f) {
                ProgressBar(
                    progress = item.progress,
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .fillMaxWidth()
                )
            }
            
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.8f)
                            )
                        )
                    )
                    .padding(12.dp)
            ) {
                Column {
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.titleSmall,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    item.subtitle?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.7f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
            
            if (item.rating != null) {
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp),
                    shape = RoundedCornerShape(4.dp),
                    color = Color.Black.copy(alpha = 0.6f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Star,
                            contentDescription = null,
                            modifier = Modifier.size(12.dp),
                            tint = Color(0xFFFFD700)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = String.format("%.1f", item.rating),
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White
                        )
                    }
                }
            }
            
            // 悬浮图标
            if (isFocused) {
                Row(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .background(
                            color = Color.Black.copy(alpha = 0.6f),
                            shape = RoundedCornerShape(20.dp)
                        )
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onClick,
                        modifier = Modifier
                            .size(32.dp)
                            .background(
                                color = MaterialTheme.colorScheme.primary,
                                shape = CircleShape
                            )
                    ) {
                        Icon(
                            Icons.Default.PlayArrow,
                            contentDescription = "播放",
                            tint = Color.White
                        )
                    }
                    IconButton(
                        onClick = onLongClick,
                        modifier = Modifier
                            .size(32.dp)
                            .background(
                                color = MaterialTheme.colorScheme.secondary,
                                shape = CircleShape
                            )
                    ) {
                        Icon(
                            Icons.Default.MoreVert,
                            contentDescription = "更多",
                            tint = Color.White
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MediaListItem(
    item: MediaItem,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(100.dp)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .width(70.dp)
                    .fillMaxHeight()
            ) {
                PosterImage(
                    url = item.posterUrl,
                    modifier = Modifier.fillMaxSize()
                )
                if (item.isNew) {
                    NewBadge(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(4.dp)
                    )
                }
            }
            
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(12.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    item.subtitle?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item.genres.take(2).forEach { genre ->
                        SuggestionChip(
                            onClick = {},
                            label = { Text(genre, style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }
                
                if (item.progress > 0f) {
                    LinearProgressIndicator(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(3.dp)
                            .clip(RoundedCornerShape(2.dp)),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                }
            }
            
            if (item.rating != null) {
                Column(
                    modifier = Modifier
                        .padding(12.dp)
                        .align(Alignment.CenterVertically),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.Star,
                        contentDescription = null,
                        tint = Color(0xFFFFD700),
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = String.format("%.1f", item.rating),
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MediaCompactItem(
    item: MediaItem,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            ),
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    PosterImage(
                        url = item.posterUrl,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                
                Column {
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.bodyLarge,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    item.subtitle?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1
                        )
                    }
                }
            }
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (item.isNew) {
                    NewBadge()
                }
                if (item.isFavorite) {
                    Icon(
                        Icons.Default.Favorite,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                Icon(
                    Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun NetworkTab() {
    var showUrlDialog by remember { mutableStateOf(false) }
    var url by remember { mutableStateOf("") }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "网络播放",
            style = MaterialTheme.typography.headlineSmall
        )
        
        NetworkActionCard(
            icon = Icons.Outlined.Link,
            title = "URL播放",
            subtitle = "输入网络链接直接播放",
            onClick = {
                showUrlDialog = true
            }
        )
        
        NetworkActionCard(
            icon = Icons.Outlined.Cast,
            title = "投屏",
            subtitle = "AirPlay / DLNA",
            onClick = {
                // Implement casting functionality
            }
        )
        
        NetworkActionCard(
            icon = Icons.Outlined.FolderOpen,
            title = "局域网",
            subtitle = "扫描SMB/FTP设备",
            onClick = {
                // Implement network scanning
            }
        )
        
        NetworkActionCard(
            icon = Icons.Outlined.Cloud,
            title = "云盘",
            subtitle = "OneDrive / 百度网盘",
            onClick = {
                // Implement cloud storage connection
            }
        )
    }
    
    // URL Playback Dialog
    if (showUrlDialog) {
        AlertDialog(
            onDismissRequest = { showUrlDialog = false },
            title = { Text("URL播放") },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OutlinedTextField(
                        value = url,
                        onValueChange = { url = it },
                        label = { Text("视频链接") },
                        placeholder = { Text("https://example.com/video.mp4") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        text = "支持 HTTP、HTTPS、RTMP、HLS 等协议",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (url.isNotEmpty()) {
                        // Handle URL playback
                        showUrlDialog = false
                    }
                }) {
                    Text("播放")
                }
            },
            dismissButton = {
                TextButton(onClick = { showUrlDialog = false }) {
                    Text("取消")
                }
            }
        )
    }
}

@Composable
fun NetworkActionCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(48.dp),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun RecentTab(
    embyRepository: EmbyRepository,
    onMediaClick: (MediaItem) -> Unit
) {
    val recentMedia = remember {
        (1..10).map { i ->
            MediaItem(
                id = i.toString(),
                title = "最近播放 $i",
                subtitle = "播放至 ${i * 10}%",
                progress = i / 10f,
                posterUrl = null
            )
        }
    }
    
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        TopAppBar(title = "最近播放")
        
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(recentMedia) { item ->
                MediaListItem(
                    item = item,
                    onClick = { onMediaClick(item) },
                    onLongClick = {}
                )
            }
        }
    }
}

@Composable
fun FavoritesTab(
    embyRepository: EmbyRepository,
    onMediaClick: (MediaItem) -> Unit
) {
    val favoriteMedia = remember {
        (1..8).map { i ->
            MediaItem(
                id = i.toString(),
                title = "收藏项目 $i",
                subtitle = "${2020 + i}",
                isFavorite = true,
                posterUrl = null
            )
        }
    }
    
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        TopAppBar(title = "我的收藏")
        
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 120.dp),
            contentPadding = PaddingValues(12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(favoriteMedia) { item ->
                MediaCard(
                    item = item,
                    onClick = { onMediaClick(item) },
                    onLongClick = {}
                )
            }
        }
    }
}

@Composable
fun SettingsTab(
    onSettingsClick: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        TopAppBar(title = "设置")
        
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                SettingsSection(title = "服务器")
            }
            item {
                SettingsItem(
                    icon = Icons.Outlined.Dns,
                    title = "服务器地址",
                    subtitle = "未配置",
                    onClick = {}
                )
            }
            item {
                SettingsItem(
                    icon = Icons.Outlined.Person,
                    title = "账户",
                    subtitle = "未登录",
                    onClick = {}
                )
            }
            
            item { Spacer(modifier = Modifier.height(8.dp)) }
            
            item {
                SettingsSection(title = "播放")
            }
            item {
                SettingsItem(
                    icon = Icons.Outlined.PlayCircleOutline,
                    title = "默认播放器",
                    subtitle = "Media3",
                    onClick = {}
                )
            }
            item {
                SettingsSwitch(
                    icon = Icons.Outlined.Memory,
                    title = "硬件解码",
                    subtitle = "使用GPU加速解码",
                    checked = true,
                    onCheckedChange = {}
                )
            }
            item {
                SettingsSwitch(
                    icon = Icons.Outlined.HighQuality,
                    title = "HDR支持",
                    subtitle = "自动检测HDR内容",
                    checked = true,
                    onCheckedChange = {}
                )
            }
            item {
                DefaultSubtitleSetting()
            }
            item {
                DefaultAudioSetting()
            }
            item {
                PreloadSettings()
            }
            
            item { Spacer(modifier = Modifier.height(8.dp)) }
            
            item {
                SettingsSection(title = "界面")
            }
            item {
                SettingsItem(
                    icon = Icons.Outlined.Palette,
                    title = "主题",
                    subtitle = "跟随系统",
                    onClick = {}
                )
            }
            item {
                SettingsItem(
                    icon = Icons.Outlined.Image,
                    title = "海报质量",
                    subtitle = "高清",
                    onClick = {}
                )
            }
            
            item { Spacer(modifier = Modifier.height(8.dp)) }
            
            item {
                SettingsSection(title = "高级")
            }
            item {
                SettingsItem(
                    icon = Icons.Outlined.BugReport,
                    title = "调试日志",
                    subtitle = "关闭",
                    onClick = {}
                )
            }
            item {
                SettingsItem(
                    icon = Icons.Outlined.Delete,
                    title = "清除缓存",
                    subtitle = "0 MB",
                    onClick = {}
                )
            }
            item {
                SettingsItem(
                    icon = Icons.Outlined.Info,
                    title = "关于",
                    subtitle = "版本 1.0.0",
                    onClick = {}
                )
            }
        }
    }
}

@Composable
fun SettingsSection(
    title: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = modifier.padding(vertical = 8.dp)
    )
}

@Composable
fun SettingsItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun SettingsSwitch(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange
            )
        }
    }
}

@Composable
fun PreloadSettings() {
    var isPreloadEnabled by remember { mutableStateOf(true) }
    var preloadSize by remember { mutableStateOf(2) } // 默认2GB
    var showSizeMenu by remember { mutableStateOf(false) }
    
    val sizeOptions = listOf(1, 2, 3, 4, 5) // 1GB到5GB
    
    Column {
        // 预加载开关
        SettingsSwitch(
            icon = Icons.Outlined.DownloadForOffline,
            title = "预加载功能",
            subtitle = "启用视频下一集智能预加载",
            checked = isPreloadEnabled,
            onCheckedChange = { isPreloadEnabled = it }
        )
        
        // 预加载大小选择
        if (isPreloadEnabled) {
            SettingsItem(
                icon = Icons.Outlined.Storage,
                title = "预加载大小",
                subtitle = "${sizeOptions[preloadSize]}GB",
                onClick = { showSizeMenu = true }
            )
            
            // 大小选择下拉菜单
            DropdownMenu(
                expanded = showSizeMenu,
                onDismissRequest = { showSizeMenu = false }
            ) {
                sizeOptions.forEachIndexed { index, size ->
                    DropdownMenuItem(
                        text = { Text("${size}GB") },
                        onClick = {
                            preloadSize = index
                            showSizeMenu = false
                            // 这里应该保存设置到用户配置
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun DefaultSubtitleSetting() {
    var selectedSubtitle by remember { mutableStateOf(0) }
    var showSubtitleMenu by remember { mutableStateOf(false) }
    
    val subtitleOptions = listOf(
        "自动",
        "中文",
        "英文",
        "无字幕"
    )
    
    Column {
        SettingsItem(
            icon = Icons.Outlined.ClosedCaption,
            title = "默认字幕",
            subtitle = subtitleOptions[selectedSubtitle],
            onClick = { showSubtitleMenu = true }
        )
        
        // 字幕选择下拉菜单
        DropdownMenu(
            expanded = showSubtitleMenu,
            onDismissRequest = { showSubtitleMenu = false }
        ) {
            subtitleOptions.forEachIndexed { index, option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        selectedSubtitle = index
                        showSubtitleMenu = false
                        // 这里应该保存设置到用户配置
                    }
                )
            }
        }
    }
}

@Composable
fun DefaultAudioSetting() {
    var selectedAudio by remember { mutableStateOf(0) }
    var showAudioMenu by remember { mutableStateOf(false) }
    
    val audioOptions = listOf(
        "自动",
        "中文",
        "英文",
        "原始音轨"
    )
    
    Column {
        SettingsItem(
            icon = Icons.AutoMirrored.Outlined.VolumeUp,
            title = "默认音频",
            subtitle = audioOptions[selectedAudio],
            onClick = { showAudioMenu = true }
        )
        
        // 音频选择下拉菜单
        DropdownMenu(
            expanded = showAudioMenu,
            onDismissRequest = { showAudioMenu = false }
        ) {
            audioOptions.forEachIndexed { index, option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        selectedAudio = index
                        showAudioMenu = false
                        // 这里应该保存设置到用户配置
                    }
                )
            }
        }
    }
}
