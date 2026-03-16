package com.resonance.core.utils

import android.util.Log
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.PrintWriter
import java.io.StringWriter
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ConcurrentHashMap

/**
 * 高级日志系统
 * 提供日志记录、分析、导出等功能
 */
class AdvancedLogger private constructor(
    private val tag: String,
    private val minLevel: LogLevel = LogLevel.DEBUG
) {
    
    companion object {
        private val instances = ConcurrentHashMap<String, AdvancedLogger>()
        private val logHistory = Collections.synchronizedList(mutableListOf<LogEntry>())
        private const val MAX_HISTORY_SIZE = 1000
        
        fun getInstance(tag: String, minLevel: LogLevel = LogLevel.DEBUG): AdvancedLogger {
            return instances.getOrPut(tag) {
                AdvancedLogger(tag, minLevel)
            }
        }
        
        fun getHistory(): List<LogEntry> {
            return logHistory.toList()
        }
        
        fun clearHistory() {
            logHistory.clear()
        }
        
        fun exportLogs(): String {
            return logHistory.joinToString("\n") { entry ->
                "${entry.timestamp} [${entry.level}] ${entry.tag}: ${entry.message}"
            }
        }
    }
    
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault())
    
    /**
     * 调试日志
     */
    fun d(message: String, throwable: Throwable? = null) {
        log(LogLevel.DEBUG, message, throwable)
    }
    
    /**
     * 信息日志
     */
    fun i(message: String, throwable: Throwable? = null) {
        log(LogLevel.INFO, message, throwable)
    }
    
    /**
     * 警告日志
     */
    fun w(message: String, throwable: Throwable? = null) {
        log(LogLevel.WARN, message, throwable)
    }
    
    /**
     * 错误日志
     */
    fun e(message: String, throwable: Throwable? = null) {
        log(LogLevel.ERROR, message, throwable)
    }
    
    /**
     * 严重错误日志
     */
    fun wtf(message: String, throwable: Throwable? = null) {
        log(LogLevel.WTF, message, throwable)
    }
    
    /**
     * 记录日志
     */
    private fun log(level: LogLevel, message: String, throwable: Throwable?) {
        if (level.priority < minLevel.priority) return
        
        val fullMessage = if (throwable != null) {
            "$message\n${getStackTrace(throwable)}"
        } else {
            message
        }
        
        // 记录到历史
        val entry = LogEntry(
            timestamp = dateFormat.format(Date()),
            level = level,
            tag = tag,
            message = fullMessage
        )
        
        if (logHistory.size >= MAX_HISTORY_SIZE) {
            logHistory.removeAt(0)
        }
        logHistory.add(entry)
        
        // 输出到 Logcat
        when (level) {
            LogLevel.DEBUG -> Log.d(tag, fullMessage)
            LogLevel.INFO -> Log.i(tag, fullMessage)
            LogLevel.WARN -> Log.w(tag, fullMessage)
            LogLevel.ERROR -> Log.e(tag, fullMessage)
            LogLevel.WTF -> Log.wtf(tag, fullMessage)
        }
    }
    
    /**
     * 获取堆栈跟踪
     */
    private fun getStackTrace(throwable: Throwable): String {
        val sw = StringWriter()
        val pw = PrintWriter(sw)
        throwable.printStackTrace(pw)
        pw.close()
        return sw.toString()
    }
    
    /**
     * 日志级别
     */
    enum class LogLevel(val priority: Int) {
        DEBUG(2),
        INFO(3),
        WARN(4),
        ERROR(5),
        WTF(6)
    }
    
    /**
     * 日志条目数据类
     */
    data class LogEntry(
        val timestamp: String,
        val level: LogLevel,
        val tag: String,
        val message: String
    )
}

/**
 * 应用监控器
 * 提供性能监控、错误监控、状态监控等功能
 */
class ApplicationMonitor private constructor() {
    
    companion object {
        @Volatile
        private var instance: ApplicationMonitor? = null
        
        fun getInstance(): ApplicationMonitor {
            return instance ?: synchronized(this) {
                instance ?: ApplicationMonitor().also { instance = it }
            }
        }
    }
    
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private val _monitoringState = MutableStateFlow(MonitoringState.IDLE)
    val monitoringState: StateFlow<MonitoringState> = _monitoringState.asStateFlow()
    
