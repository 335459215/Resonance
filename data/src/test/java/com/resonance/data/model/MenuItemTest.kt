package com.resonance.data.model

import org.junit.Test
import kotlin.test.*

class MenuItemTest {

    @Test
    fun testMenuItem_creationWithAllProperties() {
        val menuItem = MenuItem(
            id = "1",
            name = "测试菜单",
            icon = "TestIcon",
            path = "/test",
            level = 1,
            parentId = "0",
            requiredPermission = "test_permission",
            visible = true,
            order = 1
        )

        assertEquals("1", menuItem.id)
        assertEquals("测试菜单", menuItem.name)
        assertEquals("TestIcon", menuItem.icon)
        assertEquals("/test", menuItem.path)
        assertEquals(1, menuItem.level)
        assertEquals("0", menuItem.parentId)
        assertEquals("test_permission", menuItem.requiredPermission)
        assertTrue(menuItem.visible)
        assertEquals(1, menuItem.order)
    }

    @Test
    fun testMenuItem_creationWithDefaultProperties() {
        val menuItem = MenuItem(
            id = "1",
            name = "测试菜单",
            icon = "TestIcon",
            path = "/test",
            level = 1,
            order = 1
        )

        assertEquals("1", menuItem.id)
        assertEquals("测试菜单", menuItem.name)
        assertEquals("TestIcon", menuItem.icon)
        assertEquals("/test", menuItem.path)
        assertEquals(1, menuItem.level)
        assertNull(menuItem.parentId)
        assertNull(menuItem.requiredPermission)
        assertTrue(menuItem.visible)
        assertEquals(1, menuItem.order)
    }

    @Test
    fun testMenuResponse_successResponse() {
        val menuItems = listOf(
            MenuItem(
                id = "1",
                name = "测试菜单",
                icon = "TestIcon",
                path = "/test",
                level = 1,
                order = 1
            )
        )

        val response = MenuResponse(
            success = true,
            menus = menuItems
        )

        assertTrue(response.success)
        assertEquals(1, response.menus.size)
        assertNull(response.error)
    }

    @Test
    fun testMenuResponse_errorResponse() {
        val response = MenuResponse(
            success = false,
            menus = emptyList(),
            error = "加载失败"
        )

        assertFalse(response.success)
        assertTrue(response.menus.isEmpty())
        assertEquals("加载失败", response.error)
    }

    @Test
    fun testMenuPermissionCheck_withPermission() {
        val permissionCheck = MenuPermissionCheck(
            menuId = "1",
            hasPermission = true
        )

        assertEquals("1", permissionCheck.menuId)
        assertTrue(permissionCheck.hasPermission)
        assertNull(permissionCheck.reason)
    }

    @Test
    fun testMenuPermissionCheck_withoutPermission() {
        val permissionCheck = MenuPermissionCheck(
            menuId = "1",
            hasPermission = false,
            reason = "没有权限"
        )

        assertEquals("1", permissionCheck.menuId)
        assertFalse(permissionCheck.hasPermission)
        assertEquals("没有权限", permissionCheck.reason)
    }
}
