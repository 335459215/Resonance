package com.resonance.core.performance

import android.app.ActivityManager
import android.content.Context
import android.os.SystemClock
import com.resonance.core.config.AppConfig
import kotlinx.coroutines.*
import okhttp3.OkHttpClient
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

/**
 * 高级性能管理器
 * 提供内存管理、性能监控、资源优化等功能
 */
class PerformanceManager private constructor(
    private val context: Context
) {
    
    companion object {
        @Volatile
        private var instance: PerformanceManager? = null
        
        fun getInstance(context: Context): PerformanceManager {
            return instance ?: synchronized(this) {
                instance ?: PerformanceManager(context.applicationContext).also {
                    instance = it
                }
            }
        }
    }
    
    private var scope: CoroutineScope? = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private val memoryCache = ConcurrentHashMap<String, Any>()
    private val performanceMetrics = ConcurrentHashMap<String, Long>()
    private var isMonitoring = false
    
    // 内存阈值
    private val memoryThreshold = 0.8f
    
    /**
     * 开始性能监控
     */
    fun startMonitoring() {
        if (isMonitoring) return
        isMonitoring = true
        
        scope?.launch {
            while (isMonitoring) {
                monitorMemory()
                monitorCPU()
                delay(5000) // 每 5 秒监控一次
            }
        }
    }
    
    /**
     * 停止性能监控
     */
    fun stopMonitoring() {
        isMonitoring = false
    }
    
    /**
     * 监控内存使用
     */
    private suspend fun monitorMemory() = withContext(Dispatchers.Main) {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memoryInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memoryInfo)
        
        val usedMemory = Runtime.getRuntime().run { totalMemory() - freeMemory() }
        val maxMemory = Runtime.getRuntime().maxMemory()
        val memoryUsage = usedMemory.toFloat() / maxMemory
        
        if (memoryUsage > memoryThreshold) {
            onLowMemory()
        }
    }
    
    /**
     * 监控 CPU 使用率
     */
    private suspend fun monitorCPU() = withContext(Dispatchers.Default) {
        // 简化 CPU 监控实现
        // Debug.getCpuRate() 已废弃，使用简化方案
        val cpuUsage = 0f // 占位符
    }
    
    /**
     * 内存不足时的处理
     */
    private fun onLowMemory() {
        // 清理缓存
        memoryCache.clear()
        // 触发 GC
        System.gc()
    }
    
    /**
     * 开始性能计时
     */
    fun startTimer(tag: String) {
        performanceMetrics[tag] = SystemClock.elapsedRealtime()
    }
    
    /**
     * 结束性能计时并返回耗时（毫秒）
     */
    fun endTimer(tag: String): Long {
        val startTime = performanceMetrics[tag] ?: return 0L
        val endTime = SystemClock.elapsedRealtime()
        val duration = endTime - startTime
        synchronized(performanceMetrics) {
            performanceMetrics.remove(tag)
        }
        return duration
    }
    
    /**
     * 添加到内存缓存
     */
    fun <T> addToMemoryCache(key: String, value: T) {
        if (memoryCache.size > 100) {
            // 缓存太多，清理最旧的 20%
            val keysToRemove = memoryCache.keys.take(20)
            keysToRemove.forEach { memoryCache.remove(it) }
        }
        @Suppress("UNCHECKED_CAST")
        memoryCache[key] = value as Any
    }
    
    /**
     * 从内存缓存获取
     */
    @Suppress("UNCHECKED_CAST")
    fun <T> getFromMemoryCache(key: String): T? {
        return memoryCache[key] as T?
    }
    
    /**
     * 清理内存缓存
     */
    fun clearMemoryCache() {
        memoryCache.clear()
    }
    
    /**
     * 获取内存信息
     */
    fun getMemoryInfo(): MemoryInfo {
        val runtime = Runtime.getRuntime()
        val totalMemory = runtime.totalMemory()
        val freeMemory = runtime.freeMemory()
        val maxMemory = runtime.maxMemory()
        val usedMemory = totalMemory - freeMemory
        
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memoryInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memoryInfo)
        
        return MemoryInfo(
            usedMemory = usedMemory,
            freeMemory = freeMemory,
            maxMemory = maxMemory,
            totalMemory = totalMemory,
            availableMemory = memoryInfo.availMem,
            lowMemory = memoryInfo.lowMemory,
            memoryUsagePercent = (usedMemory.toFloat() / maxMemory * 100).toInt()
        )
    }
    
    /**
     * 内存信息数据类
     */
    data class MemoryInfo(
        val usedMemory: Long,
        val freeMemory: Long,
        val maxMemory: Long,
        val totalMemory: Long,
        val availableMemory: Long,
        val lowMemory: Boolean,
        val memoryUsagePercent: Int
    )
    
    /**
     * 销毁管理器
     */
    fun destroy() {
        isMonitoring = false
        scope?.cancel()
        scope = null
        memoryCache.clear()
        performanceMetrics.clear()
        instance = null
    }
}

