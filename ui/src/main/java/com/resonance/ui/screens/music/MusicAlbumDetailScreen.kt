package com.resonance.ui.screens.music

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.resonance.data.model.MediaItem
import com.resonance.data.model.MediaType
import com.resonance.ui.components.ForwardButton
import com.resonance.ui.theme.AppColors
import com.resonance.ui.theme.AppShapes
import com.resonance.ui.theme.ForwardSpacing
import com.resonance.ui.theme.ForwardTypography

/**
 * 音乐专辑详情页
 * 包含专辑封面、信息、歌曲列表等
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MusicAlbumDetailScreen(
    album: MediaItem,
    onNavigateBack: () -> Unit,
    onPlayClick: (MediaItem) -> Unit,
    onSongClick: (MediaItem) -> Unit,
    onFavoriteClick: (MediaItem) -> Unit,
    modifier: Modifier = Modifier
) {
    var isFavorite by remember { mutableStateOf(false) }
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(AppColors.DarkBackground)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            // 背景专辑封面（模糊）
            item {
                BlurredAlbumBackdrop(album)
            }
            
            // 专辑信息卡片
            item {
                AlbumInfoCard(
                    album = album,
                    isFavorite = isFavorite,
                    onFavoriteClick = { isFavorite = !isFavorite },
                    onPlayAllClick = { onPlayClick(album) }
                )
            }
            
            // 歌曲列表
            item {
                SongListSection(
                    album = album,
                    onSongClick = onSongClick
                )
            }
        }
        
        // 顶部导航栏
        TopAppBar(
            title = {},
            modifier = Modifier
                .fillMaxWidth()
                .alpha(0.9f),
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "返回",
                        tint = Color.White
                    )
                }
            },
            actions = {
                IconButton(onClick = { /* 更多选项待实现 */ }) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "更多",
                        tint = Color.White
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.Transparent
            )
        )
    }
}

/**
 * 模糊专辑封面背景
 */
@Composable
fun BlurredAlbumBackdrop(
    album: MediaItem,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(400.dp)
    ) {
        // 专辑封面
        AsyncImage(
            model = album.posterUrl,
            contentDescription = album.name,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .blur(30.dp)
        )
        
        // 渐变遮罩
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            AppColors.DarkBackground.copy(alpha = 0.9f),
                            AppColors.DarkBackground
                        )
                    )
                )
        )
    }
}

/**
 * 专辑信息卡片
 */
@Composable
fun AlbumInfoCard(
    album: MediaItem,
    isFavorite: Boolean,
    onFavoriteClick: () -> Unit,
    onPlayAllClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(ForwardSpacing.PhoneMargin),
        shape = AppShapes.Forward,
        color = AppColors.GlassMorphismDark.copy(alpha = 0.6f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(ForwardSpacing.ModuleSpacing),
            verticalArrangement = Arrangement.spacedBy(ForwardSpacing.CardSpacingVertical)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 专辑封面
                Surface(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    AsyncImage(
                        model = album.posterUrl,
                        contentDescription = album.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                
                // 专辑信息
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = album.name,
                        style = ForwardTypography.TitleLarge,
                        color = AppColors.DarkOnBackground,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    
                    // 歌手
                    album.tags.firstOrNull()?.let { artist ->
                        Text(
                            text = artist,
                            style = ForwardTypography.BodyMedium,
                            color = AppColors.DarkOnSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    
                    // 年份和歌曲数
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        album.releaseYear?.let { year ->
                            Text(
                                text = year.toString(),
                                style = ForwardTypography.BodySmall,
                                color = AppColors.DarkOnSurfaceVariant
                            )
                        }
                        
                        album.playCount?.let { count ->
                            Text(
                                text = "$count 首歌曲",
                                style = ForwardTypography.BodySmall,
                                color = AppColors.DarkOnSurfaceVariant
                            )
                        }
                    }
                }
            }
            
            // 简介
            album.overview?.let { overview ->
                Text(
                    text = overview,
                    style = ForwardTypography.BodyMedium,
                    color = AppColors.DarkOnSurface,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
            }
            
            // 操作按钮
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(ForwardSpacing.CardSpacingHorizontal)
            ) {
                // 播放全部按钮
                ForwardButton(
                    onClick = onPlayAllClick,
                    text = "播放全部",
                    modifier = Modifier.weight(1f)
                )
                
                // 收藏按钮
                OutlinedButton(
                    onClick = onFavoriteClick,
                    modifier = Modifier.weight(1f),
                    shape = AppShapes.Forward
                ) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                        contentDescription = "收藏",
                        tint = if (isFavorite) AppColors.Primary else AppColors.DarkOnSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (isFavorite) "已收藏" else "收藏")
                }
            }
        }
    }
}

/**
 * 歌曲列表模块
 */
@Composable
fun SongListSection(
    album: MediaItem,
    onSongClick: (MediaItem) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = ForwardSpacing.ModuleSpacing)
    ) {
        Text(
            text = "歌曲列表",
            style = ForwardTypography.TitleMedium,
            color = AppColors.DarkOnBackground,
            modifier = Modifier.padding(horizontal = ForwardSpacing.PhoneMargin)
        )
        
        // 模拟歌曲列表（实际应从 API 获取）
        val songs = List(10) { index ->
            MediaItem(
                id = "${album.id}_$index",
                name = "歌曲 ${index + 1}",
                mediaType = MediaType.MUSIC,
                playCount = index + 1
            )
        }
        
        LazyColumn {
            items(songs) { song ->
                SongListItem(
                    song = song,
                    index = songs.indexOf(song) + 1,
                    onClick = { onSongClick(song) }
                )
            }
        }
    }
}

/**
 * 歌曲列表项
 */
@Composable
fun SongListItem(
    song: MediaItem,
    index: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = ForwardSpacing.PhoneMargin, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 序号
            Text(
                text = index.toString(),
                style = ForwardTypography.BodyMedium,
                color = AppColors.DarkOnSurfaceVariant,
                modifier = Modifier.width(24.dp)
            )
            
            // 歌曲信息
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = song.name,
                    style = ForwardTypography.BodyMedium,
                    color = AppColors.DarkOnBackground,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                // 歌手
                song.tags.firstOrNull()?.let { artist ->
                    Text(
                        text = artist,
                        style = ForwardTypography.BodySmall,
                        color = AppColors.DarkOnSurfaceVariant
                    )
                }
            }
            
            // 播放按钮
            IconButton(onClick = onClick) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "播放",
                    tint = AppColors.Primary,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}
