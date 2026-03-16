package com.resonance.ui.model

import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.automirrored.outlined.Sort
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.filled.*

/**
 * 底部导航项
 */
data class NavigationItem(
    val id: String,
    val label: String,
    val icon: ImageVector,
    val selectedIcon: ImageVector? = null,
    val badge: Int? = null
)

/**
 * 导航配置
 * 包含核心 Tab 和辅助按钮
 */
data class NavigationConfig(
    val coreTabs: List<NavigationItem>,      // 中间 3 个核心 Tab
    val leftButton: NavigationItem? = null,   // 左侧辅助按钮
    val rightButton: NavigationItem? = null   // 右侧辅助按钮
)

/**
 * 导航状态
 */
sealed class NavigationState {
    object Loading : NavigationState()
    data class Success(val config: NavigationConfig) : NavigationState()
    data class Error(val message: String) : NavigationState()
}

/**
 * 服务器模式导航配置
 */
object NavigationConfigs {
    /**
     * 影视服务器导航配置
     */
    val VideoNavigation = NavigationConfig(
        coreTabs = listOf(
            NavigationItem(
                id = "discovery",
                label = "发现",
                icon = Icons.Outlined.Home,
                selectedIcon = Icons.Filled.Home
            ),
            NavigationItem(
                id = "series",
                label = "追剧",
                icon = Icons.Outlined.Movie,
                selectedIcon = Icons.Filled.Movie
            ),
            NavigationItem(
                id = "settings",
                label = "设置",
                icon = Icons.Outlined.Settings,
                selectedIcon = Icons.Filled.Settings
            )
        ),
        leftButton = NavigationItem(
            id = "server",
            label = "服务器",
            icon = Icons.Outlined.Dns,
            selectedIcon = Icons.Filled.Dns
        ),
        rightButton = NavigationItem(
            id = "search",
            label = "搜索",
            icon = Icons.Outlined.Search,
            selectedIcon = Icons.Filled.Search
        )
    )
    
    /**
     * 音乐服务器导航配置
     */
    val MusicNavigation = NavigationConfig(
        coreTabs = listOf(
            NavigationItem(
                id = "discovery",
                label = "发现",
                icon = Icons.Outlined.Home,
                selectedIcon = Icons.Filled.Home
            ),
            NavigationItem(
                id = "playlist",
                label = "歌单",
                icon = Icons.Outlined.MusicNote,
                selectedIcon = Icons.Filled.MusicNote
            ),
            NavigationItem(
                id = "settings",
                label = "设置",
                icon = Icons.Outlined.Settings,
                selectedIcon = Icons.Filled.Settings
            )
        ),
        leftButton = NavigationItem(
            id = "server",
            label = "服务器",
            icon = Icons.Outlined.Dns,
            selectedIcon = Icons.Filled.Dns
        ),
        rightButton = NavigationItem(
            id = "search",
            label = "搜索",
            icon = Icons.Outlined.Search,
            selectedIcon = Icons.Filled.Search
        )
    )
    
    /**
     * 根据页面获取辅助按钮配置
     */
    fun getNavigationConfigForPage(serverType: String, pageId: String): NavigationConfig {
        val baseConfig = if (serverType == "music") MusicNavigation else VideoNavigation
        
        return when (pageId) {
            "discovery" -> baseConfig.copy(
                rightButton = NavigationItem(
                    id = "search",
                    label = "搜索",
                    icon = Icons.Outlined.Search,
                    selectedIcon = Icons.Filled.Search
                )
            )
            "series", "playlist" -> baseConfig.copy(
                leftButton = NavigationItem(
                    id = "filter",
                    label = "筛选",
                    icon = Icons.AutoMirrored.Outlined.Sort,
                    selectedIcon = Icons.AutoMirrored.Filled.Sort
                ),
                rightButton = NavigationItem(
                    id = "add",
                    label = "添加",
                    icon = Icons.Outlined.Add,
                    selectedIcon = Icons.Filled.Add
                )
            )
            "settings" -> baseConfig.copy(
                leftButton = NavigationItem(
                    id = "modules",
                    label = "模块",
                    icon = Icons.Outlined.ViewModule,
                    selectedIcon = Icons.Filled.ViewModule
                ),
                rightButton = NavigationItem(
                    id = "download",
                    label = "下载",
                    icon = Icons.Outlined.Download,
                    selectedIcon = Icons.Filled.Download
                )
            )
            else -> baseConfig
        }
    }
}
