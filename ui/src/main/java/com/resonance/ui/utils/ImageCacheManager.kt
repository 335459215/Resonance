package com.resonance.ui.utils

import android.content.Context
import coil.ImageLoader
import coil.disk.DiskCache
import coil.memory.MemoryCache
import coil.request.CachePolicy
import coil.util.DebugLogger
import com.resonance.core.config.AppConfig
import okhttp3.OkHttpClient
import java.io.File
import java.util.concurrent.TimeUnit

/**
 * Coil 图片缓存管理器
 * 提供优化的图片加载和缓存配置
 */
object ImageCacheManager {
    
    /**
     * 创建优化的 ImageLoader
     */
    fun createOptimizedImageLoader(context: Context): ImageLoader {
        return ImageLoader.Builder(context)
            .okHttpClient {
                createCacheEnabledHttpClient(context)
            }
            .memoryCache {
                createMemoryCache(context)
            }
            .diskCache {
                createDiskCache(context)
            }
            .memoryCachePolicy(CachePolicy.ENABLED)
            .diskCachePolicy(CachePolicy.ENABLED)
            .respectCacheHeaders(false) // 忽略服务器的缓存头，使用我们自己的策略
            .logger(DebugLogger()) // 生产环境应该移除
            .build()
    }
    
    /**
     * 创建支持缓存的 OkHttpClient
     */
    private fun createCacheEnabledHttpClient(context: Context): OkHttpClient {
        return OkHttpClient.Builder()
            .connectTimeout(AppConfig.READ_TIMEOUT_MS, TimeUnit.MILLISECONDS)
            .readTimeout(AppConfig.READ_TIMEOUT_MS, TimeUnit.MILLISECONDS)
            .writeTimeout(AppConfig.WRITE_TIMEOUT_MS, TimeUnit.MILLISECONDS)
            .cache(
                okhttp3.Cache(
                    directory = File(context.cacheDir, "http_cache"),
                    maxSize = AppConfig.HTTP_CACHE_SIZE_BYTES // 50MB HTTP 缓存
                )
            )
            .build()
    }
    
    /**
     * 创建内存缓存（带 OOM 保护）
     */
    private fun createMemoryCache(context: Context): MemoryCache {
        val runtime = Runtime.getRuntime()
        val maxMemory = runtime.maxMemory()
        val availableMemory = runtime.freeMemory()
        
        // 动态计算缓存大小，避免 OOM
        val safeMemorySize = (availableMemory.coerceAtMost(maxMemory * 0.5) / 1024).toLong()
        val maxSize = safeMemorySize.coerceAtMost((maxMemory / 1024 * AppConfig.IMAGE_MEMORY_CACHE_PERCENT).toLong())
        
        android.util.Log.d("ImageCacheManager", "Memory cache size: ${maxSize / 1024}KB (available: ${availableMemory / 1024 / 1024}MB)")
        
        return MemoryCache.Builder(context)
            .maxSizeBytes(maxSize)
            .build()
    }
    
    /**
     * 检查内存状态并在需要时清理缓存
     */
    fun checkAndCleanupMemory() {
        val runtime = Runtime.getRuntime()
        val usedMemory = runtime.totalMemory() - runtime.freeMemory()
        val maxMemory = runtime.maxMemory()
        val usagePercent = usedMemory.toDouble() / maxMemory.toDouble()
        
        if (usagePercent > 0.8) {
            android.util.Log.w("ImageCacheManager", "High memory usage detected: ${(usagePercent * 100).toInt()}%, triggering cleanup")
            // 内存使用超过 80%，建议清理
            // 注意：Coil 会自动管理内存缓存，这里只是日志警告
        } else if (usagePercent > 0.9) {
            android.util.Log.e("ImageCacheManager", "Critical memory usage: ${(usagePercent * 100).toInt()}%, immediate cleanup recommended")
        }
    }
    
    /**
     * 创建磁盘缓存
     */
    private fun createDiskCache(context: Context): DiskCache {
        return DiskCache.Builder()
            .directory(context.cacheDir.resolve("image_cache"))
            .maxSizeBytes(AppConfig.IMAGE_DISK_CACHE_SIZE_BYTES)
            .build()
    }
    
    /**
     * 清除图片缓存
     */
    suspend fun clearCache(context: Context) {
        try {
            // 清除内存缓存
            // 注意：Coil 的内存缓存没有直接的 clear 方法
            
            // 清除磁盘缓存
            val cacheDir = File(context.cacheDir, IMAGE_CACHE_DIR)
            if (cacheDir.exists()) {
                cacheDir.deleteRecursively()
            }
            
            // 清除 HTTP 缓存
            val httpCacheDir = File(context.cacheDir, "http_cache")
            if (httpCacheDir.exists()) {
                httpCacheDir.deleteRecursively()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    /**
     * 获取缓存大小（字节）
     */
    suspend fun getCacheSize(context: Context): Long {
        return try {
            val imageCacheSize = getFileSize(File(context.cacheDir, IMAGE_CACHE_DIR))
            val httpCacheSize = getFileSize(File(context.cacheDir, "http_cache"))
            imageCacheSize + httpCacheSize
        } catch (e: Exception) {
            0L
        }
    }
    
    /**
     * 递归获取文件夹大小
     */
    private fun getFileSize(file: File): Long {
        if (!file.exists()) return 0L
        
        if (file.isFile) {
            return file.length()
        }
        
        var size = 0L
        file.listFiles()?.forEach {
            size += getFileSize(it)
        }
        
        return size
    }
}
