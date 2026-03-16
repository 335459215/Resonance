package com.resonance.ui.utils

import android.util.Log

/**
 * 列表滚动优化器
 * 根据滚动速度动态调整加载策略
 */
object ListScrollOptimizer {
    
    private val scrollVelocities = mutableListOf<Long>()
    private var isScrollingFast = false
    private var lastScrollTime = 0L
    
    private const val TAG = "ListScrollOptimizer"
    private const val FAST_SCROLL_THRESHOLD = 5 // 200ms 内 5 次滚动
    private const val TIME_WINDOW = 200L // 200ms 时间窗口
    private const val STOP_DELAY = 300L // 停止后延迟 300ms
    
    /**
     * 滚动状态
     */
    enum class ScrollState {
        IDLE,           // 静止
        SLOW_SCROLL,    // 慢速滚动
        FAST_SCROLL,    // 快速滚动
        FLING           // 惯性滚动
    }
    
    private var currentState = ScrollState.IDLE
    
    /**
     * 记录滚动事件
     */
    fun onScroll(velocity: Float) {
        val currentTime = System.currentTimeMillis()
        scrollVelocities.add(currentTime)
        lastScrollTime = currentTime
        
        // 清理旧数据
        val timeWindowStart = currentTime - TIME_WINDOW
        scrollVelocities.removeAll { it < timeWindowStart }
        
        // 判断滚动状态
        val newScrollState = when {
            scrollVelocities.size >= FAST_SCROLL_THRESHOLD -> {
                ScrollState.FAST_SCROLL
            }
            velocity > 2000 -> {
                ScrollState.FLING
            }
            velocity > 500 -> {
                ScrollState.SLOW_SCROLL
            }
            else -> {
                ScrollState.IDLE
            }
        }
        
        if (newScrollState != currentState) {
            Log.d(TAG, "滚动状态变化：$currentState -> $newScrollState")
            currentState = newScrollState
        }
    }
    
    /**
     * 是否应该加载图片
     */
    fun shouldLoadImages(): Boolean {
        return currentState == ScrollState.IDLE || currentState == ScrollState.SLOW_SCROLL
    }
    
    /**
     * 是否应该预加载
     */
    fun shouldPrefetch(): Boolean {
        return currentState == ScrollState.IDLE
    }
    
    /**
     * 获取可见区域范围
     */
    fun getVisibleRange(currentPosition: Int, visibleItemCount: Int): IntRange {
        return when (currentState) {
            ScrollState.FAST_SCROLL, ScrollState.FLING -> {
                // 快速滚动时只加载可见区域
                currentPosition..(currentPosition + visibleItemCount)
            }
            ScrollState.SLOW_SCROLL -> {
                // 慢速滚动时加载可见区域 + 1 个缓冲
                (currentPosition - 1)..(currentPosition + visibleItemCount + 1)
            }
            ScrollState.IDLE -> {
                // 静止时加载可见区域 + 2 个缓冲
                (currentPosition - 2)..(currentPosition + visibleItemCount + 2)
            }
        }
    }
    
    /**
     * 检查是否已停止滚动
     */
    fun isScrollingStopped(): Boolean {
        return System.currentTimeMillis() - lastScrollTime > STOP_DELAY
    }
    
    /**
     * 重置状态
     */
    fun reset() {
        scrollVelocities.clear()
        currentState = ScrollState.IDLE
        isScrollingFast = false
    }
    
    /**
     * 获取当前滚动状态
     */
    fun getScrollState(): ScrollState = currentState
    
    /**
     * 获取滚动速度
     */
    fun getScrollVelocity(): Float {
        if (scrollVelocities.size < 2) return 0f
        
        val timeDiff = scrollVelocities.last() - scrollVelocities.first()
        return if (timeDiff > 0) {
            scrollVelocities.size.toFloat() / timeDiff * 1000
        } else {
            0f
        }
    }
}

/**
 * 智能内存清理器
 * 根据内存使用趋势进行智能清理
 */
object SmartMemoryCleaner {
    
    private val memoryHistory = mutableListOf<MemorySnapshot>()
    private val maxHistorySize = 60 // 保留 1 分钟数据（每秒一个点）
    
    data class MemorySnapshot(
        val timestamp: Long,
        val usedMemory: Long,
        val totalMemory: Long,
        val gcCount: Long
    )
    
