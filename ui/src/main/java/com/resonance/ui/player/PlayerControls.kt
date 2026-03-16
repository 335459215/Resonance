package com.resonance.ui.player

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * 格式化时间
 */
private fun formatPlayerTime(millis: Long): String {
    val totalSeconds = millis / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format("%02d:%02d", minutes, seconds)
}

/**
 * 播放控制界面
 */
@Composable
fun PlayerControls(
    playerService: MdkPlayerService,
    onPrevious: () -> Unit = {},
    onNext: () -> Unit = {},
    onBack: () -> Unit = {}
) {
    var showControls by remember { mutableStateOf(true) }
    var showAudioDialog by remember { mutableStateOf(false) }
    var showSubtitleDialog by remember { mutableStateOf(false) }
    var showSpeedDialog by remember { mutableStateOf(false) }
    var showRatioDialog by remember { mutableStateOf(false) }
    
    val coroutineScope = rememberCoroutineScope()
    
    // 自动隐藏控制界面
    LaunchedEffect(showControls) {
        if (showControls && playerService.isPlaying.value) {
            coroutineScope.launch {
                delay(3000)
                showControls = false
            }
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent)
            .clickable {
                showControls = !showControls
            }
    ) {
        // 顶部控制栏
        AnimatedVisibility(
            visible = showControls,
            enter = fadeIn() + slideInVertically(initialOffsetY = { -it }),
            exit = fadeOut() + slideOutVertically(targetOffsetY = { -it })
        ) {
            TopControls(
                onBack = onBack,
                title = "当前播放",
                subtitle = "视频标题"
            )
        }
        
        // 中间控制区
        AnimatedVisibility(
            visible = showControls,
            enter = fadeIn() + scaleIn(),
            exit = fadeOut() + scaleOut()
        ) {
            CentralControls(
                playerService = playerService,
                onPrevious = onPrevious,
                onNext = onNext
            )
        }
        
        // 底部控制栏
        AnimatedVisibility(
            visible = showControls,
            enter = fadeIn() + slideInVertically(initialOffsetY = { it }),
            exit = fadeOut() + slideOutVertically(targetOffsetY = { it })
        ) {
            BottomControls(
                playerService = playerService,
                onAudioClick = { showAudioDialog = true },
                onSubtitleClick = { showSubtitleDialog = true },
                onSpeedClick = { showSpeedDialog = true },
                onRatioClick = { showRatioDialog = true }
            )
        }
        
        // 音频选择对话框
        if (showAudioDialog) {
            AudioSelectionDialog(
                onDismiss = { showAudioDialog = false }
            )
        }
        
        // 字幕选择对话框
        if (showSubtitleDialog) {
            SubtitleSelectionDialog(
                onDismiss = { showSubtitleDialog = false }
            )
        }
        
        // 播放速度选择对话框
        if (showSpeedDialog) {
            SpeedSelectionDialog(
                onDismiss = { showSpeedDialog = false }
            )
        }
        
        // 画面比例选择对话框
        if (showRatioDialog) {
            RatioSelectionDialog(
                onDismiss = { showRatioDialog = false }
            )
        }
    }
}

/**
 * 顶部控制栏
 */
@Composable
fun TopControls(
    onBack: () -> Unit,
    title: String,
    subtitle: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Black.copy(alpha = 0.7f))
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBack) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "返回",
                tint = Color.White
            )
        }
        
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                color = Color.White,
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = subtitle,
                color = Color.White.copy(alpha = 0.7f),
                style = MaterialTheme.typography.bodySmall
            )
        }
        
        IconButton(onClick = {}) {
            Icon(
                imageVector = Icons.Default.MoreVert,
                contentDescription = "更多",
                tint = Color.White
            )
        }
    }
}

/**
 * 中间控制区
 */
