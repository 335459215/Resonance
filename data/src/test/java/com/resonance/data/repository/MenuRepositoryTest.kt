package com.resonance.data.repository

import com.resonance.data.model.MenuItem
import com.resonance.data.model.MenuResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import kotlin.test.*

class MenuRepositoryTest {

    private lateinit var menuRepository: MenuRepository

    @Before
    fun setUp() {
        menuRepository = MenuRepository()
    }

    @Test
    fun testGetMenus_returnsSuccessResponse() = runBlocking(Dispatchers.Main) {
        var response: MenuResponse? = null
        val testUserId = "test_user"

        // 调用 getMenus 方法
        menuRepository.getMenus(testUserId) {
            response = it
        }

        // 等待回调执行
        delay(100)

        // 验证响应
        assertNotNull(response)
        assertTrue(response!!.success)
        assertFalse(response!!.menus.isEmpty())
        assertNull(response!!.error)
    }

    @Test
    fun testGetMenus_withoutUserId_filtersPermissionMenus() = runBlocking(Dispatchers.Main) {
        var response: MenuResponse? = null

        // 不提供 userId 调用 getMenus 方法
        menuRepository.getMenus(null) {
            response = it
        }

        // 等待回调执行
        delay(100)

        // 验证响应
        assertNotNull(response)
        assertTrue(response!!.success)
        assertFalse(response!!.menus.isEmpty())
        
        // 验证需要权限的菜单项被过滤掉
        val hasEliteMenu = response!!.menus.any { it.id == "6" }
        assertFalse(hasEliteMenu)
    }

    @Test
    fun testGetMenus_withUserId_includesAllMenus() = runBlocking(Dispatchers.Main) {
        var response: MenuResponse? = null
        val testUserId = "test_user"

        // 提供 userId 调用 getMenus 方法
        menuRepository.getMenus(testUserId) {
            response = it
        }

        // 等待回调执行
        delay(100)

        // 验证响应
        assertNotNull(response)
        assertTrue(response!!.success)
        assertFalse(response!!.menus.isEmpty())
        
        // 验证包含需要权限的菜单项
        val hasEliteMenu = response!!.menus.any { it.id == "6" }
        assertTrue(hasEliteMenu)
    }

    @Test
    fun testRefreshMenus_clearsCache() = runBlocking(Dispatchers.Main) {
        var firstResponse: MenuResponse? = null
        var secondResponse: MenuResponse? = null
        val testUserId = "test_user"

        // 第一次调用 getMenus
        menuRepository.getMenus(testUserId) {
            firstResponse = it
        }
        delay(100)

        // 刷新菜单
        menuRepository.refreshMenus(testUserId) {
            secondResponse = it
        }
        delay(100)

        // 验证两次响应都成功
        assertNotNull(firstResponse)
        assertNotNull(secondResponse)
        assertTrue(firstResponse!!.success)
        assertTrue(secondResponse!!.success)
        
        // 验证菜单数量一致
        assertEquals(firstResponse!!.menus.size, secondResponse!!.menus.size)
    }

    @Test
    fun testGetMenuItem_returnsCorrectMenuItem() = runBlocking(Dispatchers.Main) {
        var menuItem: MenuItem? = null
        val testMenuId = "1"

        // 调用 getMenuItem 方法
        menuRepository.getMenuItem(testMenuId) {
            menuItem = it
        }

        // 等待回调执行
        delay(100)

        // 验证菜单项
        assertNotNull(menuItem)
        assertEquals(testMenuId, menuItem!!.id)
        assertEquals("媒体库", menuItem!!.name)
    }

    @Test
    fun testGetMenuItem_withInvalidId_returnsNull() = runBlocking(Dispatchers.Main) {
        var menuItem: MenuItem? = null
        val invalidMenuId = "999"

        // 调用 getMenuItem 方法
        menuRepository.getMenuItem(invalidMenuId) {
            menuItem = it
        }

        // 等待回调执行
        delay(100)

        // 验证返回 null
        assertNull(menuItem)
    }

    @Test
    fun testClearCache_clearsMenuCache() = runBlocking(Dispatchers.Main) {
        var responseBeforeClear: MenuResponse? = null
        var responseAfterClear: MenuResponse? = null
        val testUserId = "test_user"

        // 第一次调用 getMenus
        menuRepository.getMenus(testUserId) {
            responseBeforeClear = it
        }
        delay(100)

        // 清除缓存
        menuRepository.clearCache()

        // 再次调用 getMenus
        menuRepository.getMenus(testUserId) {
            responseAfterClear = it
        }
        delay(100)

        // 验证两次响应都成功
        assertNotNull(responseBeforeClear)
        assertNotNull(responseAfterClear)
        assertTrue(responseBeforeClear!!.success)
        assertTrue(responseAfterClear!!.success)
        
        // 验证菜单数量一致
        assertEquals(responseBeforeClear!!.menus.size, responseAfterClear!!.menus.size)
    }
}
