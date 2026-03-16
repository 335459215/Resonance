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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Search
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
 * 优化的底部导航栏组件
 * 使用 UIDesignSystem 统一设计规范
 */
@Composable
fun OptimizedBottomNavigationBar(
    items: List<NavigationBarItem>,
    selectedItem: String,
    onItemSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(80.dp),
        color = UIDesignSystem.Colors.Surface.copy(alpha = 0.95f),
        shadowElevation = UIDesignSystem.Elevation.Lg,
        shape = RoundedCornerShape(
            topStart = UIDesignSystem.Radius.Xl,
            topEnd = UIDesignSystem.Radius.Xl
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = UIDesignSystem.Spacing.Lg),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEach { item ->
                val isSelected = selectedItem == item.id
                OptimizedNavigationItem(
                    item = item,
                    isSelected = isSelected,
                    onClick = { onItemSelected(item.id) }
                )
            }
        }
    }
}

/**
 * 优化的导航项
 */
@Composable
private fun OptimizedNavigationItem(
    item: NavigationBarItem,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()
    
    val scale by animateFloatAsState(
        targetValue = if (isSelected || isHovered) 1.1f else 1f,
        label = "scale"
    )
    
    val iconColor by animateColorAsState(
        targetValue = if (isSelected) UIDesignSystem.Colors.Primary 
                     else UIDesignSystem.Colors.OnSurfaceVariant,
        label = "iconColor"
    )
    
    val textColor by animateColorAsState(
        targetValue = if (isSelected) UIDesignSystem.Colors.Primary 
                     else UIDesignSystem.Colors.OnSurfaceVariant,
        label = "textColor"
    )
    
    Column(
        modifier = Modifier
            .scale(scale)
            .clip(CircleShape)
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) { onClick() }
            .padding(
                horizontal = UIDesignSystem.Spacing.Lg,
                vertical = UIDesignSystem.Spacing.Sm
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(UIDesignSystem.Spacing.Xs)
    ) {
        Icon(
            imageVector = if (isSelected) item.selectedIcon ?: item.icon else item.icon,
            contentDescription = item.label,
            tint = iconColor,
            modifier = Modifier.size(UIDesignSystem.IconSize.Lg)
        )
        
        Text(
            text = item.label,
            color = textColor,
            fontSize = UIDesignSystem.FontSize.Xs,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            maxLines = 1
        )
    }
}

/**
 * 优化的搜索栏组件
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OptimizedSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "搜索...",
    enabled: Boolean = true
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
        placeholder = {
            Text(
                text = placeholder,
                color = UIDesignSystem.Colors.OnSurfaceVariant
            )
        },
        leadingIcon = {
            Icon(
                imageVector = if (query.isNotEmpty()) Icons.Filled.Search else Icons.Outlined.Search,
                contentDescription = "搜索",
                tint = UIDesignSystem.Colors.OnSurfaceVariant
            )
        },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { 
                    onQueryChange("")
                }) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = "清除",
                        tint = UIDesignSystem.Colors.OnSurfaceVariant,
                        modifier = Modifier.size(UIDesignSystem.IconSize.Md)
                    )
                }
            }
        },
        singleLine = true,
        shape = RoundedCornerShape(UIDesignSystem.Radius.Xl),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = UIDesignSystem.Colors.Primary,
            unfocusedBorderColor = UIDesignSystem.Colors.SurfaceVariant,
            focusedTextColor = UIDesignSystem.Colors.OnSurface,
            unfocusedTextColor = UIDesignSystem.Colors.OnSurface,
            focusedPlaceholderColor = UIDesignSystem.Colors.OnSurfaceVariant,
            unfocusedPlaceholderColor = UIDesignSystem.Colors.OnSurfaceVariant,
            focusedLeadingIconColor = UIDesignSystem.Colors.Primary,
            unfocusedLeadingIconColor = UIDesignSystem.Colors.OnSurfaceVariant,
            focusedTrailingIconColor = UIDesignSystem.Colors.Primary,
            unfocusedTrailingIconColor = UIDesignSystem.Colors.OnSurfaceVariant
        ),
        enabled = enabled
    )
}

/**
 * 导航项数据类
 */
data class NavigationBarItem(
    val id: String,
    val label: String,
    val icon: ImageVector,
    val selectedIcon: ImageVector? = null,
    val badge: Int? = null
)

/**
 * 骨架屏加载组件
 */
@Composable
fun OptimizedSkeletonLoading(
    modifier: Modifier = Modifier,
    shape: androidx.compose.ui.graphics.Shape? = null
) {
    val skeletonColor = UIDesignSystem.Colors.SurfaceVariant
    val highlightColor = UIDesignSystem.Colors.SurfaceVariant.copy(alpha = 0.5f)
    
    Box(
        modifier = modifier
            .background(
                brush = androidx.compose.ui.graphics.Brush.horizontalGradient(
                    colors = listOf(
                        skeletonColor,
                        highlightColor,
                        skeletonColor
                    )
                )
            )
            .then(
                if (shape != null) {
                    Modifier.clip(shape)
                } else {
                    Modifier.clip(RoundedCornerShape(UIDesignSystem.Radius.Md))
                }
            )
    )
}

/**
 * 卡片骨架屏
 */
@Composable
fun CardSkeletonLoading(
    modifier: Modifier = Modifier,
    showImage: Boolean = true,
    showTitle: Boolean = true,
    showSubtitle: Boolean = true
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(UIDesignSystem.Spacing.Sm)
    ) {
        if (showImage) {
            OptimizedSkeletonLoading(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(2f / 3f)
            )
        }
        
        if (showTitle) {
            OptimizedSkeletonLoading(
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .height(16.dp)
            )
        }
        
        if (showSubtitle) {
            OptimizedSkeletonLoading(
                modifier = Modifier
                    .fillMaxWidth(0.6f)
                    .height(12.dp)
            )
        }
    }
}
