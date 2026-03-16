package com.resonance.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.colorspace.connect
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * UI 设计系统
 * 提供统一的设计规范和组件样式
 */
object UIDesignSystem {
    
    // ==================== 颜色系统 ====================
    
    object Colors {
        // 主色调
        val Primary = Color(0xFF6366F1)
        val PrimaryVariant = Color(0xFF4F46E5)
        val OnPrimary = Color.White
        
        // 辅助色
        val Secondary = Color(0xFF10B981)
        val SecondaryVariant = Color(0xFF059669)
        val OnSecondary = Color.White
        
        val Accent = Color(0xFFF59E0B)
        val AccentVariant = Color(0xFFD97706)
        val OnAccent = Color.White
        
        // 背景色
        val Background = Color(0xFF0F172A)
        val Surface = Color(0xFF1E293B)
        val SurfaceVariant = Color(0xFF334155)
        
        // 前景色
        val OnBackground = Color(0xFFF8FAFC)
        val OnSurface = Color(0xFFF1F5F9)
        val OnSurfaceVariant = Color(0xFF94A3B8)
        
        // 状态色
        val Success = Color(0xFF22C55E)
        val Warning = Color(0xFFF59E0B)
        val Error = Color(0xFFEF4444)
        val Info = Color(0xFF3B82F6)
        
        // 特殊色
        val Divider = Color(0xFF475569)
        val Overlay = Color(0x80000000)
        
        // 渐变色
        val GradientStart = Color(0xFF6366F1)
        val GradientMiddle = Color(0xFF8B5CF6)
        val GradientEnd = Color(0xFFEC4899)
    }
    
    // ==================== 间距系统 ====================
    
    object Spacing {
        val Xxs = 2.dp
        val Xs = 4.dp
        val Sm = 8.dp
        val Md = 16.dp
        val Lg = 24.dp
        val Xl = 32.dp
        val Xxl = 48.dp
        val Xxxl = 64.dp
    }
    
    // ==================== 圆角系统 ====================
    
    object Radius {
        val None = 0.dp
        val Sm = 4.dp
        val Md = 8.dp
        val Lg = 12.dp
        val Xl = 16.dp
        val Xxl = 24.dp
        val Full = 9999.dp
    }
    
    // ==================== 阴影系统 ====================
    
    object Elevation {
        val None = 0.dp
        val Sm = 2.dp
        val Md = 4.dp
        val Lg = 8.dp
        val Xl = 16.dp
        val Xxl = 24.dp
    }
    
    // ==================== 动画时长系统 ====================
    
    object AnimationDuration {
        const val Instant = 0
        const val Fast = 150
        const val Normal = 300
        const val Slow = 500
        const val VerySlow = 700
    }
    
    // ==================== 字体大小系统 ====================
    
    object FontSize {
        val Xxs = 10.sp
        val Xs = 12.sp
        val Sm = 14.sp
        val Md = 16.sp
        val Lg = 18.sp
        val Xl = 20.sp
        val Xxl = 24.sp
        val Xxxl = 32.sp
    }
    
    // ==================== 图标尺寸系统 ====================
    
    object IconSize {
        val Xs = 16.dp
        val Sm = 20.dp
        val Md = 24.dp
        val Lg = 32.dp
        val Xl = 48.dp
        val Xxl = 64.dp
    }
    
    // ==================== 卡片样式预设 ====================
    
    object CardStyles {
        // 标准卡片
        val StandardRadius = Radius.Lg
        val StandardElevation = Elevation.Md
        
        // 紧凑卡片
        val CompactRadius = Radius.Md
        val CompactElevation = Elevation.Sm
        
        // 强调卡片
        val ProminentRadius = Radius.Xl
        val ProminentElevation = Elevation.Lg
        
        // 扁平卡片
        val FlatRadius = Radius.Md
        val FlatElevation = Elevation.None
    }
    
    // ==================== 按钮样式预设 ====================
    
    object ButtonStyles {
        // 标准按钮
        val StandardRadius = Radius.Md
        val StandardHeight = 48.dp
        
        // 紧凑按钮
        val CompactRadius = Radius.Sm
        val CompactHeight = 40.dp
        
        // 大按钮
        val LargeRadius = Radius.Lg
        val LargeHeight = 56.dp
        
        // 图标按钮
        val IconButtonSize = 40.dp
        val SmallIconButtonSize = 32.dp
    }
    
    // ==================== 输入框样式预设 ====================
    
    object InputStyles {
        val RadiusVal = 8.dp
        val Height = 56.dp
        val BorderWidth = 1.dp
        val FocusedBorderWidth = 2.dp
    }
    
    // ==================== 列表项样式预设 ====================
    
    object ListItemStyles {
        val RadiusVal = 8.dp
        val PaddingVal = 16.dp
        val IconSpacing = 16.dp
        val MinHeight = 72.dp
    }
    
    // ==================== 海报卡片样式预设 ====================
    
    object PosterStyles {
        val RadiusVal = 12.dp
        val AspectRatio = 2f / 3f
        val MinWidth = 120.dp
        val MaxWidth = 200.dp
        val ShadowElevation = Elevation.Md
    }
    
    // ==================== 播放器控件样式预设 ====================
    
    object PlayerStyles {
        val ControlButtonSize = 48.dp
        val LargeControlButtonSize = 64.dp
        val ProgressbarHeight = 4.dp
        val VolumeSliderWidth = 120.dp
    }
    
    // ==================== 工具函数 ====================
    
    /**
     * 获取对比度足够的文字颜色
     */
    fun getOnBackgroundColor(backgroundColor: Color): Color {
        // 简化实现：根据背景色深浅返回黑或白
        val brightness = (backgroundColor.red + backgroundColor.green + backgroundColor.blue) / 3f
        return if (brightness > 0.5f) Color.Black else Color.White
    }
    
    /**
     * 颜色混合
     */
    fun blendColors(color1: Color, color2: Color, fraction: Float): Color {
        return Color(
            red = (color1.red * (1 - fraction) + color2.red * fraction),
            green = (color1.green * (1 - fraction) + color2.green * fraction),
            blue = (color1.blue * (1 - fraction) + color2.blue * fraction),
            alpha = (color1.alpha * (1 - fraction) + color2.alpha * fraction)
        )
    }
    
    /**
     * 调整颜色亮度
     */
    fun adjustColorBrightness(color: Color, factor: Float): Color {
        return color.copy(
            red = (color.red * (1 + factor)).coerceIn(0f, 1f),
            green = (color.green * (1 + factor)).coerceIn(0f, 1f),
            blue = (color.blue * (1 + factor)).coerceIn(0f, 1f)
        )
    }
}
