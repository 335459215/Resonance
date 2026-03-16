package com.resonance.core

import android.content.Context
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.media3.common.util.UnstableApi
import com.resonance.core.performance.CpuUsageMonitor
import com.resonance.core.performance.FpsMonitor
import com.resonance.core.performance.FpsMonitor.FpsStatistics
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

@UnstableApi
class PerformanceOptimizer(private val context: Context) {
    private val executor: ScheduledExecutorService = Executors.newSingleThreadScheduledExecutor()
    private val mainHandler = Handler(Looper.getMainLooper())
    private val memoryThreshold = 80 * 1024 * 1024 // 80MB
    private val cpuThreshold1080p = 12 // 12%
    private val cpuThreshold4K = 20 // 20%
    private val cpuMonitor = CpuUsageMonitor()
    private val fpsMonitor = FpsMonitor()

    fun startMonitoring() {
        // 启动内存监控
        executor.scheduleAtFixedRate(
            { monitorMemory() },
            0,
            5000,
            TimeUnit.MILLISECONDS
        )

        // 启动CPU监控
        executor.scheduleAtFixedRate(
            { monitorCpu() },
            0,
            3000,
            TimeUnit.MILLISECONDS
        )

        // 启动帧率监控
        executor.scheduleAtFixedRate(
            { monitorFps() },
            0,
            2000,
            TimeUnit.MILLISECONDS
        )
    }

    fun stopMonitoring() {
        executor.shutdown()
    }

    private fun monitorMemory() {
        val currentMemory = getCurrentMemoryUsage()
        Log.d("Performance", "Current memory usage: ${currentMemory / (1024 * 1024)}MB")
        
        if (currentMemory > memoryThreshold) {
            // 内存超过阈值，进行内存回收
            CoroutineScope(Dispatchers.IO).launch {
                optimizeMemory()
            }
        }
    }

    private fun monitorCpu() {
        val cpuUsage = cpuMonitor.getCurrentCpuUsage()
        Log.d("Performance", """
            CPU Usage: ${cpuUsage.processCpu.toInt()}%
            System CPU: ${cpuUsage.totalCpu.toInt()}%
            Cores: ${cpuUsage.coreCount}
            Frequency: ${cpuUsage.frequency / 1000}MHz
            Temperature: ${cpuMonitor.getCpuTemperature()}°C
        """.trimIndent())
        
        // 根据播放分辨率调整 CPU 阈值
        val is4K = isPlaying4K()
        val threshold = if (is4K) cpuThreshold4K else cpuThreshold1080p
        
        if (cpuUsage.processCpu > threshold) {
            // CPU 超过阈值，进行优化
            CoroutineScope(Dispatchers.IO).launch {
                optimizeCpu(cpuUsage)
            }
        }
    }

    private fun monitorFps() {
        val currentFps = fpsMonitor.getCurrentFps()
        val stats = fpsMonitor.getFpsStatistics()
        
        Log.d("Performance", """
            Current FPS: $currentFps
            Average FPS: ${stats.avgFps.toInt()}
            Min FPS: ${stats.minFps}
            Max FPS: ${stats.maxFps}
            Jank Count: ${stats.jankCount}
            Jank Rate: ${stats.jankRate * 100}%
        """.trimIndent())
        
        if (currentFps < 59 && isPlaying4K()) {
            // 4K 播放帧率不足，进行优化
            CoroutineScope(Dispatchers.IO).launch {
                optimizeFps(currentFps, stats)
            }
        }
    }

    private fun getCurrentMemoryUsage(): Long {
        val runtime = Runtime.getRuntime()
        return runtime.totalMemory() - runtime.freeMemory()
    }

    private fun getCurrentFps(): Int {
        // 这里需要实现帧率检测逻辑
        // 可以通过Choreographer或SurfaceFlinger获取
        return 60 // 暂时返回默认值
    }

    private fun isPlaying4K(): Boolean {
        // 检测当前是否在播放4K内容
        return false // 暂时返回默认值
    }

    private fun optimizeMemory() {
        // 1. 清理缓存
        clearCache()
        
        // 2. 释放不必要的资源
        releaseUnusedResources()
        
        // 3. 强制GC
        System.gc()
    }

