package com.resonance.ui.repository

import com.resonance.ui.model.MenuItem
import com.resonance.ui.model.MenuResponse
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * 菜单仓库
 * 用于管理菜单数据和权限控制
 */
class MenuRepository {
    private val scope = CoroutineScope(Dispatchers.IO)
    private val CACHE_DURATION = 5 * 60 * 1000L // 5分钟缓存
    private var menuCache: List<MenuItem>? = null
    private var lastCacheTime = 0L
    
    /**
     * 获取所有菜单项
     * @param userId 用户ID，用于权限检查
     * @param onComplete 完成回调
     */
    fun getMenus(userId: String? = null, onComplete: (MenuResponse) -> Unit) {
        scope.launch {
            try {
                // 检查缓存
                val currentTime = System.currentTimeMillis()
                if (menuCache != null && currentTime - lastCacheTime < CACHE_DURATION) {
                    // 使用缓存数据
                    val filteredMenus = filterMenusByPermission(menuCache!!, userId)
                    withContext(Dispatchers.Main) {
                        onComplete(MenuResponse(true, filteredMenus))
                    }
                    return@launch
                }
                
                // 加载菜单数据
                val menus = loadMenus()
                menuCache = menus
                lastCacheTime = currentTime
                
                // 根据用户权限过滤菜单
                val filteredMenus = filterMenusByPermission(menus, userId)
                
                withContext(Dispatchers.Main) {
                    onComplete(MenuResponse(true, filteredMenus))
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    onComplete(MenuResponse(false, emptyList(), e.message))
                }
            }
        }
    }
    
    /**
     * 刷新菜单数据
     * @param userId 用户ID，用于权限检查
     * @param onComplete 完成回调
     */
    fun refreshMenus(userId: String? = null, onComplete: (MenuResponse) -> Unit) {
        // 清除缓存
        menuCache = null
        lastCacheTime = 0
        
        // 重新加载菜单
        getMenus(userId, onComplete)
    }
    
    /**
     * 根据用户权限过滤菜单
     * @param menus 原始菜单列表
     * @param userId 用户ID
     * @return 过滤后的菜单列表
     */
    private fun filterMenusByPermission(menus: List<MenuItem>, userId: String?): List<MenuItem> {
        // 这里可以根据用户ID和权限系统进行复杂的权限检查
        // 目前简化处理，只过滤需要特定权限的菜单项
        return menus.filter { menu ->
            // 如果菜单不需要权限，或者用户已登录，则显示
            menu.requiredPermission == null || userId != null
        }
    }
    
    /**
     * 加载菜单数据
     * 这里应该从服务器或本地存储加载菜单数据
     * 目前使用硬编码的模拟数据
     */
    private fun loadMenus(): List<MenuItem> {
        // 模拟从服务器加载菜单数据
        return listOf(
            MenuItem(
                id = "1",
                name = "媒体库",
                icon = "VideoLibrary",
                path = "/library",
                level = 1,
                parentId = null,
                requiredPermission = null,
                visible = true,
                order = 1
            ),
            MenuItem(
                id = "2",
                name = "网络",
                icon = "CloudQueue",
                path = "/network",
                level = 1,
                parentId = null,
                requiredPermission = null,
                visible = true,
                order = 2
            ),
            MenuItem(
                id = "3",
                name = "最近",
                icon = "History",
                path = "/recent",
                level = 1,
                parentId = null,
                requiredPermission = null,
                visible = true,
                order = 3
            ),
            MenuItem(
                id = "4",
                name = "收藏",
                icon = "FavoriteBorder",
                path = "/favorites",
                level = 1,
                parentId = null,
                requiredPermission = null,
                visible = true,
                order = 4
            ),
            MenuItem(
                id = "5",
                name = "设置",
                icon = "Settings",
                path = "/settings",
                level = 1,
                parentId = null,
                requiredPermission = null,
                visible = true,
                order = 5
            ),
            MenuItem(
                id = "6",
                name = "精英版",
                icon = "Star",
                path = "/forward-elite",
                level = 1,
                parentId = null,
                requiredPermission = "elite_access",
                visible = true,
                order = 6
            )
        )
    }
}
