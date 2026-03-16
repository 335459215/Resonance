package com.resonance.core.performance

import android.util.Log

/**
 * 性能趋势分析器
 * 分析 CPU、内存、帧率的变化趋势
 */
class PerformanceTrendAnalyzer {
    
    companion object {
        private const val TAG = "PerformanceTrend"
        private const val MAX_HISTORY_SIZE = 3600 // 保留 1 小时数据（每分钟一个点）
    }
    
    private val metrics = mutableListOf<PerformanceDataPoint>()
    
    data class PerformanceDataPoint(
        val timestamp: Long,
        val cpuUsage: Float,
        val memoryUsage: Long,
        val fps: Int
    )
    
    enum class PerformanceTrend {
        INCREASING,   // 上升
        DECREASING,   // 下降
        STABLE        // 稳定
    }
    
    /**
     * 记录性能数据
     */
    fun recordMetrics(cpu: Float, memory: Long, fps: Int) {
        val point = PerformanceDataPoint(
            timestamp = System.currentTimeMillis(),
            cpuUsage = cpu,
            memoryUsage = memory,
            fps = fps
        )
        
        metrics.add(point)
        
        // 保留最近 1 小时数据
        val oneHourAgo = System.currentTimeMillis() - 3600_000
        metrics.removeAll { it.timestamp < oneHourAgo }
        
        // 限制数据点数量
        if (metrics.size > MAX_HISTORY_SIZE) {
            metrics.removeAt(0)
        }
    }
    
    /**
     * 获取 CPU 趋势
     */
    fun getCpuTrend(): PerformanceTrend {
        return getTrend { it.cpuUsage }
    }
    
    /**
     * 获取内存趋势
     */
    fun getMemoryTrend(): PerformanceTrend {
        return getTrend { it.memoryUsage.toFloat() }
    }
    
    /**
     * 获取帧率趋势
     */
    fun getFpsTrend(): PerformanceTrend {
        return getTrend { it.fps.toFloat() }
    }
    
    /**
     * 获取通用趋势
     */
    private fun getTrend(selector: (PerformanceDataPoint) -> Float): PerformanceTrend {
        if (metrics.size < 20) return PerformanceTrend.STABLE
        
        val recent = metrics.takeLast(10).map { selector(it) }.average()
        val old = metrics.take(10).map { selector(it) }.average()
        
        val changePercent = if (old != 0.0) ((recent - old) / old) * 100 else 0.0
        
        return when {
            changePercent > 10 -> PerformanceTrend.INCREASING
            changePercent < -10 -> PerformanceTrend.DECREASING
            else -> PerformanceTrend.STABLE
        }
    }
    
    /**
     * 获取性能报告
     */
    fun getReport(): String {
        if (metrics.isEmpty()) return "无数据"
        
        val latest = metrics.last()
        val avgCpu = metrics.map { it.cpuUsage }.average()
        val avgMemory = metrics.map { it.memoryUsage }.average() / 1024 / 1024
        val avgFps = metrics.map { it.fps }.average()
        
        return buildString {
            appendLine("=== 性能趋势报告 ===")
            appendLine("最新 CPU: ${latest.cpuUsage.toInt()}%, 平均：${avgCpu.toInt()}%")
            appendLine("最新内存：${latest.memoryUsage / 1024 / 1024}MB, 平均：${avgMemory.toInt()}MB")
            appendLine("最新 FPS: ${latest.fps}, 平均：${avgFps.toInt()}")
            appendLine("CPU 趋势：${getCpuTrend()}")
            appendLine("内存趋势：${getMemoryTrend()}")
            appendLine("帧率趋势：${getFpsTrend()}")
        }
    }
    
    /**
     * 重置数据
     */
    fun reset() {
        metrics.clear()
    }
}

/**
 * 性能告警管理器
 * 监控性能指标并发出告警
 */
class PerformanceAlertManager {
    
    companion object {
        private const val TAG = "PerformanceAlert"
    }
    
    private val alertThresholds: MutableMap<String, Number> = mutableMapOf(
        "cpu_high" to 80f,
        "cpu_critical" to 95f,
        "memory_high" to (90 * 1024 * 1024).toLong(),
        "memory_critical" to (95 * 1024 * 1024).toLong(),
        "fps_low" to 30,
        "fps_critical" to 15
    )
    
    private val alertCallbacks = mutableListOf<AlertCallback>()
    
    interface AlertCallback {
        fun onAlert(alertType: String, message: String, severity: Severity)
    }
    
    enum class Severity {
        WARNING,    // 警告
        CRITICAL,   // 严重
        INFO        // 信息
    }
    
    /**
     * 注册告警回调
     */
    fun registerCallback(callback: AlertCallback) {
        alertCallbacks.add(callback)
    }
    
    /**
     * 移除告警回调
     */
    fun unregisterCallback(callback: AlertCallback) {
        alertCallbacks.remove(callback)
    }
    
    /**
     * 检查性能指标
     */
    fun checkAlerts(cpu: Float, memory: Long, fps: Int) {
        // CPU 告警
        val cpuCritical = alertThresholds["cpu_critical"] as? Float ?: 95f
        val cpuHigh = alertThresholds["cpu_high"] as? Float ?: 80f
        
        if (cpu > cpuCritical) {
            sendAlert("CPU_CRITICAL", "CPU 使用率严重过高：${cpu.toInt()}%", Severity.CRITICAL)
        } else if (cpu > cpuHigh) {
            sendAlert("CPU_HIGH", "CPU 使用率过高：${cpu.toInt()}%", Severity.WARNING)
        }
        
        // 内存告警
        val memoryCritical = alertThresholds["memory_critical"] as? Long ?: Long.MAX_VALUE
        val memoryHigh = alertThresholds["memory_high"] as? Long ?: Long.MAX_VALUE
        
        if (memory > memoryCritical) {
            sendAlert("MEMORY_CRITICAL", "内存使用严重过高：${memory / 1024 / 1024}MB", Severity.CRITICAL)
        } else if (memory > memoryHigh) {
            sendAlert("MEMORY_HIGH", "内存使用过高：${memory / 1024 / 1024}MB", Severity.WARNING)
        }
        
        // FPS 告警
        val fpsCritical = alertThresholds["fps_critical"] as? Int ?: 15
        val fpsLow = alertThresholds["fps_low"] as? Int ?: 30
        
        if (fps < fpsCritical) {
            sendAlert("FPS_CRITICAL", "帧率严重过低：${fps}fps", Severity.CRITICAL)
        } else if (fps < fpsLow) {
            sendAlert("FPS_LOW", "帧率过低：${fps}fps", Severity.WARNING)
        }
    }
    
    /**
     * 发送告警
     */
    private fun sendAlert(alertType: String, message: String, severity: Severity) {
        Log.w(TAG, "[$severity] $alertType: $message")
        
        alertCallbacks.forEach { callback ->
            try {
                callback.onAlert(alertType, message, severity)
            } catch (e: Exception) {
                Log.e(TAG, "告警回调失败", e)
            }
        }
    }
    
    /**
     * 更新阈值
     */
    fun updateThreshold(key: String, value: Number) {
        when (value) {
            is Float -> alertThresholds[key] = value
            is Long -> alertThresholds[key] = value
            is Int -> alertThresholds[key] = value.toFloat() // Convert int to float for consistency
            is Double -> alertThresholds[key] = value.toFloat() // Convert double to float
            else -> throw IllegalArgumentException("Unsupported threshold value type: ${value::class.simpleName}")
        }
    }
    
    /**
     * 获取当前阈值
     */
    fun getThresholds(): Map<String, Number> = alertThresholds.toMap()
}
