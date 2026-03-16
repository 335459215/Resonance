package com.resonance.ui.player

import android.view.KeyEvent
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

data class TrackInfo(
    val id: Int,
    val name: String,
    val language: String? = null,
    val isSelected: Boolean = false
)

data class SubtitleInfo(
    val id: Int,
    val name: String,
    val language: String? = null,
    val isSelected: Boolean = false
)

enum class LoopMode {
    NONE, SINGLE, ALL
}

enum class PlayerState {
    IDLE, LOADING, READY, PLAYING, PAUSED, ENDED, ERROR
}

@Composable
fun PlayerScreen(
    title: String = "",
    subtitle: String = "",
    isPlaying: Boolean = false,
    currentPosition: Long = 0L,
    duration: Long = 0L,
    bufferedPosition: Long = 0L,
    playerState: PlayerState = PlayerState.IDLE,
    audioTracks: List<TrackInfo> = emptyList(),
    subtitleTracks: List<SubtitleInfo> = emptyList(),
    onPlayPauseClick: () -> Unit = {},
    onSeek: (Long) -> Unit = {},
    onSeekForward: () -> Unit = {},
    onSeekBackward: () -> Unit = {},
    onSpeedChange: (Float) -> Unit = {},
    onAudioTrackSelect: (Int) -> Unit = {},
    onSubtitleSelect: (Int) -> Unit = {},
    onLoopModeChange: (LoopMode) -> Unit = {},
    onPiPClick: () -> Unit = {},
    onFullscreenClick: () -> Unit = {},
    onBackClick: () -> Unit = {},
    onLockToggle: (Boolean) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var controlsVisible by remember { mutableStateOf(true) }
    var isLocked by remember { mutableStateOf(false) }
    var showSpeedDialog by remember { mutableStateOf(false) }
    var showAudioDialog by remember { mutableStateOf(false) }
    var showSubtitleDialog by remember { mutableStateOf(false) }
    var showMoreOptions by remember { mutableStateOf(false) }
    var loopMode by remember { mutableStateOf(LoopMode.NONE) }
    var playbackSpeed by remember { mutableStateOf(1.0f) }
    
    val scope = rememberCoroutineScope()
    var boxSize by remember { mutableStateOf(IntSize.Zero) }
    
    LaunchedEffect(controlsVisible, isPlaying) {
        if (controlsVisible && isPlaying && !isLocked) {
            delay(5000)
            controlsVisible = false
        }
    }
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .onSizeChanged { boxSize = it }
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = {
                        if (!isLocked) {
                            controlsVisible = !controlsVisible
                        }
                    },
                    onDoubleTap = { offset ->
                        if (!isLocked) {
                            val centerX = boxSize.width / 2
                            when {
                                offset.x < centerX - boxSize.width * 0.2 -> {
                                    onSeekBackward()
                                }
                                offset.x > centerX + boxSize.width * 0.2 -> {
                                    onSeekForward()
                                }
                                else -> {
                                    onPlayPauseClick()
                                }
                            }
                        }
                    }
                )
            }
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .zIndex(0f),
            contentAlignment = Alignment.Center
        ) {
            if (playerState == PlayerState.LOADING) {
                CircularProgressIndicator(
                    modifier = Modifier.size(48.dp),
                    color = Color.White
                )
            } else if (playerState == PlayerState.ERROR) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Icon(
                        Icons.Default.ErrorOutline,
                        contentDescription = "播放错误",
                        modifier = Modifier.size(64.dp),
                        tint = Color.White.copy(alpha = 0.7f)
                    )
                    Text(
                        text = "播放出错",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                    Button(onClick = onPlayPauseClick) {
                        Text("重试")
                    }
                }
            }
        }
        
        if (!isLocked) {
            AnimatedVisibility(
                visible = controlsVisible,
                enter = fadeIn(tween(200)),
                exit = fadeOut(tween(200)),
                modifier = Modifier.zIndex(1f)
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    TopControlBar(
                        title = title,
                        subtitle = subtitle,
                        onBackClick = onBackClick,
                        onLockClick = { isLocked = true; onLockToggle(true) },
                        modifier = Modifier.align(Alignment.TopCenter)
                    )
                    
                    CenterControlButtons(
                        isPlaying = isPlaying,
                        onPlayPauseClick = onPlayPauseClick,
                        onSeekForward = onSeekForward,
                        onSeekBackward = onSeekBackward,
                        modifier = Modifier.align(Alignment.Center)
                    )
                    
                    BottomControlBar(
                        currentPosition = currentPosition,
                        duration = duration,
                        bufferedPosition = bufferedPosition,
                        isPlaying = isPlaying,
                        playbackSpeed = playbackSpeed,
                        loopMode = loopMode,
                        onSeek = onSeek,
                        onPlayPauseClick = onPlayPauseClick,
                        onSpeedClick = { showSpeedDialog = true },
                        onAudioClick = { showAudioDialog = true },
                        onSubtitleClick = { showSubtitleDialog = true },
                        onLoopClick = {
                            loopMode = when (loopMode) {
                                LoopMode.NONE -> LoopMode.SINGLE
                                LoopMode.SINGLE -> LoopMode.ALL
                                LoopMode.ALL -> LoopMode.NONE
                            }
                            onLoopModeChange(loopMode)
                        },
                        onPiPClick = onPiPClick,
                        onFullscreenClick = onFullscreenClick,
                        onMoreClick = { showMoreOptions = true },
                        modifier = Modifier.align(Alignment.BottomCenter)
                    )
                }
            }
        }
        
        if (isLocked) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .zIndex(2f),
                contentAlignment = Alignment.Center
            ) {
                FilledIconButton(
                    onClick = { 
                        isLocked = false
                        controlsVisible = true
                        onLockToggle(false)
                    },
                    modifier = Modifier.size(56.dp),
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = Color.Black.copy(alpha = 0.5f)
                    )
                ) {
                    Icon(
                        Icons.Default.LockOpen,
                        contentDescription = "解锁",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }
        
        GestureIndicators(
            modifier = Modifier.zIndex(3f)
        )
    }
    
    if (showSpeedDialog) {
        SpeedSelectionDialog(
            currentSpeed = playbackSpeed,
            onSpeedSelected = { speed ->
                playbackSpeed = speed
                onSpeedChange(speed)
                showSpeedDialog = false
            },
            onDismiss = { showSpeedDialog = false }
        )
    }
    
    if (showAudioDialog) {
        TrackSelectionDialog(
            title = "音轨选择",
            tracks = audioTracks.map { it.name to it.isSelected },
            onTrackSelected = { index ->
                onAudioTrackSelect(audioTracks[index].id)
                showAudioDialog = false
            },
            onDismiss = { showAudioDialog = false }
        )
    }
    
    if (showSubtitleDialog) {
        TrackSelectionDialog(
            title = "字幕选择",
            tracks = subtitleTracks.map { it.name to it.isSelected },
            onTrackSelected = { index ->
                onSubtitleSelect(subtitleTracks[index].id)
                showSubtitleDialog = false
            },
            onDismiss = { showSubtitleDialog = false }
        )
    }
    
    if (showMoreOptions) {
        MoreOptionsSheet(
            onDismiss = { showMoreOptions = false },
            onAspectRatioClick = {},
            onDecoderClick = {},
            onScreenshotClick = {},
            onRecordClick = {}
        )
    }
}

