package com.resonance.data.repository

import android.content.Context
import com.resonance.data.model.ServerConfig
import com.resonance.data.model.ServerType
import com.tencent.mmkv.MMKV
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString

/**
 * 服务器状态管理器
 * 负责服务器切换、状态管理和数据隔离
 */
class ServerStateManager(context: Context) {
    
    private val mmkv = MMKV.defaultMMKV()
    private val CURRENT_SERVER_KEY = "current_server_id"
    private val SERVER_STATE_KEY = "server_state_"
    
    // JSON 序列化单例
    private val jsonFormat = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }
    
    // 当前选中的服务器
    private val _currentServer = MutableStateFlow<ServerConfig?>(null)
    val currentServer: StateFlow<ServerConfig?> = _currentServer.asStateFlow()
    
    // 服务器切换状态
    private val _isSwitching = MutableStateFlow(false)
    val isSwitching: StateFlow<Boolean> = _isSwitching.asStateFlow()
    
    // 当前服务器模式（影视/音乐）
    private val _currentServerMode = MutableStateFlow<ServerType?>(null)
    val currentServerMode: StateFlow<ServerType?> = _currentServerMode.asStateFlow()
    
    init {
        // 加载当前服务器
        loadCurrentServer()
    }
    
    /**
     * 设置当前服务器
     */
    suspend fun setCurrentServer(server: ServerConfig) {
        _currentServer.value = server
        _currentServerMode.value = server.type
        mmkv.encode(CURRENT_SERVER_KEY, server.id)
        
        // 保存服务器最后访问页面
        updateLastVisitedPage(server.id, server.lastVisitedPage)
    }
    
    /**
     * 切换服务器
     * 包含完整的切换流程：
     * 1. 暂停当前播放任务
     * 2. 全局 UI 淡隐（0.15s）
     * 3. 切换底部导航模式
     * 4. 刷新页面内容
     * 5. 全局 UI 淡显（0.15s）
     */
    suspend fun switchServer(server: ServerConfig, onSwitchComplete: () -> Unit = {}) {
        if (_currentServer.value?.id == server.id) return
        
        _isSwitching.value = true
        
        try {
            // 1. 暂停当前播放任务（由外部处理）
            pauseCurrentPlayback()
            
            // 2. 全局 UI 淡隐（动画由 UI 层处理）
            
            // 3. 切换服务器
            setCurrentServer(server)
            
            // 4. 刷新页面内容（由外部处理）
            
            // 5. 延迟模拟动画时间
            kotlinx.coroutines.delay(150)
            
            onSwitchComplete()
        } finally {
            _isSwitching.value = false
        }
    }
    
    /**
     * 获取当前服务器模式（影视/音乐）
     */
    fun getCurrentServerMode(): ServerType? {
        return _currentServerMode.value
    }
    
    /**
     * 判断当前是否为影视服务器
     */
    fun isVideoMode(): Boolean {
        return when(_currentServerMode.value) {
            ServerType.VIDEO, ServerType.EMBY, ServerType.PLEX -> true
            else -> false
        }
    }
    
    /**
     * 判断当前是否为音乐服务器
     */
    fun isMusicMode(): Boolean {
        return _currentServerMode.value == ServerType.MUSIC
    }
    
    /**
     * 获取服务器状态（页面状态、播放状态等）
     */
    fun getServerState(serverId: String): ServerState {
        val json = mmkv.decodeString("${SERVER_STATE_KEY}$serverId")
        return json?.let { parseServerStateFromJson(it) } ?: ServerState(serverId = serverId)
    }
    
    /**
     * 保存服务器状态
     */
    fun saveServerState(state: ServerState) {
        val json = serializeServerStateToJson(state)
        mmkv.encode("${SERVER_STATE_KEY}${state.serverId}", json)
    }
    
    /**
     * 更新服务器最后访问页面
     */
    fun updateLastVisitedPage(serverId: String, page: String) {
        val state = getServerState(serverId)
        val updatedState = state.copy(currentPage = page)
        saveServerState(updatedState)
    }
    
    /**
     * 获取服务器最后访问页面
     */
    fun getLastVisitedPage(serverId: String): String {
        return getServerState(serverId).currentPage
    }
    
    /**
     * 暂停当前播放任务
     */
    private fun pauseCurrentPlayback() {
        // 播放暂停逻辑由播放器服务处理
        // 这里通过事件总线或回调通知播放器服务
    }
    
    /**
     * 加载当前服务器
     */
    private fun loadCurrentServer() {
        val serverId = mmkv.decodeString(CURRENT_SERVER_KEY)
        if (serverId != null) {
            // 从 Repository 加载服务器配置
            // 注意：这里需要在初始化时传入 Context 或使用应用上下文
            // 由于 init 块中已经使用了 context.applicationContext，可以直接使用
            _currentServer.value = null // 暂时保持为空，等待外部初始化完成后再加载
            _currentServerMode.value = null
        }
    }
    
    /**
     * 从 JSON 解析服务器状态
     */
    private fun parseServerStateFromJson(json: String): ServerState {
        return try {
            jsonFormat.decodeFromString<ServerState>(json)
        } catch (e: Exception) {
            e.printStackTrace()
            ServerState(serverId = "")
        }
    }
    
    /**
     * 将服务器状态序列化为 JSON
     */
    private fun serializeServerStateToJson(state: ServerState): String {
        return try {
            jsonFormat.encodeToString(state)
        } catch (e: Exception) {
            e.printStackTrace()
            ""
        }
    }
    
    companion object {
        @Volatile
        private var instance: ServerStateManager? = null
        
        fun getInstance(context: Context): ServerStateManager {
            return instance ?: synchronized(this) {
                instance ?: ServerStateManager(context.applicationContext).also {
                    instance = it
                }
            }
        }
    }
}

/**
 * 服务器状态数据类
 * 每个服务器独立记录页面状态、播放状态等
 */
@kotlinx.serialization.Serializable
data class ServerState(
    val serverId: String,
    val currentPage: String = "discovery",
    val scrollPosition: Float = 0f,
    val playbackState: PlaybackState? = null,
    val favorites: List<String> = emptyList(),
    val history: List<String> = emptyList(),
    val downloads: List<String> = emptyList(),
    val lastUpdated: Long = System.currentTimeMillis()
)

/**
 * 播放状态数据类
 */
@kotlinx.serialization.Serializable
data class PlaybackState(
    val mediaId: String,
    val position: Long,
    val duration: Long,
    val isPlaying: Boolean,
    val mediaType: String
)
