package com.resonance.ui.utils

import android.content.Context
import okhttp3.Cache
import okhttp3.OkHttpClient
import java.io.File
import java.util.concurrent.TimeUnit

/**
 * 网络缓存管理器
 * 提供优化的 HTTP 缓存配置
 */
object NetworkCacheManager {
    
    private const val HTTP_CACHE_DIR = "http_cache"
    private const val MAX_CACHE_SIZE = 50L * 1024 * 1024 // 50MB
    private const val CONNECT_TIMEOUT = 30L // 30 秒
    private const val READ_TIMEOUT = 30L // 30 秒
    private const val WRITE_TIMEOUT = 30L // 30 秒
    
    private var cache: Cache? = null
    private var okHttpClient: OkHttpClient? = null
    
    /**
     * 初始化缓存
     */
    fun initialize(context: Context) {
        if (cache == null) {
            val cacheDir = File(context.cacheDir, HTTP_CACHE_DIR)
            cache = Cache(cacheDir, MAX_CACHE_SIZE)
        }
    }
    
    /**
     * 获取配置了缓存的 OkHttpClient
     */
    fun getOkHttpClient(context: Context): OkHttpClient {
        if (okHttpClient == null) {
            initialize(context)
            okHttpClient = OkHttpClient.Builder()
                .cache(cache)
                .connectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS)
                .readTimeout(READ_TIMEOUT, TimeUnit.SECONDS)
                .writeTimeout(WRITE_TIMEOUT, TimeUnit.SECONDS)
                .addInterceptor { chain ->
                    val request = chain.request()
                    val response = chain.proceed(request)
                    
                    // 添加缓存控制头
                    val maxAge = 60 * 60 * 24 // 1 天
                    response.newBuilder()
                        .removeHeader("Pragma")
                        .removeHeader("Cache-Control")
                        .header("Cache-Control", "public, max-age=$maxAge")
                        .build()
                }
                .addNetworkInterceptor { chain ->
                    val response = chain.proceed(chain.request())
                    
                    // 网络拦截器：设置缓存控制
                    val maxAge = 60 * 60 * 24 // 1 天
                    response.newBuilder()
                        .removeHeader("Pragma")
                        .removeHeader("Cache-Control")
                        .header("Cache-Control", "public, max-age=$maxAge")
                        .build()
                }
                .build()
        }
        return okHttpClient!!
    }
    
    /**
     * 清除网络缓存
     */
    suspend fun clearCache() {
        try {
            cache?.delete()
            cache = null
            okHttpClient = null
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    /**
     * 获取缓存大小（字节）
     */
    fun getCacheSize(): Long {
        return cache?.size() ?: 0L
    }
    
    /**
     * 检查缓存是否命中
     */
    fun isCacheEnabled(): Boolean {
        return cache != null && !cache!!.isClosed
    }
}
