package com.resonance.ui.utils

import android.content.Context
import android.view.HapticFeedbackConstants
import android.view.View
import android.widget.Toast
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView

/**
 * 交互优化工具类
 * 提供手势操作、触觉反馈、用户提示等交互优化功能
 */
object InteractionOptimizer {
    
    // ==================== 触觉反馈 ====================
    
    /**
     * 轻触反馈
     */
    fun performLightHaptic(view: View) {
        view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
    }
    
    /**
     * 中等强度反馈
     */
    fun performMediumHaptic(view: View) {
        view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
    }
    
    /**
     * 强度反馈
     */
    fun performStrongHaptic(view: View) {
        view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
    }
    
    /**
     * 确认反馈
     */
    fun performConfirmHaptic(view: View) {
        view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
    }
    
    /**
     * 拒绝反馈
     */
    fun performRejectHaptic(view: View) {
        view.performHapticFeedback(HapticFeedbackConstants.REJECT)
    }
    
    /**
     * 时钟滴答反馈
     */
    fun performClockTickHaptic(view: View) {
        view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
    }
    
    /**
     * 上下文点击反馈
     */
    fun performContextClickHaptic(view: View) {
        view.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK)
    }
    
    /**
     * 手势完成反馈
     */
    fun performGestureCompleteHaptic(view: View) {
        view.performHapticFeedback(HapticFeedbackConstants.GESTURE_END)
    }
    
    // ==================== Toast 提示优化 ====================
    
    /**
     * 显示短 Toast
     */
    fun showShortToast(context: Context, message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }
    
    /**
     * 显示长 Toast
     */
    fun showLongToast(context: Context, message: String) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }
    
    /**
     * 显示 Toast（资源 ID）
     */
    fun showToast(context: Context, messageId: Int) {
        Toast.makeText(context, messageId, Toast.LENGTH_LONG).show()
    }
    
    /**
     * 显示成功提示
     */
    fun showSuccessToast(context: Context, message: String) {
        showShortToast(context, "✓ $message")
    }
    
    /**
     * 显示错误提示
     */
    fun showErrorToast(context: Context, message: String) {
        showLongToast(context, "✗ $message")
    }
    
    /**
     * 显示警告提示
     */
    fun showWarningToast(context: Context, message: String) {
        showLongToast(context, "⚠ $message")
    }
    
    /**
     * 显示信息提示
     */
    fun showInfoToast(context: Context, message: String) {
        showShortToast(context, "ℹ $message")
    }
    
    // ==================== 手势操作优化 ====================
    
    /**
     * 双击手势检测
     */
    fun Modifier.doubleTapGesture(
        onDoubleTap: () -> Unit
    ): Modifier = composed {
        this.pointerInput(Unit) {
            detectTapGestures(
                onDoubleTap = { onDoubleTap() }
            )
        }
    }
    
    /**
     * 长按手势检测
     */
    fun Modifier.longPressGesture(
        onLongPress: () -> Unit
    ): Modifier = composed {
        this.pointerInput(Unit) {
            detectTapGestures(
                onLongPress = { onLongPress() }
            )
        }
    }
    
    /**
     * 拖动手势检测
     */
    fun Modifier.dragGesture(
        onDrag: (offset: androidx.compose.ui.geometry.Offset) -> Unit
    ): Modifier = composed {
        this.pointerInput(Unit) {
            detectDragGestures(
                onDrag = { change, dragAmount ->
                    change.consume()
                    onDrag(dragAmount)
                }
            )
        }
    }
    
    /**
     * 垂直拖动手势检测
     */
    fun Modifier.verticalDragGesture(
        onDrag: (delta: Float) -> Unit
    ): Modifier = composed {
        this.pointerInput(Unit) {
            detectVerticalDragGestures(
                onVerticalDrag = { change, dragAmount ->
                    change.consume()
                    onDrag(dragAmount)
                }
            )
        }
    }
    
    /**
     * 防抖点击（防止快速重复点击）
     */
    fun debounceClick(
        action: () -> Unit,
        delayMillis: Long = 300L
    ): () -> Unit {
        var lastClickTime = 0L
        
        return {
            val currentTime = System.currentTimeMillis()
            if (currentTime - lastClickTime > delayMillis) {
                action()
                lastClickTime = currentTime
            }
        }
    }
    
    /**
     * 延迟执行（用于平滑过渡）
     */
    suspend fun smoothDelay(delayMillis: Long = 100L) {
        kotlinx.coroutines.delay(delayMillis)
    }
    
    /**
     * 分步执行动画（用于复杂交互）
     */
    suspend fun stepByStepAnimation(
        steps: List<suspend () -> Unit>,
        delayBetweenSteps: Long = 100L
    ) {
        steps.forEachIndexed { index, step ->
            step()
            if (index < steps.size - 1) {
                kotlinx.coroutines.delay(delayBetweenSteps)
            }
        }
    }
}

/**
 * Composable 函数：获取触觉反馈
 */
@Composable
fun getHapticFeedback(): (feedbackType: Int) -> Unit {
    val view = LocalView.current
    
    return { feedbackType ->
        view.performHapticFeedback(feedbackType)
    }
}

/**
 * Composable 函数：获取 Toast 提示
 */
@Composable
fun getToastHelper(): (message: String, duration: Int) -> Unit {
    val context = LocalContext.current
    
    return { message, duration ->
        Toast.makeText(context, message, duration).show()
    }
}

/**
 * Composable 函数：快捷 Toast 提示
 */
@Composable
fun useToast(): (message: String) -> Unit {
    val context = LocalContext.current
    
    return { message ->
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }
}
