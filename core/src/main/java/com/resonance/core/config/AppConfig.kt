package com.resonance.core.config

/**
 * 应用全局配置类
 * 统一管理所有配置常量
 */
object AppConfig {
    
    // ==================== 网络配置 ====================
    /**
     * 连接超时时间（毫秒）
     */
    const val CONNECTION_TIMEOUT_MS = 5000L
    
    /**
     * 读取超时时间（毫秒）
     */
    const val READ_TIMEOUT_MS = 10000L
    
    /**
     * 写入超时时间（毫秒）
     */
    const val WRITE_TIMEOUT_MS = 10000L
    
    /**
     * 网络请求最大重试次数
     */
    const val MAX_NETWORK_RETRIES = 3
    
    /**
     * 网络请求重试延迟（毫秒）
     */
    const val NETWORK_RETRY_DELAY_MS = 500L
    
    // ==================== 缓存配置 ====================
    /**
     * 图片内存缓存占比（25%）
     */
    const val IMAGE_MEMORY_CACHE_PERCENT = 0.25
    
    /**
     * 图片磁盘缓存大小（100MB）
     */
    const val IMAGE_DISK_CACHE_SIZE_BYTES = 100L * 1024 * 1024
    
    /**
     * HTTP 缓存大小（50MB）
     */
    const val HTTP_CACHE_SIZE_BYTES = 50L * 1024 * 1024
    
    /**
     * 请求缓存最大数量
     */
    const val MAX_REQUEST_CACHE_SIZE = 100
    
    /**
     * 请求缓存过期时间（5 分钟）
     */
    const val REQUEST_CACHE_EXPIRE_TIME_MS = 5 * 60 * 1000L
    
    // ==================== 日志配置 ====================
    /**
     * 最大日志文件数量
     */
    const val MAX_LOG_FILES = 5
    
    /**
     * 单个日志文件最大大小（MB）
     */
    const val MAX_LOG_FILE_SIZE_MB = 10L
    
    /**
     * 是否启用调试日志
     */
    const val DEBUG_LOG_ENABLED = true
    
    // ==================== 性能监控配置 ====================
    /**
     * 性能监控间隔（毫秒）
     */
    const val PERFORMANCE_MONITOR_INTERVAL_MS = 5000L
    
    /**
     * CPU 使用率告警阈值（80%）
     */
    const val CPU_WARNING_THRESHOLD = 80f
    
    /**
     * CPU 使用率严重告警阈值（95%）
     */
    const val CPU_CRITICAL_THRESHOLD = 95f
    
    /**
     * 内存使用率告警阈值（80%）
     */
    const val MEMORY_WARNING_THRESHOLD = 0.8f
    
    /**
     * 内存使用率严重告警阈值（90%）
     */
    const val MEMORY_CRITICAL_THRESHOLD = 0.9f
    
    /**
     * FPS 低帧率告警阈值（30fps）
     */
    const val FPS_LOW_THRESHOLD = 30
    
    /**
     * FPS 严重低帧率告警阈值（15fps）
     */
    const val FPS_CRITICAL_THRESHOLD = 15
    
    // ==================== 播放器配置 ====================
    /**
     * 播放器缓冲时间（毫秒）
     */
    const val PLAYER_BUFFER_TIME_MS = 150L
    
    /**
     * 播放器最大重试次数
     */
    const val PLAYER_MAX_RETRIES = 3
    
    // ==================== 动画配置 ====================
    /**
     * 最大并发动画数量
     */
    const val MAX_CONCURRENT_ANIMATIONS = 10
    
    /**
     * 动画超时时间（毫秒）
     */
    const val ANIMATION_TIMEOUT_MS = 5000L
}
