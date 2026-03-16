package com.resonance.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.resonance.ui.theme.AppColors
import com.resonance.ui.theme.AppShapes
import com.resonance.ui.theme.ForwardSpacing
import com.resonance.ui.theme.ForwardTypography

enum class ButtonSize {
    LARGE,
    MEDIUM
}

/**
 * Forward 风格按钮
 * 珊瑚红主色，圆角 20pt，带按压效果
 */
@Composable
fun ForwardButton(
    onClick: () -> Unit,
    text: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    showLoading: Boolean = false,
    size: ButtonSize = ButtonSize.LARGE
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    val backgroundColor = if (enabled) {
        if (isPressed) AppColors.PrimaryDark else AppColors.Primary
    } else {
        AppColors.DarkSurfaceVariant
    }
    
    val textStyle = when (size) {
        ButtonSize.LARGE -> ForwardTypography.ButtonLarge
        ButtonSize.MEDIUM -> ForwardTypography.ButtonMedium
    }
    
    Button(
        onClick = onClick,
        modifier = modifier
            .height(if (size == ButtonSize.LARGE) 56.dp else 44.dp),
        enabled = enabled,
        interactionSource = interactionSource,
        colors = ButtonDefaults.buttonColors(
            containerColor = backgroundColor,
            contentColor = Color.White,
            disabledContainerColor = AppColors.DarkSurfaceVariant,
            disabledContentColor = AppColors.DarkOnSurfaceVariant
        ),
        shape = AppShapes.Forward
    ) {
        if (showLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                color = Color.White,
                strokeWidth = 2.dp
            )
            Spacer(modifier = Modifier.width(8.dp))
        }
        
        Text(
            text = text,
            style = textStyle
        )
    }
}

/**
 * Forward 风格毛玻璃卡片
 * 圆角 20pt，半透明背景，带边框
 */
@Composable
fun ForwardCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = modifier
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
        shape = AppShapes.Forward,
        color = AppColors.GlassMorphismDark.copy(alpha = 0.6f),
        tonalElevation = 0.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = 1.dp,
                    color = AppColors.GlassBorder,
                    shape = AppShapes.Forward
                )
                .padding(ForwardSpacing.ModuleSpacing)
        ) {
            content()
        }
    }
}

/**
 * Forward 风格搜索框
 * 毛玻璃效果，圆角 20pt，高度 36pt
 */
@Composable
fun ForwardSearchBar(
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "搜索电影、剧集、音乐...",
    onSearch: (String) -> Unit = {},
    onClear: () -> Unit = {}
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier
            .height(36.dp),
        placeholder = {
            Text(
                text = placeholder,
                style = ForwardTypography.BodySmall,
                color = AppColors.DarkOnSurfaceVariant
            )
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "搜索",
                tint = AppColors.DarkOnSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        },
        trailingIcon = {
            if (value.text.isNotEmpty()) {
                IconButton(onClick = {
                    onClear()
                    onValueChange(TextFieldValue(""))
                }) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = "清除",
                        tint = AppColors.DarkOnSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        },
        shape = AppShapes.Forward,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Color.Transparent,
            unfocusedBorderColor = Color.Transparent,
            focusedContainerColor = AppColors.GlassMorphismDark,
            unfocusedContainerColor = AppColors.GlassMorphismDark,
            cursorColor = AppColors.Primary
        ),
        textStyle = ForwardTypography.BodyLarge.copy(color = AppColors.DarkOnBackground),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(
            onSearch = {
                onSearch(value.text)
            }
        ),
        singleLine = true,
        maxLines = 1
    )
}

/**
 * Forward 风格顶部导航栏
 * 包含服务器选择、搜索框、更多按钮
 */
@Composable
fun ForwardAppBar(
    serverName: String,
    onServerClick: () -> Unit,
    onSearchClick: () -> Unit,
    onMoreClick: () -> Unit,
    modifier: Modifier = Modifier,
    showBackButton: Boolean = false,
    onBackClick: () -> Unit = {}
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .padding(horizontal = ForwardSpacing.PhoneMargin),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 左侧：返回按钮/服务器选择
        if (showBackButton) {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "返回",
                    tint = AppColors.DarkOnBackground
                )
            }
        } else {
            ForwardServerSelector(
                serverName = serverName,
                onClick = onServerClick
            )
        }
        
        // 中部：搜索框（省略，由外部提供）
        Spacer(modifier = Modifier.width(ForwardSpacing.CardSpacingHorizontal))
        
        // 右侧：更多按钮
        IconButton(onClick = onMoreClick) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = "更多",
                tint = AppColors.DarkOnBackground
            )
        }
    }
}

