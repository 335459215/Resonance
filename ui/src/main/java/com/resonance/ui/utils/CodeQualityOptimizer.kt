package com.resonance.ui.utils

import android.app.ActivityManager
import android.content.Context
import android.os.Debug
import android.util.Log

/**
 * 代码质量优化工具类
 * 提供性能监控、内存泄漏检测、代码重构辅助等功能
 */
object CodeQualityOptimizer {
    
    private const val TAG = "CodeQualityOptimizer"
    
    // ==================== 性能监控 ====================
    
    /**
     * 性能监控数据类
     */
    data class PerformanceMetrics(
        val memoryUsage: Long,
        val memoryMax: Long,
        val cpuUsage: Float,
        val threadCount: Int,
        val isLowMemory: Boolean
    )
    
    /**
     * 获取当前应用性能指标
     */
    fun getPerformanceMetrics(context: Context): PerformanceMetrics {
        val runtime = Runtime.getRuntime()
        val memoryUsage = runtime.totalMemory() - runtime.freeMemory()
        val memoryMax = runtime.maxMemory()
        val threadCount = Thread.activeCount()
        val isLowMemory = memoryUsage.toDouble() / memoryMax > 0.8
        
        // CPU 使用率（简化计算，返回 0）
        val cpuUsage = 0f
        
        return PerformanceMetrics(
            memoryUsage = memoryUsage,
            memoryMax = memoryMax,
            cpuUsage = cpuUsage,
            threadCount = threadCount,
            isLowMemory = isLowMemory
        )
    }
    
    /**
     * 打印性能日志
     */
    fun logPerformance(context: Context, tag: String = TAG) {
        val metrics = getPerformanceMetrics(context)
        
        Log.d(tag, "===== 性能监控 =====")
        Log.d(tag, "内存使用：${formatBytes(metrics.memoryUsage)} / ${formatBytes(metrics.memoryMax)}")
        Log.d(tag, "内存占比：${(metrics.memoryUsage.toDouble() / metrics.memoryMax * 100).toInt()}%")
        Log.d(tag, "CPU 使用率：${(metrics.cpuUsage * 100).toInt()}%")
        Log.d(tag, "线程数量：${metrics.threadCount}")
        Log.d(tag, "低内存状态：${metrics.isLowMemory}")
        Log.d(tag, "===================")
    }
    
    /**
     * 检查是否应该执行耗时操作
     */
    fun shouldExecuteHeavyOperation(context: Context): Boolean {
        val metrics = getPerformanceMetrics(context)
        
        // 内存使用超过 80% 不执行耗时操作
        if (metrics.isLowMemory) {
            Log.w(TAG, "内存不足，跳过耗时操作")
            return false
        }
        
        // CPU 使用率超过 70% 不执行耗时操作
        if (metrics.cpuUsage > 0.7f) {
            Log.w(TAG, "CPU 使用率过高，跳过耗时操作")
            return false
        }
        
        return true
    }
    
    // ==================== 内存泄漏检测 ====================
    
    /**
     * 内存泄漏检测数据类
     */
    data class LeakDetectionResult(
        val isLeakDetected: Boolean,
        val memoryGrowth: Long,
        val gcCount: Int,
        val recommendation: String
    )
    
    /**
     * 检测可能的内存泄漏
     * @param context 上下文
     * @param memoryThreshold 内存增长阈值（字节）
     */
    fun detectMemoryLeak(
        context: Context,
        memoryThreshold: Long = 10 * 1024 * 1024 // 10MB
    ): LeakDetectionResult {
        val runtime = Runtime.getRuntime()
        val memoryUsage = runtime.totalMemory() - runtime.freeMemory()
        val memoryMax = runtime.maxMemory()
        
        // 获取 GC 次数
        val gcCount = Debug.getNativeHeapAllocatedSize()
        
        // 计算内存增长
        val memoryGrowth = memoryUsage
        
        // 判断是否可能泄漏
        val isLeakDetected = memoryGrowth > memoryThreshold
        
        val recommendation = when {
            !isLeakDetected -> "内存使用正常"
            memoryGrowth > memoryMax * 0.9 -> "严重：内存使用接近上限，建议立即检查"
            memoryGrowth > memoryMax * 0.7 -> "警告：内存使用较高，建议检查 Activity/Fragment 引用"
            else -> "注意：内存使用偏高，建议监控"
        }
        
        return LeakDetectionResult(
            isLeakDetected = isLeakDetected,
            memoryGrowth = memoryGrowth,
            gcCount = gcCount.toInt(),
            recommendation = recommendation
        )
    }
    
