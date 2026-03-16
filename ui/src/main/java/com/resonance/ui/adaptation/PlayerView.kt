package com.resonance.ui.adaptation

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.*
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest

// --- 播放器视图 ---
@Composable
fun PlayerView(
    isAiActive: Boolean,
    toggleAi: () -> Unit,
    onBack: () -> Unit,
    onBackToHome: () -> Unit = {}
) {
    var isSmallScreen by remember { mutableStateOf(false) }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .onSizeChanged {
                isSmallScreen = it.width < 600
            }
    ) {
        // 视频内容（使用占位图像）
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data("https://images.unsplash.com/photo-1536440136628-849c177e76a1?q=80&w=2000")
                .build(),
            contentDescription = "Movie",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            alpha = 0.8f
        )
        
        // 顶部标题栏
        TopHeaderBar(
            isAiActive = isAiActive,
            onBack = onBack,
            onBackToHome = onBackToHome,
            isSmallScreen = isSmallScreen
        )
        
        // AI 增强面板
        AiEnhancementPanel(
            isAiActive = isAiActive,
            toggleAi = toggleAi,
            isSmallScreen = isSmallScreen
        )
    }
}

// --- 顶部标题栏 ---
@Composable
fun TopHeaderBar(
    isAiActive: Boolean,
    onBack: () -> Unit,
    onBackToHome: () -> Unit = {},
    isSmallScreen: Boolean
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(if (isSmallScreen) 24.dp else 32.dp)
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFF1F2F6).copy(alpha = 0.9f),
                        Color.Transparent
                    )
                )
            )
            .padding(if (isSmallScreen) 8.dp else 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 返回按钮
            Button(
                onClick = onBack,
                modifier = Modifier
                    .size(if (isSmallScreen) 10.dp else 12.dp)
                    .clip(CircleShape),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White.copy(alpha = 0.4f)
                ),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.6f))
            ) {
                Text(
                    text = "←", 
                    fontSize = if (isSmallScreen) 10.sp else 12.sp, 
                    color = Color(0xFF2D3436)
                )
            }
            
            // 标题和 AI 状态
            Row(
                horizontalArrangement = Arrangement.spacedBy(if (isSmallScreen) 12.dp else 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isAiActive && !isSmallScreen) {
                    Box(
                        modifier = Modifier
                            .background(
                                color = Color(0xFF00BCD4).copy(alpha = 0.2f),
                                shape = RoundedCornerShape(if (isSmallScreen) 6.dp else 8.dp)
                            )
                            .border(
                                width = 1.dp,
                                color = Color(0xFF00BCD4).copy(alpha = 0.4f),
                                shape = RoundedCornerShape(if (isSmallScreen) 6.dp else 8.dp)
                            )
                            .padding(
                                horizontal = if (isSmallScreen) 8.dp else 12.dp,
                                vertical = if (isSmallScreen) 2.dp else 4.dp
                            )
                    ) {
                        Text(
                            text = "AI 增强",
                            fontSize = if (isSmallScreen) 10.sp else 12.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF00BCD4),
                            modifier = Modifier.animateContentSize()
                        )
                    }
                }
                
                Text(
                    text = "播放器",
                    fontSize = if (isSmallScreen) 12.sp else 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2D3436)
                )
            }
            
            // 主屏幕按钮
            Button(
                onClick = onBackToHome,
                modifier = Modifier
                    .size(if (isSmallScreen) 10.dp else 12.dp)
                    .clip(CircleShape),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White.copy(alpha = 0.4f)
                ),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.6f))
            ) {
                Text(
                    text = "✕", 
                    fontSize = if (isSmallScreen) 10.sp else 12.sp, 
                    color = Color(0xFF2D3436)
                )
            }
        }
    }
}

