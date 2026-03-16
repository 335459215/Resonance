package com.resonance.ui.adaptation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest

// --- 发现视图 ---
@Composable
fun DiscoveryView(
    onPlay: () -> Unit,
    onBackClick: () -> Unit = {}
) {
    val isSmallScreen = false
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        Color(0xFFF1F2F6),
                        Color(0xFFE8EAF2),
                        Color(0xFFDDE1ED)
                    )
                )
            )
            .padding(if (isSmallScreen) 10.dp else 20.dp)
    ) {
        // 返回按钮
        Button(
            onClick = onBackClick,
            modifier = Modifier
                .align(Alignment.TopStart)
                .size(if (isSmallScreen) 32.dp else 40.dp)
                .clip(CircleShape),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.White.copy(alpha = 0.3f)
            )
        ) {
            Text(
                    text = "✕",
                    modifier = Modifier,
                    fontSize = if (isSmallScreen) 14.sp else 16.sp,
                    color = Color(0xFF2D3436)
                )
        }
        
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.spacedBy(if (isSmallScreen) 8.dp else 12.dp)
        ) {
            // 侧边导航栏
            if (!isSmallScreen) {
                SideNavigationBar()
            }
            
            // 主内容区域
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // 主标题元数据
                MainMetadataSection(
                    onPlay = onPlay,
                    isSmallScreen = isSmallScreen
                )
                
                // 海报网格
                PosterGridSection(isSmallScreen = isSmallScreen)
            }
        }
    }
}

// --- 侧边导航栏 ---
@Composable
fun SideNavigationBar() {
    Column(
        modifier = Modifier
            .width(16.dp)
            .fillMaxHeight()
            .padding(vertical = 10.dp)
            .background(
                color = Color.White.copy(alpha = 0.3f),
                shape = RoundedCornerShape(50)
            )
            .padding(horizontal = 8.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        NavigationItem(icon = "🏠", isActive = true)
        NavigationItem(icon = "🌐", isActive = false)
        NavigationItem(icon = "📥", isActive = false)
        NavigationItem(icon = "⚙️", isActive = false)
    }
}

// --- 导航项 ---
@Composable
fun NavigationItem(icon: String, isActive: Boolean) {
    Text(
        text = icon,
        fontSize = 20.sp,
        color = Color(0xFF2D3436).copy(alpha = if (isActive) 1f else 0.4f)
    )
}

// --- 主标题元数据区域 ---
@Composable
fun MainMetadataSection(onPlay: () -> Unit, isSmallScreen: Boolean) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(if (isSmallScreen) 12.dp else 16.dp)
    ) {
        // 分辨率标签
        ResolutionTags(isSmallScreen = isSmallScreen)
        
        // 标题
        Text(
            text = "DUNE: PART TWO",
            fontSize = if (isSmallScreen) 40.sp else 80.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = if (isSmallScreen) (-1).sp else (-2).sp,
            color = Color(0xFF1E272E),
            lineHeight = if (isSmallScreen) 35.sp else 70.sp
        )
        
        // 描述
        Text(
            text = "保罗·厄崔迪公爵加入了弗雷曼人阵营，开始了一场足以毁灭全宇宙的伟大复仇...",
            fontSize = if (isSmallScreen) 14.sp else 18.sp,
            color = Color(0xFF636E72),
            lineHeight = if (isSmallScreen) 20.sp else 24.sp,
            maxLines = 3,
            modifier = Modifier.widthIn(max = if (isSmallScreen) 300.dp else 600.dp)
        )
        
        // 操作按钮
        ActionButtons(onPlay = onPlay, isSmallScreen = isSmallScreen)
    }
}

// --- 分辨率标签 ---
@Composable
fun ResolutionTags(isSmallScreen: Boolean) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(if (isSmallScreen) 8.dp else 12.dp)
    ) {
        ResolutionTag(
            text = "4K HDR",
            isSmallScreen = isSmallScreen
        )
        ResolutionTag(
            text = "80Mbps",
            isSmallScreen = isSmallScreen
        )
        ResolutionTag(
            text = "HEVC",
            isSmallScreen = isSmallScreen
        )
        ResolutionTag(
            text = "Atmos",
            isSmallScreen = isSmallScreen
        )
    }
}

