package com.resonance.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.resonance.data.model.ServerType
import com.resonance.data.repository.ServerStateManager
import com.resonance.ui.model.NavigationConfig
import com.resonance.ui.model.NavigationConfigs
import com.resonance.ui.model.NavigationItem
import com.resonance.ui.model.NavigationState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * 导航栏 ViewModel
 * 管理导航状态和服务器切换联动
 */
class NavigationViewModel(
    private val serverStateManager: ServerStateManager
) : ViewModel() {
    
    // 当前导航配置
    private val _navigationState = MutableStateFlow<NavigationState>(NavigationState.Loading)
    val navigationState: StateFlow<NavigationState> = _navigationState.asStateFlow()
    
    // 当前选中的 Tab
    private val _currentTabId = MutableStateFlow("discovery")
    val currentTabId: StateFlow<String> = _currentTabId.asStateFlow()
    
    // 当前页面 ID
    private val _currentPageId = MutableStateFlow("discovery")
    val currentPageId: StateFlow<String> = _currentPageId.asStateFlow()
    
    // 服务器切换状态
    private val _isSwitching = MutableStateFlow(false)
    val isSwitching: StateFlow<Boolean> = _isSwitching.asStateFlow()
    
    // 当前服务器类型
    private val _currentServerType = MutableStateFlow<ServerType?>(null)
    val currentServerType: StateFlow<ServerType?> = _currentServerType.asStateFlow()
    
    init {
        // 监听服务器状态变化
        observeServerState()
    }
    
    /**
     * 监听服务器状态变化
     */
    private fun observeServerState() {
        viewModelScope.launch {
            serverStateManager.currentServer.collect { server ->
                server?.let {
                    _currentServerType.value = it.type
                    _currentPageId.value = it.lastVisitedPage
                    updateNavigationConfig()
                }
            }
        }
    }
    
    /**
     * 更新导航配置
     */
    private fun updateNavigationConfig() {
        val serverType = _currentServerType.value ?: return
        val pageId = _currentPageId.value
        
        val config = NavigationConfigs.getNavigationConfigForPage(
            serverType = if (serverType == ServerType.MUSIC) "music" else "video",
            pageId = pageId
        )
        
        _navigationState.value = NavigationState.Success(config)
    }
    
    /**
     * 切换 Tab
     */
    fun selectTab(tabId: String) {
        _currentTabId.value = tabId
        _currentPageId.value = tabId
        
        // 更新服务器最后访问页面
        serverStateManager.currentServer.value?.let { server ->
            viewModelScope.launch {
                serverStateManager.updateLastVisitedPage(server.id, tabId)
            }
        }
        
        // 更新导航配置（辅助按钮可能变化）
        updateNavigationConfig()
    }
    
    /**
     * 切换服务器
     */
    fun switchServer(serverType: ServerType, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            _isSwitching.value = true
            
            try {
                val targetServer: com.resonance.data.model.ServerConfig? = serverStateManager.currentServer.value?.let { current ->
                    if (current.type == serverType) {
                        // 已经是目标类型，无需切换
                        return@launch
                    }
                    // 从 Repository 获取对应类型的服务器
                    null
                }
                
                if (targetServer != null) {
                    serverStateManager.switchServer(targetServer) {
                        _currentServerType.value = targetServer.type
                        _currentPageId.value = targetServer.lastVisitedPage
                        _currentTabId.value = targetServer.lastVisitedPage
                        updateNavigationConfig()
                        onComplete()
                    }
                } else {
                    // 如果没有找到目标服务器，直接完成
                    _isSwitching.value = false
                    onComplete()
                }
            } finally {
                // 延迟模拟动画时间
                delay(150)
                _isSwitching.value = false
            }
        }
    }
    
    /**
     * 获取当前导航配置
     */
    fun getCurrentConfig(): NavigationConfig? {
        return when (val state = _navigationState.value) {
            is NavigationState.Success -> state.config
            else -> null
        }
    }
    
    /**
     * 刷新导航配置
     */
    fun refreshNavigation() {
        updateNavigationConfig()
    }
}

/**
 * 导航栏辅助按钮类型
 */
enum class AuxiliaryButtonType {
    SERVER_SWITCH,    // 服务器切换
    SEARCH,          // 搜索
    FILTER,          // 筛选
    ADD,             // 添加
    MODULES,         // 模块管理
    DOWNLOAD         // 下载
}

/**
 * 导航栏核心 Tab 类型
 */
enum class CoreTabType {
    DISCOVERY,       // 发现
    SERIES,          // 追剧（影视）
    PLAYLIST,        // 歌单（音乐）
    SETTINGS         // 设置
}
