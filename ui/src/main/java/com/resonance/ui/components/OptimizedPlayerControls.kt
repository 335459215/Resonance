package com.resonance.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.resonance.ui.theme.UIDesignSystem
import java.util.Locale

/**
 * 优化的播放器控制按钮
 */
@Composable
fun OptimizedPlayerControlButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isSelected: Boolean = false,
    size: PlayerControlSize = PlayerControlSize.Medium
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.85f else 1f,
        animationSpec = tween(100),
        label = "scale"
    )
    
    val backgroundColor by animateColorAsState(
        targetValue = when {
            isSelected -> UIDesignSystem.Colors.Primary
            isPressed -> UIDesignSystem.Colors.SurfaceVariant
            else -> Color.Transparent
        },
        label = "backgroundColor"
    )
    
    val iconColor by animateColorAsState(
        targetValue = when {
            isSelected -> UIDesignSystem.Colors.OnPrimary
            isPressed -> UIDesignSystem.Colors.OnSurface
            else -> UIDesignSystem.Colors.OnSurface
        },
        label = "iconColor"
    )
    
    val buttonSize = when (size) {
        PlayerControlSize.Small -> UIDesignSystem.PlayerStyles.ControlButtonSize
        PlayerControlSize.Medium -> UIDesignSystem.PlayerStyles.ControlButtonSize * 1.2f
        PlayerControlSize.Large -> UIDesignSystem.PlayerStyles.LargeControlButtonSize
    }
    
    Box(
        modifier = modifier
            .size(buttonSize)
            .scale(scale)
            .clip(CircleShape)
            .background(backgroundColor)
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = iconColor,
            modifier = Modifier.size(UIDesignSystem.IconSize.Lg)
        )
    }
}

/**
 * 播放器控制按钮尺寸
 */
enum class PlayerControlSize {
    Small,
    Medium,
    Large
}

/**
 * 优化的播放/暂停按钮
 */
@Composable
fun OptimizedPlayPauseButton(
    isPlaying: Boolean,
    onPlayPause: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.9f else 1f,
        animationSpec = tween(100),
        label = "scale"
    )
    
    Box(
        modifier = modifier
            .size(UIDesignSystem.PlayerStyles.LargeControlButtonSize * 1.5f)
            .scale(scale)
            .clip(CircleShape)
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(
                        UIDesignSystem.Colors.Primary,
                        UIDesignSystem.Colors.PrimaryVariant
                    )
                )
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) { onPlayPause() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
            contentDescription = if (isPlaying) "暂停" else "播放",
            tint = UIDesignSystem.Colors.OnPrimary,
            modifier = Modifier.size(UIDesignSystem.IconSize.Xxl)
        )
    }
}

/**
 * 优化的进度条
 */
@Composable
fun OptimizedPlayerSeekBar(
    value: Float,
    onValueChange: (Float) -> Unit,
    onValueChangeFinished: () -> Unit,
    modifier: Modifier = Modifier,
    bufferedValue: Float = 0f,
    showTimeLabels: Boolean = true,
    duration: Long = 0L
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(UIDesignSystem.Spacing.Sm)
    ) {
        // 进度条
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(UIDesignSystem.PlayerStyles.ProgressbarHeight)
                .clip(RoundedCornerShape(UIDesignSystem.Radius.Full))
                .background(UIDesignSystem.Colors.SurfaceVariant)
        ) {
            // 缓冲进度
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(UIDesignSystem.PlayerStyles.ProgressbarHeight)
                    .background(
                        UIDesignSystem.Colors.SurfaceVariant.copy(alpha = 0.5f)
                    )
            )
            
            // 播放进度
            Box(
                modifier = Modifier
                    .fillMaxWidth(bufferedValue)
                    .height(UIDesignSystem.PlayerStyles.ProgressbarHeight)
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                UIDesignSystem.Colors.Primary,
                                UIDesignSystem.Colors.Secondary
                            )
                        )
                    )
            )
            
            // 当前进度
            Box(
                modifier = Modifier
                    .fillMaxWidth(value)
                    .height(UIDesignSystem.PlayerStyles.ProgressbarHeight)
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                UIDesignSystem.Colors.Primary,
                                UIDesignSystem.Colors.Accent
                            )
                        )
                    )
            )
            
            // 拖动点
            var isPressed by remember { mutableStateOf(false) }
            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .fillMaxWidth(value)
                    .offset(x = (-12).dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(UIDesignSystem.Colors.Primary)
                        .scale(if (isPressed) 1.2f else 1f)
                )
            }
        }
        
        // 时间标签
        if (showTimeLabels && duration > 0) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = formatTime((value * duration).toLong()),
                    color = UIDesignSystem.Colors.OnSurfaceVariant,
                    fontSize = UIDesignSystem.FontSize.Xs,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = formatTime(duration),
                    color = UIDesignSystem.Colors.OnSurfaceVariant,
                    fontSize = UIDesignSystem.FontSize.Xs,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

