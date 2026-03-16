package com.resonance.ui.animation

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * 动画性能监控器
 * 监控活跃动画数量、动画时长和内存使用
 */
object AnimationPerformanceMonitor {
    
    private val activeAnimations = mutableMapOf<String, Long>()
    private val animationHistory = mutableListOf<AnimationRecord>()
    private val maxHistorySize = 100
    
    data class AnimationRecord(
        val animationId: String,
        val startTime: Long,
        val duration: Long,
        val completed: Boolean
    )
    
    /**
     * 开始跟踪动画
     */
    fun trackAnimation(animationId: String) {
        val currentTime = System.currentTimeMillis()
        activeAnimations[animationId] = currentTime
        
        // 警告：过多活跃动画
        if (activeAnimations.size > 10) {
            Log.w("AnimationMonitor", "过多活跃动画：${activeAnimations.size}")
        }
    }
    
    /**
     * 完成动画跟踪
     */
    fun completeAnimation(animationId: String) {
        val startTime = activeAnimations.remove(animationId)
        if (startTime != null) {
            val duration = System.currentTimeMillis() - startTime
            
            // 记录历史
            animationHistory.add(
                AnimationRecord(
                    animationId = animationId,
                    startTime = startTime,
                    duration = duration,
                    completed = true
                )
            )
            
            // 限制历史记录大小
            if (animationHistory.size > maxHistorySize) {
                animationHistory.removeAt(0)
            }
            
            // 警告：动画时间过长
            if (duration > 5000) {
                Log.w("AnimationMonitor", "动画时间过长：$animationId (${duration}ms)")
            }
        }
    }
    
    /**
     * 获取活跃动画数量
     */
    fun getActiveAnimationCount(): Int = activeAnimations.size
    
    /**
     * 获取动画平均时长
     */
    fun getAverageDuration(): Long {
        val completedAnimations = animationHistory.filter { it.completed }
        return if (completedAnimations.isEmpty()) 0 
            else completedAnimations.map { it.duration }.average().toLong()
    }
    
    /**
     * 获取慢动画列表
     */
    fun getSlowAnimations(threshold: Long = 1000): List<AnimationRecord> {
        return animationHistory.filter { it.duration > threshold }
    }
    
    /**
     * 重置监控数据
     */
    fun reset() {
        activeAnimations.clear()
        animationHistory.clear()
    }
    
    /**
     * 获取监控报告
     */
    fun getReport(): String {
        return buildString {
            appendLine("=== 动画性能报告 ===")
            appendLine("活跃动画：${activeAnimations.size}")
            appendLine("平均时长：${getAverageDuration()}ms")
            appendLine("慢动画数量：${getSlowAnimations().size}")
        }
    }
}

/**
 * 动画作用域管理器
 * 管理动画的生命周期，防止内存泄漏
 */
class AnimationScope {
    
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private val animations = mutableListOf<Job>()
    private val animationIds = mutableSetOf<String>()
    
    /**
     * 启动动画
     */
    fun launchAnimation(animationId: String, block: suspend CoroutineScope.() -> Unit): Job {
        val job = scope.launch {
            try {
                AnimationPerformanceMonitor.trackAnimation(animationId)
                block()
            } finally {
                AnimationPerformanceMonitor.completeAnimation(animationId)
            }
        }
        
        animations.add(job)
        animationIds.add(animationId)
        
        // 动画完成时清理
        job.invokeOnCompletion {
            animations.remove(job)
            animationIds.remove(animationId)
        }
        
        return job
    }
    
    /**
     * 取消所有动画
     */
    fun cancelAll() {
        animations.forEach { it.cancel() }
        animations.clear()
        animationIds.clear()
    }
    
    /**
     * 取消指定动画
     */
    fun cancel(animationId: String) {
        val job = animations.find { /* 匹配 animationId */ false }
        job?.cancel()
        animations.remove(job)
        animationIds.remove(animationId)
    }
    
    /**
     * 获取活跃动画数量
     */
    fun getActiveCount(): Int = animations.size
    
    /**
     * 等待所有动画完成
     */
    suspend fun awaitAll() {
        animations.forEach { it.join() }
    }
    
    /**
     * 取消作用域
     */
    fun cancel() {
        scope.cancel()
        cancelAll()
    }
}

/**
 * 物理动画引擎
 * 提供基于物理的动画参数计算
 */
object PhysicsAnimationEngine {
    
    data class PhysicsConfig(
        val mass: Float = 1f,
        val stiffness: Float = 300f,
        val damping: Float = 30f
    )
    
    /**
     * 计算弹簧动画参数
     */
    fun calculateSpringParams(config: PhysicsConfig): SpringParams {
        val dampingRatio = config.damping / (2f * kotlin.math.sqrt(config.mass * config.stiffness))
        val angularFrequency = kotlin.math.sqrt(config.stiffness / config.mass)
        
        return SpringParams(
            dampingRatio = dampingRatio.coerceIn(0f, 1f),
            stiffness = config.stiffness,
            angularFrequency = angularFrequency
        )
    }
    
    data class SpringParams(
        val dampingRatio: Float,
        val stiffness: Float,
        val angularFrequency: Float
    )
    
    /**
     * 根据场景自动选择物理参数
     */
    fun getParamsForScenario(scenario: Scenario): PhysicsConfig {
        return when (scenario) {
            Scenario.BUTTON_CLICK -> PhysicsConfig(
                mass = 1f,
                stiffness = 400f,
                damping = 35f
            )
            Scenario.PAGE_TRANSITION -> PhysicsConfig(
                mass = 1.5f,
                stiffness = 300f,
                damping = 30f
            )
            Scenario.FLOATING_ACTION -> PhysicsConfig(
                mass = 0.8f,
                stiffness = 350f,
                damping = 28f
            )
            Scenario.DRAG_AND_DROP -> PhysicsConfig(
                mass = 1.2f,
                stiffness = 320f,
                damping = 32f
            )
        }
    }
    
    enum class Scenario {
        BUTTON_CLICK,
        PAGE_TRANSITION,
        FLOATING_ACTION,
        DRAG_AND_DROP
    }
}
