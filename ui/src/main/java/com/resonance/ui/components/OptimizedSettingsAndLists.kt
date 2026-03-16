package com.resonance.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.resonance.ui.theme.UIDesignSystem

/**
 * 优化的设置项组件
 */
@Composable
fun OptimizedSettingsItem(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    trailingContent: @Composable (() -> Unit)? = null,
    showArrow: Boolean = true
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()
    
    val backgroundColor by animateColorAsState(
        targetValue = if (isHovered) UIDesignSystem.Colors.SurfaceVariant else Color.Transparent,
        label = "backgroundColor"
    )
    
    val scale by animateFloatAsState(
        targetValue = if (isHovered) 1.02f else 1f,
        label = "scale"
    )
    
    Row(
        modifier = modifier
            .fillMaxWidth()
            .scale(scale)
            .clip(RoundedCornerShape(UIDesignSystem.Radius.Md))
            .background(backgroundColor)
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) { onClick() }
            .padding(UIDesignSystem.Spacing.Md),
        horizontalArrangement = Arrangement.spacedBy(UIDesignSystem.Spacing.Md),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 图标
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(UIDesignSystem.Colors.Primary.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = UIDesignSystem.Colors.Primary,
                modifier = Modifier.size(UIDesignSystem.IconSize.Lg)
            )
        }
        
        // 文字内容
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(UIDesignSystem.Spacing.Xxs)
        ) {
            Text(
                text = title,
                color = UIDesignSystem.Colors.OnSurface,
                fontSize = UIDesignSystem.FontSize.Md,
                fontWeight = FontWeight.Medium,
                maxLines = 1
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    color = UIDesignSystem.Colors.OnSurfaceVariant,
                    fontSize = UIDesignSystem.FontSize.Xs,
                    maxLines = 1
                )
            }
        }
        
        // 尾部内容
        if (trailingContent != null) {
            trailingContent()
        } else if (showArrow) {
            Icon(
                imageVector = Icons.Filled.ChevronRight,
                contentDescription = "进入",
                tint = UIDesignSystem.Colors.OnSurfaceVariant,
                modifier = Modifier.size(UIDesignSystem.IconSize.Md)
            )
        }
    }
}

/**
 * 优化的设置开关项
 */
@Composable
fun OptimizedSettingsSwitchItem(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    OptimizedSettingsItem(
        icon = icon,
        title = title,
        subtitle = subtitle,
        onClick = { onCheckedChange(!checked) },
        modifier = modifier,
        showArrow = false,
        trailingContent = {
            Switch(
                checked = checked,
                onCheckedChange = null,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = UIDesignSystem.Colors.OnPrimary,
                    checkedTrackColor = UIDesignSystem.Colors.Primary,
                    uncheckedThumbColor = UIDesignSystem.Colors.OnSurfaceVariant,
                    uncheckedTrackColor = UIDesignSystem.Colors.SurfaceVariant
                )
            )
        }
    )
}

/**
 * 优化的设置分组标题
 */
@Composable
fun OptimizedSettingsGroupTitle(
    title: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = title,
        color = UIDesignSystem.Colors.Primary,
        fontSize = UIDesignSystem.FontSize.Xs,
        fontWeight = FontWeight.SemiBold,
        modifier = modifier.padding(
            horizontal = UIDesignSystem.Spacing.Md,
            vertical = UIDesignSystem.Spacing.Sm
        )
    )
}

/**
 * 优化的分隔线
 */
@Composable
fun OptimizedDivider(
    modifier: Modifier = Modifier,
    color: Color = UIDesignSystem.Colors.Divider
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(color)
    )
}

/**
 * 优化的海报卡片
 */