/**
 * Forward 风格服务器选择器
 */
@Composable
fun ForwardServerSelector(
    serverName: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.clickable(onClick = onClick),
        shape = AppShapes.Forward,
        color = AppColors.GlassMorphismDark.copy(alpha = 0.6f)
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = serverName,
                style = ForwardTypography.BodyMedium,
                color = AppColors.DarkOnBackground,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = AppColors.DarkOnBackground,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

/**
 * Forward 风格分类标题栏
 * 包含标题和"查看所有"操作
 */
@Composable
fun ForwardSectionHeader(
    title: String,
    actionText: String? = null,
    onActionClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = ForwardSpacing.PhoneMargin, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = ForwardTypography.TitleMedium,
            color = AppColors.DarkOnBackground
        )
        
        if (actionText != null && onActionClick != null) {
            Text(
                text = actionText,
                style = ForwardTypography.LabelLarge,
                color = AppColors.Primary,
                modifier = Modifier.clickable(onClick = onActionClick)
            )
        }
    }
}

/**
 * Forward 风格横向滚动列表
 */
@Composable
fun <T> ForwardHorizontalList(
    items: List<T>,
    key: ((T) -> Any)? = null,
    itemContent: @Composable (T) -> Unit
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = ForwardSpacing.PhoneMargin),
        horizontalArrangement = Arrangement.spacedBy(ForwardSpacing.CardSpacingHorizontal)
    ) {
        if (key != null) {
            items(items, key = key) { item ->
                itemContent(item)
            }
        } else {
            items(items) { item ->
                itemContent(item)
            }
        }
    }
}

/**
 * Forward 风格骨架屏加载
 */
@Composable
fun ForwardShimmer(
    modifier: Modifier = Modifier
) {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val alpha by transition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shimmerAlpha"
    )
    
    Spacer(
        modifier = modifier
            .alpha(alpha)
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        AppColors.DarkSurfaceVariant,
                        AppColors.DarkSurface,
                        AppColors.DarkSurfaceVariant
                    ),
                    start = Offset.Zero,
                    end = Offset(1000f, 0f)
                )
            )
    )
}

/**
 * Forward 风格空状态
 */
@Composable
fun ForwardEmptyState(
    title: String,
    subtitle: String? = null,
    actionText: String? = null,
    onActionClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(ForwardSpacing.ModuleSpacing),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(ForwardSpacing.CardSpacingVertical)
    ) {
        Text(
            text = title,
            style = ForwardTypography.HeadlineSmall,
            color = AppColors.DarkOnSurfaceVariant
        )
        
        subtitle?.let {
            Text(
                text = it,
                style = ForwardTypography.BodyMedium,
                color = AppColors.DarkOnSurfaceVariant
            )
        }
        
        if (actionText != null && onActionClick != null) {
            ForwardButton(
                onClick = onActionClick,
                text = actionText,
                size = ButtonSize.MEDIUM
            )
        }
    }
}

/**
 * Forward 风格加载状态
 */
@Composable
fun ForwardLoadingState(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            color = AppColors.Primary,
            modifier = Modifier.size(40.dp),
            strokeWidth = 3.dp
        )
    }
}

/**
 * Forward 风格错误状态
 */
@Composable
fun ForwardErrorState(
    title: String,
    message: String? = null,
    retryText: String = "重试",
    onRetryClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(ForwardSpacing.ModuleSpacing),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(ForwardSpacing.CardSpacingVertical)
    ) {
        Text(
            text = title,
            style = ForwardTypography.HeadlineSmall,
            color = AppColors.Error
        )
        
        message?.let {
            Text(
                text = it,
                style = ForwardTypography.BodyMedium,
                color = AppColors.DarkOnSurfaceVariant
            )
        }
        
        ForwardButton(
            onClick = onRetryClick,
            text = retryText,
            size = ButtonSize.MEDIUM
        )
    }
}