    private val errorCount = ConcurrentHashMap<String, Int>()
    private val performanceMetrics = ConcurrentHashMap<String, MutableList<Long>>()
    
    private var isMonitoring = false
    
    /**
     * 开始监控
     */
    fun startMonitoring() {
        if (isMonitoring) return
        isMonitoring = true
        _monitoringState.value = MonitoringState.RUNNING
        
        // 启动内存监控
        startMemoryMonitoring()
        
        // 启动错误监控
        startErrorMonitoring()
        
        // 启动性能监控
        startPerformanceMonitoring()
    }
    
    /**
     * 停止监控
     */
    fun stopMonitoring() {
        isMonitoring = false
        _monitoringState.value = MonitoringState.STOPPED
        scope.cancel()
    }
    
    /**
     * 内存监控
     */
    private fun startMemoryMonitoring() {
        scope.launch {
            while (isMonitoring) {
                val runtime = Runtime.getRuntime()
                val usedMemory = runtime.totalMemory() - runtime.freeMemory()
                val maxMemory = runtime.maxMemory()
                val usagePercent = (usedMemory.toFloat() / maxMemory * 100).toInt()
                
                if (usagePercent > 80) {
                    reportWarning("高内存使用率：${usagePercent}%")
                }
                
                delay(5000)
            }
        }
    }
    
    /**
     * 错误监控
     */
    private fun startErrorMonitoring() {
        // 设置全局异常处理器
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            val errorKey = "${thread.name}_${throwable.javaClass.simpleName}"
            errorCount[errorKey] = (errorCount[errorKey] ?: 0) + 1
            
            AdvancedLogger.getInstance("AppMonitor").e(
                "未捕获的异常",
                throwable
            )
        }
    }
    
    /**
     * 性能监控
     */
    private fun startPerformanceMonitoring() {
        scope.launch {
            while (isMonitoring) {
                // 收集性能指标
                collectPerformanceMetrics()
                delay(10000)
            }
        }
    }
    
    /**
     * 收集性能指标
     */
    private fun collectPerformanceMetrics() {
        // 这里可以添加 CPU、内存、网络等性能指标的收集
    }
    
    /**
     * 记录性能数据
     */
    fun recordPerformance(metric: String, value: Long) {
        if (!performanceMetrics.containsKey(metric)) {
            performanceMetrics[metric] = mutableListOf()
        }
        
        val metrics = performanceMetrics[metric]!!
        if (metrics.size >= 100) {
            metrics.removeAt(0)
        }
        metrics.add(value)
    }
    
    /**
     * 报告警告
     */
    private fun reportWarning(message: String) {
        AdvancedLogger.getInstance("AppMonitor").w(message)
    }
    
    /**
     * 获取错误统计
     */
    fun getErrorStatistics(): Map<String, Int> {
        return errorCount.toMap()
    }
    
    /**
     * 获取性能统计
     */
    fun getPerformanceStatistics(metric: String): PerformanceStats? {
        val metrics = performanceMetrics[metric] ?: return null
        
        if (metrics.isEmpty()) return null
        
        return PerformanceStats(
            count = metrics.size,
            min = metrics.minOrNull() ?: 0,
            max = metrics.maxOrNull() ?: 0,
            avg = metrics.average().toLong(),
            sum = metrics.sum()
        )
    }
    
    /**
     * 监控状态
     */
    enum class MonitoringState {
        IDLE,
        RUNNING,
        STOPPED,
        ERROR
    }
    
    /**
     * 性能统计数据类
     */
    data class PerformanceStats(
        val count: Int,
        val min: Long,
        val max: Long,
        val avg: Long,
        val sum: Long
    )
}

/**
 * 容错机制管理器
 */
class FaultToleranceManager {
    
    private val retryPolicies = ConcurrentHashMap<String, RetryPolicy>()
    private val circuitBreakers = ConcurrentHashMap<String, CircuitBreaker>()
    
