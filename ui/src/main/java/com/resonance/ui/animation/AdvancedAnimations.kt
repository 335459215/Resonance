package com.resonance.ui.animation

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.graphicsLayer

/**
 * 高级动画工具类
 * 提供转场动画、微交互、物理动画等功能
 */
object AdvancedAnimations {
    
    // ==================== 页面转场动画 ====================
    
    /**
     * 淡入淡出转场
     */
    fun fadeInOutTransition(
        duration: Int = 300
    ): ContentTransform {
        return fadeIn(tween(duration)) togetherWith fadeOut(tween(duration))
    }
    
    /**
     * 缩放转场
     */
    fun scaleTransition(
        duration: Int = 300,
        initialScale: Float = 0.8f
    ): ContentTransform {
        return scaleIn(tween(duration), initialScale) togetherWith scaleOut(tween(duration), initialScale)
    }
    
    /**
     * 滑动转场（从左到右）
     */
    fun slideHorizontalTransition(
        duration: Int = 300,
        slideDistance: Int = 100
    ): ContentTransform {
        return slideInHorizontally(tween(duration)) { -slideDistance } togetherWith
                slideOutHorizontally(tween(duration)) { slideDistance }
    }
    
    /**
     * 滑动转场（从上到下）
     */
    fun slideVerticalTransition(
        duration: Int = 300,
        slideDistance: Int = 100
    ): ContentTransform {
        return slideInVertically(tween(duration)) { -slideDistance } togetherWith
                slideOutVertically(tween(duration)) { slideDistance }
    }
    
    // ==================== 微交互动画 ====================
    
    /**
     * 按钮点击动画
     */
    @Composable
    fun clickAnimation(
        targetValue: Boolean,
        modifier: Modifier = Modifier
    ): Modifier {
        val scale = animateFloatAsState(
            targetValue = if (targetValue) 0.9f else 1f,
            animationSpec = tween(100),
            label = "clickScale"
        )
        return modifier.scale(scale.value)
    }
    
    /**
     * 悬停放大动画
     */
    @Composable
    fun hoverAnimation(
        targetValue: Boolean,
        modifier: Modifier = Modifier,
        scale: Float = 1.05f
    ): Modifier {
        val animatedScale = animateFloatAsState(
            targetValue = if (targetValue) scale else 1f,
            animationSpec = spring(
                dampingRatio = 0.8f,
                stiffness = 300f
            ),
            label = "hoverScale"
        )
        return modifier.scale(animatedScale.value)
    }
    
    /**
     * 心跳动画
     */
    @Composable
    fun heartbeatAnimation(
        isAnimating: Boolean,
        modifier: Modifier = Modifier
    ): Modifier {
        val scale = animateFloatAsState(
            targetValue = if (isAnimating) 1.2f else 1f,
            animationSpec = infiniteRepeatable(
                animation = keyframes {
                    durationMillis = 1000
                    1f at 0
                    1.2f at 250
                    1f at 500
                    1.2f at 750
                    1f at 1000
                },
                repeatMode = RepeatMode.Restart
            ),
            label = "heartbeat"
        )
        return modifier.scale(scale.value)
    }
    
    /**
     * 脉冲动画
     */
    @Composable
    fun pulseAnimation(
        isAnimating: Boolean,
        modifier: Modifier = Modifier
    ): Modifier {
        val alpha = animateFloatAsState(
            targetValue = if (isAnimating) 0.5f else 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(1000),
                repeatMode = RepeatMode.Reverse
            ),
            label = "pulse"
        )
        return modifier.alpha(alpha.value)
    }
    
    /**
     * 抖动动画
     */
    @Composable
    fun shakeAnimation(
        isShaking: Boolean,
        modifier: Modifier = Modifier
    ): Modifier {
        val offsetX = animateFloatAsState(
            targetValue = if (isShaking) 10f else 0f,
            animationSpec = infiniteRepeatable(
                animation = tween(100),
                repeatMode = RepeatMode.Reverse
            ),
            label = "shake"
        )
        return modifier.graphicsLayer(translationX = offsetX.value)
    }
    
    // ==================== 组合动画 ====================
    
    /**
     * 弹出动画
     */
    @Composable
    fun popInAnimation(
        visible: Boolean,
        modifier: Modifier = Modifier
    ): Modifier {
        val scale = animateFloatAsState(
            targetValue = if (visible) 1f else 0f,
            animationSpec = spring(
                dampingRatio = 0.6f,
                stiffness = 400f
            ),
            label = "popScale"
        )
        val alpha = animateFloatAsState(
            targetValue = if (visible) 1f else 0f,
            animationSpec = tween(200),
            label = "popAlpha"
        )
        return modifier
            .scale(scale.value)
            .alpha(alpha.value)
    }
    
    /**
     * 滑入动画
     */
    @Composable
    fun slideInAnimation(
        visible: Boolean,
        fromLeft: Boolean = true,
        modifier: Modifier = Modifier
    ): Modifier {
        val offsetX = animateFloatAsState(
            targetValue = if (visible) 0f else (if (fromLeft) -200f else 200f),
            animationSpec = spring(
                dampingRatio = 0.8f,
                stiffness = 300f
            ),
            label = "slideOffset"
        )
        val alpha = animateFloatAsState(
            targetValue = if (visible) 1f else 0f,
            animationSpec = tween(200),
            label = "slideAlpha"
        )
        return modifier
            .graphicsLayer(translationX = offsetX.value)
            .alpha(alpha.value)
    }
    
    /**
     * 淡入上移动画
     */
    @Composable
    fun fadeInSlideUp(
        visible: Boolean,
        modifier: Modifier = Modifier
    ): Modifier {
        val offsetY = animateFloatAsState(
            targetValue = if (visible) 0f else 100f,
            animationSpec = spring(
                dampingRatio = 0.8f,
                stiffness = 300f
            ),
            label = "fadeSlideOffset"
        )
        val alpha = animateFloatAsState(
            targetValue = if (visible) 1f else 0f,
            animationSpec = tween(300),
            label = "fadeSlideAlpha"
        )
        return modifier
            .graphicsLayer(translationY = offsetY.value)
            .alpha(alpha.value)
    }
}
