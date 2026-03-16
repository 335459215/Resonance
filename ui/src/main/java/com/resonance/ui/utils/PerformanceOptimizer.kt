package com.resonance.ui.utils

import android.content.Context
import coil.ImageLoader
import coil.request.ImageRequest
import coil.size.Size
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * 性能优化工具类
 * 提供列表优化、图片预加载、内存管理等性能优化功能
 */
object PerformanceOptimizer {
    
    /**
     * 图片预加载
     * 提前加载可能需要的图片到缓存
     */
    suspend fun preloadImages(context: Context, imageUrls: List<String>) {
        withContext(Dispatchers.IO) {
            val imageLoader = ImageCacheManager.createOptimizedImageLoader(context)
            
            imageUrls.forEach { url ->
                val request = ImageRequest.Builder(context)
                    .data(url)
                    .size(Size.ORIGINAL)
                    .build()
                
                // 预加载到内存缓存
                imageLoader.enqueue(request)
            }
        }
    }
    
    /**
     * 批量图片预加载（带优先级）
     */
    suspend fun preloadImagesWithPriority(
        context: Context,
        highPriorityUrls: List<String>,
        lowPriorityUrls: List<String>
    ) {
        withContext(Dispatchers.IO) {
            val imageLoader = ImageCacheManager.createOptimizedImageLoader(context)
            
            // 优先加载高优先级图片
            highPriorityUrls.forEach { url ->
                val request = ImageRequest.Builder(context)
                    .data(url)
                    .size(Size.ORIGINAL)
                    .build()
                imageLoader.enqueue(request)
            }
            
            // 延迟加载低优先级图片
            withContext(Dispatchers.Main) {
                kotlinx.coroutines.delay(300)
                lowPriorityUrls.forEach { url ->
                    val request = ImageRequest.Builder(context)
                        .data(url)
                        .size(Size.ORIGINAL)
                        .build()
                    imageLoader.enqueue(request)
                }
            }
        }
    }
    
    /**
     * 清理内存缓存（在低内存时调用）
     */
    fun clearMemoryCache(context: Context) {
        // Coil 的内存缓存会在系统内存不足时自动清理
        // 这里可以手动触发 GC
        System.gc()
    }
    
    /**
     * 优化列表滚动性能
     * 在快速滚动时暂停图片加载
     */
    fun optimizeListScroll() {
        // 这个功能需要在 LazyColumn 的 state 中实现
        // 检测滚动速度，快速滚动时暂停加载
    }
    
    /**
     * 计算最佳图片缓存大小
     */
    fun calculateOptimalCacheSize(context: Context): Long {
        val runtime = Runtime.getRuntime()
        val maxMemory = runtime.maxMemory() / 1024 / 1024 // MB
        
        // 使用可用内存的 1/4 作为图片缓存
        return (maxMemory * 0.25 * 1024 * 1024).toLong()
    }
    
    /**
     * 监控内存使用
     */
    fun getMemoryUsage(): MemoryInfo {
        val runtime = Runtime.getRuntime()
        val maxMemory = runtime.maxMemory() / 1024 / 1024 // MB
        val totalMemory = runtime.totalMemory() / 1024 / 1024 // MB
        val freeMemory = runtime.freeMemory() / 1024 / 1024 // MB
        val usedMemory = totalMemory - freeMemory
        
        return MemoryInfo(
            maxMemory = maxMemory,
            totalMemory = totalMemory,
            freeMemory = freeMemory,
            usedMemory = usedMemory,
            usagePercent = (usedMemory.toDouble() / maxMemory * 100).toInt()
        )
    }
    
    /**
     * 内存信息数据类
     */
    data class MemoryInfo(
        val maxMemory: Long,
        val totalMemory: Long,
        val freeMemory: Long,
        val usedMemory: Long,
        val usagePercent: Int
    )
}
