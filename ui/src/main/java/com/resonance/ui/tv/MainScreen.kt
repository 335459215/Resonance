package com.resonance.ui.tv

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.resonance.ui.components.*

data class TVMediaItem(
    val id: String,
    val title: String,
    val subtitle: String? = null,
    val posterUrl: String? = null,
    val backdropUrl: String? = null,
    val overview: String? = null,
    val rating: Float? = null,
    val progress: Float = 0f,
    val isNew: Boolean = false
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TVMainScreen(
    onMediaClick: (TVMediaItem) -> Unit = {},
    onSettingsClick: () -> Unit = {}
) {
    var selectedCategory by remember { mutableStateOf(0) }
    val focusRequester = remember { FocusRequester() }
    
    val categories = listOf("首页推荐", "电影", "剧集", "最近播放", "收藏")
    
    val sampleMedia = remember {
        (1..20).map { i ->
            TVMediaItem(
                id = i.toString(),
                title = "媒体项目 $i",
                subtitle = if (i % 3 == 0) "第1季" else "${2020 + i % 5}",
                posterUrl = null,
                backdropUrl = null,
                rating = (7..9).random() + (0..9).random() / 10f,
                progress = if (i % 4 == 0) (i % 100) / 100f else 0f,
                isNew = i % 7 == 0
            )
        }
    }
    
    val featuredMedia = remember { sampleMedia.take(5) }
    
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize()
        ) {
            item {
                TVCategoryRow(
                    categories = categories,
                    selectedIndex = selectedCategory,
                    onCategorySelected = { selectedCategory = it },
                    modifier = Modifier.focusRequester(focusRequester)
                )
            }
            
            item {
                TVFeaturedSection(
                    items = featuredMedia,
                    onItemClick = onMediaClick
                )
            }
            
            item {
                TVMediaSection(
                    title = "继续观看",
                    items = sampleMedia.filter { it.progress > 0 },
                    onItemClick = onMediaClick
                )
            }
            
            item {
                TVMediaSection(
                    title = "最新添加",
                    items = sampleMedia.filter { it.isNew },
                    onItemClick = onMediaClick
                )
            }
            
            item {
                TVMediaSection(
                    title = "全部内容",
                    items = sampleMedia,
                    onItemClick = onMediaClick
                )
            }
        }
        
        TVTopBar(
            onSettingsClick = onSettingsClick,
            modifier = Modifier.align(Alignment.TopEnd)
        )
    }
}

@Composable
fun TVTopBar(
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .padding(24.dp)
            .height(48.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TVIconButton(
            icon = Icons.Outlined.Search,
            onClick = {}
        )
        TVIconButton(
            icon = Icons.Outlined.Settings,
            onClick = onSettingsClick
        )
    }
}

@Composable
fun TVIconButton(
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isFocused by remember { mutableStateOf(false) }
    
    Surface(
        onClick = onClick,
        modifier = modifier
            .size(48.dp)
            .onFocusChanged { isFocused = it.isFocused },
        shape = RoundedCornerShape(8.dp),
        color = if (isFocused) MaterialTheme.colorScheme.primaryContainer
                else Color.Transparent,
        contentColor = if (isFocused) MaterialTheme.colorScheme.onPrimaryContainer
                       else MaterialTheme.colorScheme.onBackground
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TVCategoryRow(
    categories: List<String>,
    selectedIndex: Int,
    onCategorySelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 48.dp, vertical = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(categories.indices.toList()) { index ->
            TVCategoryChip(
                text = categories[index],
                selected = selectedIndex == index,
                onClick = { onCategorySelected(index) }
            )
        }
    }
}

@Composable
fun TVCategoryChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isFocused by remember { mutableStateOf(false) }
    
    val backgroundColor by animateColorAsState(
        targetValue = when {
            selected -> MaterialTheme.colorScheme.primary
            isFocused -> MaterialTheme.colorScheme.primaryContainer
            else -> Color.Transparent
        },
        animationSpec = tween(200),
        label = "backgroundColor"
    )
    
    val contentColor by animateColorAsState(
        targetValue = when {
            selected -> Color.White
            isFocused -> MaterialTheme.colorScheme.onPrimaryContainer
            else -> MaterialTheme.colorScheme.onBackground
        },
        animationSpec = tween(200),
        label = "contentColor"
    )
    
    Surface(
        onClick = onClick,
        modifier = modifier
            .height(40.dp)
            .onFocusChanged { isFocused = it.isFocused },
        shape = RoundedCornerShape(20.dp),
        color = backgroundColor,
        contentColor = contentColor
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 24.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}

@Composable
fun TVFeaturedSection(
    items: List<TVMediaItem>,
    onItemClick: (TVMediaItem) -> Unit
) {
    var selectedItem by remember { mutableStateOf(0) }
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(400.dp)
    ) {
        val item = items.getOrElse(selectedItem) { items.firstOrNull() }
        
        if (item != null) {
            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    MaterialTheme.colorScheme.background
                                ),
                                startY = 200f
                            )
                        )
                )
                
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(48.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.displaySmall,
                        color = Color.White
                    )
                    
                    item.subtitle?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    }
                    
                    item.rating?.let { rating ->
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Star,
                                contentDescription = null,
                                tint = Color(0xFFFFD700),
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = String.format("%.1f", rating),
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White
                            )
                        }
                    }
                    
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        TVActionButton(
                            icon = Icons.Default.PlayArrow,
                            text = "播放",
                            onClick = { onItemClick(item) }
                        )
                        
                        TVActionButton(
                            icon = Icons.Default.Info,
                            text = "详情",
                            onClick = {}
                        )
                    }
                }
            }
        }
        
        LazyRow(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(items.indices.toList()) { index ->
                TVIndicatorDot(
                    selected = selectedItem == index,
                    onClick = { selectedItem = index }
                )
            }
        }
    }
}

