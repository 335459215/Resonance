package com.resonance.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.resonance.data.model.ServerConfig
import com.resonance.data.model.ServerType
import com.resonance.data.repository.ServerRepository
import com.resonance.data.repository.ServerStateManager
import com.resonance.ui.screens.ServerData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

/**
 * 服务器管理 ViewModel
 */
class ServerManagementViewModel(
    private val serverRepository: ServerRepository,
    private val serverStateManager: ServerStateManager
) : ViewModel() {
    
    // 服务器列表
    private val _servers = MutableStateFlow<List<ServerConfig>>(emptyList())
    val servers: StateFlow<List<ServerConfig>> = _servers.asStateFlow()
    
    // 当前服务器 ID
    private val _currentServerId = MutableStateFlow<String?>(null)
    val currentServerId: StateFlow<String?> = _currentServerId.asStateFlow()
    
    // 加载状态
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    // 错误信息
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    init {
        loadServers()
    }
    
    /**
     * 加载服务器列表
     */
    fun loadServers() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val allServers = serverRepository.getAllServers()
                _servers.value = allServers
                _currentServerId.value = serverRepository.getDefaultServer()?.id
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * 选择服务器
     */
    fun selectServer(server: ServerConfig) {
        viewModelScope.launch {
            try {
                // 如果已经是当前服务器，直接返回
                if (server.id == _currentServerId.value) return@launch
                
                // 设置为默认服务器
                serverRepository.setDefaultServer(server.id)
                _currentServerId.value = server.id
                
                // 更新服务器管理器状态
                serverStateManager.setCurrentServer(server)
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }
    
    /**
     * 添加服务器
     */
    fun addServer(serverData: ServerData, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val server = ServerConfig(
                    id = UUID.randomUUID().toString(),
                    name = serverData.name,
                    url = serverData.url,
                    type = serverData.type,
                    username = serverData.username,
                    password = serverData.password,
                    isConnected = false,
                    isDefault = _servers.value.isEmpty()
                )
                
                val success = serverRepository.addServer(server)
                if (success) {
                    loadServers()
                    onSuccess()
                } else {
                    _error.value = "添加服务器失败"
                }
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * 更新服务器
     */
    fun updateServer(server: ServerConfig, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val success = serverRepository.updateServer(server)
                if (success) {
                    loadServers()
                    onSuccess()
                } else {
                    _error.value = "更新服务器失败"
                }
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * 删除服务器
     */
    fun deleteServer(server: ServerConfig, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val success = serverRepository.deleteServer(server.id)
                if (success) {
                    loadServers()
                    onSuccess()
                } else {
                    _error.value = "删除服务器失败"
                }
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * 测试服务器连接
     */
    suspend fun testServerConnection(server: ServerConfig): Boolean {
        return try {
            serverRepository.testServerConnection(server)
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * 清除错误
     */
    fun clearError() {
        _error.value = null
    }
}

/**
 * ViewModel Factory
 */
class ServerManagementViewModelFactory(
    private val context: Context
) : ViewModelProvider.Factory {
    
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ServerManagementViewModel::class.java)) {
            return ServerManagementViewModel(
                serverRepository = ServerRepository.getInstance(context),
                serverStateManager = ServerStateManager.getInstance(context)
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
