package com.resonance.core.performance

import org.junit.Test
import org.junit.Assert.*
import org.junit.Before

/**
 * CpuUsageMonitor 单元测试
 */
class CpuUsageMonitorTest {
    
    private lateinit var cpuMonitor: CpuUsageMonitor
    
    @Before
    fun setUp() {
        cpuMonitor = CpuUsageMonitor()
    }
    
    @Test
    fun testGetCurrentCpuUsage() {
        // 测试 CPU 使用率获取
        val cpuUsage = cpuMonitor.getCurrentCpuUsage()
        
        // 验证返回值不为 null
        assertNotNull(cpuUsage)
        
        // 验证 CPU 使用率在合理范围内
        assertTrue(cpuUsage.processCpu >= 0f)
        assertTrue(cpuUsage.processCpu <= 100f)
        assertTrue(cpuUsage.totalCpu >= 0f)
        assertTrue(cpuUsage.totalCpu <= 100f)
        
        // 验证 CPU 核心数
        assertTrue(cpuUsage.coreCount > 0)
    }
    
    @Test
    fun testCpuUsageDataClass() {
        // 测试数据类
        val cpuUsage = CpuUsageMonitor.CpuUsage(
            processCpu = 25.5f,
            totalCpu = 35.2f,
            coreCount = 8,
            frequency = 1804000
        )
        
        assertEquals(25.5f, cpuUsage.processCpu, 0.01f)
        assertEquals(35.2f, cpuUsage.totalCpu, 0.01f)
        assertEquals(8, cpuUsage.coreCount)
        assertEquals(1804000, cpuUsage.frequency)
    }
    
    @Test
    fun testGetCpuTemperature() {
        // 测试 CPU 温度获取（可能返回 0 如果设备不支持）
        val temperature = cpuMonitor.getCpuTemperature()
        
        // 温度应该在合理范围内（-50 到 150 度）
        assertTrue(temperature >= -50f)
        assertTrue(temperature <= 150f)
    }
    
    @Test
    fun testReset() {
        // 测试重置功能
        cpuMonitor.reset()
        
        // 重置后再次获取 CPU 使用率应该正常工作
        val cpuUsage = cpuMonitor.getCurrentCpuUsage()
        assertNotNull(cpuUsage)
    }
    
    @Test
    fun testMultipleReadings() {
        // 测试多次读取的一致性
        val readings = mutableListOf<CpuUsageMonitor.CpuUsage>()
        
        repeat(5) {
            readings.add(cpuMonitor.getCurrentCpuUsage())
            Thread.sleep(100)
        }
        
        // 验证所有读数都在合理范围内
        readings.forEach { reading ->
            assertTrue(reading.processCpu >= 0f)
            assertTrue(reading.processCpu <= 100f)
        }
    }
    
    @Test
    fun testConcurrentAccess() {
        // 测试并发访问
        val threads = mutableListOf<Thread>()
        val results = mutableListOf<CpuUsageMonitor.CpuUsage>()
        
        repeat(5) {
            val thread = Thread {
                synchronized(results) {
                    results.add(cpuMonitor.getCurrentCpuUsage())
                }
            }
            threads.add(thread)
            thread.start()
        }
        
        threads.forEach { it.join() }
        
        // 验证所有线程都成功获取了数据
        assertEquals(5, results.size)
    }
}
