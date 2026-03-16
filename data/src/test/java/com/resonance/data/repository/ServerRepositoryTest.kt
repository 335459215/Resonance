package com.resonance.data.repository

import com.resonance.data.model.ServerConfig
import com.resonance.data.model.ServerType
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * ServerRepository 单元测试
 * 测试服务器配置管理功能
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class ServerRepositoryTest {
    
    private lateinit var repository: ServerRepository
    
    @Before
    fun setup() {
        val context = RuntimeEnvironment.getApplication()
        repository = ServerRepository(context)
    }
    
    @Test
    fun testAddServer() {
        // 准备测试数据
        val server = ServerConfig(
            id = "test-server-1",
            name = "测试服务器",
            url = "https://test.emby.com",
            type = ServerType.VIDEO,
            username = "testuser",
            password = "testpass"
        )
        
        // 执行测试
        val result = repository.addServer(server)
        
        // 验证结果
        assertTrue(result, "添加服务器应该成功")
    }
    
    @Test
    fun testGetAllServers() {
        // 准备测试数据
        val server1 = ServerConfig(
            id = "test-server-1",
            name = "测试服务器 1",
            url = "https://test1.emby.com",
            type = ServerType.VIDEO,
            username = "user1",
            password = "pass1"
        )
        
        val server2 = ServerConfig(
            id = "test-server-2",
            name = "测试服务器 2",
            url = "https://test2.emby.com",
            type = ServerType.MUSIC,
            username = "user2",
            password = "pass2"
        )
        
        // 添加服务器
        repository.addServer(server1)
        repository.addServer(server2)
        
        // 获取所有服务器
        val servers = repository.getAllServers()
        
        // 验证结果
        assertNotNull(servers)
        assertTrue(servers.size >= 2, "应该至少有 2 个服务器")
    }
    
    @Test
    fun testPasswordEncryption() {
        // 准备测试数据
        val originalPassword = "testpassword123"
        val server = ServerConfig(
            id = "test-server-encrypt",
            name = "测试加密",
            url = "https://test.emby.com",
            type = ServerType.VIDEO,
            username = "testuser",
            password = originalPassword
        )
        
        // 添加服务器
        repository.addServer(server)
        
        // 获取服务器（密码应该被解密）
        val servers = repository.getAllServers()
        val savedServer = servers.find { it.id == server.id }
        
        // 验证密码被正确解密
        assertNotNull(savedServer)
        assertEquals(originalPassword, savedServer.password, "密码应该被正确解密")
    }
    
    @Test
    fun testUrlHttpsConversion() {
        // 准备测试数据（HTTP URL）
        val server = ServerConfig(
            id = "test-server-http",
            name = "测试 HTTP 转换",
            url = "http://test.emby.com",
            type = ServerType.VIDEO,
            username = "testuser",
            password = "testpass"
        )
        
        // 添加服务器
        repository.addServer(server)
        
        // 获取服务器（URL 应该被转换为 HTTPS）
        val servers = repository.getAllServers()
        val savedServer = servers.find { it.id == server.id }
        
        // 验证 URL 被转换为 HTTPS
        assertNotNull(savedServer)
        assertTrue(savedServer.url.startsWith("https://"), "URL 应该被转换为 HTTPS")
    }
    
    @Test
    fun testUpdateServer() {
        // 准备测试数据
        val server = ServerConfig(
            id = "test-server-update",
            name = "测试更新",
            url = "https://test.emby.com",
            type = ServerType.VIDEO,
            username = "testuser",
            password = "testpass"
        )
        
        // 添加服务器
        repository.addServer(server)
        
        // 更新服务器
        val updatedServer = server.copy(
            name = "更新后的服务器",
            updatedAt = System.currentTimeMillis()
        )
        
        val result = repository.updateServer(updatedServer)
        
        // 验证更新成功
        assertTrue(result, "更新服务器应该成功")
        
        // 获取更新后的服务器
        val servers = repository.getAllServers()
        val savedServer = servers.find { it.id == updatedServer.id }
        
        // 验证名称被更新
        assertNotNull(savedServer)
        assertEquals("更新后的服务器", savedServer.name, "服务器名称应该被更新")
    }
}
