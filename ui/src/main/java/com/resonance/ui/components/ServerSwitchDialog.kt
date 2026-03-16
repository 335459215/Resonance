package com.resonance.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.resonance.data.model.ServerConfig
import com.resonance.data.model.ServerType
import com.resonance.ui.theme.AppColors
import com.resonance.ui.theme.AppShapes
import com.resonance.ui.theme.ForwardSpacing
import com.resonance.ui.theme.ForwardTypography
import kotlinx.coroutines.delay

/**
 * 服务器切换弹窗
 * 毛玻璃背景，显示可选的服务器列表
 */
@Composable
fun ServerSwitchDialog(
    currentServer: ServerConfig,
    availableServers: List<ServerConfig>,
    onServerSelected: (ServerConfig) -> Unit,
    onDismiss: () -> Unit,
    isSwitching: Boolean = false,
    modifier: Modifier = Modifier
) {
    // 淡入动画
    val alpha by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(300),
        label = "dialogAlpha"
    )
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.7f))
            .clickable(onClick = onDismiss)
            .alpha(alpha),
        contentAlignment = Alignment.Center
    ) {
        // 弹窗内容
        ServerSwitchDialogContent(
            currentServer = currentServer,
            availableServers = availableServers,
            onServerSelected = onServerSelected,
            isSwitching = isSwitching,
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .clip(AppShapes.Forward)
        )
    }
}

/**
 * 服务器切换弹窗内容
 */
@Composable
fun ServerSwitchDialogContent(
    currentServer: ServerConfig,
    availableServers: List<ServerConfig>,
    onServerSelected: (ServerConfig) -> Unit,
    isSwitching: Boolean,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = AppShapes.Forward,
        color = AppColors.DarkSurface.copy(alpha = 0.95f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(ForwardSpacing.ModuleSpacing),
            verticalArrangement = Arrangement.spacedBy(ForwardSpacing.CardSpacingVertical)
        ) {
            // 标题
            Text(
                text = "切换服务器",
                style = ForwardTypography.TitleLarge,
                color = AppColors.DarkOnBackground,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            // 当前服务器
            ServerSwitchItem(
                server = currentServer,
                isCurrent = true,
                isSelected = false,
                onClick = { },
                enabled = false
            )
            
            // 分隔线
            HorizontalDivider(
                color = AppColors.GlassBorder,
                thickness = 1.dp
            )
            
            // 可用服务器列表
            availableServers.forEach { server ->
                ServerSwitchItem(
                    server = server,
                    isCurrent = false,
                    isSelected = false,
                    onClick = { onServerSelected(server) },
                    enabled = !isSwitching
                )
            }
            
            // 加载中
            if (isSwitching) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = ForwardSpacing.CardSpacingVertical),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = AppColors.Primary,
                            strokeWidth = 2.dp
                        )
                        Text(
                            text = "切换中...",
                            style = ForwardTypography.BodyMedium,
                            color = AppColors.DarkOnSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

/**
 * 服务器切换项
 */
@Composable
fun ServerSwitchItem(
    server: ServerConfig,
    isCurrent: Boolean,
    isSelected: Boolean,
    onClick: () -> Unit,
    enabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    val backgroundColor = when {
        isCurrent -> AppColors.GlassMorphismDark.copy(alpha = 0.8f)
        isSelected -> AppColors.Primary.copy(alpha = 0.2f)
        isPressed -> AppColors.DarkSurfaceVariant
        else -> Color.Transparent
    }
    
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(AppShapes.Forward),
        color = backgroundColor,
        onClick = onClick,
        enabled = enabled
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(ForwardSpacing.CardSpacingVertical),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 类型图标
            ServerTypeIcon(
                type = server.type,
                modifier = Modifier.size(40.dp)
            )
            
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
                    
                    if (isCurrent) {
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
                    imageVector = Icons.Default.Check,
                    contentDescription = "在线",
                    tint = AppColors.Success,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

/**
 * 服务器类型图标
 */
@Composable
fun ServerTypeIcon(
    type: ServerType,
    modifier: Modifier = Modifier
) {
    val (icon, color) = when (type) {
        ServerType.VIDEO -> Icons.Default.Movie to AppColors.Primary
        ServerType.MUSIC -> Icons.Default.MusicNote to AppColors.Accent
        ServerType.EMBY -> Icons.Default.Movie to AppColors.Primary
        ServerType.PLEX -> Icons.Default.Movie to AppColors.Primary
        ServerType.LOCAL -> Icons.Default.Dns to AppColors.DarkOnSurfaceVariant
        ServerType.CLOUD_115 -> Icons.Default.Dns to AppColors.Primary
        ServerType.CLOUD_123 -> Icons.Default.Dns to AppColors.Primary
    }
    
    Surface(
        modifier = modifier,
        shape = AppShapes.Forward,
        color = color.copy(alpha = 0.2f)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = type.name,
                tint = color,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp)
            )
        }
    }
}

/**
 * 服务器切换动画覆盖层
 * 在全局 UI 切换时显示淡入淡出效果
 */
@Composable
fun ServerSwitchOverlay(
    isSwitching: Boolean,
    progress: Float,
    modifier: Modifier = Modifier
) {
    val alpha by animateFloatAsState(
        targetValue = if (isSwitching) 0.5f else 0f,
        animationSpec = tween(150),
        label = "overlayAlpha"
    )
    
    AnimatedVisibility(
        visible = isSwitching,
        enter = fadeIn(animationSpec = tween(150)),
        exit = fadeOut(animationSpec = tween(150))
    ) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(Color.Black)
                .alpha(alpha)
        )
    }
}

/**
 * 服务器切换指示器
 * 显示切换进度
 */
@Composable
fun ServerSwitchIndicator(
    isSwitching: Boolean,
    progress: Float,
    modifier: Modifier = Modifier
) {
    if (!isSwitching) return
    
    val scale by animateFloatAsState(
        targetValue = if (isSwitching) 1f else 0f,
        animationSpec = spring(
            dampingRatio = 0.8f,
            stiffness = Spring.StiffnessMedium
        ),
        label = "indicatorScale"
    )
    
    Box(
        modifier = modifier
            .size(60.dp)
            .scale(scale),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            modifier = Modifier.fillMaxSize(),
            color = AppColors.Primary,
            strokeWidth = 3.dp,
            trackColor = AppColors.DarkSurfaceVariant,
            progress = { progress }
        )
        
        // 服务器类型图标
        Icon(
            imageVector = Icons.Default.Dns,
            contentDescription = "切换中",
            tint = AppColors.Primary,
            modifier = Modifier.size(24.dp)
        )
    }
}