@Composable
fun TopControlBar(
    title: String,
    subtitle: String,
    onBackClick: () -> Unit,
    onLockClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color.Black.copy(alpha = 0.7f),
                        Color.Transparent
                    )
                )
            )
            .padding(horizontal = 8.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "返回",
                        tint = Color.White
                    )
                }
                
                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (subtitle.isNotEmpty()) {
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.7f),
                            maxLines = 1
                        )
                    }
                }
            }
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                IconButton(onClick = onLockClick) {
                    Icon(
                        Icons.Outlined.Lock,
                        contentDescription = "锁定",
                        tint = Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun CenterControlButtons(
    isPlaying: Boolean,
    onPlayPauseClick: () -> Unit,
    onSeekForward: () -> Unit,
    onSeekBackward: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(32.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        SeekButton(
            icon = Icons.Default.Replay10,
            contentDescription = "后退10秒",
            onClick = onSeekBackward
        )
        
        PlayPauseButton(
            isPlaying = isPlaying,
            onClick = onPlayPauseClick,
            size = 72
        )
        
        SeekButton(
            icon = Icons.Default.Forward10,
            contentDescription = "前进10秒",
            onClick = onSeekForward
        )
    }
}

@Composable
fun PlayPauseButton(
    isPlaying: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Int = 56
) {
    FilledIconButton(
        onClick = onClick,
        modifier = modifier.size(size.dp),
        colors = IconButtonDefaults.filledIconButtonColors(
            containerColor = Color.White.copy(alpha = 0.2f)
        )
    ) {
        Icon(
            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
            contentDescription = if (isPlaying) "暂停" else "播放",
            tint = Color.White,
            modifier = Modifier.size((size * 0.5f).dp)
        )
    }
}

@Composable
fun SeekButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    IconButton(
        onClick = onClick,
        modifier = modifier.size(48.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = Color.White,
            modifier = Modifier.size(36.dp)
        )
    }
}

@Composable
fun BottomControlBar(
    currentPosition: Long,
    duration: Long,
    bufferedPosition: Long,
    isPlaying: Boolean,
    playbackSpeed: Float,
    loopMode: LoopMode,
    onSeek: (Long) -> Unit,
    onPlayPauseClick: () -> Unit,
    onSpeedClick: () -> Unit,
    onAudioClick: () -> Unit,
    onSubtitleClick: () -> Unit,
    onLoopClick: () -> Unit,
    onPiPClick: () -> Unit,
    onFullscreenClick: () -> Unit,
    onMoreClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color.Transparent,
                        Color.Black.copy(alpha = 0.7f)
                    )
                )
            )
            .padding(horizontal = 8.dp, vertical = 12.dp)
    ) {
        VideoProgressBar(
            currentPosition = currentPosition,
            duration = duration,
            bufferedPosition = bufferedPosition,
            onSeek = onSeek
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = formatTime(currentPosition),
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White
                )
                Text(
                    text = "/",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White.copy(alpha = 0.5f)
                )
                Text(
                    text = formatTime(duration),
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White.copy(alpha = 0.7f)
                )
            }
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                if (playbackSpeed != 1.0f) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = MaterialTheme.colorScheme.primary
                    ) {
                        Text(
                            text = "${playbackSpeed}x",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
                
                IconButton(onClick = onSpeedClick, modifier = Modifier.size(40.dp)) {
                    Icon(
                        Icons.Outlined.Speed,
                        contentDescription = "倍速",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
                
                IconButton(onClick = onAudioClick, modifier = Modifier.size(40.dp)) {
                    Icon(
                        Icons.Outlined.AudioFile,
                        contentDescription = "音轨",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
                
                IconButton(onClick = onSubtitleClick, modifier = Modifier.size(40.dp)) {
                    Icon(
                        Icons.Outlined.Subtitles,
                        contentDescription = "字幕",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
                
                IconButton(onClick = onLoopClick, modifier = Modifier.size(40.dp)) {
                    Icon(
                        when (loopMode) {
                            LoopMode.NONE -> Icons.Outlined.Repeat
                            LoopMode.SINGLE -> Icons.Default.RepeatOne
                            LoopMode.ALL -> Icons.Default.Repeat
                        },
                        contentDescription = "循环",
                        tint = if (loopMode != LoopMode.NONE) MaterialTheme.colorScheme.primary
                               else Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
                
                IconButton(onClick = onPiPClick, modifier = Modifier.size(40.dp)) {
                    Icon(
                        Icons.Outlined.PictureInPictureAlt,
                        contentDescription = "画中画",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
                
                IconButton(onClick = onFullscreenClick, modifier = Modifier.size(40.dp)) {
                    Icon(
                        Icons.Outlined.Fullscreen,
                        contentDescription = "全屏",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
                
                IconButton(onClick = onMoreClick, modifier = Modifier.size(40.dp)) {
                    Icon(
                        Icons.Outlined.MoreVert,
                        contentDescription = "更多",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoProgressBar(
    currentPosition: Long,
    duration: Long,
    bufferedPosition: Long,
    onSeek: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    var isDragging by remember { mutableStateOf(false) }
    var dragPosition by remember { mutableStateOf(0L) }
    
    val progress = if (duration > 0) {
        (if (isDragging) dragPosition else currentPosition).toFloat() / duration
    } else 0f
    
    val bufferedProgress = if (duration > 0) {
        bufferedPosition.toFloat() / duration
    } else 0f
    
    Slider(
        value = progress,
        onValueChange = { newProgress ->
            isDragging = true
            dragPosition = (newProgress * duration).toLong()
        },
        onValueChangeFinished = {
            if (isDragging) {
                onSeek(dragPosition)
                isDragging = false
            }
        },
        modifier = modifier
            .fillMaxWidth()
            .height(24.dp),
        valueRange = 0f..1f,
        thumb = {
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .clip(CircleShape)
                    .background(Color.White)
                    .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
            )
        },
        track = { sliderState ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color.White.copy(alpha = 0.3f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(bufferedProgress)
                        .background(Color.White.copy(alpha = 0.5f))
                )
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(sliderState.value)
                        .background(MaterialTheme.colorScheme.primary)
                )
            }
        }
    )
}

@Composable
fun GestureIndicators(
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize())
}

@Composable
fun SpeedSelectionDialog(
    currentSpeed: Float,
    onSpeedSelected: (Float) -> Unit,
    onDismiss: () -> Unit
) {
    val speeds = listOf(0.25f, 0.5f, 0.75f, 1.0f, 1.25f, 1.5f, 1.75f, 2.0f, 2.5f, 3.0f, 4.0f)
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("播放速度") },
        text = {
            LazyColumn {
                items(speeds) { speed ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSpeedSelected(speed) }
                            .padding(vertical = 12.dp, horizontal = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (speed == 1.0f) "正常" else "${speed}x",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        if (speed == currentSpeed) {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}

@Composable
fun TrackSelectionDialog(
    title: String,
    tracks: List<Pair<String, Boolean>>,
    onTrackSelected: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            LazyColumn {
                items(tracks.indices.toList()) { index ->
                    val (name, isSelected) = tracks[index]
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onTrackSelected(index) }
                            .padding(vertical = 12.dp, horizontal = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = name,
                            style = MaterialTheme.typography.bodyLarge
                        )
                        if (isSelected) {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoreOptionsSheet(
    onDismiss: () -> Unit,
    onAspectRatioClick: () -> Unit,
    onDecoderClick: () -> Unit,
    onScreenshotClick: () -> Unit,
    onRecordClick: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        ) {
            MoreOptionItem(
                icon = Icons.Outlined.AspectRatio,
                title = "画面比例",
                subtitle = "16:9",
                onClick = onAspectRatioClick
            )
            MoreOptionItem(
                icon = Icons.Outlined.Memory,
                title = "解码器",
                subtitle = "硬件解码",
                onClick = onDecoderClick
            )
            MoreOptionItem(
                icon = Icons.Outlined.Screenshot,
                title = "截图",
                onClick = onScreenshotClick
            )
            MoreOptionItem(
                icon = Icons.Outlined.Videocam,
                title = "录屏",
                onClick = onRecordClick
            )
        }
    }
}

@Composable
fun MoreOptionItem(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurface
        )
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge
            )
            subtitle?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

fun formatTime(millis: Long): String {
    val seconds = (millis / 1000) % 60
    val minutes = (millis / (1000 * 60)) % 60
    val hours = millis / (1000 * 60 * 60)
    return if (hours > 0) {
        String.format("%02d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format("%02d:%02d", minutes, seconds)
    }
}
