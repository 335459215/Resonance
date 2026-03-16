package com.resonance.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp

/**
 * Forward 风格背景渐变
 * 纯黑到深灰渐变 (#000000 → #121212)
 */
@Composable
fun ForwardBackgroundGradient(
    modifier: Modifier = Modifier,
    isDarkTheme: Boolean = isSystemInDarkTheme()
) {
    val colors = if (isDarkTheme) {
        listOf(
            AppColors.DarkBackground,
            AppColors.DarkBackgroundVariant
        )
    } else {
        listOf(
            AppColors.LightBackground,
            AppColors.LightSurface
        )
    }
    
    Spacer(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = colors,
                    startY = 0f,
                    endY = 1000f
                )
            )
    )
}

/**
 * Forward 风格毛玻璃背景修饰符
 * 圆角 20pt，带半透明背景和边框
 */
@Composable
fun Modifier.forwardGlassBackground(): Modifier {
    val isDark = isSystemInDarkTheme()
    return this
        .clip(AppShapes.Forward)
        .background(
            if (isDark) AppColors.GlassMorphismDark else AppColors.GlassMorphismLight
        )
        .border(
            width = 1.dp,
            color = AppColors.GlassBorder,
            shape = AppShapes.Forward
        )
}

/**
 * Forward 风格毛玻璃背景修饰符（指定颜色）
 */
fun Modifier.forwardGlassBackground(
    backgroundColor: Color,
    borderColor: Color
): Modifier {
    return this
        .clip(AppShapes.Forward)
        .background(backgroundColor)
        .border(
            width = 1.dp,
            color = borderColor,
            shape = AppShapes.Forward
        )
}

/**
 * Forward 风格模糊背景修饰符
 * 用于背景模糊效果
 */
fun Modifier.forwardBlurBackground(
    blurRadius: Float = 20f
): Modifier {
    return this
        .blur(blurRadius.dp)
        .clip(AppShapes.Forward)
}

/**
 * Forward 风格渐变背景（用于播放器背景）
 */
@Composable
fun ForwardPlayerBackground(
    modifier: Modifier = Modifier,
    baseColor: Color = AppColors.DarkBackground
) {
    Spacer(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        baseColor.copy(alpha = 0.8f),
                        baseColor
                    ),
                    center = Offset(0.5f, 0.3f),
                    radius = 1000f
                )
            )
    )
}

/**
 * Forward 风格间距系统
 * 页面边距：16pt（手机）→ 24pt（平板）
 * 模块间距：20pt
 * 卡片间距：8pt（横向）/ 12pt（纵向）
 */
object ForwardSpacing {
    val PhoneMargin = 16.dp
    val TabletMargin = 24.dp
    val ModuleSpacing = 20.dp
    val CardSpacingHorizontal = 8.dp
    val CardSpacingVertical = 12.dp
    val ListItemSpacing = 12.dp
}

/**
 * Forward 风格页面边距（响应式）
 */
@Composable
fun forwardPageMargin(): androidx.compose.ui.unit.Dp {
    return ForwardSpacing.PhoneMargin
}

/**
 * Forward 风格模块间距
 */
@Composable
fun forwardModuleSpacing(): androidx.compose.ui.unit.Dp {
    return ForwardSpacing.ModuleSpacing
}

/**
 * Forward 风格安全区适配
 * iOS：刘海/灵动岛适配
 * 安卓：挖孔/折叠屏适配
 */
@Composable
fun Modifier.forwardSafeAreaPadding(): Modifier {
    return this
        .statusBarsPadding()
        .padding(
            top = 0.dp,
            bottom = 0.dp
        )
}

/**
 * Forward 风格圆角（统一 20pt）
 */
val ForwardCornerShape: RoundedCornerShape
    @Composable
    get() = AppShapes.Forward

/**
 * Forward 风格阴影
 * 0 8px 24px rgba(0,0,0,0.5)
 */
fun Modifier.forwardShadow(): Modifier {
    return this
        .graphicsLayer {
            shadowElevation = 8.dp.toPx()
            spotShadowColor = Color.Black.copy(alpha = 0.5f)
        }
}

/**
 * Forward 风格图片阴影
 */
fun Modifier.forwardImageShadow(): Modifier {
    return this
        .graphicsLayer {
            shadowElevation = 24.dp.toPx()
            spotShadowColor = Color.Black.copy(alpha = 0.5f)
        }
}
