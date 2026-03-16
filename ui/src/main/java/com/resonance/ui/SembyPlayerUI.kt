package com.resonance.ui

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.resonance.core.R

// --- UI Strings for Localization ---
val UI_STRINGS = mapOf(
    "zh" to mapOf(
        "appName" to "Semby",
        "aiEnhance" to "AI 视觉增强",
        "aiStatus" to "AI 运算中",
        "ultraRes" to "AI 超分辨率重建",
        "colorBoost" to "神经色彩增强",
        "splitPreview" to "实时对比模式",
        "play" to "播放",
        "trailer" to "预告片",
        "continue" to "继续观看",
        "resolutions" to listOf("4K HDR", "80Mbps", "HEVC", "Atmos"),
        "tags" to listOf("科幻", "动作", "2024")
    ),
    "en" to mapOf(
        "appName" to "Semby",
        "aiEnhance" to "AI Vision Enhancement",
        "aiStatus" to "AI Processing",
        "ultraRes" to "AI Ultra-Resolution",
        "colorBoost" to "Neural Color Boost",
        "splitPreview" to "Live Split Preview",
        "play" to "Play",
        "trailer" to "Trailer",
        "continue" to "Continue Watching",
        "resolutions" to listOf("4K HDR", "80Mbps", "HEVC", "Atmos"),
        "tags" to listOf("Sci-Fi", "Action", "2024")
    )
)

// --- Main Semby App Composable ---
@Composable
fun SembyApp() {
    var currentView by remember { mutableStateOf(SembyView.DISCOVERY) }
    var isAiActive by remember { mutableStateOf(true) }
    var currentLanguage by remember { mutableStateOf("zh") }
    
    val strings = UI_STRINGS[currentLanguage] ?: UI_STRINGS["zh"]!!
    
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        when (currentView) {
            SembyView.DISCOVERY -> {
                DiscoveryView(
                    strings = strings,
                    onPlay = {
                        currentView = SembyView.PLAYER
                    }
                )
            }
            SembyView.PLAYER -> {
                PlayerView(
                    strings = strings,
                    isAiActive = isAiActive,
                    onToggleAi = { isAiActive = !isAiActive },
                    onBack = {
                        currentView = SembyView.DISCOVERY
                    }
                )
            }
        }
    }
}

// --- View States ---
enum class SembyView {
    DISCOVERY,
    PLAYER
}

// --- Discovery View (Light Gallery) ---
@Suppress("UNCHECKED_CAST")
@Composable
fun DiscoveryView(
    strings: Map<String, Any>,
    onPlay: () -> Unit
) {
    val resolutions = strings["resolutions"] as? List<String> ?: emptyList()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFF1F2F6),
                        Color(0xFFE8EAF2),
                        Color(0xFFDDE1ED)
                    )
                )
            )
            .padding(40.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start
        ) {
            // Sidebar - Minimalist Glass
            Column(
                modifier = Modifier
                    .width(64.dp)
                    .background(
                        color = Color.White.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(32.dp)
                    )
                    .border(
                        width = 1.dp,
                        color = Color.White.copy(alpha = 0.4f),
                        shape = RoundedCornerShape(32.dp)
                    )
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Home,
                    contentDescription = "Home",
                    modifier = Modifier.size(24.dp),
                    tint = Color(0xFF2D3436)
                )
                Icon(
                    imageVector = Icons.Default.VideoLibrary,
                    contentDescription = "Library",
                    modifier = Modifier.size(24.dp),
                    tint = Color(0xFF2D3436).copy(alpha = 0.4f)
                )
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = "Favorites",
                    modifier = Modifier.size(24.dp),
                    tint = Color(0xFF2D3436).copy(alpha = 0.4f)
                )
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Settings",
                    modifier = Modifier.size(24.dp),
                    tint = Color(0xFF2D3436).copy(alpha = 0.4f)
                )
            }
            
            Spacer(modifier = Modifier.width(48.dp))
            
            // Main Hero Metadata
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.Top
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    resolutions.forEach { resolution ->
                        Box(
                            modifier = Modifier
                                .padding(end = 12.dp)
                                .background(
                                    color = Color.White.copy(alpha = 0.6f),
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .border(
                                    width = 1.dp,
                                    color = Color.White.copy(alpha = 0.5f),
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .padding(horizontal = 12.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = resolution,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFF636E72)
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Text(
                    text = "DUNE: PART TWO",
                    fontSize = 80.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF1E272E),
                    lineHeight = 80.sp,
                    letterSpacing = (-2).sp
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Text(
                    text = "保罗·厄崔迪公爵加入了弗雷曼人阵营，开始了一场足以毁灭全宇宙的伟大复仇...",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF636E72),
                    lineHeight = 24.sp,
                    modifier = Modifier.width(400.dp)
                )
                
                Spacer(modifier = Modifier.height(32.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .background(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(
                                        Color(0xFF0984E3),
                                        Color(0xFF74B9FF)
                                    )
                                ),
                                shape = RoundedCornerShape(16.dp)
                            )
                            .shadow(
                                elevation = 8.dp,
                                spotColor = Color(0xFF0984E3).copy(alpha = 0.2f),
                                shape = RoundedCornerShape(16.dp)
                            )
                            .padding(horizontal = 40.dp, vertical = 16.dp)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
                                onPlay()
                            }
                    ) {
                        Text(
                            text = strings["play"] as? String ?: "Play",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(16.dp))
                    
                    Box(
                        modifier = Modifier
                            .background(
                                color = Color.White.copy(alpha = 0.6f),
                                shape = RoundedCornerShape(16.dp)
                            )
                            .border(
                                width = 1.dp,
                                color = Color.White.copy(alpha = 0.5f),
                                shape = RoundedCornerShape(16.dp)
                            )
                            .padding(horizontal = 40.dp, vertical = 16.dp)
                    ) {
                        Text(
                            text = strings["trailer"] as? String ?: "Trailer",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2D3436)
                        )
                    }
                }
            }
        }
        
        // Continue Watching Section
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.Bottom
        ) {
            Text(
                text = strings["continue"] as? String ?: "Continue Watching",
                fontSize = 14.sp,
                fontWeight = FontWeight.Black,
                color = Color(0xFF636E72).copy(alpha = 0.5f),
                letterSpacing = 2.sp,
                modifier = Modifier.padding(bottom = 24.dp)
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(4) {index ->
                    val isSelected = index == 0
                    
                    Box(
                        modifier = Modifier
                            .width(224.dp)
                            .height(320.dp)
                            .shadow(
                                elevation = if (isSelected) 24.dp else 8.dp,
                                shape = RoundedCornerShape(30.dp)
                            )
                            .border(
                                width = if (isSelected) 4.dp else 2.dp,
                                color = if (isSelected) Color(0xFF0984E3) else Color.White.copy(alpha = 0.4f),
                                shape = RoundedCornerShape(30.dp)
                            )
                            .padding(if (isSelected) 0.dp else 1.dp)
                            .alpha(if (isSelected) 1.0f else 0.6f)
                            .scale(if (isSelected) 1.0f else 0.95f)
                    ) {
                        Image(
                            painter = rememberAsyncImagePainter(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data("https://picsum.photos/seed/${index + 40}/300/450")
                                    .crossfade(true)
                                    .build()
                            ),
                            contentDescription = "Movie poster",
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(if (isSelected) 26.dp else 28.dp)),
                            contentScale = ContentScale.Crop
                        )
                    }
                    
                    if (index < 3) {
                        Spacer(modifier = Modifier.width(32.dp))
                    }
                }
            }
        }
    }
}

