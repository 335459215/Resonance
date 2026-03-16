package com.resonance.data.model

import kotlinx.serialization.Serializable

/**
 * 服务器类型枚举
 * 影视服务器和音乐服务器完全隔离
 */
@Serializable
enum class ServerType {
    VIDEO,    // 影视服务器
    MUSIC,    // 音乐服务器
    EMBY,     // Emby 服务器（兼容旧版）
    PLEX,     // Plex 服务器
    LOCAL,    // 本地服务器
    CLOUD_115, // 115 网盘
    CLOUD_123  // 123 网盘
}

/**
 * 服务器配置数据类
 * 支持影视/音乐双服务器类型
 */
@Serializable
data class ServerConfig(
    val id: String,
    val name: String,
    val url: String,
    val type: ServerType,
    val username: String = "",
    val password: String = "",
    val isConnected: Boolean = false,
    val isDefault: Boolean = false,
    val isEnabled: Boolean = true,
    val lastVisitedPage: String = "discovery",  // 上次访问的页面
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    /**
     * 判断是否为影视服务器
     */
    fun isVideoServer(): Boolean {
        return type == ServerType.VIDEO || type == ServerType.EMBY || type == ServerType.PLEX
    }
    
    /**
     * 判断是否为音乐服务器
     */
    fun isMusicServer(): Boolean {
        return type == ServerType.MUSIC
    }
    
    /**
     * 获取服务器类型图标
     */
    fun getTypeIcon(): String {
        return when (type) {
            ServerType.VIDEO -> "🎬"
            ServerType.MUSIC -> "🎵"
            ServerType.EMBY -> "📺"
            ServerType.PLEX -> "🎞️"
            ServerType.LOCAL -> "💻"
            ServerType.CLOUD_115 -> "☁️"
            ServerType.CLOUD_123 -> "☁️"
        }
    }
}

/**
 * 服务器连接状态
 */
sealed class ServerConnectionState {
    object Unknown : ServerConnectionState()
    object Connecting : ServerConnectionState()
    data class Connected(val server: ServerConfig) : ServerConnectionState()
    data class Error(val message: String, val server: ServerConfig) : ServerConnectionState()
    object Offline : ServerConnectionState()
}

/**
 * 服务器测试连接结果
 */
sealed class ConnectionTestResult {
    object Success : ConnectionTestResult()
    data class Failure(val message: String, val errorCode: Int = -1) : ConnectionTestResult()
    
    val isSuccess: Boolean
        get() = this is Success
    
    val isFailure: Boolean
        get() = this is Failure
    
    fun getOrNull(): Unit? {
        return when (this) {
            is Success -> Unit
            is Failure -> null
        }
    }
    
    fun exceptionOrNull(): String? {
        return when (this) {
            is Success -> null
            is Failure -> message
        }
    }
}
