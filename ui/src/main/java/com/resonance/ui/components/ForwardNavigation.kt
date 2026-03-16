package com.resonance.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.resonance.ui.model.NavigationConfig
import com.resonance.ui.model.NavigationItem
import com.resonance.ui.theme.AppColors
import com.resonance.ui.theme.AppShapes
import com.resonance.ui.theme.ForwardTypography
import kotlinx.coroutines.delay

/**
 * Forward 风格动态底部导航栏
 * 高度 56pt，毛玻璃背景，圆角 20pt
 */
@Composable
fun ForwardBottomNavigation(
    config: NavigationConfig,
    currentTabId: String,
    onTabSelected: (String) -> Unit,
    onLeftButtonClick: (() -> Unit)? = null,
    onRightButtonClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    isSwitching: Boolean = false
) {
    // 服务器切换时的淡入淡出动画
    val alpha by animateFloatAsState(
        targetValue = if (isSwitching) 0f else 1f,
        animationSpec = tween(150),
        label = "navigationAlpha"
    )
    
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .background(
                color = AppColors.GlassMorphismDark.copy(alpha = 0.9f),
                shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
            )
            .alpha(alpha)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 左侧辅助按钮
            if (config.leftButton != null) {
                ForwardNavigationButton(
                    item = config.leftButton,
                    isSelected = false,
                    onClick = { onLeftButtonClick?.invoke() },
                    isAuxiliary = true
                )
            } else {
                Spacer(modifier = Modifier.width(48.dp))
            }
            
            // 中间核心 Tab
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                config.coreTabs.forEach { tab ->
                    ForwardNavigationButton(
                        item = tab,
                        isSelected = currentTabId == tab.id,
                        onClick = { onTabSelected(tab.id) },
                        isAuxiliary = false
                    )
                }
            }
            
            // 右侧辅助按钮
            if (config.rightButton != null) {
                ForwardNavigationButton(
                    item = config.rightButton,
                    isSelected = false,
                    onClick = { onRightButtonClick?.invoke() },
                    isAuxiliary = true
                )
            } else {
                Spacer(modifier = Modifier.width(48.dp))
            }
        }
    }
}

/**
 * Forward 风格导航按钮
 */
@Composable
fun ForwardNavigationButton(
    item: NavigationItem,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isAuxiliary: Boolean = false
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    // 按钮大小
    val buttonSize = if (isAuxiliary) 40.dp else 64.dp
    
    // 颜色
    val iconColor = if (isSelected) AppColors.Primary else Color.White
    val backgroundColor = if (isPressed) {
        AppColors.GlassMorphismDark.copy(alpha = 0.3f)
    } else {
        Color.Transparent
    }
    
    // 缩放动画（选中时略微放大）
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.1f else 1f,
        animationSpec = spring(
            dampingRatio = 0.8f,
            stiffness = Spring.StiffnessMedium
        ),
        label = "buttonScale"
    )
    
    Box(
        modifier = modifier
            .size(buttonSize)
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .scale(scale),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(horizontal = 4.dp)
        ) {
            // 图标
            val icon = if (isSelected && item.selectedIcon != null) {
                item.selectedIcon
            } else {
                item.icon
            }
            
            Icon(
                imageVector = icon,
                contentDescription = item.label,
                tint = iconColor,
                modifier = Modifier.size(if (isAuxiliary) 24.dp else 28.dp)
            )
            
            // 文字（仅核心 Tab 显示）
            if (!isAuxiliary) {
                Text(
                    text = item.label,
                    style = ForwardTypography.LabelSmall,
                    color = iconColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
            
            // 徽章
            if (item.badge != null && item.badge > 0) {
                Box(
                    modifier = Modifier
                        .offset { IntOffset(12, -8) }
                        .background(
                            color = AppColors.Primary,
                            shape = RoundedCornerShape(50)
                        )
                        .padding(horizontal = 4.dp, vertical = 1.dp)
                ) {
                    Text(
                        text = item.badge.toString(),
                        style = ForwardTypography.LabelSmall,
                        color = Color.White
                    )
                }
            }
        }
    }
}

/**
 * 辅助按钮淡入淡出动画
 */
@Composable
fun AnimatedAuxiliaryButton(
    visible: Boolean,
    item: NavigationItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(300)) + slideInHorizontally(
            initialOffsetX = { if (it > 0) -50 else 50 },
            animationSpec = spring(dampingRatio = 0.8f)
        ),
        exit = fadeOut(animationSpec = tween(300)) + slideOutHorizontally(
            targetOffsetX = { if (it > 0) -50 else 50 },
            animationSpec = spring(dampingRatio = 0.8f)
        )
    ) {
        ForwardNavigationButton(
            item = item,
            isSelected = false,
            onClick = onClick,
            isAuxiliary = true,
            modifier = modifier
        )
    }
}

/**
 * 导航栏分隔线
 */
@Composable
fun NavigationDivider(
    modifier: Modifier = Modifier
) {
    Spacer(
        modifier = modifier
            .width(1.dp)
            .height(24.dp)
            .background(AppColors.GlassBorder)
    )
}