// --- Player View with AI Enhancement Panel ---
@Composable
fun PlayerView(
    strings: Map<String, Any>,
    isAiActive: Boolean,
    onToggleAi: () -> Unit,
    onBack: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // Mock Video Content
        Image(
            painter = rememberAsyncImagePainter(
                model = ImageRequest.Builder(LocalContext.current)
                    .data("https://images.unsplash.com/photo-1536440136628-849c177e76a1?q=80&w=2000")
                    .crossfade(true)
                    .build()
            ),
            contentDescription = "Movie",
            modifier = Modifier
                .fillMaxSize()
                .alpha(0.8f),
            contentScale = ContentScale.Crop
        )
        
        // Top Header - Zero Black
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(128.dp)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFFF1F2F6).copy(alpha = 0.9f),
                            Color.Transparent
                        )
                    )
                )
                .padding(48.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            color = Color.White.copy(alpha = 0.4f),
                            shape = CircleShape
                        )
                        .border(
                            width = 1.dp,
                            color = Color.White.copy(alpha = 0.5f),
                            shape = CircleShape
                        )
                        .clickable {
                            onBack()
                        }
                ) {
                    Text(
                        text = "✕",
                        modifier = Modifier
                            .fillMaxSize()
                            .align(Alignment.Center),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2D3436),
                        textAlign = TextAlign.Center
                    )
                }
                
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isAiActive) {
                        Box(
                            modifier = Modifier
                                .background(
                                    color = Color(0xFF06B6D4).copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .border(
                                    width = 1.dp,
                                    color = Color(0xFF06B6D4).copy(alpha = 0.4f),
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .padding(horizontal = 12.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = strings["aiStatus"] as? String ?: "AI Processing",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFF06B6D4)
                            )
                        }
                        
                        Spacer(modifier = Modifier.width(16.dp))
                    }
                    
                    Text(
                        text = "Dune: Part Two (4K REMUX)",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2D3436)
                    )
                }
                
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            color = Color.White.copy(alpha = 0.4f),
                            shape = CircleShape
                        )
                )
            }
        }
        
        // AI MASTER PANEL (Floating Glass)
        AnimatedVisibility(
            visible = true,
            enter = slideInVertically(
                initialOffsetY = { it * 2 },
                animationSpec = tween(durationMillis = 500)
            ) + fadeIn(
                animationSpec = tween(durationMillis = 500)
            ),
            exit = slideOutVertically(
                targetOffsetY = { it * 2 },
                animationSpec = tween(durationMillis = 300)
            ) + fadeOut(
                animationSpec = tween(durationMillis = 300)
            )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 40.dp)
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 64.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = Color.White.copy(alpha = 0.4f),
                            shape = RoundedCornerShape(40.dp)
                        )
                        .border(
                            width = 1.dp,
                            color = Color.White.copy(alpha = 0.6f),
                            shape = RoundedCornerShape(40.dp)
                        )
                        .shadow(
                            elevation = 40.dp,
                            spotColor = Color.Black.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(40.dp)
                        )
                        .padding(32.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.Start,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(
                                modifier = Modifier.padding(end = 40.dp)
                            ) {
                                Text(
                                    text = strings["aiEnhance"] as? String ?: "AI Vision Enhancement",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFF636E72).copy(alpha = 0.5f),
                                    letterSpacing = 1.sp
                                )
                                Text(
                                    text = strings["ultraRes"] as? String ?: "AI Ultra-Resolution",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF2D3436)
                                )
                            }
                            
                            // AI Toggle Switch
                            Box(
                                modifier = Modifier
                                    .width(80.dp)
                                    .height(40.dp)
                                    .background(
                                        color = if (isAiActive) Color(0xFF0984E3) else Color(0xFFDDE1ED),
                                        shape = RoundedCornerShape(20.dp)
                                    )
                                    .padding(6.dp)
                                    .clickable {
                                        onToggleAi()
                                    }
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .background(
                                            color = Color.White,
                                            shape = CircleShape
                                        )
                                        .shadow(
                                            elevation = 4.dp,
                                            shape = CircleShape
                                        )
                                        .align(if (isAiActive) Alignment.CenterEnd else Alignment.CenterStart)
                                )
                            }
                            
                            Spacer(modifier = Modifier.width(16.dp))
                            
                            Box(
                                modifier = Modifier
                                    .width(1.dp)
                                    .height(40.dp)
                                    .background(Color(0xFF636E72).copy(alpha = 0.1f))
                            )
                            
                            Spacer(modifier = Modifier.width(16.dp))
                            
                            Row(
                                horizontalArrangement = Arrangement.Start,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                AiControlItem(
                                    icon = "🎨",
                                    label = strings["colorBoost"] as? String ?: "Neural Color Boost",
                                    active = isAiActive
                                )
                                
                                Spacer(modifier = Modifier.width(24.dp))
                                
                                AiControlItem(
                                    icon = "📐",
                                    label = strings["splitPreview"] as? String ?: "Live Split Preview",
                                    active = false
                                )
                            }
                        }
                        
                        Box(
                            modifier = Modifier
                                .background(
                                    color = Color(0xFF0984E3).copy(alpha = 0.1f),
                                    shape = RoundedCornerShape(16.dp)
                                )
                                .border(
                                    width = 1.dp,
                                    color = Color(0xFF0984E3).copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(16.dp)
                                )
                                .padding(horizontal = 24.dp, vertical = 12.dp)
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Neural Engine v4.2",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF0984E3)
                                )
                                
                                Spacer(modifier = Modifier.width(12.dp))
                                
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .background(
                                            color = Color(0xFF0984E3),
                                            shape = CircleShape
                                        )
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AiControlItem(
    icon: String,
    label: String,
    active: Boolean
) {
    Box(
        modifier = Modifier
            .background(
                color = if (active) Color.White.copy(alpha = 0.8f) else Color.White.copy(alpha = 0.1f),
                shape = RoundedCornerShape(16.dp)
            )
            .border(
                width = 1.dp,
                color = if (active) Color(0xFF0984E3).copy(alpha = 0.3f) else Color.White.copy(alpha = 0.3f),
                shape = RoundedCornerShape(16.dp)
            )
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = icon,
                fontSize = 20.sp,
                modifier = Modifier.padding(end = 12.dp)
            )
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = FontWeight.Black,
                color = if (active) Color(0xFF0984E3) else Color(0xFF2D3436).copy(alpha = 0.4f),
                letterSpacing = 0.5.sp
            )
        }
    }
}

// --- Previews ---
@Preview(
    name = "Light Mode",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_NO
)
@Preview(
    name = "Dark Mode",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
fun SembyAppPreview() {
    MaterialTheme {
        SembyApp()
    }
}

@Preview(
    name = "Discovery View",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_NO
)
@Composable
fun DiscoveryViewPreview() {
    val strings = UI_STRINGS["zh"] ?: emptyMap()
    MaterialTheme {
        DiscoveryView(
            strings = strings,
            onPlay = {}
        )
    }
}

@Preview(
    name = "Player View",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_NO
)
@Composable
fun PlayerViewPreview() {
    val strings = UI_STRINGS["zh"] ?: emptyMap()
    MaterialTheme {
        PlayerView(
            strings = strings,
            isAiActive = true,
            onToggleAi = {},
            onBack = {}
        )
    }
}