    enum class MemoryTrend {
        INCREASING_FAST,  // 快速增长
        INCREASING,       // 增长
        STABLE,           // 稳定
        DECREASING        // 下降
    }
    
    /**
     * 记录内存快照
     */
    fun recordSnapshot(usedMemory: Long, totalMemory: Long, gcCount: Long) {
        val snapshot = MemorySnapshot(
            timestamp = System.currentTimeMillis(),
            usedMemory = usedMemory,
            totalMemory = totalMemory,
            gcCount = gcCount
        )
        
        memoryHistory.add(snapshot)
        
        // 限制历史记录大小
        if (memoryHistory.size > maxHistorySize) {
            memoryHistory.removeAt(0)
        }
    }
    
    /**
     * 获取内存趋势
     */
    fun getMemoryTrend(): MemoryTrend {
        if (memoryHistory.size < 10) return MemoryTrend.STABLE
        
        val recentMemory = memoryHistory.takeLast(10).map { it.usedMemory }.average()
        val oldMemory = memoryHistory.take(10).map { it.usedMemory }.average()
        
        val changePercent = ((recentMemory - oldMemory) / oldMemory) * 100
        
        return when {
            changePercent > 20 -> MemoryTrend.INCREASING_FAST
            changePercent > 5 -> MemoryTrend.INCREASING
            changePercent < -5 -> MemoryTrend.DECREASING
            else -> MemoryTrend.STABLE
        }
    }
    
    /**
     * 智能清理
     */
    fun smartCleanup(): CleanupResult {
        val currentMemory = memoryHistory.lastOrNull() ?: return CleanupResult.NONE
        
        val usagePercent = (currentMemory.usedMemory.toFloat() / currentMemory.totalMemory) * 100
        val trend = getMemoryTrend()
        
        return when {
            // 紧急清理：使用率>90% 或快速增长
            usagePercent > 90 || (trend == MemoryTrend.INCREASING_FAST && usagePercent > 80) -> {
                performAggressiveCleanup()
                CleanupResult.AGGRESSIVE
            }
            
            // 普通清理：使用率>80% 或增长
            usagePercent > 80 || trend == MemoryTrend.INCREASING -> {
                performNormalCleanup()
                CleanupResult.NORMAL
            }
            
            // 轻量清理：定期清理
            shouldPeriodicCleanup() -> {
                performLightCleanup()
                CleanupResult.LIGHT
            }
            
            else -> {
                CleanupResult.NONE
            }
        }
    }
    
    /**
     * 紧急清理
     */
    private fun performAggressiveCleanup(): CleanupResult {
        Log.w("SmartMemoryCleaner", "执行紧急内存清理")
        
        // 1. 清理所有缓存 - 暂时注释，使用 ImageCacheManager 替代
        // ImageCacheManager.clearAll()
        // NetworkCacheManager.clear()
        
        // 2. 通知 GC
        System.gc()
        
        return CleanupResult.AGGRESSIVE
    }
    
    /**
     * 普通清理
     */
    private fun performNormalCleanup(): CleanupResult {
        Log.d("SmartMemoryCleaner", "执行普通内存清理")
        
        // 1. 清理部分缓存 - 暂时注释
        // ImageCacheManager.trim(30) // 清理 30%
        // NetworkCacheManager.trim(50)
        
        // 2. 建议 GC
        System.gc()
        
        return CleanupResult.NORMAL
    }
    
    /**
     * 轻量清理
     */
    private fun performLightCleanup(): CleanupResult {
        Log.d("SmartMemoryCleaner", "执行轻量内存清理")
        
        // 1. 清理旧缓存 - 暂时注释
        // ImageCacheManager.trim(10)
        
        return CleanupResult.LIGHT
    }
    
    /**
     * 检查是否需要定期清理
     */
    private fun shouldPeriodicCleanup(): Boolean {
        // 距离上次清理超过 5 分钟
        val lastCleanup = memoryHistory.lastOrNull()?.timestamp ?: 0
        return System.currentTimeMillis() - lastCleanup > 5 * 60 * 1000
    }
    
    /**
     * 清理结果
     */
    enum class CleanupResult {
        NONE,       // 无需清理
        LIGHT,      // 轻量清理
        NORMAL,     // 普通清理
        AGGRESSIVE  // 紧急清理
    }
}
