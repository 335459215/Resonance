package com.resonance.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.resonance.data.model.ServerConfig
import com.resonance.data.repository.ServerRepository
import com.resonance.ui.components.ForwardButton
import com.resonance.ui.components.ForwardServerList
import com.resonance.ui.theme.AppColors
import com.resonance.ui.theme.ForwardSpacing
import com.resonance.ui.theme.ForwardTypography

/**
 * 服务器列表屏幕
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServerListScreen(
    servers: List<ServerConfig>,
    currentServerId: String?,
    onNavigateBack: () -> Unit,
    onServerSelect: (ServerConfig) -> Unit,
    onServerEdit: (ServerConfig) -> Unit,
    onServerDelete: (ServerConfig) -> Unit,
    onAddServer: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showMoreMenu by remember { mutableStateOf(false) }
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(AppColors.DarkBackground)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // 顶部导航栏
            ServerListAppBar(
                onNavigateBack = onNavigateBack,
                onAddClick = onAddServer,
                onMoreClick = { showMoreMenu = true }
            )
            
            // 服务器列表
            ForwardServerList(
                servers = servers,
                currentServerId = currentServerId,
                onServerClick = onServerSelect,
                onServerEdit = onServerEdit,
                onServerDelete = onServerDelete,
                showAddButton = true,
                onAddClick = onAddServer,
                modifier = Modifier.weight(1f)
            )
        }
        
        // 更多菜单
        DropdownMenu(
            expanded = showMoreMenu,
            onDismissRequest = { showMoreMenu = false }
        ) {
            DropdownMenuItem(
                text = { Text("帮助") },
                onClick = {
                    showMoreMenu = false
                    // 帮助功能待实现
                }
            )
            DropdownMenuItem(
                text = { Text("关于") },
                onClick = {
                    showMoreMenu = false
                    // 关于功能待实现
                }
            )
        }
    }
}

/**
 * 服务器列表顶部导航栏
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServerListAppBar(
    onNavigateBack: () -> Unit,
    onAddClick: () -> Unit,
    onMoreClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    TopAppBar(
        title = {
            Text(
                text = "服务器管理",
                style = ForwardTypography.TitleLarge,
                color = AppColors.DarkOnBackground
            )
        },
        modifier = modifier,
        navigationIcon = {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "返回",
                    tint = AppColors.DarkOnBackground
                )
            }
        },
        actions = {
            IconButton(onClick = onAddClick) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "添加",
                    tint = AppColors.Primary
                )
            }
            
            IconButton(onClick = onMoreClick) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "更多",
                    tint = AppColors.DarkOnBackground
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Transparent,
            scrolledContainerColor = AppColors.DarkSurface.copy(alpha = 0.9f)
        )
    )
}
