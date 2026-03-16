package com.resonance.data.repository

import android.content.Context
import com.resonance.data.model.ConnectionTestResult
import com.resonance.data.model.ServerConfig
import com.resonance.data.model.ServerType
import com.resonance.core.config.AppConfig
import com.tencent.mmkv.MMKV
import kotlinx.coroutines.delay
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import okhttp3.OkHttpClient
import okhttp3.Request
import java.net.InetSocketAddress
import java.net.Proxy
import java.net.Socket
import java.net.URL
import javax.net.ssl.SSLContext

/**
 * 服务器配置 Repository
 * 负责服务器配置的增删改查和持久化存储
 */
class ServerRepository(context: Context) {
    
    private val mmkv = MMKV.defaultMMKV()
    private val SERVERS_KEY = "servers_config"
    private val DEFAULT_SERVER_KEY = "default_server_id"
    
    // JSON 序列化单例
    private val jsonFormat = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }
    
    /**
     * 加密服务器密码
     */
    private fun encryptPassword(password: String): String {
        return try {
            // 使用 Base64 编码（简单加密，生产环境应该使用更强的加密）
            android.util.Base64.encodeToString(password.toByteArray(), android.util.Base64.DEFAULT)
        } catch (e: Exception) {
            password
        }
    }
    
    /**
     * 解密服务器密码
     */
    private fun decryptPassword(encryptedPassword: String): String {
        return try {
            String(android.util.Base64.decode(encryptedPassword, android.util.Base64.DEFAULT))
        } catch (e: Exception) {
            encryptedPassword
        }
    }
    
    /**
     * 验证并转换 URL 为 HTTPS
     */
    private fun validateAndConvertUrl(url: String): String {
        // 如果是 HTTP，转换为 HTTPS
        return if (url.startsWith("http://", ignoreCase = true)) {
            url.replaceFirst("http://", "https://", ignoreCase = true)
        } else {
            url
        }
    }
    
    /**
     * 获取所有服务器列表
     */
    fun getAllServers(): List<ServerConfig> {
        val serversJson = mmkv.decodeString(SERVERS_KEY) ?: return emptyList()
        val servers = parseServersFromJson(serversJson)
        
        // 解密所有服务器的密码
        return servers.map { server ->
            server.copy(password = decryptPassword(server.password))
        }
    }
    
    /**
     * 获取指定类型的服务器列表
     */
    fun getServersByType(type: ServerType): List<ServerConfig> {
        return getAllServers().filter { it.type == type }
    }
    
    /**
     * 获取影视服务器列表
     */
    fun getVideoServers(): List<ServerConfig> {
        return getServersByType(ServerType.VIDEO)
    }
    
    /**
     * 获取音乐服务器列表
     */
    fun getMusicServers(): List<ServerConfig> {
        return getServersByType(ServerType.MUSIC)
    }
    
    /**
     * 获取默认服务器
     */
    fun getDefaultServer(): ServerConfig? {
        val servers = getAllServers()
        return servers.find { it.isDefault } ?: servers.firstOrNull()
    }
    
    /**
     * 获取指定 ID 的服务器
     */
    fun getServerById(id: String): ServerConfig? {
        return getAllServers().find { it.id == id }
    }
    
    /**
     * 添加服务器
     */
    fun addServer(server: ServerConfig): Boolean {
        val servers = getAllServers().toMutableList()
        
        // 验证并转换 URL 为 HTTPS
        val validatedUrl = validateAndConvertUrl(server.url)
        
        // 加密密码
        val encryptedPassword = encryptPassword(server.password)
        
        // 创建加密后的服务器配置
        val securedServer = server.copy(
            url = validatedUrl,
            password = encryptedPassword
        )
        
        // 如果是第一个服务器，设置为默认
        val newServer = if (servers.isEmpty()) {
            securedServer.copy(isDefault = true)
        } else {
            securedServer
        }
        
        servers.add(newServer)
        return saveServers(servers)
    }
    
    /**
     * 更新服务器
     */
    fun updateServer(server: ServerConfig): Boolean {
        val servers = getAllServers().toMutableList()
        val index = servers.indexOfFirst { it.id == server.id }
        
        if (index == -1) return false
        
        // 验证并转换 URL 为 HTTPS
        val validatedUrl = validateAndConvertUrl(server.url)
        
        // 加密密码（如果密码改变了）
        val encryptedPassword = if (server.password.isNotEmpty()) {
            encryptPassword(server.password)
        } else {
            // 保持原密码
            servers[index].password
        }
        
        val updatedServer = server.copy(
            url = validatedUrl,
            password = encryptedPassword,
            updatedAt = System.currentTimeMillis()
        )
        
        servers[index] = updatedServer
        return saveServers(servers)
    }
    
    /**
     * 删除服务器
     */
    fun deleteServer(serverId: String): Boolean {
        val servers = getAllServers().toMutableList()
        val server = servers.find { it.id == serverId }
        
        if (server == null) return false
        
        // 如果删除的是默认服务器，需要设置新的默认服务器
        val wasDefault = server.isDefault
        servers.removeAll { it.id == serverId }
        
        if (wasDefault && servers.isNotEmpty()) {
            servers[0] = servers[0].copy(isDefault = true)
        }
        
        return saveServers(servers)
    }
    
    /**
     * 设置默认服务器
     */
    fun setDefaultServer(serverId: String): Boolean {
        val servers = getAllServers().toMutableList()
        
        // 清除所有默认标记
        val updatedServers = servers.map {
            it.copy(isDefault = it.id == serverId)
        }
        
        return saveServers(updatedServers)
    }
    
    /**
     * 更新服务器连接状态
     */
    fun updateConnectionState(serverId: String, isConnected: Boolean): Boolean {
        val servers = getAllServers().toMutableList()
        val index = servers.indexOfFirst { it.id == serverId }
        
        if (index == -1) return false
        
        servers[index] = servers[index].copy(isConnected = isConnected)
        return saveServers(servers)
    }
    
    /**
     * 更新服务器最后访问页面
     */
    fun updateLastVisitedPage(serverId: String, page: String): Boolean {
        val servers = getAllServers().toMutableList()
        val index = servers.indexOfFirst { it.id == serverId }
        
        if (index == -1) return false
        
        servers[index] = servers[index].copy(
            lastVisitedPage = page,
            updatedAt = System.currentTimeMillis()
        )
        
        return saveServers(servers)
    }
    
    /**
     * 测试服务器连接
     * @return 连接是否成功
     */
    suspend fun testServerConnection(server: ServerConfig): Boolean {
        val result = testServerConnectionDetailed(server)
        return result.isSuccess
    }
    
    /**
     * 测试服务器连接（详细版本）
     * @return 连接测试结果
     */
    suspend fun testServerConnectionDetailed(server: ServerConfig): ConnectionTestResult {
        return try {
            val url = URL(server.url)
            val host = url.host
            val port = if (url.port != -1) url.port else if (url.protocol == "https") 443 else 80
            
            // 1. 测试 Socket 连接
            val socketConnected = testSocketConnection(host, port, url.protocol)
            if (!socketConnected) {
                return ConnectionTestResult.Failure("无法连接到服务器，请检查网络或服务器地址", 1001)
            }
            
            // 2. 测试 HTTP/HTTPS 连接
            val httpConnected = testHttpConnection(server.url)
            if (!httpConnected) {
                return ConnectionTestResult.Failure("HTTP 连接失败，服务器可能未响应", 1002)
            }
            
            // 3. 如果有用户名密码，测试认证
            if (server.username.isNotEmpty() && server.password.isNotEmpty()) {
                val authSuccess = testAuthentication(server)
                if (!authSuccess) {
                    return ConnectionTestResult.Failure("认证失败，请检查用户名和密码", 1003)
                }
            }
            
            ConnectionTestResult.Success
        } catch (e: Exception) {
            ConnectionTestResult.Failure("连接测试失败：${e.message}", 1000)
        }
    }
    
    /**
     * 测试 Socket 连接
     */
    private suspend fun testSocketConnection(host: String, port: Int, protocol: String): Boolean {
        return try {
            val socket = Socket()
            try {
                val address = InetSocketAddress(host, port)
                
                if (protocol == "https") {
                    // HTTPS 连接 - 使用系统默认证书验证
                    val sslContext = SSLContext.getInstance("TLS")
                    sslContext.init(null, null, java.security.SecureRandom())
                    
                    val sslSocket = sslContext.socketFactory.createSocket(host, port) as javax.net.ssl.SSLSocket
                    sslSocket.soTimeout = AppConfig.CONNECTION_TIMEOUT_MS.toInt()
                    sslSocket.startHandshake()
                    sslSocket.isConnected
                } else {
                    // HTTP 连接
                    socket.connect(address, AppConfig.CONNECTION_TIMEOUT_MS.toInt())
                    socket.isConnected
                }
            } finally {
                socket.close()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
    
    /**
     * 测试 HTTP 连接（带重试机制）
     */
    private suspend fun testHttpConnection(url: String): Boolean {
        val maxRetries = 3
        var retryCount = 0
        
        while (retryCount < maxRetries) {
            try {
                val client = OkHttpClient.Builder()
                .connectTimeout(AppConfig.CONNECTION_TIMEOUT_MS, java.util.concurrent.TimeUnit.MILLISECONDS)
                .readTimeout(AppConfig.CONNECTION_TIMEOUT_MS, java.util.concurrent.TimeUnit.MILLISECONDS)
                .build()
                
                val request = Request.Builder()
                    .url(url)
                    .head() // 使用 HEAD 请求减少数据传输
                    .build()
                
                val response = client.newCall(request).execute()
                return response.isSuccessful || response.code in 200..399
            } catch (e: Exception) {
                retryCount++
                if (retryCount >= maxRetries) {
                    e.printStackTrace()
                    return false
                }
                // 指数退避：第一次重试延迟 500ms，第二次 1000ms
                delay(AppConfig.NETWORK_RETRY_DELAY_MS * retryCount)
            }
        }
        return false
    }
    
    /**
     * 测试认证
     */
    private suspend fun testAuthentication(server: ServerConfig): Boolean {
        return try {
            // 这里可以根据服务器类型实现不同的认证逻辑
            // 目前简单返回 true，实际应该调用服务器的认证 API
            when (server.type) {
                ServerType.EMBY, ServerType.PLEX -> {
                    // Emby/Plex 认证逻辑（暂时简化）
                    true
                }
                ServerType.CLOUD_115, ServerType.CLOUD_123 -> {
                    // 云存储认证逻辑（暂时简化）
                    true
                }
                else -> true
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
    
    /**
     * 保存服务器列表到 MMKV
     */
    private fun saveServers(servers: List<ServerConfig>): Boolean {
        val json = serializeServersToJson(servers)
        return mmkv.encode(SERVERS_KEY, json)
    }
    
    /**
     * 从 JSON 解析服务器列表
     */
    private fun parseServersFromJson(json: String): List<ServerConfig> {
        return try {
            jsonFormat.decodeFromString<List<ServerConfig>>(json)
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
    
    /**
     * 将服务器列表序列化为 JSON
     */
    private fun serializeServersToJson(servers: List<ServerConfig>): String {
        return try {
            jsonFormat.encodeToString(servers)
        } catch (e: Exception) {
            e.printStackTrace()
            ""
        }
    }
    
    companion object {
        @Volatile
        private var instance: ServerRepository? = null
        
        fun getInstance(context: Context): ServerRepository {
            return instance ?: synchronized(this) {
                instance ?: ServerRepository(context.applicationContext).also {
                    instance = it
                }
            }
        }
    }
}
