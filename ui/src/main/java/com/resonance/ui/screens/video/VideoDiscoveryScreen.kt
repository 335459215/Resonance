package com.resonance.ui.screens.video

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
import androidx.compose.material.icons.filled.*
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
import kotlinx.coroutines.launch

/**
 * 影视发现页（首页）
 * 包含轮播 Banner、继续观看、媒体库、分类内容等
 */
@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun VideoDiscoveryScreen(
    bannerItems: List<MediaItem>,
    continueWatching: List<MediaItem>,
    categories: List<VideoCategory>,
    onMediaClick: (MediaItem) -> Unit,
    onBannerClick: (MediaItem) -> Unit,
    onSeeAllClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val pagerState = rememberPagerState(pageCount = { bannerItems.size })
    val scope = rememberCoroutineScope()
    
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
        
        // 继续观看
        if (continueWatching.isNotEmpty()) {
            item {
                ContinueWatchingSection(
                    items = continueWatching,
                    onItemClick = onMediaClick
                )
            }
        }
        
        // 媒体库分类
        item {
            MediaLibrarySection(
                onCategoryClick = onSeeAllClick
            )
        }
        
        // 分类内容模块组
        categories.forEach { category ->
            item {
                VideoCategorySection(
                    category = category,
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
 * 轮播 Banner
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
    val bannerHeight = screenWidth * 0.56f // 16:9 比例
    
    HorizontalPager(
        state = pagerState,
        modifier = modifier
            .fillMaxWidth()
            .height(bannerHeight)
    ) { page ->
        val item = items.getOrNull(page)
        item?.let {
            BannerItem(
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
 * Banner 项
 */
@Composable
fun BannerItem(
    item: MediaItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(AppShapes.Forward)
            .background(AppColors.DarkSurface)
    ) {
        // 背景图片
        AsyncImage(
            model = item.backdropUrl ?: item.posterUrl,
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
                            Color.Black.copy(alpha = 0.8f)
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
            
            item.overview?.let { overview ->
                Text(
                    text = overview,
                    style = ForwardTypography.BodySmall,
                    color = Color.White.copy(alpha = 0.8f),
                    maxLines = 2,
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
                            text = "立即播放",
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
 * 继续观看模块
 */
@Composable
fun ContinueWatchingSection(
    items: List<MediaItem>,
    onItemClick: (MediaItem) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        ForwardSectionHeader(
            title = "继续观看",
            actionText = "查看全部",
            onActionClick = { /* 待实现：查看全部 */ }
        )
        
        ForwardHorizontalList(
            items = items
        ) { item ->
            ContinueWatchingCard(
                item = item,
                onClick = { onItemClick(item) }
            )
        }
    }
}

/**
 * 继续观看卡片
 */
@Composable
fun ContinueWatchingCard(
    item: MediaItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .width(200.dp)
            .clip(AppShapes.Forward),
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = AppColors.DarkSurface
        )
    ) {
        Column {
            // 海报
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
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
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = item.name,
                    style = ForwardTypography.BodyMedium,
                    color = AppColors.DarkOnBackground,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                // 进度条
                item.userData?.playedPercentage?.let { percentage ->
                    Column {
                        LinearProgressIndicator(
                            progress = { percentage / 100f },
                            modifier = Modifier.fillMaxWidth(),
                            color = AppColors.Primary,
                            trackColor = AppColors.DarkSurfaceVariant
                        )
                        Text(
                            text = "已观看 $percentage%",
                            style = ForwardTypography.LabelSmall,
                            color = AppColors.DarkOnSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

/**
 * 媒体库分类
 */
@Composable
fun MediaLibrarySection(
    onCategoryClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val categories = listOf(
        MediaLibraryCategory("电影", Icons.Default.Movie),
        MediaLibraryCategory("剧集", Icons.Default.Tv),
        MediaLibraryCategory("动漫", Icons.Default.Star),
        MediaLibraryCategory("纪录片", Icons.Default.Info)
    )
    
    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        ForwardSectionHeader(title = "媒体库")
        
        LazyRow(
            contentPadding = PaddingValues(horizontal = ForwardSpacing.PhoneMargin),
            horizontalArrangement = Arrangement.spacedBy(ForwardSpacing.CardSpacingHorizontal)
        ) {
            items(categories) { category ->
                MediaLibraryCard(
                    category = category,
                    onClick = { onCategoryClick(category.name) }
                )
            }
        }
    }
}

data class MediaLibraryCategory(
    val name: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)

/**
 * 媒体库卡片
 */
@Composable
fun MediaLibraryCard(
    category: MediaLibraryCategory,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .width(100.dp)
            .height(100.dp),
        shape = AppShapes.Forward,
        color = AppColors.DarkSurfaceVariant,
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(ForwardSpacing.CardSpacingVertical),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = category.icon,
                contentDescription = category.name,
                tint = AppColors.Primary,
                modifier = Modifier.size(32.dp)
            )
            
            Text(
                text = category.name,
                style = ForwardTypography.BodySmall,
                color = AppColors.DarkOnSurface,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

/**
 * 影视分类数据
 */
data class VideoCategory(
    val id: String,
    val name: String,
    val items: List<MediaItem>
)

/**
 * 分类内容模块
 */
@Composable
fun VideoCategorySection(
    category: VideoCategory,
    onItemClick: (MediaItem) -> Unit,
    onSeeAllClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        ForwardSectionHeader(
            title = category.name,
            actionText = "查看全部",
            onActionClick = { onSeeAllClick(category.id) }
        )
        
        ForwardHorizontalList(
            items = category.items
        ) { item ->
            VideoMediaCard(
                item = item,
                onClick = { onItemClick(item) }
            )
        }
    }
}

/**
 * 影视媒体卡片
 */
@Composable
fun VideoMediaCard(
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
            // 海报
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(2f / 3f)
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
                
                item.releaseYear?.let { year ->
                    Text(
                        text = year.toString(),
                        style = ForwardTypography.LabelSmall,
                        color = AppColors.DarkOnSurfaceVariant
                    )
                }
            }
        }
    }
}