// --- 分辨率标签项 ---
@Composable
fun ResolutionTag(text: String, isSmallScreen: Boolean) {
    Box(
        modifier = Modifier
            .background(
                color = Color.White.copy(alpha = 0.6f),
                shape = RoundedCornerShape(if (isSmallScreen) 6.dp else 8.dp)
            )
            .padding(
                horizontal = if (isSmallScreen) 8.dp else 12.dp,
                vertical = if (isSmallScreen) 2.dp else 4.dp
            )
    ) {
        Text(
            text = text,
            fontSize = if (isSmallScreen) 8.sp else 10.sp,
            fontWeight = FontWeight.Black,
            color = Color(0xFF636E72)
        )
    }
}

// --- 操作按钮 ---
@Composable
fun ActionButtons(onPlay: () -> Unit, isSmallScreen: Boolean) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(if (isSmallScreen) 12.dp else 16.dp),
        modifier = Modifier.padding(top = if (isSmallScreen) 12.dp else 16.dp)
    ) {
        Button(
            onClick = onPlay,
            modifier = Modifier
                .padding(
                    horizontal = if (isSmallScreen) 20.dp else 40.dp,
                    vertical = if (isSmallScreen) 12.dp else 16.dp
                )
                .clip(RoundedCornerShape(if (isSmallScreen) 16.dp else 24.dp)),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Transparent
            )
        ) {
            Box(
                modifier = Modifier
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                Color(0xFF0984E3),
                                Color(0xFF74B9FF)
                            )
                        )
                    )
                    .padding(
                        horizontal = if (isSmallScreen) 20.dp else 40.dp,
                        vertical = if (isSmallScreen) 12.dp else 16.dp
                    )
                    .clip(RoundedCornerShape(if (isSmallScreen) 16.dp else 24.dp))
            ) {
                Text(
                    text = "播放",
                    color = Color.White,
                    fontSize = if (isSmallScreen) 12.sp else 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        
        Button(
            onClick = {},
            modifier = Modifier
                .padding(
                    horizontal = if (isSmallScreen) 20.dp else 40.dp,
                    vertical = if (isSmallScreen) 12.dp else 16.dp
                )
                .clip(RoundedCornerShape(if (isSmallScreen) 16.dp else 24.dp)),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.White.copy(alpha = 0.6f)
            )
        ) {
            Text(
                text = "预告片",
                color = Color(0xFF2D3436),
                fontSize = if (isSmallScreen) 12.sp else 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

// --- 海报网格区域 ---
@Composable
fun PosterGridSection(isSmallScreen: Boolean) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(if (isSmallScreen) 12.dp else 16.dp)
    ) {
        // 标题
        Text(
            text = "继续观看",
            fontSize = if (isSmallScreen) 12.sp else 14.sp,
            fontWeight = FontWeight.Black,
            color = Color(0xFF636E72).copy(alpha = 0.5f),
            letterSpacing = if (isSmallScreen) 1.sp else 2.sp
        )
        
        // 海报网格
        Row(
            horizontalArrangement = Arrangement.spacedBy(if (isSmallScreen) 16.dp else 32.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            val itemCount = if (isSmallScreen) 3 else 4
            for (i in 1..itemCount) {
                PosterCard(
                    imageUrl = "https://picsum.photos/seed/${i + 40}/300/450",
                    isSelected = i == 1,
                    isSmallScreen = isSmallScreen
                )
            }
        }
    }
}

// --- 海报卡片 ---
@Composable
fun PosterCard(imageUrl: String, isSelected: Boolean, isSmallScreen: Boolean) {
    Box(
        modifier = Modifier
            .width(if (isSmallScreen) 40.dp else 56.dp)
            .height(if (isSmallScreen) 60.dp else 80.dp)
            .clip(RoundedCornerShape(if (isSmallScreen) 20.dp else 30.dp))
            .background(
                color = Color.White.copy(alpha = 0.2f)
            )
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(imageUrl)
                .build(),
            contentDescription = "Poster",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
    }
}
