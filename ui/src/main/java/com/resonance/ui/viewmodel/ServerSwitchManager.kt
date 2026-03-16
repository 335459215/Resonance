package com.resonance.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.resonance.data.model.ServerConfig
import com.resonance.data.model.ServerType
import com.resonance.data.repository.ServerRepository
import com.resonance.data.repository.ServerStateManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * 服务器切换管理器
 * 管理服务器切换的完整流程和动画
 */
class ServerSwitchManager(application: Application) : AndroidViewModel(application) {
    
    private val serverStateManager = ServerStateManager.getInstance(application)
    private val serverRepository = ServerRepository.getInstance(application)
    
    // 切换状态
    private val _isSwitching = MutableStateFlow(false)
    val isSwitching: StateFlow<Boolean> = _isSwitching.asStateFlow()
    
    // 切换进度（0.0 - 1.0）
    private val _switchProgress = MutableStateFlow(0f)
    val switchProgress: StateFlow<Float> = _switchProgress.asStateFlow()
    
    // 当前服务器
    private val _currentServer = MutableStateFlow<ServerConfig?>(null)
    val currentServer: StateFlow<ServerConfig?> = _currentServer.asStateFlow()
    
    // 目标服务器
    private val _targetServer = MutableStateFlow<ServerConfig?>(null)
    val targetServer: StateFlow<ServerConfig?> = _targetServer.asStateFlow()
    
    // 服务器模式
    private val _serverMode = MutableStateFlow<ServerType?>(null)
    val serverMode: StateFlow<ServerType?> = _serverMode.asStateFlow()
    
    init {
        // 监听当前服务器
        viewModelScope.launch {
            serverStateManager.currentServer.collect { server ->
                _currentServer.value = server
                _serverMode.value = server?.type
            }
        }
    }
    
    /**
     * 切换服务器
     */
    suspend fun switchServer(
        targetServer: ServerConfig,
        onPausePlayback: () -> Unit = {},
        onSwitchComplete: () -> Unit = {}
    ) {
        if (targetServer.id == _currentServer.value?.id) return
        
        _isSwitching.value = true
        _targetServer.value = targetServer
        _switchProgress.value = 0f
        
        try {
            onPausePlayback()
            _switchProgress.value = 0.2f
            delay(150)
            
            serverStateManager.switchServer(targetServer) {
                _currentServer.value = targetServer
                _serverMode.value = targetServer.type
            }
            _switchProgress.value = 0.5f
            delay(150)
            
            _switchProgress.value = 0.8f
            delay(150)
            
            _switchProgress.value = 1f
            onSwitchComplete()
        } finally {
            delay(100)
            _isSwitching.value = false
            _targetServer.value = null
            _switchProgress.value = 0f
        }
    }
    
    fun getAlternativeServer(type: ServerType): ServerConfig? {
        return serverRepository.getServersByType(type).find { it.id != _currentServer.value?.id }
    }
    
    fun cancelSwitch() {
        _isSwitching.value = false
        _targetServer.value = null
        _switchProgress.value = 0f
    }
}

/**
 * 服务器切换 ViewModel
 */
class ServerSwitchViewModel(application: Application) : AndroidViewModel(application) {
    
    private val switchManager = ServerSwitchManager(application)
    
    val isSwitching = switchManager.isSwitching
    val switchProgress = switchManager.switchProgress
    val currentServer = switchManager.currentServer
    val targetServer = switchManager.targetServer
    val serverMode = switchManager.serverMode
    
    fun switchToServer(server: ServerConfig, onPausePlayback: () -> Unit = {}, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            switchManager.switchServer(server, onPausePlayback, onComplete)
        }
    }
    
    fun switchToServerType(type: ServerType, onPausePlayback: () -> Unit = {}, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            switchManager.getAlternativeServer(type)?.let {
                switchToServer(it, onPausePlayback, onComplete)
            }
        }
    }
    
    fun cancelSwitch() {
        switchManager.cancelSwitch()
    }
}

/**
 * ViewModel Factory
 */
class ServerSwitchViewModelFactory(
    private val application: Application
) : ViewModelProvider.Factory {
    
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ServerSwitchViewModel::class.java)) {
            return ServerSwitchViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