@Composable
fun CentralControls(
    playerService: MdkPlayerService,
    onPrevious: () -> Unit,
    onNext: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(32.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 上一集按钮
            IconButton(
                onClick = onPrevious,
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.5f))
            ) {
                Icon(
                    imageVector = Icons.Default.SkipPrevious,
                    contentDescription = "上一集",
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }
            
            // 播放/暂停按钮
            IconButton(
                onClick = {
                    playerService.togglePlay()
                },
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
            ) {
                Icon(
                    imageVector = if (playerService.isPlaying.value) {
                        Icons.Default.Pause
                    } else {
                        Icons.Default.PlayArrow
                    },
                    contentDescription = if (playerService.isPlaying.value) "暂停" else "播放",
                    tint = Color.White,
                    modifier = Modifier.size(40.dp)
                )
            }
            
            // 下一集按钮
            IconButton(
                onClick = onNext,
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.5f))
            ) {
                Icon(
                    imageVector = Icons.Default.SkipNext,
                    contentDescription = "下一集",
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}

/**
 * 底部控制栏
 */
@Composable
fun BottomControls(
    playerService: MdkPlayerService,
    onAudioClick: () -> Unit,
    onSubtitleClick: () -> Unit,
    onSpeedClick: () -> Unit,
    onRatioClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Black.copy(alpha = 0.7f))
            .padding(16.dp)
    ) {
        // 进度条
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = formatPlayerTime(playerService.currentPosition.value),
                color = Color.White,
                style = MaterialTheme.typography.bodySmall
            )
            
            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp)
            ) {
                // 进度条背景
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .background(Color.White.copy(alpha = 0.3f))
                        .clip(CircleShape)
                )
                
                // 进度条前景
                Box(
                    modifier = Modifier
                        .fillMaxWidth(fraction = if (playerService.duration.value > 0) {
                            playerService.currentPosition.value.toFloat() / playerService.duration.value.toFloat()
                        } else {
                            0f
                        })
                        .height(4.dp)
                        .background(MaterialTheme.colorScheme.primary)
                        .clip(CircleShape)
                )
            }
            
            Text(
                text = formatPlayerTime(playerService.duration.value),
                color = Color.White,
                style = MaterialTheme.typography.bodySmall
            )
        }
        
        // 控制按钮
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            // 音频选择
            ControlButton(
                icon = Icons.AutoMirrored.Filled.VolumeUp,
                label = "音频",
                onClick = onAudioClick
            )
            
            // 字幕选择
            ControlButton(
                icon = Icons.Default.ClosedCaption,
                label = "字幕",
                onClick = onSubtitleClick
            )
            
            // 播放速度
            ControlButton(
                icon = Icons.Default.Speed,
                label = "1.0x",
                onClick = onSpeedClick
            )
            
            // 画面比例
            ControlButton(
                icon = Icons.Default.AspectRatio,
                label = "16:9",
                onClick = onRatioClick
            )
        }
    }
}

/**
 * 控制按钮
 */
@Composable
fun ControlButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = Color.White,
            modifier = Modifier.size(24.dp)
        )
        Text(
            text = label,
            color = Color.White,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

/**
 * 音频选择对话框
 */
@Composable
fun AudioSelectionDialog(
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("选择音频") },
        text = {
            Column {
                RadioButton(
                    selected = true,
                    onClick = {},
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                Text("中文 (立体声)")
                
                RadioButton(
                    selected = false,
                    onClick = {},
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                Text("英文 (5.1环绕声)")
                
                RadioButton(
                    selected = false,
                    onClick = {},
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                Text("原声 (立体声)")
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("确定")
            }
        }
    )
}

/**
 * 字幕选择对话框
 */
@Composable
fun SubtitleSelectionDialog(
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("选择字幕") },
        text = {
            Column {
                RadioButton(
                    selected = true,
                    onClick = {},
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                Text("自动")
                
                RadioButton(
                    selected = false,
                    onClick = {},
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                Text("中文")
                
                RadioButton(
                    selected = false,
                    onClick = {},
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                Text("英文")
                
                RadioButton(
                    selected = false,
                    onClick = {},
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                Text("无字幕")
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("确定")
            }
        }
    )
}

/**
 * 播放速度选择对话框
 */
@Composable
fun SpeedSelectionDialog(
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("播放速度") },
        text = {
            Column {
                RadioButton(
                    selected = false,
                    onClick = {},
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                Text("0.5x")
                
                RadioButton(
                    selected = false,
                    onClick = {},
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                Text("0.75x")
                
                RadioButton(
                    selected = true,
                    onClick = {},
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                Text("1.0x")
                
                RadioButton(
                    selected = false,
                    onClick = {},
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                Text("1.25x")
                
                RadioButton(
                    selected = false,
                    onClick = {},
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                Text("1.5x")
                
                RadioButton(
                    selected = false,
                    onClick = {},
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                Text("2.0x")
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("确定")
            }
        }
    )
}

/**
 * 画面比例选择对话框
 */
@Composable
fun RatioSelectionDialog(
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("画面比例") },
        text = {
            Column {
                RadioButton(
                    selected = true,
                    onClick = {},
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                Text("自动")
                
                RadioButton(
                    selected = false,
                    onClick = {},
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                Text("16:9")
                
                RadioButton(
                    selected = false,
                    onClick = {},
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                Text("4:3")
                
                RadioButton(
                    selected = false,
                    onClick = {},
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                Text("原始")
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("确定")
            }
        }
    )
}


