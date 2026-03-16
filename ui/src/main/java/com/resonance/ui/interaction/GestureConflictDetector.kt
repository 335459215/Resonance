package com.resonance.ui.interaction

import android.util.Log

/**
 * 手势冲突检测器
 * 检测和管理多个手势之间的冲突
 */
class GestureConflictDetector {
    
    companion object {
        private const val TAG = "GestureConflictDetector"
    }
    
    private val activeGestures = mutableSetOf<GestureType>()
    private val gesturePriorities = mapOf(
        GestureType.TAP to 1,
        GestureType.DOUBLE_TAP to 2,
        GestureType.LONG_PRESS to 3,
        GestureType.DRAG to 4,
        GestureType.ZOOM to 5,
        GestureType.ROTATE to 6,
        GestureType.PINCH to 5,
        GestureType.SWIPE to 4
    )
    
    /**
     * 手势类型
     */
    enum class GestureType {
        TAP,          // 点击
        DOUBLE_TAP,   // 双击
        LONG_PRESS,   // 长按
        DRAG,         // 拖动
        ZOOM,         // 缩放
        ROTATE,       // 旋转
        PINCH,        // 捏合
        SWIPE         // 滑动
    }
    
    /**
     * 手势冲突类型
     */
    enum class ConflictType {
        NONE,           // 无冲突
        MUTUAL_EXCLUSIVE, // 互斥
        PRIORITY_BASED,   // 基于优先级
        SEQUENTIAL        // 顺序执行
    }
    
    /**
     * 检查是否可以开始新手势
     */
    fun canStartGesture(newGesture: GestureType): Boolean {
        if (activeGestures.isEmpty()) {
            return true
        }
        
        val conflictType = getConflictType(newGesture)
        
        return when (conflictType) {
            ConflictType.NONE -> true
            ConflictType.PRIORITY_BASED -> {
                // 检查新手势的优先级是否更高
                val newPriority = gesturePriorities[newGesture] ?: 0
                val maxActivePriority = activeGestures.maxOfOrNull { gesturePriorities[it] ?: 0 } ?: 0
                newPriority > maxActivePriority
            }
            ConflictType.SEQUENTIAL -> true // 顺序执行，可以开始
            ConflictType.MUTUAL_EXCLUSIVE -> false
        }
    }
    
    /**
     * 获取冲突类型
     */
    private fun getConflictType(newGesture: GestureType): ConflictType {
        return when (newGesture) {
            GestureType.TAP, GestureType.DOUBLE_TAP -> {
                // 点击类手势互斥
                if (activeGestures.any { it in setOf(GestureType.TAP, GestureType.DOUBLE_TAP, GestureType.LONG_PRESS) }) {
                    ConflictType.MUTUAL_EXCLUSIVE
                } else {
                    ConflictType.NONE
                }
            }
            GestureType.ZOOM, GestureType.ROTATE, GestureType.PINCH -> {
                // 变换类手势互斥
                if (activeGestures.any { it in setOf(GestureType.ZOOM, GestureType.ROTATE, GestureType.PINCH) }) {
                    ConflictType.MUTUAL_EXCLUSIVE
                } else {
                    ConflictType.PRIORITY_BASED
                }
            }
            GestureType.DRAG, GestureType.SWIPE -> {
                // 拖动手势基于优先级
                ConflictType.PRIORITY_BASED
            }
            GestureType.LONG_PRESS -> {
                // 长按可以与点击共存
                ConflictType.SEQUENTIAL
            }
        }
    }
    
    /**
     * 开始手势
     */
    fun startGesture(gesture: GestureType): Boolean {
        if (canStartGesture(gesture)) {
            // 如果需要，取消低优先级手势
            val newPriority = gesturePriorities[gesture] ?: 0
            val toCancel = activeGestures.filter { (gesturePriorities[it] ?: 0) < newPriority }
            
            if (toCancel.isNotEmpty()) {
                Log.d(TAG, "取消低优先级手势：$toCancel")
                toCancel.forEach { activeGestures.remove(it) }
            }
            
            activeGestures.add(gesture)
            Log.d(TAG, "开始手势：$gesture, 活跃手势：$activeGestures")
            return true
        } else {
            Log.w(TAG, "手势冲突，无法开始：$gesture")
            return false
        }
    }
    
    /**
     * 结束手势
     */
    fun endGesture(gesture: GestureType) {
        activeGestures.remove(gesture)
        Log.d(TAG, "结束手势：$gesture, 活跃手势：$activeGestures")
    }
    
    /**
     * 获取当前活跃的手势
     */
    fun getActiveGestures(): Set<GestureType> = activeGestures.toSet()
    
    /**
     * 获取活跃手势数量
     */
    fun getActiveCount(): Int = activeGestures.size
    
    /**
     * 重置所有手势
     */
    fun reset() {
        activeGestures.clear()
    }
    
    /**
     * 检查是否有冲突
     */
    fun hasConflict(): Boolean {
        return activeGestures.size > 1
    }
    
    /**
     * 获取最高优先级的手势
     */
    fun getHighestPriorityGesture(): GestureType? {
        if (activeGestures.isEmpty()) return null
        
        return activeGestures.maxByOrNull { gesturePriorities[it] ?: 0 }
    }
}

/**
 * 手势识别优化器
 * 基于时间窗口和特征分析的手势识别
 */
object AdvancedGestureRecognizer {
    
    data class GestureEvent(
        val timestamp: Long,
        val type: GestureConflictDetector.GestureType,
        val x: Float,
        val y: Float,
        val velocity: Float = 0f
    )
    
    /**
     * 识别手势
     */
    fun recognizeGesture(events: List<GestureEvent>): GestureConflictDetector.GestureType {
        if (events.isEmpty()) return GestureConflictDetector.GestureType.TAP
        
        val timeWindow = 300L // 300ms 时间窗口
        val currentTime = System.currentTimeMillis()
        val recentEvents = events.filter { it.timestamp > currentTime - timeWindow }
        
        if (recentEvents.isEmpty()) return GestureConflictDetector.GestureType.TAP
        
        // 计算特征
        val avgVelocity = recentEvents.map { it.velocity }.average()
        val totalDistance = calculateTotalDistance(recentEvents)
        val eventCount = recentEvents.size
        
        // 基于特征识别手势
        return when {
            // 快速滑动
            avgVelocity > 1000 && totalDistance > 200 -> GestureConflictDetector.GestureType.SWIPE
            
            // 多点触控缩放
            eventCount > 2 && totalDistance > 100 -> GestureConflictDetector.GestureType.ZOOM
            
            // 长按
            recentEvents.all { it.velocity < 10 } && 
            (currentTime - recentEvents.first().timestamp) > 500 -> 
                GestureConflictDetector.GestureType.LONG_PRESS
            
            // 拖动
            totalDistance > 50 && avgVelocity < 500 -> GestureConflictDetector.GestureType.DRAG
            
            // 点击
            else -> GestureConflictDetector.GestureType.TAP
        }
    }
    
    private fun calculateTotalDistance(events: List<GestureEvent>): Float {
        var total = 0f
        for (i in 1 until events.size) {
            val dx = events[i].x - events[i - 1].x
            val dy = events[i].y - events[i - 1].y
            total += kotlin.math.sqrt(dx * dx + dy * dy)
        }
        return total
    }
}
