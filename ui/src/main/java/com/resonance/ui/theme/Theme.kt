package com.resonance.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Forward 风格颜色系统
object AppColors {
    // 主色调 - Forward 标志性珊瑚红
    val Primary = Color(0xFFFF3A3B)
    val PrimaryDark = Color(0xFFCC2F30)
    val PrimaryLight = Color(0xFFFF6B6C)
    
    // 强调色 - 活力橙色
    val Accent = Color(0xFFFF9500)
    val AccentDark = Color(0xFFCC7700)
    val AccentLight = Color(0xFFFFB84D)
    
    // 成功/警告/错误
    val Success = Color(0xFF34C759)
    val Warning = Color(0xFFFF9500)
    val Error = Color(0xFFFF3B30)
    
    // 亮色主题
    val LightBackground = Color(0xFFF2F2F7)
    val LightSurface = Color(0xFFFFFFFF)
    val LightSurfaceVariant = Color(0xFFE5E5EA)
    val LightOnBackground = Color(0xFF1C1C1E)
    val LightOnSurface = Color(0xFF1C1C1E)
    val LightOnSurfaceVariant = Color(0xFF8E8E93)
    
    // 暗色主题 - 纯黑到深灰渐变
    val DarkBackground = Color(0xFF000000)
    val DarkBackgroundVariant = Color(0xFF121212)
    val DarkSurface = Color(0xFF1C1C1E)
    val DarkSurfaceVariant = Color(0xFF2C2C2E)
    val DarkOnBackground = Color(0xFFFFFFFF)      // 纯白标题
    val DarkOnSurface = Color(0xFFE1E1E1)         // 次白正文
    val DarkOnSurfaceVariant = Color(0xFF8A8A8A)  // 浅灰辅助
    
    // 特殊效果色
    val GlassMorphismLight = Color(0x80FFFFFF)
    val GlassMorphismDark = Color(0x80000000)
    val GlassBorder = Color(0x40FFFFFF)
    val Highlight = Color(0xFFFFD60A)
    val Overlay = Color(0x40000000)
}

// 扩展颜色
data class ExtendedColors(
    val accent: Color,
    val accentDark: Color,
    val accentLight: Color,
    val success: Color,
    val warning: Color,
    val glassMorphism: Color,
    val highlight: Color,
    val overlay: Color,
    val shimmer: Color,
    val divider: Color
)

val LocalExtendedColors = staticCompositionLocalOf {
    ExtendedColors(
        accent = AppColors.Accent,
        accentDark = AppColors.AccentDark,
        accentLight = AppColors.AccentLight,
        success = AppColors.Success,
        warning = AppColors.Warning,
        glassMorphism = AppColors.GlassMorphismLight,
        highlight = AppColors.Highlight,
        overlay = AppColors.Overlay,
        shimmer = Color(0xFFE0E0E0),
        divider = Color(0xFFE5E5EA)
    )
}

// 亮色主题配色方案
private val LightColorScheme = lightColorScheme(
    primary = AppColors.Primary,
    onPrimary = Color.White,
    primaryContainer = AppColors.PrimaryLight,
    onPrimaryContainer = AppColors.PrimaryDark,
    secondary = AppColors.Accent,
    onSecondary = Color.White,
    secondaryContainer = AppColors.AccentLight,
    onSecondaryContainer = AppColors.AccentDark,
    background = AppColors.LightBackground,
    onBackground = AppColors.LightOnBackground,
    surface = AppColors.LightSurface,
    onSurface = AppColors.LightOnSurface,
    surfaceVariant = AppColors.LightSurfaceVariant,
    onSurfaceVariant = AppColors.LightOnSurfaceVariant,
    error = AppColors.Error,
    onError = Color.White
)

// 暗色主题配色方案
private val DarkColorScheme = darkColorScheme(
    primary = AppColors.Primary,
    onPrimary = Color.White,
    primaryContainer = AppColors.PrimaryLight,
    onPrimaryContainer = AppColors.PrimaryDark,
    secondary = AppColors.Accent,
    onSecondary = Color.White,
    secondaryContainer = AppColors.AccentLight,
    onSecondaryContainer = AppColors.AccentDark,
    background = AppColors.DarkBackground,
    onBackground = AppColors.DarkOnBackground,
    surface = AppColors.DarkSurface,
    onSurface = AppColors.DarkOnSurface,
    surfaceVariant = AppColors.DarkSurfaceVariant,
    onSurfaceVariant = AppColors.DarkOnSurfaceVariant,
    error = AppColors.Error,
    onError = Color.White
)