// --- AI 增强面板 ---
@Composable
fun AiEnhancementPanel(
    isAiActive: Boolean,
    toggleAi: () -> Unit,
    isSmallScreen: Boolean
) {
    AnimatedVisibility(
        visible = true,
        enter = slideInVertically(initialOffsetY = { it }),
        exit = slideOutVertically(targetOffsetY = { it })
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = if (isSmallScreen) 12.dp else 16.dp)
        ) {
            Box(
                modifier = Modifier
                    .width(if (isSmallScreen) 300.dp else 900.dp)
                    .align(Alignment.Center)
                    .background(
                        color = Color.White.copy(alpha = 0.4f),
                        shape = RoundedCornerShape(if (isSmallScreen) 24.dp else 40.dp)
                    )
                    .border(
                        width = 1.dp,
                        color = Color.White.copy(alpha = 0.6f),
                        shape = RoundedCornerShape(if (isSmallScreen) 24.dp else 40.dp)
                    )
                    .padding(if (isSmallScreen) 6.dp else 8.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(if (isSmallScreen) 8.dp else 0.dp)
                ) {
                    if (isSmallScreen) {
                        // 小屏幕布局
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(
                                    verticalArrangement = Arrangement.spacedBy(2.dp)
                                ) {
                                    Text(
                                        text = "AI 增强",
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Black,
                                        color = Color(0xFF636E72).copy(alpha = 0.5f),
                                        letterSpacing = 0.5.sp
                                    )
                                    Text(
                                        text = "超高清",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF2D3436)
                                    )
                                }
                                
                                // AI 开关
                                Switch(
                                    checked = isAiActive,
                                    onCheckedChange = { toggleAi() },
                                    modifier = Modifier
                                        .width(16.dp)
                                        .height(8.dp),
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color.White,
                                        checkedTrackColor = Color(0xFF0984E3),
                                        uncheckedThumbColor = Color.White,
                                        uncheckedTrackColor = Color(0xFFDDE1ED)
                                    )
                                )
                            }
                            
                            // AI 控制项
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                AiControlItem(
                                    icon = "🎨",
                                    label = "色彩增强",
                                    active = isAiActive,
                                    isSmallScreen = isSmallScreen
                                )
                                AiControlItem(
                                    icon = "📐",
                                    label = "分屏预览",
                                    active = false,
                                    isSmallScreen = isSmallScreen
                                )
                            }
                        }
                    } else {
                        // 大屏幕布局
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(40.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = "AI 增强",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Black,
                                        color = Color(0xFF636E72).copy(alpha = 0.5f),
                                        letterSpacing = 1.sp
                                    )
                                    Text(
                                        text = "超高清",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF2D3436)
                                    )
                                }
                                
                                // AI 开关
                                Switch(
                                    checked = isAiActive,
                                    onCheckedChange = { toggleAi() },
                                    modifier = Modifier
                                        .width(20.dp)
                                        .height(10.dp),
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color.White,
                                        checkedTrackColor = Color(0xFF0984E3),
                                        uncheckedThumbColor = Color.White,
                                        uncheckedTrackColor = Color(0xFFDDE1ED)
                                    )
                                )
                                
                                // 分隔线
                                Box(
                                    modifier = Modifier
                                        .width(1.dp)
                                        .height(10.dp)
                                        .background(Color(0xFF636E72).copy(alpha = 0.1f))
                                        .padding(horizontal = 4.dp)
                                ) {}
                                
                                // AI 控制项
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(24.dp)
                                ) {
                                    AiControlItem(
                                        icon = "🎨",
                                        label = "色彩增强",
                                        active = isAiActive,
                                        isSmallScreen = isSmallScreen
                                    )
                                    AiControlItem(
                                        icon = "📐",
                                        label = "分屏预览",
                                        active = false,
                                        isSmallScreen = isSmallScreen
                                    )
                                }
                            }
                            
                            // 神经引擎状态
                            Box(
                                modifier = Modifier
                                    .background(
                                        color = Color(0xFF0984E3).copy(alpha = 0.1f),
                                        shape = RoundedCornerShape(24.dp)
                                    )
                                    .border(
                                        width = 1.dp,
                                        color = Color(0xFF0984E3).copy(alpha = 0.2f),
                                        shape = RoundedCornerShape(24.dp)
                                    )
                                    .padding(horizontal = 24.dp, vertical = 12.dp)
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "神经引擎",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF0984E3)
                                    )
                                    Box(
                                        modifier = Modifier
                                            .size(2.dp)
                                            .background(Color(0xFF0984E3))
                                            .clip(CircleShape)
                                    ) {}
                                }
                            }
                        }
                    }
                    
                    // 小屏幕显示神经引擎状态
                    if (isSmallScreen) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.CenterHorizontally)
                                .background(
                                    color = Color(0xFF0984E3).copy(alpha = 0.1f),
                                    shape = RoundedCornerShape(16.dp)
                                )
                                .border(
                                    width = 1.dp,
                                    color = Color(0xFF0984E3).copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(16.dp)
                                )
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "神经引擎",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF0984E3)
                                )
                                Box(
                                    modifier = Modifier
                                        .size(2.dp)
                                        .background(Color(0xFF0984E3))
                                        .clip(CircleShape)
                                ) {}
                            }
                        }
                    }
                }
            }
        }
    }
}

// --- AI 控制项 ---
@Composable
fun AiControlItem(
    icon: String,
    label: String,
    active: Boolean,
    isSmallScreen: Boolean
) {
    Box(
        modifier = Modifier
            .background(
                color = if (active) Color.White.copy(alpha = 0.8f) else Color.White.copy(alpha = 0.1f),
                shape = RoundedCornerShape(if (isSmallScreen) 16.dp else 24.dp)
            )
            .border(
                width = 1.dp,
                color = if (active) Color(0xFF0984E3).copy(alpha = 0.3f) else Color.White.copy(alpha = 0.3f),
                shape = RoundedCornerShape(if (isSmallScreen) 16.dp else 24.dp)
            )
            .padding(
                horizontal = if (isSmallScreen) 12.dp else 20.dp,
                vertical = if (isSmallScreen) 8.dp else 12.dp
            )
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(if (isSmallScreen) 8.dp else 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = icon, 
                fontSize = if (isSmallScreen) 16.sp else 20.sp
            )
            Text(
                text = label,
                fontSize = if (isSmallScreen) 10.sp else 12.sp,
                fontWeight = FontWeight.Black,
                color = if (active) Color(0xFF0984E3) else Color(0xFF636E72).copy(alpha = 0.4f),
                letterSpacing = if (isSmallScreen) 0.3.sp else 0.5.sp
            )
        }
    }
}
