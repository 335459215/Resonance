package com.resonance.ui.player

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeDown
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEvent
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs

enum class GestureZone {
    LEFT, CENTER, RIGHT
}

enum class GestureType {
    NONE, VOLUME, BRIGHTNESS, SEEK, ZOOM
}

@Composable
fun GestureOverlay(
    onVolumeChange: (Float) -> Unit = {},
    onBrightnessChange: (Float) -> Unit = {},
    onSeek: (Long) -> Unit = {},
    onZoomChange: (Float) -> Unit = {},
    onDoubleTapSeek: (Boolean) -> Unit = {},
    onTap: () -> Unit = {},
    enabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    var gestureType by remember { mutableStateOf(GestureType.NONE) }
    var gestureValue by remember { mutableStateOf(0f) }
    var showIndicator by remember { mutableStateOf(false) }
    var containerSize by remember { mutableStateOf(IntSize.Zero) }
    
    val scope = rememberCoroutineScope()
    
    LaunchedEffect(showIndicator) {
        if (showIndicator) {
            delay(1500)
            showIndicator = false
            gestureType = GestureType.NONE
        }
    }
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .onSizeChanged { containerSize = it }
            .pointerInput(enabled) {
                if (!enabled) return@pointerInput
                
                detectVerticalDragGestures(
                    onDragStart = { offset ->
                        val zone = when {
                            offset.x < containerSize.width * 0.33f -> GestureZone.LEFT
                            offset.x > containerSize.width * 0.67f -> GestureZone.RIGHT
                            else -> GestureZone.CENTER
                        }
                        gestureType = when (zone) {
                            GestureZone.LEFT -> GestureType.BRIGHTNESS
                            GestureZone.RIGHT -> GestureType.VOLUME
                            else -> GestureType.NONE
                        }
                        if (gestureType != GestureType.NONE) {
                            showIndicator = true
                        }
                    },
                    onDragEnd = {
                        when (gestureType) {
                            GestureType.VOLUME -> onVolumeChange(gestureValue)
                            GestureType.BRIGHTNESS -> onBrightnessChange(gestureValue)
                            else -> {}
                        }
                        scope.launch {
                            delay(500)
                            showIndicator = false
                            gestureType = GestureType.NONE
                        }
                    },
                    onVerticalDrag = { change, dragAmount ->
                        change.consume()
                        val normalizedDelta = -dragAmount / containerSize.height
                        gestureValue = (gestureValue + normalizedDelta).coerceIn(-1f, 1f)
                    }
                )
            }
            .pointerInput(enabled) {
                if (!enabled) return@pointerInput
                
                detectHorizontalDragGestures(
                    onDragStart = {
                        gestureType = GestureType.SEEK
                        showIndicator = true
                    },
                    onDragEnd = {
                        onSeek((gestureValue * 60000).toLong())
                        scope.launch {
                            delay(500)
                            showIndicator = false
                            gestureType = GestureType.NONE
                            gestureValue = 0f
                        }
                    },
                    onHorizontalDrag = { change, dragAmount ->
                        change.consume()
                        val normalizedDelta = dragAmount / containerSize.width
                        gestureValue = (gestureValue + normalizedDelta * 2).coerceIn(-1f, 1f)
                    }
                )
            }
    ) {
        AnimatedVisibility(
            visible = showIndicator && gestureType != GestureType.NONE,
            enter = fadeIn(tween(200)),
            exit = fadeOut(tween(200)),
            modifier = Modifier.align(Alignment.Center)
        ) {
            GestureIndicator(
                type = gestureType,
                value = gestureValue
            )
        }
    }
}

@Composable
fun GestureIndicator(
    type: GestureType,
    value: Float,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .size(120.dp),
        shape = RoundedCornerShape(16.dp),
        color = Color.Black.copy(alpha = 0.6f)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            when (type) {
                GestureType.VOLUME -> {
                    VolumeIndicator(value = (value + 1f) / 2f)
                }
                GestureType.BRIGHTNESS -> {
                    BrightnessIndicator(value = (value + 1f) / 2f)
                }
                GestureType.SEEK -> {
                    SeekIndicator(value = value)
                }
                else -> {}
            }
        }
    }
}

@Composable
fun VolumeIndicator(
    value: Float,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = when {
                value <= 0f -> Icons.AutoMirrored.Filled.VolumeOff
                value < 0.5f -> Icons.AutoMirrored.Filled.VolumeDown
                else -> Icons.AutoMirrored.Filled.VolumeUp
            },
            contentDescription = "音量",
            tint = Color.White,
            modifier = Modifier.size(32.dp)
        )
        
        LinearProgressIndicator(
            modifier = Modifier
                .width(80.dp)
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp)),
            color = Color.White,
            trackColor = Color.White.copy(alpha = 0.3f),
            progress = { value.coerceIn(0f, 1f) }
        )
        
        Text(
            text = "${(value * 100).toInt()}%",
            style = MaterialTheme.typography.labelMedium,
            color = Color.White
        )
    }
}