// 排版系统
object AppTypography {
    val DisplayLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 57.sp,
        lineHeight = 64.sp,
        letterSpacing = (-0.25).sp
    )
    
    val DisplayMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 45.sp,
        lineHeight = 52.sp,
        letterSpacing = 0.sp
    )
    
    val DisplaySmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 36.sp,
        lineHeight = 44.sp,
        letterSpacing = 0.sp
    )
    
    val HeadlineLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 32.sp,
        lineHeight = 40.sp,
        letterSpacing = 0.sp
    )
    
    val HeadlineMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 28.sp,
        lineHeight = 36.sp,
        letterSpacing = 0.sp
    )
    
    val HeadlineSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp,
        lineHeight = 32.sp,
        letterSpacing = 0.sp
    )
    
    val TitleLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    )
    
    val TitleMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.15.sp
    )
    
    val TitleSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    )
    
    val BodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    )
    
    val BodyMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.25.sp
    )
    
    val BodySmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.4.sp
    )
    
    val LabelLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    )
    
    val LabelMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    )
    
    val LabelSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    )
}

// 形状系统 - Forward 风格统一圆角
object AppShapes {
    val ExtraSmall = androidx.compose.foundation.shape.RoundedCornerShape(4.dp)
    val Small = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
    val Medium = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
    val Large = androidx.compose.foundation.shape.RoundedCornerShape(16.dp)
    val ExtraLarge = androidx.compose.foundation.shape.RoundedCornerShape(20.dp)  // 统一 20pt 圆角
    val Full = androidx.compose.foundation.shape.RoundedCornerShape(50)
    
    // Forward 标准圆角（20pt）
    val Forward = androidx.compose.foundation.shape.RoundedCornerShape(20.dp)
}

// 主题组合
@Composable
fun EmbyPlayerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    
    val extendedColors = if (darkTheme) {
        ExtendedColors(
            accent = AppColors.Accent,
            accentDark = AppColors.AccentDark,
            accentLight = AppColors.AccentLight,
            success = AppColors.Success,
            warning = AppColors.Warning,
            glassMorphism = AppColors.GlassMorphismDark,
            highlight = AppColors.Highlight,
            overlay = AppColors.Overlay,
            shimmer = Color(0xFF3A3A3C),
            divider = Color(0xFF3A3A3C)
        )
    } else {
        ExtendedColors(
            accent = AppColors.Accent,
            accentDark = AppColors.AccentDark,
            accentLight = AppColors.AccentLight,
            success = AppColors.Success,
            warning = AppColors.Warning,
            glassMorphism = AppColors.GlassMorphismLight,
            highlight = AppColors.Highlight,
            overlay = AppColors.Overlay,
            shimmer = Color(0xFFE0E0E0),
            divider = Color(0xFFE5E5EA)
        )
    }
    
    val typography = Typography(
        displayLarge = AppTypography.DisplayLarge,
        displayMedium = AppTypography.DisplayMedium,
        displaySmall = AppTypography.DisplaySmall,
        headlineLarge = AppTypography.HeadlineLarge,
        headlineMedium = AppTypography.HeadlineMedium,
        headlineSmall = AppTypography.HeadlineSmall,
        titleLarge = AppTypography.TitleLarge,
        titleMedium = AppTypography.TitleMedium,
        titleSmall = AppTypography.TitleSmall,
        bodyLarge = AppTypography.BodyLarge,
        bodyMedium = AppTypography.BodyMedium,
        bodySmall = AppTypography.BodySmall,
        labelLarge = AppTypography.LabelLarge,
        labelMedium = AppTypography.LabelMedium,
        labelSmall = AppTypography.LabelSmall
    )
    
    val shapes = Shapes(
        extraSmall = AppShapes.ExtraSmall,
        small = AppShapes.Small,
        medium = AppShapes.Medium,
        large = AppShapes.Large,
        extraLarge = AppShapes.ExtraLarge
    )
    
    MaterialTheme(
        colorScheme = colorScheme,
        typography = typography,
        shapes = shapes,
        content = content
    )
}

// 扩展属性访问
val MaterialTheme.extendedColors: ExtendedColors
    @Composable
    get() = LocalExtendedColors.current