    private fun optimizeCpu(cpuUsage: CpuUsageMonitor.CpuUsage) {
        when {
            cpuUsage.processCpu > 80 -> {
                // 紧急优化：降低分辨率、减少后台任务
                Log.w("Performance", "紧急 CPU 优化：${cpuUsage.processCpu.toInt()}%")
                reduceResolution()
                reduceBackgroundTasks()
            }
            cpuUsage.processCpu > 60 -> {
                // 普通优化：调整解码模式
                Log.w("Performance", "普通 CPU 优化：${cpuUsage.processCpu.toInt()}%")
                adjustDecodingMode()
            }
            cpuUsage.frequency > 2000000 -> { // 2.0GHz
                // 高频优化：降低 CPU 频率
                Log.w("Performance", "高频 CPU 优化：${cpuUsage.frequency / 1000}MHz")
                throttleCpu()
            }
        }
    }

    private fun optimizeFps(currentFps: Int, stats: FpsStatistics) {
        when {
            currentFps < 30 -> {
                // 严重帧率下降：紧急优化
                Log.w("Performance", "紧急 FPS 优化：${currentFps}fps")
                enableHardwareAcceleration()
                reduceResolution()
                adjustBufferingStrategy()
            }
            currentFps < 45 -> {
                // 中度帧率下降：普通优化
                Log.w("Performance", "普通 FPS 优化：${currentFps}fps")
                optimizeRendering()
                reduceBackgroundTasks()
            }
            stats.jankRate > 0.1f -> {
                // 卡顿率高：优化渲染
                Log.w("Performance", "高卡顿率：${stats.jankRate * 100}%")
                optimizeRendering()
            }
        }
    }

    private fun clearCache() {
        // 清理图片缓存
        // 清理网络缓存
        // 清理临时文件
    }

    private fun releaseUnusedResources() {
        // 释放未使用的播放器实例
        // 释放未使用的网络连接
        // 释放未使用的图片资源
    }

    private fun reduceResolution() {
        // 根据设备性能动态调整播放分辨率
    }

    private fun adjustDecodingMode() {
        // 在高负载时切换到更节能的解码模式
    }

    private fun reduceBackgroundTasks() {
        // 暂停非必要的后台任务
    }

    private fun throttleCpu() {
        // 降低 CPU 频率（需要系统权限）
        // 这里只是一个占位符，实际需要 root 权限或系统签名
        Log.d("Performance", "CPU 节流：尝试降低频率")
    }

    private fun enableHardwareAcceleration() {
        // 确保硬件加速已开启
    }

    private fun adjustBufferingStrategy() {
        // 调整缓冲区大小和策略
    }

    private fun optimizeRendering() {
        // 优化渲染管线
        // 减少过度绘制
    }

    fun getDeviceCompatibilityInfo(): DeviceCompatibilityInfo {
        return DeviceCompatibilityInfo(
            deviceModel = Build.MODEL,
            androidVersion = Build.VERSION.RELEASE,
            isTv = isTvDevice(),
            hasHardwareDecode = hasHardwareDecode(),
            maxResolution = getMaxSupportedResolution(),
            recommendedPlayerType = getRecommendedPlayerType()
        )
    }

    private fun isTvDevice(): Boolean {
        return context.packageManager.hasSystemFeature("android.software.leanback")
    }

    private fun hasHardwareDecode(): Boolean {
        // 检测设备是否支持硬件解码
        return true
    }

    private fun getMaxSupportedResolution(): String {
        // 检测设备支持的最大分辨率
        return "4K"
    }

    private fun getRecommendedPlayerType(): PlayerManager.PlayerType {
        val deviceInfo = getDeviceCompatibilityInfo()
        return when {
            deviceInfo.isTv -> PlayerManager.PlayerType.MEDIA3
            deviceInfo.androidVersion.toDouble() >= 10.0 -> PlayerManager.PlayerType.MEDIA3
            else -> PlayerManager.PlayerType.IJK
        }
    }

    data class DeviceCompatibilityInfo(
        val deviceModel: String,
        val androidVersion: String,
        val isTv: Boolean,
        val hasHardwareDecode: Boolean,
        val maxResolution: String,
        val recommendedPlayerType: PlayerManager.PlayerType
    )

    companion object {
        @Volatile
        private var instance: PerformanceOptimizer? = null

        fun getInstance(context: Context): PerformanceOptimizer {
            return instance ?: synchronized(this) {
                instance ?: PerformanceOptimizer(context.applicationContext).also {
                    instance = it
                }
            }
        }
        
        /**
         * 帧率严重下降时的处理
         */
        fun onLowFps(fps: Int) {
            Log.e("Performance", "帧率严重下降：${fps}fps，触发紧急优化")
        }
        
        /**
         * 帧率中度下降时的处理
         */
        fun onMediumLowFps(fps: Int) {
            Log.w("Performance", "帧率下降：${fps}fps，触发警告")
        }
    }
}