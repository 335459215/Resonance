package com.resonance.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.resonance.data.model.ServerConfig
import com.resonance.data.model.ServerType
import com.resonance.ui.theme.AppColors
import com.resonance.ui.theme.AppShapes
import com.resonance.ui.theme.ForwardSpacing
import com.resonance.ui.theme.ForwardTypography

/**
 * Forward 风格服务器列表
 * 分音乐/影视两组展示
 */
@Composable
fun ForwardServerList(
    servers: List<ServerConfig>,
    currentServerId: String?,
    onServerClick: (ServerConfig) -> Unit,
    onServerEdit: (ServerConfig) -> Unit,
    onServerDelete: (ServerConfig) -> Unit,
    modifier: Modifier = Modifier,
    showAddButton: Boolean = true,
    onAddClick: () -> Unit = {}
) {
    // 按服务器类型分组
    val videoServers = servers.filter { it.isVideoServer() }
    val musicServers = servers.filter { it.isMusicServer() }
    
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(ForwardSpacing.PhoneMargin),
        verticalArrangement = Arrangement.spacedBy(ForwardSpacing.ModuleSpacing)
    ) {
        // 影视服务器组
        if (videoServers.isNotEmpty()) {
            item {
                ServerGroupHeader(
                    title = "影视服务器",
                    icon = Icons.Default.Movie
                )
            }
            
            items(videoServers, key = { it.id }) { server ->
                ServerListItem(
                    server = server,
                    isCurrentServer = server.id == currentServerId,
                    onClick = { onServerClick(server) },
                    onEdit = { onServerEdit(server) },
                    onDelete = { onServerDelete(server) }
                )
            }
        }
        
        // 音乐服务器组
        if (musicServers.isNotEmpty()) {
            item {
                ServerGroupHeader(
                    title = "音乐服务器",
                    icon = Icons.Default.MusicNote
                )
            }
            
            items(musicServers, key = { it.id }) { server ->
                ServerListItem(
                    server = server,
                    isCurrentServer = server.id == currentServerId,
                    onClick = { onServerClick(server) },
                    onEdit = { onServerEdit(server) },
                    onDelete = { onServerDelete(server) }
                )
            }
        }
        
        // 空状态
        if (servers.isEmpty()) {
            item {
                ForwardEmptyState(
                    title = "暂无服务器",
                    subtitle = "点击右上角添加服务器",
                    modifier = Modifier.padding(vertical = 40.dp)
                )
            }
        }
        
        // 底部占位空间
        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

/**
 * 服务器分组标题
 */
@Composable
fun ServerGroupHeader(
    title: String,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = AppColors.Primary,
            modifier = Modifier.size(20.dp)
        )
        
        Text(
            text = title,
            style = ForwardTypography.TitleMedium,
            color = AppColors.DarkOnSurfaceVariant
        )
    }
}

/**
 * 服务器列表项
 */
@Composable
fun ServerListItem(
    server: ServerConfig,
    isCurrentServer: Boolean,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showEditOptions by remember { mutableStateOf(false) }
    
    ServerCard(
        server = server,
        isCurrentServer = isCurrentServer,
        onClick = onClick,
        onLongClick = { showEditOptions = !showEditOptions },
        modifier = modifier
    )
    
    // 编辑选项（长按显示）
    AnimatedVisibility(
        visible = showEditOptions,
        exit = fadeOut(animationSpec = tween(200))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = ForwardSpacing.PhoneMargin, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(ForwardSpacing.CardSpacingHorizontal)
        ) {
            OutlinedButton(
                onClick = {
                    showEditOptions = false
                    onEdit()
                },
                modifier = Modifier.weight(1f),
                shape = AppShapes.Forward
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "编辑",
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("编辑")
            }
            
            OutlinedButton(
                onClick = {
                    showEditOptions = false
                    onDelete()
                },
                modifier = Modifier.weight(1f),
                shape = AppShapes.Forward,
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = AppColors.Error
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "删除",
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("删除")
            }
        }
    }
}

/**
 * 服务器卡片
 */
@Composable
fun ServerCard(
    server: ServerConfig,
    isCurrentServer: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(AppShapes.Forward),
        colors = CardDefaults.cardColors(
            containerColor = if (isCurrentServer) {
                AppColors.GlassMorphismDark.copy(alpha = 0.8f)
            } else {
                AppColors.GlassMorphismDark.copy(alpha = 0.6f)
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = onClick
                )
                .padding(ForwardSpacing.ModuleSpacing),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 服务器类型图标
            Surface(
                modifier = Modifier.size(48.dp),
                shape = AppShapes.Forward,
                color = if (server.isVideoServer()) AppColors.Primary else AppColors.Accent
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = server.getTypeIcon(),
                        style = ForwardTypography.TitleLarge
                    )
                }
            }
            
            // 服务器信息
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = server.name,
                        style = ForwardTypography.TitleMedium,
                        color = AppColors.DarkOnBackground,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    
                    if (isCurrentServer) {
                        Surface(
                            color = AppColors.Primary,
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = "当前",
                                style = ForwardTypography.LabelSmall,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
                
                Text(
                    text = server.url,
                    style = ForwardTypography.BodySmall,
                    color = AppColors.DarkOnSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            
            // 连接状态
            if (server.isConnected) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "在线",
                    tint = AppColors.Success,
                    modifier = Modifier.size(20.dp)
                )
            } else {
                Icon(
                    imageVector = Icons.Default.OfflinePin,
                    contentDescription = "离线",
                    tint = AppColors.DarkOnSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }
            
            // 箭头
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = AppColors.DarkOnSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
