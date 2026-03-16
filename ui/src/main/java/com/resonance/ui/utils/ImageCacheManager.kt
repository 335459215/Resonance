package com.resonance.ui.utils

import android.content.Context
import coil.ImageLoader
import coil.disk.DiskCache
import coil.memory.MemoryCache
import coil.request.CachePolicy
import coil.util.DebugLogger
import okhttp3.OkHttpClient
import java.io.File
import java.util.concurrent.TimeUnit

/**
 * Coil 图片缓存管理器
 * 提供优化的图片加载和缓存配置
 */
object ImageCacheManager {
    
    private const val MAX_MEMORY_CACHE_PERCENT = 0.25 // 使用 25% 的可用内存作为内存缓存
    private const val MAX_DISK_CACHE_SIZE = 100L * 1024 * 1024 // 100MB 磁盘缓存
    private const val IMAGE_CACHE_DIR = "image_cache"
    
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
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .cache(
                okhttp3.Cache(
                    directory = File(context.cacheDir, "http_cache"),
                    maxSize = 50L * 1024 * 1024 // 50MB HTTP 缓存
                )
            )
            .build()
    }
    
    /**
     * 创建内存缓存
     */
    private fun createMemoryCache(context: Context): MemoryCache {
        val availableMemorySize = Runtime.getRuntime().maxMemory() / 1024
        val maxSize = (availableMemorySize * MAX_MEMORY_CACHE_PERCENT).toLong()
        
        return MemoryCache.Builder(context)
            .maxSizePercent(MAX_MEMORY_CACHE_PERCENT)
            .build()
    }
    
    /**
     * 创建磁盘缓存
     */
    private fun createDiskCache(context: Context): DiskCache {
        return DiskCache.Builder()
            .directory(context.cacheDir.resolve(IMAGE_CACHE_DIR))
            .maxSizeBytes(MAX_DISK_CACHE_SIZE)
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
