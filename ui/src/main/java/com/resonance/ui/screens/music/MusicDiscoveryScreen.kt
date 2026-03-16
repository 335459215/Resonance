package com.resonance.ui.screens.music

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.resonance.data.model.MediaItem
import com.resonance.data.model.MediaType
import com.resonance.ui.components.*
import com.resonance.ui.theme.AppColors
import com.resonance.ui.theme.AppShapes
import com.resonance.ui.theme.ForwardSpacing
import com.resonance.ui.theme.ForwardTypography

/**
 * 音乐发现页（首页）
 * 包含轮播 Banner、最近播放、推荐歌单、我的歌单等
 */
@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun MusicDiscoveryScreen(
    bannerItems: List<MediaItem>,
    recentlyPlayed: List<MediaItem>,
    recommendedPlaylists: List<MediaItem>,
    myPlaylists: List<MediaItem>,
    onMediaClick: (MediaItem) -> Unit,
    onBannerClick: (MediaItem) -> Unit,
    onSeeAllClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val pagerState = rememberPagerState(pageCount = { bannerItems.size })
    
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(AppColors.DarkBackground),
        contentPadding = PaddingValues(bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(ForwardSpacing.ModuleSpacing)
    ) {
        // 轮播 Banner
        item {
            CarouselBanner(
                items = bannerItems,
                pagerState = pagerState,
                onItemClick = onBannerClick
            )
            
            // 分页指示器
            BannerPageIndicator(
                pageCount = bannerItems.size,
                currentPage = pagerState.currentPage
            )
        }
        
        // 最近播放
        if (recentlyPlayed.isNotEmpty()) {
            item {
                RecentlyPlayedSection(
                    items = recentlyPlayed,
                    onItemClick = onMediaClick
                )
            }
        }
        
        // 推荐歌单
        if (recommendedPlaylists.isNotEmpty()) {
            item {
                RecommendedSection(
                    items = recommendedPlaylists,
                    onItemClick = onMediaClick,
                    onSeeAllClick = onSeeAllClick
                )
            }
        }
        
        // 我的歌单
        if (myPlaylists.isNotEmpty()) {
            item {
                MyPlaylistsSection(
                    items = myPlaylists,
                    onItemClick = onMediaClick,
                    onSeeAllClick = onSeeAllClick
                )
            }
        }
        
        // 底部占位空间
        item {
            Spacer(modifier = Modifier.height(ForwardSpacing.ModuleSpacing))
        }
    }
}

/**
 * 轮播 Banner（音乐版）
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CarouselBanner(
    items: List<MediaItem>,
    pagerState: androidx.compose.foundation.pager.PagerState,
    onItemClick: (MediaItem) -> Unit,
    modifier: Modifier = Modifier
) {
    val screenWidth = androidx.compose.ui.platform.LocalConfiguration.current.screenWidthDp.dp
    val bannerHeight = screenWidth * 0.45f
    
    HorizontalPager(
        state = pagerState,
        modifier = modifier
            .fillMaxWidth()
            .height(bannerHeight)
    ) { page ->
        val item = items.getOrNull(page)
        item?.let {
            MusicBannerItem(
                item = it,
                onClick = { onItemClick(it) },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = ForwardSpacing.PhoneMargin)
            )
        }
    }
}

/**
 * 音乐 Banner 项
 */
