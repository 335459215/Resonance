package com.resonance.ui.screens.video

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import com.resonance.ui.components.*
import com.resonance.ui.theme.AppColors
import com.resonance.ui.theme.AppShapes
import com.resonance.ui.theme.ForwardSpacing
import com.resonance.ui.theme.ForwardTypography

/**
 * 影视详情页面
 * 包含海报、信息、演员、相关推荐等
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoDetailScreen(
    mediaItem: MediaItem,
    onNavigateBack: () -> Unit,
    onPlayClick: (MediaItem) -> Unit,
    onFavoriteClick: (MediaItem) -> Unit,
    onRelatedClick: (MediaItem) -> Unit,
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
            // 背景海报（模糊）
            item {
                BlurredBackdrop(mediaItem)
            }
            
            // 信息卡片
            item {
                VideoInfoCard(
                    mediaItem = mediaItem,
                    isFavorite = isFavorite,
                    onFavoriteClick = { isFavorite = !isFavorite },
                    onPlayClick = { onPlayClick(mediaItem) }
                )
            }
            
            // 演员列表
            if (mediaItem.tags.isNotEmpty()) {
                item {
                    CastSection(
                        title = "演员",
                        cast = mediaItem.tags
                    )
                }
            }
            
            // 相关推荐
            item {
                RelatedSection(
                    title = "相关推荐",
                    items = emptyList() // 待实现：加载相关推荐
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
 * 模糊背景海报
 */
@Composable
fun BlurredBackdrop(
    mediaItem: MediaItem,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(400.dp)
    ) {
        // 背景图片
        AsyncImage(
            model = mediaItem.backdropUrl ?: mediaItem.posterUrl,
            contentDescription = mediaItem.name,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .blur(20.dp)
        )
        
        // 渐变遮罩
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            AppColors.DarkBackground.copy(alpha = 0.8f),
                            AppColors.DarkBackground
                        )
                    )
                )
        )
    }
}

/**
 * 影视信息卡片
 */
@Composable
fun VideoInfoCard(
    mediaItem: MediaItem,
    isFavorite: Boolean,
    onFavoriteClick: () -> Unit,
    onPlayClick: () -> Unit,
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
            // 标题和评分
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = mediaItem.name,
                    style = ForwardTypography.HeadlineSmall,
                    color = AppColors.DarkOnBackground,
                    modifier = Modifier.weight(1f)
                )
                
                mediaItem.rating?.let { rating ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "评分",
                            tint = AppColors.Highlight,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = String.format("%.1f", rating),
                            style = ForwardTypography.BodyLarge,
                            color = AppColors.DarkOnBackground
                        )
                    }
                }
            }
            
            // 元信息（年份、时长等）
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                mediaItem.releaseYear?.let { year ->
                    Text(
                        text = year.toString(),
                        style = ForwardTypography.BodyMedium,
                        color = AppColors.DarkOnSurfaceVariant
                    )
                }
                
                mediaItem.runTimeTicks?.let { ticks ->
                    val minutes = ticks / 600000000L
                    Text(
                        text = "${minutes}分钟",
                        style = ForwardTypography.BodyMedium,
                        color = AppColors.DarkOnSurfaceVariant
                    )
                }
                
                // 类型标签
                mediaItem.genres.firstOrNull()?.let { genre ->
                    Surface(
                        color = AppColors.Primary.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = genre,
                            style = ForwardTypography.LabelSmall,
                            color = AppColors.Primary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }
            }
            
            // 简介
            mediaItem.overview?.let { overview ->
                Text(
                    text = overview,
                    style = ForwardTypography.BodyMedium,
                    color = AppColors.DarkOnSurface,
                    maxLines = 4,
                    overflow = TextOverflow.Ellipsis
                )
            }
            
            // 操作按钮
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(ForwardSpacing.CardSpacingHorizontal)
            ) {
                // 播放按钮
                ForwardButton(
                    onClick = onPlayClick,
                    text = "立即播放",
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
 * 演员列表模块
 */
@Composable
fun CastSection(
    title: String,
    cast: List<String>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = ForwardSpacing.ModuleSpacing)
    ) {
        Text(
            text = title,
            style = ForwardTypography.TitleMedium,
            color = AppColors.DarkOnBackground,
            modifier = Modifier.padding(horizontal = ForwardSpacing.PhoneMargin)
        )
        
        LazyRow(
            contentPadding = PaddingValues(
                horizontal = ForwardSpacing.PhoneMargin,
                vertical = ForwardSpacing.CardSpacingVertical
            ),
            horizontalArrangement = Arrangement.spacedBy(ForwardSpacing.CardSpacingHorizontal)
        ) {
            items(cast) { castMember ->
                CastMemberCard(name = castMember)
            }
        }
    }
}

/**
 * 演员卡片
 */
@Composable
fun CastMemberCard(
    name: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .width(80.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 头像（占位）
        Surface(
            modifier = Modifier
                .size(64.dp)
                .clip(RoundedCornerShape(50)),
            color = AppColors.DarkSurfaceVariant
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = name,
                    tint = AppColors.DarkOnSurfaceVariant,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
        
        // 名字
        Text(
            text = name,
            style = ForwardTypography.LabelSmall,
            color = AppColors.DarkOnSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

/**
 * 相关推荐模块
 */
@Composable
fun RelatedSection(
    title: String,
    items: List<MediaItem>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = ForwardSpacing.ModuleSpacing)
    ) {
        ForwardSectionHeader(
            title = title,
            actionText = "查看全部",
            onActionClick = { /* 待实现 */ }
        )
        
        if (items.isEmpty()) {
            // 空状态占位
            Spacer(modifier = Modifier.height(100.dp))
        } else {
            ForwardHorizontalList(
                items = items
            ) { item ->
                RelatedMediaCard(item = item)
            }
        }
    }
}

/**
 * 推荐媒体卡片
 */
@Composable
fun RelatedMediaCard(
    item: MediaItem,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .width(140.dp)
            .clip(AppShapes.Forward),
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
