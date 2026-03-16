package com.resonance.ui

import android.content.res.Configuration
import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.resonance.data.repository.EmbyRepository
import com.resonance.ui.mobile.MainScreen
import com.resonance.ui.repository.MenuRepository
import com.resonance.ui.tv.TVMainScreen

// --- Device Type Detection ---
@Composable
fun isTvDevice(): Boolean {
    val context = LocalContext.current
    val uiMode = context.resources.configuration.uiMode and Configuration.UI_MODE_TYPE_MASK
    return uiMode == Configuration.UI_MODE_TYPE_TELEVISION
}

// --- Adaptive UI Entry Point ---
@Composable
fun AdaptiveMainScreen(
    embyRepository: EmbyRepository,
    selectedServer: ServerConfig?,
    servers: List<ServerConfig>,
    onServerSelect: (ServerConfig) -> Unit,
    onMediaClick: (Any) -> Unit = {},
    onSearchClick: () -> Unit = {},
    onServersClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {}
) {
    val isTv = isTvDevice()
    
    if (isTv) {
        TVMainScreen(
            onMediaClick = {
                onMediaClick(it)
            },
            onSettingsClick = onSettingsClick
        )
    } else {
        MainScreen(
            embyRepository = embyRepository,
            menuRepository = MenuRepository(),
            selectedServer = selectedServer,
            servers = servers,
            onServerSelect = onServerSelect,
            onMediaClick = {
                onMediaClick(it)
            },
            onSearchClick = onSearchClick,
            onServersClick = onServersClick,
            onSettingsClick = onSettingsClick
        )
    }
}

// --- Server and Cloud Storage Types ---
enum class ServerType {
    EMBY,
    PLEX,
    LOCAL,
    CLOUD_115,
    CLOUD_123
}

// --- Server Configuration Data Class ---
data class ServerConfig(
    val id: String,
    val name: String,
    val url: String,
    val type: ServerType,
    val username: String = "",
    val password: String = "",
    val isDefault: Boolean = false,
    val isEnabled: Boolean = true
)

// --- Cloud Storage Configuration ---
data class CloudStorageConfig(
    val id: String,
    val type: ServerType,
    val name: String,
    val username: String = "",
    val password: String = "",
    val isConnected: Boolean = false
)