@Composable
fun BrightnessIndicator(
    value: Float,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = when {
                value <= 0.2f -> Icons.Default.BrightnessLow
                value < 0.7f -> Icons.Default.BrightnessMedium
                else -> Icons.Default.BrightnessHigh
            },
            contentDescription = "亮度",
            tint = Color.White,
            modifier = Modifier.size(32.dp)
        )
        
        LinearProgressIndicator(
            modifier = Modifier
                .width(80.dp)
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp)),
            color = Color.White,
            trackColor = Color.White.copy(alpha = 0.3f),
            progress = { value.coerceIn(0f, 1f) }
        )
        
        Text(
            text = "${(value * 100).toInt()}%",
            style = MaterialTheme.typography.labelMedium,
            color = Color.White
        )
    }
}

@Composable
fun SeekIndicator(
    value: Float,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = if (value >= 0) Icons.Default.FastForward else Icons.Default.FastRewind,
            contentDescription = "快进/快退",
            tint = Color.White,
            modifier = Modifier.size(32.dp)
        )
        
        Text(
            text = if (value >= 0) "+${(value * 60).toInt()}秒" else "${(value * 60).toInt()}秒",
            style = MaterialTheme.typography.titleMedium,
            color = Color.White
        )
    }
}

@Composable
fun DoubleTapSeekIndicator(
    isForward: Boolean,
    modifier: Modifier = Modifier
) {
    var visible by remember { mutableStateOf(true) }
    
    LaunchedEffect(Unit) {
        delay(500)
        visible = false
    }
    
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(100)) + scaleIn(tween(100)),
        exit = fadeOut(tween(200)) + scaleOut(tween(200)),
        modifier = modifier
    ) {
        Surface(
            shape = CircleShape,
            color = Color.Black.copy(alpha = 0.4f),
            modifier = Modifier.size(64.dp)
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = if (isForward) Icons.Default.FastForward else Icons.Default.FastRewind,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = "10秒",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun PinchZoomHandler(
    onZoomChange: (Float) -> Unit,
    enabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    var scale by remember { mutableStateOf(1f) }
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(enabled) {
                if (!enabled) return@pointerInput
                
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent()
                        if (event.changes.size == 2) {
                            val fingers = event.changes.toList()
                            val currentDistance = calculateDistance(
                                fingers[0].position,
                                fingers[1].position
                            )
                            
                            if (event.type == PointerEventType.Press) {
                            } else if (event.type == PointerEventType.Move) {
                                val previousDistance = calculateDistance(
                                    fingers[0].previousPosition,
                                    fingers[1].previousPosition
                                )
                                
                                if (previousDistance > 0) {
                                    val scaleDelta = currentDistance / previousDistance
                                    scale = (scale * scaleDelta).coerceIn(0.5f, 3f)
                                    onZoomChange(scale)
                                }
                            }
                        }
                    }
                }
            }
    )
}

private fun calculateDistance(p1: Offset, p2: Offset): Float {
    return kotlin.math.sqrt((p2.x - p1.x) * (p2.x - p1.x) + (p2.y - p1.y) * (p2.y - p1.y))
}

@Composable
fun GestureHintOverlay(
    showHints: Boolean,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = showHints,
        enter = fadeIn(tween(200)),
        exit = fadeOut(tween(200)),
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.8f))
                .pointerInput(Unit) {
                    detectTapGestures(onTap = { onDismiss() })
                },
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                Text(
                    text = "手势操作指南",
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color.White
                )
                
                GestureHintItem(
                    icon = Icons.Default.TouchApp,
                    title = "单击",
                    description = "显示/隐藏控制面板"
                )
                
                GestureHintItem(
                    icon = Icons.Default.DoubleArrow,
                    title = "双击",
                    description = "左侧快退 / 右侧快进 / 中间播放暂停"
                )
                
                GestureHintItem(
                    icon = Icons.Default.SwipeVertical,
                    title = "上下滑动",
                    description = "左侧调节亮度 / 右侧调节音量"
                )
                
                GestureHintItem(
                    icon = Icons.Default.Swipe,
                    title = "左右滑动",
                    description = "快进/快退进度"
                )
                
                GestureHintItem(
                    icon = Icons.Default.Pinch,
                    title = "双指缩放",
                    description = "调整画面大小"
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                TextButton(onClick = onDismiss) {
                    Text("知道了", color = Color.White)
                }
            }
        }
    }
}

@Composable
fun GestureHintItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth(0.8f)
            .padding(horizontal = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            modifier = Modifier.size(48.dp),
            shape = RoundedCornerShape(12.dp),
            color = Color.White.copy(alpha = 0.1f)
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
        
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = Color.White
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.7f)
            )
        }
    }
}
