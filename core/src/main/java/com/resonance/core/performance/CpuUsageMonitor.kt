package com.resonance.core.performance

import android.os.Build
import java.io.File
import java.io.RandomAccessFile

/**
 * CPU 使用率监控器
 * 通过读取 /proc 文件系统计算进程 CPU 使用率
 */
class CpuUsageMonitor {
    
    companion object {
        private const val CPU_INFO_PATH = "/proc/stat"
        private const val PID_CPU_INFO_PATH = "/proc/self/stat"
    }
    
    private var lastCpuTime = 0L
    private var lastProcessCpuTime = 0L
    private var lastRealTime = 0L
    
    /**
     * CPU 使用率数据类
     */
    data class CpuUsage(
        val processCpu: Float,      // 进程 CPU 使用率
        val totalCpu: Float,        // 系统总 CPU 使用率
        val coreCount: Int,         // CPU 核心数
        val frequency: Long         // CPU 频率 (kHz)
    )
    
    /**
     * 获取当前 CPU 使用率
     */
    fun getCurrentCpuUsage(): CpuUsage {
        val currentRealTime = System.currentTimeMillis()
        
        try {
            // 读取系统 CPU 时间
            val cpuInfo = File(CPU_INFO_PATH).readText()
            val cpuParts = cpuInfo.lines().first().split("\\s+".toRegex())
            
            if (cpuParts.size < 5) {
                return CpuUsage(0f, 0f, Runtime.getRuntime().availableProcessors(), 0)
            }
            
            val idle = cpuParts[4].toLong()
            val total = cpuParts.drop(1).sumOf { it.toLong() }
            
            // 读取进程 CPU 时间
            val processCpuTime = getProcessCpuTime()
            
            // 计算差值
            if (lastCpuTime > 0 && lastProcessCpuTime > 0) {
                val cpuDiff = total - lastCpuTime
                val processDiff = processCpuTime - lastProcessCpuTime
                val realDiff = currentRealTime - lastRealTime
                
                if (cpuDiff > 0 && realDiff > 0) {
                    val processCpuPercent = (processDiff.toFloat() / cpuDiff * 100)
                        .coerceIn(0f, 100f)
                    val totalCpuPercent = ((total - idle).toFloat() / total * 100)
                        .coerceIn(0f, 100f)
                    
                    lastCpuTime = total
                    lastProcessCpuTime = processCpuTime
                    lastRealTime = currentRealTime
                    
                    return CpuUsage(
                        processCpu = processCpuPercent,
                        totalCpu = totalCpuPercent,
                        coreCount = Runtime.getRuntime().availableProcessors(),
                        frequency = getCpuFrequency()
                    )
                }
            }
            
            // 首次调用，记录初始值
            lastCpuTime = total
            lastProcessCpuTime = processCpuTime
            lastRealTime = currentRealTime
            
        } catch (e: Exception) {
            e.printStackTrace()
        }
        
        return CpuUsage(0f, 0f, Runtime.getRuntime().availableProcessors(), 0)
    }
    
    /**
     * 获取进程 CPU 时间
     */
    private fun getProcessCpuTime(): Long {
        return try {
            val reader = RandomAccessFile(File(PID_CPU_INFO_PATH), "r")
            val line = reader.readLine()
            reader.close()
            
            val parts = line.split("\\s+".toRegex())
            if (parts.size < 15) {
                return 0L
            }
            
            // utime (14) + stime (15) - 用户态 + 内核态 CPU 时间
            val utime = parts[13].toLong()
            val stime = parts[14].toLong()
            utime + stime
        } catch (e: Exception) {
            e.printStackTrace()
            0L
        }
    }
    
    /**
     * 获取 CPU 频率
     */
    private fun getCpuFrequency(): Long {
        return try {
            val freqFile = File("/sys/devices/system/cpu/cpu0/cpufreq/scaling_cur_freq")
            if (freqFile.exists()) {
                freqFile.readText().trim().toLong() / 1000 // 转换为 kHz
            } else {
                0
            }
        } catch (e: Exception) {
            e.printStackTrace()
            0
        }
    }
    
    /**
     * 获取 CPU 温度（如果支持）
     */
    fun getCpuTemperature(): Float {
        return try {
            val tempFile = File("/sys/class/thermal/thermal_zone0/temp")
            if (tempFile.exists()) {
                tempFile.readText().trim().toFloat() / 1000 // 转换为摄氏度
            } else {
                0f
            }
        } catch (e: Exception) {
            e.printStackTrace()
            0f
        }
    }
    
    /**
     * 重置监控数据
     */
    fun reset() {
        lastCpuTime = 0
        lastProcessCpuTime = 0
        lastRealTime = 0
    }
}
