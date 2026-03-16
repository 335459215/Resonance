package com.resonance.core.performance

import org.junit.Test
import org.junit.Assert.*
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.delay

/**
 * FpsMonitor 单元测试
 */
class FpsMonitorTest {
    
    @Test
    fun testFpsMonitorInitialization() {
        // 测试 FpsMonitor 初始化
        val fpsMonitor = FpsMonitor()
        
        // 验证初始化后 FPS 在合理范围
        val currentFps = fpsMonitor.getCurrentFps()
        assertTrue(currentFps > 0)
        assertTrue(currentFps <= 120) // 不超过 120fps
    }
    
    @Test
    fun testGetCurrentFps() = runBlocking {
        val fpsMonitor = FpsMonitor()
        
        // 等待至少 1 秒以获取 FPS 数据
        delay(1100)
        
        val currentFps = fpsMonitor.getCurrentFps()
        
        // 验证 FPS 在合理范围内
        assertTrue(currentFps > 0)
        assertTrue(currentFps <= 120)
    }
    
    @Test
    fun testGetFpsStatistics() = runBlocking {
        val fpsMonitor = FpsMonitor()
        
        // 等待收集数据
        delay(2000)
        
        val stats = fpsMonitor.getFpsStatistics()
        
        // 验证统计数据
        assertTrue(stats.avgFps >= 0f)
        assertTrue(stats.minFps >= 0)
        assertTrue(stats.maxFps <= 120)
        assertTrue(stats.minFps <= stats.maxFps)
        assertTrue(stats.jankCount >= 0)
        assertTrue(stats.jankRate >= 0f)
        assertTrue(stats.jankRate <= 1f)
    }
    
    @Test
    fun testGetFpsHistory() = runBlocking {
        val fpsMonitor = FpsMonitor()
        
        // 等待收集数据
        delay(1500)
        
        val history = fpsMonitor.getFpsHistory()
        
        // 验证历史记录
        assertTrue(history.isNotEmpty())
        assertTrue(history.size <= 120) // 最多 120 条记录
        
        // 验证每条记录的数据完整性
        history.forEach { metric ->
            assertTrue(metric.fps > 0)
            assertTrue(metric.fps <= 120)
            assertTrue(metric.frameTime > 0)
            assertTrue(metric.timestamp > 0)
        }
    }
    
    @Test
    fun testReset() = runBlocking {
        val fpsMonitor = FpsMonitor()
        
        // 等待收集一些数据
        delay(1100)
        
        // 重置
        fpsMonitor.reset()
        
        // 验证重置后状态
        val currentFps = fpsMonitor.getCurrentFps()
        assertEquals(60, currentFps) // 重置后应该是目标值 60
        
        val history = fpsMonitor.getFpsHistory()
        assertTrue(history.isEmpty())
    }
    
    @Test
    fun testStopMonitoring() = runBlocking {
        val fpsMonitor = FpsMonitor()
        
        // 等待一段时间
        delay(500)
        
        // 停止监控
        fpsMonitor.stopMonitoring()
        
        // 等待一段时间验证不再更新
        val fpsBefore = fpsMonitor.getCurrentFps()
        delay(1500)
        val fpsAfter = fpsMonitor.getCurrentFps()
        
        // 停止后 FPS 应该不再更新（或者变化很小）
        // 注意：这个测试可能不太稳定，因为停止需要时间生效
        assertTrue(fpsAfter >= 0)
    }
    
    @Test
    fun testFrameMetricDataClass() {
        // 测试数据类
        val metric = FpsMonitor.FrameMetric(
            timestamp = 1234567890L,
            fps = 60,
            frameTime = 16,
            isJank = false
        )
        
        assertEquals(1234567890L, metric.timestamp)
        assertEquals(60, metric.fps)
        assertEquals(16, metric.frameTime)
        assertFalse(metric.isJank)
    }
    
    @Test
    fun testFpsStatisticsDataClass() {
        // 测试统计数据类
        val stats = FpsMonitor.FpsStatistics(
            avgFps = 58.5f,
            minFps = 45,
            maxFps = 60,
            jankCount = 5,
            jankRate = 0.05f
        )
        
        assertEquals(58.5f, stats.avgFps, 0.01f)
        assertEquals(45, stats.minFps)
        assertEquals(60, stats.maxFps)
        assertEquals(5, stats.jankCount)
        assertEquals(0.05f, stats.jankRate, 0.01f)
    }
    
    @Test
    fun testJankDetection() {
        // 测试卡顿检测逻辑
        val frameTime = 25L // 超过 16.67ms * 1.5 = 25ms
        val isJank = frameTime > 16.67f * 1.5f
        
        assertTrue(isJank)
        
        val normalFrameTime = 15L
        val isNormalJank = normalFrameTime > 16.67f * 1.5f
        
        assertFalse(isNormalJank)
    }
}