/**
 * 优化的音量/亮度滑块
 */
@Composable
fun OptimizedSliderControl(
    value: Float,
    onValueChange: (Float) -> Unit,
    icon: ImageVector,
    label: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(UIDesignSystem.Spacing.Sm)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = UIDesignSystem.Colors.OnSurface,
            modifier = Modifier.size(UIDesignSystem.IconSize.Md)
        )
        
        Box(
            modifier = Modifier
                .width(UIDesignSystem.PlayerStyles.VolumeSliderWidth)
                .height(4.dp)
                .clip(RoundedCornerShape(UIDesignSystem.Radius.Full))
                .background(UIDesignSystem.Colors.SurfaceVariant)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(value)
                    .height(4.dp)
                    .clip(RoundedCornerShape(UIDesignSystem.Radius.Full))
                    .background(UIDesignSystem.Colors.Primary)
            )
        }
    }
}

/**
 * 优化的媒体信息卡片
 */
@Composable
fun OptimizedMediaInfoCard(
    title: String,
    subtitle: String,
    description: String? = null,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(UIDesignSystem.Spacing.Md)
    ) {
        Text(
            text = title,
            color = UIDesignSystem.Colors.OnSurface,
            fontSize = UIDesignSystem.FontSize.Xl,
            fontWeight = FontWeight.Bold,
            maxLines = 2
        )
        
        Text(
            text = subtitle,
            color = UIDesignSystem.Colors.OnSurfaceVariant,
            fontSize = UIDesignSystem.FontSize.Sm,
            maxLines = 1
        )
        
        if (description != null) {
            Text(
                text = description,
                color = UIDesignSystem.Colors.OnSurfaceVariant.copy(alpha = 0.8f),
                fontSize = UIDesignSystem.FontSize.Xs,
                maxLines = 3
            )
        }
    }
}

/**
 * 格式化工具函数
 */
private fun formatTime(milliseconds: Long): String {
    val totalSeconds = milliseconds / 1000
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    
    return if (hours > 0) {
        String.format(Locale.getDefault(), "%d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format(Locale.getDefault(), "%d:%02d", minutes, seconds)
    }
}

/**
 * 优化的播放列表项
 */
@Composable
fun OptimizedPlaylistItem(
    title: String,
    subtitle: String,
    duration: String,
    isPlaying: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    val backgroundColor by animateColorAsState(
        targetValue = when {
            isPlaying -> UIDesignSystem.Colors.Primary.copy(alpha = 0.1f)
            isPressed -> UIDesignSystem.Colors.SurfaceVariant
            else -> Color.Transparent
        },
        label = "backgroundColor"
    )
    
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(UIDesignSystem.Radius.Md))
            .background(backgroundColor)
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) { onClick() }
            .padding(UIDesignSystem.Spacing.Md),
        horizontalArrangement = Arrangement.spacedBy(UIDesignSystem.Spacing.Md),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 播放状态图标
        if (isPlaying) {
            Icon(
                imageVector = Icons.Filled.Equalizer,
                contentDescription = "播放中",
                tint = UIDesignSystem.Colors.Primary,
                modifier = Modifier.size(UIDesignSystem.IconSize.Md)
            )
        } else {
            Box(modifier = Modifier.size(UIDesignSystem.IconSize.Md))
        }
        
        // 媒体信息
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(UIDesignSystem.Spacing.Xxs)
        ) {
            Text(
                text = title,
                color = if (isPlaying) UIDesignSystem.Colors.Primary else UIDesignSystem.Colors.OnSurface,
                fontSize = UIDesignSystem.FontSize.Md,
                fontWeight = if (isPlaying) FontWeight.SemiBold else FontWeight.Normal,
                maxLines = 1
            )
            Text(
                text = subtitle,
                color = UIDesignSystem.Colors.OnSurfaceVariant,
                fontSize = UIDesignSystem.FontSize.Xs,
                maxLines = 1
            )
        }
        
        // 时长
        Text(
            text = duration,
            color = UIDesignSystem.Colors.OnSurfaceVariant,
            fontSize = UIDesignSystem.FontSize.Xs,
            fontWeight = FontWeight.Medium
        )
    }
}
