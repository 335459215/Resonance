package com.resonance.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import org.junit.Test
import org.junit.Assert.*

class ThemeTest {

    @Test
    fun testAppColors_primaryColorsAreCorrect() {
        assertEquals(Color(0xFF007AFF), AppColors.Primary)
        assertEquals(Color(0xFF0056CC), AppColors.PrimaryDark)
        assertEquals(Color(0xFF4DA3FF), AppColors.PrimaryLight)
    }

    @Test
    fun testAppColors_accentColorsAreCorrect() {
        assertEquals(Color(0xFFFF9500), AppColors.Accent)
        assertEquals(Color(0xFFCC7700), AppColors.AccentDark)
        assertEquals(Color(0xFFFFB84D), AppColors.AccentLight)
    }

    @Test
    fun testAppColors_statusColorsAreCorrect() {
        assertEquals(Color(0xFF34C759), AppColors.Success)
        assertEquals(Color(0xFFFF9500), AppColors.Warning)
        assertEquals(Color(0xFFFF3B30), AppColors.Error)
    }

    @Test
    fun testAppColors_lightThemeColorsAreCorrect() {
        assertEquals(Color(0xFFF2F2F7), AppColors.LightBackground)
        assertEquals(Color(0xFFFFFFFF), AppColors.LightSurface)
        assertEquals(Color(0xFFE5E5EA), AppColors.LightSurfaceVariant)
        assertEquals(Color(0xFF1C1C1E), AppColors.LightOnBackground)
        assertEquals(Color(0xFF1C1C1E), AppColors.LightOnSurface)
        assertEquals(Color(0xFF8E8E93), AppColors.LightOnSurfaceVariant)
    }

    @Test
    fun testAppColors_darkThemeColorsAreCorrect() {
        assertEquals(Color(0xFF1C1C1E), AppColors.DarkBackground)
        assertEquals(Color(0xFF2C2C2E), AppColors.DarkSurface)
        assertEquals(Color(0xFF3A3A3C), AppColors.DarkSurfaceVariant)
        assertEquals(Color(0xFFFFFFFF), AppColors.DarkOnBackground)
        assertEquals(Color(0xFFFFFFFF), AppColors.DarkOnSurface)
        assertEquals(Color(0xFF8E8E93), AppColors.DarkOnSurfaceVariant)
    }

    @Test
    fun testAppColors_specialEffectColorsAreCorrect() {
        assertEquals(Color(0x80FFFFFF), AppColors.GlassMorphismLight)
        assertEquals(Color(0x80000000), AppColors.GlassMorphismDark)
        assertEquals(Color(0xFFFFD60A), AppColors.Highlight)
        assertEquals(Color(0x40000000), AppColors.Overlay)
    }

    @Test
    fun testAppShapes_cornerShapesAreCorrect() {
        // 验证形状定义是否存在
        assertNotNull(AppShapes.ExtraSmall)
        assertNotNull(AppShapes.Small)
        assertNotNull(AppShapes.Medium)
        assertNotNull(AppShapes.Large)
        assertNotNull(AppShapes.ExtraLarge)
        assertNotNull(AppShapes.Full)
    }
}
