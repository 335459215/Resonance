package com.resonance.ui.model

/**
 * 菜单项目模型
 * 用于定义应用中的菜单项结构
 */
data class MenuItem(
    val id: String,
    val name: String,
    val icon: String,
    val path: String,
    val level: Int,
    val parentId: String? = null,
    val requiredPermission: String? = null,
    val visible: Boolean = true,
    val order: Int
)

/**
 * 菜单响应模型
 * 用于API返回菜单数据
 */
data class MenuResponse(
    val success: Boolean,
    val menus: List<MenuItem>,
    val error: String? = null
)

/**
 * 菜单权限检查模型
 * 用于检查用户是否有权限访问菜单项
 */
data class MenuPermissionCheck(
    val menuId: String,
    val hasPermission: Boolean,
    val reason: String? = null
)