    /**
     * 建议的内存泄漏检查点
     */
    fun getLeakChecklist(): List<String> {
        return listOf(
            "✓ 检查静态集合类（HashMap, ArrayList 等）是否持有 Context 引用",
            "✓ 检查单例模式是否持有 Activity/View 引用",
            "✓ 检查匿名内部类是否隐式持有外部类引用",
            "✓ 检查 Handler/Runnable 是否及时移除",
            "✓ 检查 EventBus/RxJava 订阅是否取消",
            "✓ 检查 WebView 是否及时销毁",
            "✓ 检查 Bitmap 是否及时回收",
            "✓ 检查文件流/数据库连接是否关闭",
            "✓ 检查监听器/回调是否及时注销",
            "✓ 检查协程/线程是否及时取消"
        )
    }
    
    // ==================== 代码重构辅助 ====================
    
    /**
     * 代码复杂度评估
     */
    data class CodeComplexityMetrics(
        val cyclomaticComplexity: Int,
        val functionLength: Int,
        val parameterCount: Int,
        val nestingDepth: Int,
        val recommendation: String
    )
    
    /**
     * 评估代码复杂度（简化版）
     */
    fun evaluateCodeComplexity(
        cyclomaticComplexity: Int,
        functionLength: Int,
        parameterCount: Int,
        nestingDepth: Int
    ): CodeComplexityMetrics {
        val recommendation = buildString {
            appendLine("代码质量评估：")
            
            if (cyclomaticComplexity > 10) {
                appendLine("⚠ 圈复杂度过高（$cyclomaticComplexity），建议拆分函数")
            }
            
            if (functionLength > 50) {
                appendLine("⚠ 函数过长（$functionLength 行），建议提取子函数")
            }
            
            if (parameterCount > 5) {
                appendLine("⚠ 参数过多（$parameterCount 个），建议使用数据类封装")
            }
            
            if (nestingDepth > 4) {
                appendLine("⚠ 嵌套过深（$nestingDepth 层），建议提前返回或使用当卫语句")
            }
            
            if (cyclomaticComplexity <= 10 && functionLength <= 50 && 
                parameterCount <= 5 && nestingDepth <= 4) {
                appendLine("✓ 代码质量良好")
            }
        }
        
        return CodeComplexityMetrics(
            cyclomaticComplexity = cyclomaticComplexity,
            functionLength = functionLength,
            parameterCount = parameterCount,
            nestingDepth = nestingDepth,
            recommendation = recommendation
        )
    }
    
    /**
     * 重构建议清单
     */
    fun getRefactoringChecklist(): List<String> {
        return listOf(
            "□ 消除重复代码（DRY 原则）",
            "□ 函数职责单一（SRP 原则）",
            "□ 命名清晰易懂",
            "□ 减少参数数量",
            "□ 降低代码嵌套",
            "□ 提取常量/配置",
            "□ 使用扩展函数",
            "□ 合理使用协程",
            "□ 添加必要注释",
            "□ 编写单元测试"
        )
    }
    
    // ==================== 工具函数 ====================
    
    /**
     * 格式化字节数
     */
    private fun formatBytes(bytes: Long): String {
        return when {
            bytes < 1024 -> "$bytes B"
            bytes < 1024 * 1024 -> "${bytes / 1024} KB"
            bytes < 1024 * 1024 * 1024 -> "${bytes / (1024 * 1024)} MB"
            else -> "${bytes / (1024 * 1024 * 1024)} GB"
        }
    }
    
    /**
     * 获取应用内存信息
     */
    fun getMemoryInfo(context: Context): MemoryInfo {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memoryInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memoryInfo)
        
        val runtime = Runtime.getRuntime()
        val totalMemory = runtime.totalMemory()
        val freeMemory = runtime.freeMemory()
        val maxMemory = runtime.maxMemory()
        
        return MemoryInfo(
            totalMemory = totalMemory,
            freeMemory = freeMemory,
            maxMemory = maxMemory,
            usedMemory = totalMemory - freeMemory,
            availableMemory = memoryInfo.availMem,
            lowMemory = memoryInfo.lowMemory,
            threshold = memoryInfo.threshold
        )
    }
    
    /**
     * 应用内存信息数据类
     */
    data class MemoryInfo(
        val totalMemory: Long,
        val freeMemory: Long,
        val maxMemory: Long,
        val usedMemory: Long,
        val availableMemory: Long,
        val lowMemory: Boolean,
        val threshold: Long
    )
    
    /**
     * 触发垃圾回收
     */
    fun triggerGC() {
        System.gc()
    }
    
    /**
     * 获取线程信息
     */
    fun getThreadInfo(): ThreadInfo {
        val threadGroup = Thread.currentThread().threadGroup ?: Thread.currentThread().threadGroup
        val threadArray = arrayOfNulls<Thread>(threadGroup.activeCount())
        threadGroup.enumerate(threadArray)
        
        return ThreadInfo(
            activeCount = Thread.activeCount(),
            threads = threadArray.filterNotNull().map { it.name }
        )
    }
    
    /**
     * 线程信息数据类
     */
    data class ThreadInfo(
        val activeCount: Int,
        val threads: List<String>
    )
}