@Composable
fun MusicBannerItem(
    item: MediaItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(AppShapes.Forward)
            .background(AppColors.DarkSurface)
    ) {
        // 背景图片（专辑封面）
        AsyncImage(
            model = item.posterUrl,
            contentDescription = item.name,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        
        // 渐变遮罩
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.9f)
                        )
                    )
                )
        )
        
        // 内容
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(ForwardSpacing.ModuleSpacing),
            verticalArrangement = Arrangement.Bottom
        ) {
            Text(
                text = item.name,
                style = ForwardTypography.TitleLarge,
                color = Color.White,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            
            // 歌手信息
            item.tags.firstOrNull()?.let { artist ->
                Text(
                    text = artist,
                    style = ForwardTypography.BodyMedium,
                    color = Color.White.copy(alpha = 0.8f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
            
            // 播放按钮
            Row(
                modifier = Modifier.padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    onClick = onClick,
                    shape = AppShapes.Forward,
                    color = AppColors.Primary
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "播放",
                            modifier = Modifier.size(20.dp),
                            tint = Color.White
                        )
                        Text(
                            text = "播放全部",
                            style = ForwardTypography.ButtonMedium,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

/**
 * Banner 分页指示器
 */
@Composable
fun BannerPageIndicator(
    pageCount: Int,
    currentPage: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(ForwardSpacing.PhoneMargin),
        horizontalArrangement = Arrangement.Center
    ) {
        repeat(pageCount) { index ->
            Spacer(
                modifier = Modifier
                    .size(if (index == currentPage) 8.dp else 6.dp)
                    .padding(horizontal = 2.dp)
                    .background(
                        color = if (index == currentPage) AppColors.Primary else AppColors.DarkOnSurfaceVariant.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(3.dp)
                    )
            )
        }
    }
}

/**
 * 最近播放模块
 */
@Composable
fun RecentlyPlayedSection(
    items: List<MediaItem>,
    onItemClick: (MediaItem) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        ForwardSectionHeader(
            title = "最近播放",
            actionText = "查看全部",
            onActionClick = { /* 待实现 */ }
        )
        
        LazyRow(
            contentPadding = PaddingValues(horizontal = ForwardSpacing.PhoneMargin),
            horizontalArrangement = Arrangement.spacedBy(ForwardSpacing.CardSpacingHorizontal)
        ) {
            items(items) { item ->
                RecentlyPlayedCard(
                    item = item,
                    onClick = { onItemClick(item) }
                )
            }
        }
    }
}

/**
 * 最近播放卡片（1:1 专辑封面）
 */
@Composable
fun RecentlyPlayedCard(
    item: MediaItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .width(140.dp)
            .clip(AppShapes.Forward),
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = AppColors.DarkSurface
        )
    ) {
        Column {
            // 专辑封面（1:1）
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
            ) {
                AsyncImage(
                    model = item.posterUrl,
                    contentDescription = item.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                
                // 播放按钮遮罩
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center
                ) {
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = Color.White.copy(alpha = 0.8f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "播放",
                            modifier = Modifier.size(32.dp),
                            tint = Color.Black
                        )
                    }
                }
            }
            
            // 信息
            Column(
                modifier = Modifier.padding(ForwardSpacing.CardSpacingVertical),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = item.name,
                    style = ForwardTypography.BodySmall,
                    color = AppColors.DarkOnBackground,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                // 歌手
                item.tags.firstOrNull()?.let { artist ->
                    Text(
                        text = artist,
                        style = ForwardTypography.LabelSmall,
                        color = AppColors.DarkOnSurfaceVariant
                    )
                }
            }
        }
    }
}

/**
 * 推荐歌单模块
 */
@Composable
fun RecommendedSection(
    items: List<MediaItem>,
    onItemClick: (MediaItem) -> Unit,
    onSeeAllClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        ForwardSectionHeader(
            title = "推荐歌单",
            actionText = "查看全部",
            onActionClick = { onSeeAllClick("recommended") }
        )
        
        ForwardHorizontalList(
            items = items
        ) { item ->
            MusicPlaylistCard(
                item = item,
                onClick = { onItemClick(item) }
            )
        }
    }
}

/**
 * 我的歌单模块
 */
@Composable
fun MyPlaylistsSection(
    items: List<MediaItem>,
    onItemClick: (MediaItem) -> Unit,
    onSeeAllClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        ForwardSectionHeader(
            title = "我的歌单",
            actionText = "查看全部",
            onActionClick = { onSeeAllClick("my_playlists") }
        )
        
        ForwardHorizontalList(
            items = items
        ) { item ->
            MusicPlaylistCard(
                item = item,
                onClick = { onItemClick(item) }
            )
        }
    }
}

/**
 * 音乐歌单卡片（1:1 专辑封面）
 */
@Composable
fun MusicPlaylistCard(
    item: MediaItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .width(140.dp)
            .clip(AppShapes.Forward),
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = AppColors.DarkSurface
        )
    ) {
        Column {
            // 专辑封面（1:1）
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
            ) {
                AsyncImage(
                    model = item.posterUrl,
                    contentDescription = item.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
            
            // 信息
            Column(
                modifier = Modifier.padding(ForwardSpacing.CardSpacingVertical),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = item.name,
                    style = ForwardTypography.BodySmall,
                    color = AppColors.DarkOnBackground,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                // 歌曲数量
                Text(
                    text = "${item.playCount ?: 0} 首歌曲",
                    style = ForwardTypography.LabelSmall,
                    color = AppColors.DarkOnSurfaceVariant
                )
            }
        }
    }
}
