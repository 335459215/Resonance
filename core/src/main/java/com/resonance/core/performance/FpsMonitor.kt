package com.resonance.core.performance

import android.view.Choreographer
import android.util.Log

/**
 * 帧率监控器
 * 使用 Choreographer 实时监控应用帧率
 */
class FpsMonitor {
    
    companion object {
        private const val TARGET_FPS = 60
        private const val FRAME_INTERVAL_MS = 16.67f // 1000ms / 60
        private const val LOW_FPS_THRESHOLD = 30
        private const val MEDIUM_FPS_THRESHOLD = 45
    }
    
    private var frameCount = 0
    private var lastFpsTime = 0L
    private var currentFps = TARGET_FPS
    
    private val frameMetrics = mutableListOf<FrameMetric>()
    private val maxMetricsCount = 120 // 保留 2 分钟数据
    
    /**
     * 帧指标数据类
     */
    data class FrameMetric(
        val timestamp: Long,
        val fps: Int,
        val frameTime: Long,
        val isJank: Boolean
    )
    
    /**
     * FPS 统计数据类
     */
    data class FpsStatistics(
        val avgFps: Float,
        val minFps: Int,
        val maxFps: Int,
        val jankCount: Int,
        val jankRate: Float
    )
    
    init {
        startMonitoring()
    }
    
    /**
     * 开始监控
     */
    private fun startMonitoring() {
        Choreographer.getInstance().postFrameCallback(frameCallback)
    }
    
    /**
     * 停止监控
     */
    fun stopMonitoring() {
        Choreographer.getInstance().removeFrameCallback(frameCallback)
    }
    
    private val frameCallback = object : Choreographer.FrameCallback {
        override fun doFrame(frameTimeNanos: Long) {
            frameCount++
            
            val currentTime = frameTimeNanos / 1_000_000 // 转换为毫秒
            
            if (lastFpsTime == 0L) {
                lastFpsTime = currentTime
            } else {
                val elapsed = currentTime - lastFpsTime
                
                // 每秒更新一次 FPS
                if (elapsed >= 1000) {
                    val fps = (frameCount * 1000f / elapsed).toInt()
                    currentFps = fps
                    
                    // 记录帧指标
                    recordFrameMetric(fps, elapsed / frameCount)
                    
                    // 重置计数器
                    frameCount = 0
                    lastFpsTime = currentTime
                    
                    // 检测帧率下降
                    onFpsChanged(fps)
                }
            }
            
            // 继续注册回调
            Choreographer.getInstance().postFrameCallback(this)
        }
    }
    
    /**
     * 记录帧指标
     */
    private fun recordFrameMetric(fps: Int, frameTime: Long) {
        val isJank = frameTime > FRAME_INTERVAL_MS * 1.5f // 超过 1.5 倍帧时间为卡顿
        val metric = FrameMetric(
            timestamp = System.currentTimeMillis(),
            fps = fps,
            frameTime = frameTime,
            isJank = isJank
        )
        
        frameMetrics.add(metric)
        
        // 限制历史记录数量
        if (frameMetrics.size > maxMetricsCount) {
            frameMetrics.removeAt(0)
        }
    }
    
    /**
     * FPS 变化时的处理
     */
    private fun onFpsChanged(fps: Int) {
        when {
            fps < LOW_FPS_THRESHOLD -> {
                Log.w("FpsMonitor", "严重帧率下降：${fps}fps")
            }
            fps < MEDIUM_FPS_THRESHOLD -> {
                Log.w("FpsMonitor", "帧率下降：${fps}fps")
            }
        }
    }
    
    /**
     * 获取当前 FPS
     */
    fun getCurrentFps(): Int = currentFps
    
    /**
     * 获取帧率统计信息
     */
    fun getFpsStatistics(): FpsStatistics {
        if (frameMetrics.isEmpty()) {
            return FpsStatistics(0f, 0, 0, 0, 0f)
        }
        
        val fpsList = frameMetrics.map { it.fps }
        val jankMetrics = frameMetrics.filter { it.isJank }
        
        return FpsStatistics(
            avgFps = fpsList.average().toFloat(),
            minFps = fpsList.minOrNull() ?: 0,
            maxFps = fpsList.maxOrNull() ?: 0,
            jankCount = jankMetrics.size,
            jankRate = jankMetrics.size.toFloat() / frameMetrics.size
        )
    }
    
    /**
     * 获取帧率历史记录
     */
    fun getFpsHistory(): List<FrameMetric> {
        return frameMetrics.toList()
    }
    
    /**
     * 重置监控数据
     */
    fun reset() {
        frameCount = 0
        lastFpsTime = 0L
        currentFps = TARGET_FPS
        frameMetrics.clear()
    }
}