@Composable
fun OptimizedPosterCard(
    imageUrl: String,
    title: String,
    subtitle: String? = null,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
    showTitle: Boolean = true
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()
    
    val scale by animateFloatAsState(
        targetValue = if (isHovered) 1.05f else 1f,
        label = "scale"
    )
    
    val elevation by animateFloatAsState(
        targetValue = if (isHovered) UIDesignSystem.Elevation.Lg.value else UIDesignSystem.Elevation.Md.value,
        label = "elevation"
    )
    
    Column(
        modifier = modifier
            .scale(scale)
            .clip(RoundedCornerShape(UIDesignSystem.PosterStyles.RadiusVal))
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) { onClick() },
        verticalArrangement = Arrangement.spacedBy(UIDesignSystem.Spacing.Sm)
    ) {
        // 海报图片区域
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(UIDesignSystem.PosterStyles.AspectRatio)
                .clip(RoundedCornerShape(UIDesignSystem.PosterStyles.RadiusVal))
                .background(UIDesignSystem.Colors.SurfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            if (isLoading) {
                // 加载骨架屏
                OptimizedSkeletonLoading(
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                // 实际图片（这里用占位符，实际使用时替换为 AsyncImage）
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Image,
                        contentDescription = title,
                        tint = UIDesignSystem.Colors.OnSurfaceVariant.copy(alpha = 0.3f),
                        modifier = Modifier.size(UIDesignSystem.IconSize.Xl)
                    )
                }
            }
        }
        
        // 标题和副标题
        if (showTitle) {
            Column(
                verticalArrangement = Arrangement.spacedBy(UIDesignSystem.Spacing.Xxs)
            ) {
                Text(
                    text = title,
                    color = UIDesignSystem.Colors.OnSurface,
                    fontSize = UIDesignSystem.FontSize.Sm,
                    fontWeight = FontWeight.Medium,
                    maxLines = 2
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        color = UIDesignSystem.Colors.OnSurfaceVariant,
                        fontSize = UIDesignSystem.FontSize.Xs,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

/**
 * 优化的网格布局
 */
@Composable
fun OptimizedGridLayout(
    items: List<Any>,
    columns: Int = 3,
    itemContent: @Composable (index: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val chunkedItems = items.chunked(columns)
    
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(UIDesignSystem.Spacing.Md)
    ) {
        chunkedItems.forEach { rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(UIDesignSystem.Spacing.Md)
            ) {
                rowItems.forEachIndexed { index, _ ->
                    val actualIndex = chunkedItems.indexOf(rowItems) * columns + index
                    Box(
                        modifier = Modifier.weight(1f)
                    ) {
                        itemContent(actualIndex)
                    }
                }
                
                // 填充空位
                repeat(columns - rowItems.size) {
                    Box(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

/**
 * 优化的列表项（通用）
 */
@Composable
fun <T> OptimizedListItem(
    item: T,
    icon: ImageVector? = null,
    title: String,
    subtitle: String? = null,
    trailingText: String? = null,
    onClick: (T) -> Unit,
    modifier: Modifier = Modifier,
    showDivider: Boolean = true
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()
    
    val backgroundColor by animateColorAsState(
        targetValue = if (isHovered) UIDesignSystem.Colors.SurfaceVariant else Color.Transparent,
        label = "backgroundColor"
    )
    
    Column {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(UIDesignSystem.Radius.Md))
                .background(backgroundColor)
                .clickable(
                    interactionSource = interactionSource,
                    indication = null
                ) { onClick(item) }
                .padding(UIDesignSystem.Spacing.Md),
            horizontalArrangement = Arrangement.spacedBy(UIDesignSystem.Spacing.Md),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 图标
            if (icon != null) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(UIDesignSystem.Colors.Primary.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = UIDesignSystem.Colors.Primary,
                        modifier = Modifier.size(UIDesignSystem.IconSize.Md)
                    )
                }
            }
            
            // 文字内容
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(UIDesignSystem.Spacing.Xxs)
            ) {
                Text(
                    text = title,
                    color = UIDesignSystem.Colors.OnSurface,
                    fontSize = UIDesignSystem.FontSize.Md,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        color = UIDesignSystem.Colors.OnSurfaceVariant,
                        fontSize = UIDesignSystem.FontSize.Xs,
                        maxLines = 1
                    )
                }
            }
            
            // 尾部文字
            if (trailingText != null) {
                Text(
                    text = trailingText,
                    color = UIDesignSystem.Colors.OnSurfaceVariant,
                    fontSize = UIDesignSystem.FontSize.Xs,
                    fontWeight = FontWeight.Medium
                )
            }
        }
        
        // 分隔线
        if (showDivider) {
            OptimizedDivider(
                modifier = Modifier.padding(start = UIDesignSystem.Spacing.Xl)
            )
        }
    }
}