@Composable
fun TVActionButton(
    icon: ImageVector,
    text: String,
    onClick: () -> Unit
) {
    var isFocused by remember { mutableStateOf(false) }
    
    val scale by animateFloatAsState(
        targetValue = if (isFocused) 1.05f else 1f,
        animationSpec = tween(200),
        label = "scale"
    )
    
    Surface(
        onClick = onClick,
        modifier = Modifier
            .scale(scale)
            .onFocusChanged { isFocused = it.isFocused },
        shape = RoundedCornerShape(8.dp),
        color = if (isFocused) MaterialTheme.colorScheme.primary
                else Color.White.copy(alpha = 0.9f),
        contentColor = if (isFocused) Color.White else Color.Black
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}

@Composable
fun TVIndicatorDot(
    selected: Boolean,
    onClick: () -> Unit
) {
    var isFocused by remember { mutableStateOf(false) }
    
    val size by animateDpAsState(
        targetValue = when {
            selected -> 12.dp
            isFocused -> 10.dp
            else -> 8.dp
        },
        animationSpec = tween(200),
        label = "size"
    )
    
    Box(
        modifier = Modifier
            .size(size)
            .clip(RoundedCornerShape(50))
            .background(
                if (selected) MaterialTheme.colorScheme.primary
                else Color.White.copy(alpha = 0.5f)
            )
            .then(
                if (isFocused && !selected) {
                    Modifier.border(
                        width = 2.dp,
                        color = Color.White,
                        shape = RoundedCornerShape(50)
                    )
                } else Modifier
            )
            .onFocusChanged { isFocused = it.isFocused }
            .clickable(onClick = onClick)
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TVMediaSection(
    title: String,
    items: List<TVMediaItem>,
    onItemClick: (TVMediaItem) -> Unit
) {
    if (items.isEmpty()) return
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(horizontal = 48.dp, vertical = 8.dp)
        )
        
        LazyRow(
            contentPadding = PaddingValues(horizontal = 48.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(items, key = { it.id }) { item ->
                TVMediaCard(
                    item = item,
                    onClick = { onItemClick(item) },
                    modifier = Modifier.animateItem()
                )
            }
        }
    }
}

@Composable
fun TVMediaCard(
    item: TVMediaItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isFocused by remember { mutableStateOf(false) }
    
    val scale by animateFloatAsState(
        targetValue = if (isFocused) 1.08f else 1f,
        animationSpec = tween(200),
        label = "scale"
    )
    
    Card(
        modifier = modifier
            .width(180.dp)
            .aspectRatio(2f / 3f)
            .scale(scale)
            .onFocusChanged { isFocused = it.isFocused },
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isFocused) 12.dp else 4.dp
        ),
        colors = CardDefaults.cardColors(
            containerColor = if (isFocused) MaterialTheme.colorScheme.surfaceVariant
                            else MaterialTheme.colorScheme.surface
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable(onClick = onClick)
        ) {
            PosterImage(
                url = item.posterUrl,
                modifier = Modifier.fillMaxSize()
            )
            
            if (item.isNew) {
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp),
                    shape = RoundedCornerShape(4.dp),
                    color = MaterialTheme.colorScheme.primary
                ) {
                    Text(
                        text = "NEW",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
            
            if (item.progress > 0f) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .fillMaxWidth()
                        .height(4.dp)
                        .background(Color.Black.copy(alpha = 0.5f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(item.progress)
                            .background(MaterialTheme.colorScheme.primary)
                    )
                }
            }
            
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.8f)
                            )
                        )
                    )
                    .padding(12.dp)
            ) {
                Column {
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.titleSmall,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    item.subtitle?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.7f),
                            maxLines = 1
                        )
                    }
                }
            }
            
            if (isFocused) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .border(
                            width = 3.dp,
                            color = MaterialTheme.colorScheme.primary,
                            shape = RoundedCornerShape(12.dp)
                        )
                )
            }
        }
    }
}