    /**
     * 带重试的执行
     */
    suspend fun <T> executeWithRetry(
        operationId: String,
        operation: suspend () -> T,
        policy: RetryPolicy = RetryPolicy()
    ): T {
        var lastException: Exception? = null
        
        repeat(policy.maxRetries + 1) { attempt ->
            try {
                return operation()
            } catch (e: Exception) {
                lastException = e
                if (attempt < policy.maxRetries) {
                    val delay = policy.baseDelay * (1L shl attempt) // 指数退避
                    delay(delay)
                }
            }
        }
        
        throw lastException ?: IllegalStateException("Unknown error")
    }
    
    /**
     * 熔断器模式执行
     */
    suspend fun <T> executeWithCircuitBreaker(
        operationId: String,
        operation: suspend () -> T
    ): T {
        val circuitBreaker = circuitBreakers.getOrPut(operationId) {
            CircuitBreaker()
        }
        
        if (!circuitBreaker.allowRequest()) {
            throw CircuitBreakerOpenException("Circuit breaker is open for $operationId")
        }
        
        return try {
            val result = operation()
            circuitBreaker.recordSuccess()
            result
        } catch (e: Exception) {
            circuitBreaker.recordFailure()
            throw e
        }
    }
    
    /**
     * 重试策略
     */
    data class RetryPolicy(
        val maxRetries: Int = 3,
        val baseDelay: Long = 1000L,
        val maxDelay: Long = 30000L,
        val retryOn: List<Class<out Exception>> = listOf(Exception::class.java)
    )
    
    /**
     * 熔断器
     */
    class CircuitBreaker(
        private val failureThreshold: Int = 5,
        private val successThreshold: Int = 2,
        private val openTimeout: Long = 30000L
    ) {
        private var failureCount = 0
        private var successCount = 0
        private var state = CircuitState.CLOSED
        private var lastFailureTime: Long = 0
        
        fun allowRequest(): Boolean {
            return when (state) {
                CircuitState.CLOSED -> true
                CircuitState.OPEN -> {
                    if (System.currentTimeMillis() - lastFailureTime > openTimeout) {
                        state = CircuitState.HALF_OPEN
                        true
                    } else {
                        false
                    }
                }
                CircuitState.HALF_OPEN -> true
            }
        }
        
        fun recordSuccess() {
            successCount++
            if (state == CircuitState.HALF_OPEN && successCount >= successThreshold) {
                state = CircuitState.CLOSED
                failureCount = 0
                successCount = 0
            }
        }
        
        fun recordFailure() {
            failureCount++
            lastFailureTime = System.currentTimeMillis()
            if (failureCount >= failureThreshold) {
                state = CircuitState.OPEN
            }
        }
        
        enum class CircuitState {
            CLOSED,
            OPEN,
            HALF_OPEN
        }
    }
    
    /**
     * 熔断器打开异常
     */
    class CircuitBreakerOpenException(message: String) : Exception(message)
}

/**
 * 便捷扩展函数
 */

/**
 * 快速记录日志
 */
inline fun <reified T> T.logDebug(message: String) {
    AdvancedLogger.getInstance(T::class.java.simpleName).d(message)
}

inline fun <reified T> T.logInfo(message: String) {
    AdvancedLogger.getInstance(T::class.java.simpleName).i(message)
}

inline fun <reified T> T.logError(message: String, throwable: Throwable? = null) {
    AdvancedLogger.getInstance(T::class.java.simpleName).e(message, throwable)
}

/**
 * 安全执行（带默认值）
 */
suspend fun <T> safeExecute(
    operation: suspend () -> T,
    defaultValue: T,
    onError: ((Exception) -> Unit)? = null
): T {
    return try {
        operation()
    } catch (e: Exception) {
        onError?.invoke(e)
        defaultValue
    }
}

/**
 * 带超时的执行
 */
suspend fun <T> withTimeoutSafe(
    timeoutMillis: Long,
    operation: suspend () -> T
): T? {
    return try {
        withTimeout(timeoutMillis) {
            operation()
        }
    } catch (e: TimeoutCancellationException) {
        null
    }
}