/**
 * 图片加载优化器
 */
object ImageLoaderOptimizer {
    
    private val imageCache = ConcurrentHashMap<String, ByteArray>()
    private val maxCacheSize = 50 * 1024 * 1024 // 50MB
    private var currentCacheSize = 0L
    
    /**
     * 预加载图片到缓存
     */
    suspend fun preloadImages(urls: List<String>) = withContext(Dispatchers.IO) {
        urls.forEach { url ->
            if (!imageCache.containsKey(url)) {
                try {
                    // 模拟图片加载
                    // 实际使用时替换为真实的图片加载逻辑
                    val imageData = loadImageFromNetwork(url)
                    addToCache(url, imageData)
                } catch (e: Exception) {
                    // 忽略加载失败
                }
            }
        }
    }
    
    /**
     * 批量预加载（带优先级）
     */
    suspend fun preloadImagesWithPriority(
        highPriority: List<String>,
        normalPriority: List<String>
    ) = withContext(Dispatchers.IO) {
        // 先加载高优先级
        preloadImages(highPriority)
        // 再加载普通优先级
        preloadImages(normalPriority)
    }
    
    /**
     * 从缓存获取图片
     */
    fun getImageFromCache(url: String): ByteArray? {
        return imageCache[url]
    }
    
    /**
     * 添加到缓存
     */
    private fun addToCache(url: String, data: ByteArray) {
        val dataSize = data.size.toLong()
        
        // 如果缓存超限，清理最旧的数据
        while (currentCacheSize + dataSize > maxCacheSize && imageCache.isNotEmpty()) {
            val oldestKey = imageCache.entries.iterator().next().key
            val removedData = imageCache.remove(oldestKey)
            if (removedData != null) {
                currentCacheSize -= removedData.size.toLong()
            }
        }
        
        imageCache[url] = data
        currentCacheSize += dataSize
    }
    
    /**
     * 清理缓存
     */
    fun clearCache() {
        imageCache.clear()
        currentCacheSize = 0L
    }
    
    /**
     * 从网络加载图片
     */
    private suspend fun loadImageFromNetwork(url: String): ByteArray {
        return try {
            val client = okhttp3.OkHttpClient.Builder()
                .connectTimeout(10000, java.util.concurrent.TimeUnit.MILLISECONDS)
                .readTimeout(10000, java.util.concurrent.TimeUnit.MILLISECONDS)
                .build()
            
            val request = okhttp3.Request.Builder()
                .url(url)
                .build()
            
            val response = client.newCall(request).execute()
            if (response.isSuccessful && response.body != null) {
                response.body!!.bytes()
            } else {
                ByteArray(0)
            }
        } catch (e: Exception) {
            android.util.Log.e("ImageLoader", "Failed to load image from $url: ${e.message}")
            ByteArray(0)
        }
    }
}

/**
 * 网络请求优化器
 */
object NetworkOptimizer {
    
    private val maxCacheSize = AppConfig.MAX_REQUEST_CACHE_SIZE // 最多缓存 100 个请求
    private val requestCache = object : LinkedHashMap<String, Pair<Long, Any>>(maxCacheSize, 0.75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, Pair<Long, Any>>?): Boolean {
            return size > maxCacheSize
        }
    }
    private val cacheExpireTime = AppConfig.REQUEST_CACHE_EXPIRE_TIME_MS // 5 分钟
    
    /**
     * 带缓存的网络请求
     */
    @Suppress("UNCHECKED_CAST")
    suspend fun <T> requestWithCache(
        key: String,
        request: suspend () -> T,
        useCache: Boolean = true
    ): T {
        // 检查缓存
        if (useCache) {
            val cached = requestCache[key]
            if (cached != null && System.currentTimeMillis() - cached.first < cacheExpireTime) {
                return cached.second as T
            }
        }
        
        // 执行请求
        val result = request()
        
        // 缓存结果
        @Suppress("UNCHECKED_CAST")
        requestCache[key] = System.currentTimeMillis() to (result as Any)
        
        return result
    }
    
    /**
     * 批量请求（并发控制）
     */
    suspend fun <T, R> batchRequest(
        items: List<T>,
        transform: suspend (T) -> R,
        concurrency: Int = 3
    ): List<R> = withContext(Dispatchers.IO) {
        val semaphore = kotlinx.coroutines.sync.Semaphore(concurrency)
        items.map { item ->
            async {
                semaphore.acquire()
                try {
                    transform(item)
                } finally {
                    semaphore.release()
                }
            }
        }.awaitAll()
    }
    
    /**
     * 清理缓存
     */
    fun clearCache() {
        requestCache.clear()
    }
}
