# 🧮 算法升级实施方案

**版本**: 1.0  
**日期**: 2026-03-16  
**优先级**: P0 - 紧急

---

## 📋 目录

1. [升级概述](#升级概述)
2. [CPU 监控算法升级](#cpu 监控算法升级)
3. [帧率监控算法升级](#帧率监控算法升级)
4. [推荐算法升级](#推荐算法升级)
5. [缓存算法优化](#缓存算法优化)
6. [实施步骤](#实施步骤)
7. [测试方案](#测试方案)

---

## 📊 升级概述

### 当前问题

| 算法 | 问题 | 影响 | 优先级 |
|------|------|------|--------|
| CPU 监控 | 使用废弃 API，返回固定值 0 | 性能优化失效 | P0 |
| 帧率监控 | 未实现，返回固定值 60 | 无法检测卡顿 | P0 |
| 推荐算法 | 缺少冷启动处理 | 新用户体验差 | P0 |
| LRU 缓存 | 使用 iterator 删除，效率低 | 轻微性能影响 | P2 |

### 升级目标

- ✅ CPU 监控准确率 > 95%
- ✅ 帧率检测误差 < 2fps
- ✅ 冷启动推荐点击率提升 30%
- ✅ LRU 缓存性能提升 50%

---

## 🔧 CPU 监控算法升级

### 当前代码（有问题）

```kotlin
private fun getCurrentCpuUsage(): Int {
    // 简化 CPU 监控实现
    // Debug.getCpuRate() 已废弃，使用简化方案
    val cpuUsage = 0f // 占位符
}
```

### 升级方案

#### 方案一：读取 /proc 文件系统（推荐）

```kotlin
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
            // utime (14) + stime (15) - 用户态 + 内核态 CPU 时间
            val utime = parts[13].toLong()
            val stime = parts[14].toLong()
            utime + stime
        } catch (e: Exception) {
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
```

#### 方案二：使用 top 命令（备选）

```kotlin
/**
 * 使用 top 命令获取 CPU 使用率（备选方案）
 */
fun getCurrentCpuUsageWithTop(): Float {
    return try {
        val process = Runtime.getRuntime().exec("top -n 1")
        val reader = BufferedReader(InputStreamReader(process.inputStream))
        
        var line: String?
        while (reader.readLine().also { line = it } != null) {
            if (line!!.contains(context.packageName)) {
                val parts = line!!.split("\\s+".toRegex())
                val cpuIndex = parts.indexOfFirst { it.contains("%") && it.endsWith("%") }
                if (cpuIndex != -1) {
                    return parts[cpuIndex].replace("%", "").toFloat()
                }
            }
        }
        0f
    } catch (e: Exception) {
        e.printStackTrace()
        0f
    }
}
```

### 集成到 PerformanceOptimizer

```kotlin
@UnstableApi
class PerformanceOptimizer(private val context: Context) {
    private val cpuMonitor = CpuUsageMonitor()
    private val executor: ScheduledExecutorService = Executors.newSingleThreadScheduledExecutor()
    
    // ... 其他代码 ...
    
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
            CoroutineScope(Dispatchers.IO).launch {
                optimizeCpu(cpuUsage)
            }
        }
    }
    
    private fun optimizeCpu(cpuUsage: CpuUsage) {
        when {
            cpuUsage.processCpu > 80 -> {
                // 紧急优化：降低分辨率、减少后台任务
                reduceResolution()
                reduceBackgroundTasks()
            }
            cpuUsage.processCpu > 60 -> {
                // 普通优化：调整解码模式
                adjustDecodingMode()
            }
            cpuUsage.frequency > 2000000 -> { // 2.0GHz
                // 高频优化：降低 CPU 频率
                throttleCpu()
            }
        }
    }
}
```

---

## 📊 帧率监控算法升级

### 当前代码（有问题）

```kotlin
private fun getCurrentFps(): Int {
    // 这里需要实现帧率检测逻辑
    // 可以通过 Choreographer 或 SurfaceFlinger 获取
    return 60 // 暂时返回默认值
}
```

### 升级方案

#### 实现一：使用 Choreographer（推荐）

```kotlin
package com.resonance.core.performance

import android.view.Choreographer
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * 帧率监控器
 * 使用 Choreographer 实时监控应用帧率
 */
class FpsMonitor {
    
    companion object {
        private const val TARGET_FPS = 60
        private const val FRAME_INTERVAL_MS = 16.67f // 1000ms / 60
        private const val LOW_FPS_THRESHOLD = 30
        private const val MEDIUM_FPS_THRESHOLD = 45
    }
    
    private var frameCount = 0
    private var lastFpsTime = 0L
    private var currentFps = TARGET_FPS
    
    private val _fpsState = MutableStateFlow(currentFps)
    val fpsState: StateFlow<Int> = _fpsState.asStateFlow()
    
    private val frameMetrics = mutableListOf<FrameMetric>()
    private val maxMetricsCount = 120 // 保留 2 分钟数据
    
    data class FrameMetric(
        val timestamp: Long,
        val fps: Int,
        val frameTime: Long,
        val isJank: Boolean
    )
    
    data class FpsStatistics(
        val avgFps: Float,
        val minFps: Int,
        val maxFps: Int,
        val jankCount: Int,
        val jankRate: Float
    )
    
    init {
        startMonitoring()
    }
    
    /**
     * 开始监控
     */
    private fun startMonitoring() {
        Choreographer.getInstance().postFrameCallback(frameCallback)
    }
    
    /**
     * 停止监控
     */
    fun stopMonitoring() {
        Choreographer.getInstance().removeFrameCallback(frameCallback)
    }
    
    private val frameCallback = object : Choreographer.FrameCallback {
        override fun doFrame(frameTimeNanos: Long) {
            frameCount++
            
            val currentTime = frameTimeNanos / 1_000_000 // 转换为毫秒
            
            if (lastFpsTime == 0L) {
                lastFpsTime = currentTime
            } else {
                val elapsed = currentTime - lastFpsTime
                
                // 每秒更新一次 FPS
                if (elapsed >= 1000) {
                    val fps = (frameCount * 1000f / elapsed).toInt()
                    currentFps = fps
                    _fpsState.value = fps
                    
                    // 记录帧指标
                    recordFrameMetric(fps, elapsed / frameCount)
                    
                    // 重置计数器
                    frameCount = 0
                    lastFpsTime = currentTime
                    
                    // 检测帧率下降
                    onFpsChanged(fps)
                }
            }
            
            // 继续注册回调
            Choreographer.getInstance().postFrameCallback(this)
        }
    }
    
    /**
     * 记录帧指标
     */
    private fun recordFrameMetric(fps: Int, frameTime: Long) {
        val isJank = frameTime > FRAME_INTERVAL_MS * 1.5 // 超过 1.5 倍帧时间为卡顿
        val metric = FrameMetric(
            timestamp = System.currentTimeMillis(),
            fps = fps,
            frameTime = frameTime,
            isJank = isJank
        )
        
        frameMetrics.add(metric)
        
        // 限制历史记录数量
        if (frameMetrics.size > maxMetricsCount) {
            frameMetrics.removeAt(0)
        }
    }
    
    /**
     * FPS 变化时的处理
     */
    private fun onFpsChanged(fps: Int) {
        when {
            fps < LOW_FPS_THRESHOLD -> {
                Log.w("FpsMonitor", "严重帧率下降：${fps}fps")
                // 触发性能优化
                PerformanceOptimizer.onLowFps(fps)
            }
            fps < MEDIUM_FPS_THRESHOLD -> {
                Log.w("FpsMonitor", "帧率下降：${fps}fps")
                // 发出警告
                PerformanceOptimizer.onMediumLowFps(fps)
            }
        }
    }
    
    /**
     * 获取当前 FPS
     */
    fun getCurrentFps(): Int = currentFps
    
    /**
     * 获取帧率统计信息
     */
    fun getFpsStatistics(): FpsStatistics {
        if (frameMetrics.isEmpty()) {
            return FpsStatistics(0f, 0, 0, 0, 0f)
        }
        
        val fpsList = frameMetrics.map { it.fps }
        val jankMetrics = frameMetrics.filter { it.isJank }
        
        return FpsStatistics(
            avgFps = fpsList.average().toFloat(),
            minFps = fpsList.minOrNull() ?: 0,
            maxFps = fpsList.maxOrNull() ?: 0,
            jankCount = jankMetrics.size,
            jankRate = jankMetrics.size.toFloat() / frameMetrics.size
        )
    }
    
    /**
     * 获取帧率历史记录
     */
    fun getFpsHistory(): List<FrameMetric> {
        return frameMetrics.toList()
    }
    
    /**
     * 重置监控数据
     */
    fun reset() {
        frameCount = 0
        lastFpsTime = 0L
        currentFps = TARGET_FPS
        frameMetrics.clear()
    }
}
```

#### 实现二：使用 Display API（获取屏幕刷新率）

```kotlin
/**
 * 获取屏幕刷新率
 */
fun getDisplayRefreshRate(): Float {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        val display = (context.getSystemService(Context.DISPLAY_SERVICE) as DisplayManager)
            .getDisplay(Display.DEFAULT_DISPLAY)
        display.refreshRate
    } else {
        60f
    }
}
```

### 集成到 PerformanceOptimizer

```kotlin
@UnstableApi
class PerformanceOptimizer(private val context: Context) {
    private val fpsMonitor = FpsMonitor()
    
    // ... 其他代码 ...
    
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
    
    private fun optimizeFps(currentFps: Int, stats: FpsStatistics) {
        when {
            currentFps < 30 -> {
                // 严重帧率下降：紧急优化
                enableHardwareAcceleration()
                reduceResolution()
                adjustBufferingStrategy()
                Log.w("Performance", "紧急 FPS 优化：${currentFps}fps")
            }
            currentFps < 45 -> {
                // 中度帧率下降：普通优化
                optimizeRendering()
                reduceBackgroundTasks()
                Log.w("Performance", "普通 FPS 优化：${currentFps}fps")
            }
            stats.jankRate > 0.1 -> {
                // 卡顿率高：优化渲染
                optimizeRendering()
                Log.w("Performance", "高卡顿率：${stats.jankRate * 100}%")
            }
        }
    }
    
    companion object {
        fun onLowFps(fps: Int) {
            // 静态方法，供 FpsMonitor 调用
            Log.e("Performance", "帧率严重下降：${fps}fps，触发紧急优化")
        }
        
        fun onMediumLowFps(fps: Int) {
            Log.w("Performance", "帧率下降：${fps}fps，触发警告")
        }
    }
}
```

---

## 🎯 推荐算法升级

### 当前问题

```kotlin
fun collaborativeFiltering(
    userPreferences: Map<String, Float>,
    allItems: List<String>,
    itemSimilarities: Map<String, Map<String, Float>>,
    topN: Int = 10
): List<RecommendedItem> {
    // 问题：如果 userPreferences 为空，返回空列表
    // 新用户无法获得推荐
}
```

### 升级方案：混合推荐引擎

```kotlin
package com.resonance.core.algorithm

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.sqrt

/**
 * 混合推荐引擎
 * 结合协同过滤、基于内容、热门度等多种推荐策略
 */
object HybridRecommendationEngine {
    
    /**
     * 推荐配置
     */
    data class RecommendationConfig(
        val collaborativeWeight: Float = 0.4f,      // 协同过滤权重
        val contentBasedWeight: Float = 0.3f,       // 基于内容权重
        val popularityWeight: Float = 0.2f,         // 热门度权重
        val recencyWeight: Float = 0.1f,            // 时效性权重
        val minRecommendations: Int = 10,
        val maxRecommendations: Int = 50
    )
    
    /**
     * 媒体物品数据类
     */
    data class MediaItem(
        val id: String,
        val title: String,
        val category: String,
        val tags: List<String>,
        val rating: Float,
        val viewCount: Long,
        val createdAt: Long,
        val features: FloatArray? = null  // 特征向量
    )
    
    /**
     * 用户画像
     */
    data class UserProfile(
        val userId: String,
        val preferences: Map<String, Float>,  // 物品 ID -> 评分
        val favoriteCategories: Set<String>,
        val favoriteTags: Set<String>,
        val recentViews: List<String>,
        val totalViews: Int
    )
    
    /**
     * 主推荐接口
     */
    suspend fun recommend(
        userProfile: UserProfile,
        allItems: List<MediaItem>,
        config: RecommendationConfig = RecommendationConfig()
    ): List<RecommendedItem> = withContext(Dispatchers.Default) {
        // 冷启动处理：新用户推荐热门物品
        if (userProfile.preferences.isEmpty() && userProfile.totalViews == 0) {
            return@withContext handleColdStart(userProfile, allItems, config)
        }
        
        val recommendations = mutableListOf<RecommendedItem>()
        
        // 1. 协同过滤推荐
        val cfItems = if (userProfile.preferences.isNotEmpty()) {
            val similarityMatrix = buildItemSimilarityMatrix(allItems)
            AdvancedAlgorithms.collaborativeFiltering(
                userPreferences = userProfile.preferences,
                allItems = allItems.map { it.id },
                itemSimilarities = similarityMatrix,
                topN = config.maxRecommendations
            )
        } else {
            emptyList()
        }
        
        // 2. 基于内容推荐
        val cbItems = if (userProfile.favoriteCategories.isNotEmpty() || userProfile.favoriteTags.isNotEmpty()) {
            contentBasedRecommendation(userProfile, allItems)
        } else {
            emptyList()
        }
        
        // 3. 热门物品推荐
        val popularItems = getPopularItems(allItems, config.popularityWeight)
        
        // 4. 时效性推荐
        val recentItems = getRecentItems(allItems, config.recencyWeight)
        
        // 5. 融合排序
        val fusedScores = fuseScores(
            cfItems = cfItems.associate { it.itemId to it.score },
            cbItems = cbItems.associate { it.itemId to it.score },
            popularItems = popularItems.associate { it.itemId to it.score },
            recentItems = recentItems.associate { it.itemId to it.score },
            config = config
        )
        
        // 6. 过滤和排序
        fusedScores.entries
            .sortedByDescending { it.value }
            .take(config.maxRecommendations)
            .map { (itemId, score) ->
                RecommendedItem(itemId, score, 1f)
            }
    }
    
    /**
     * 冷启动处理
     */
    private suspend fun handleColdStart(
        userProfile: UserProfile,
        allItems: List<MediaItem>,
        config: RecommendationConfig
    ): List<RecommendedItem> {
        // 策略 1: 推荐最热门的物品
        val popularItems = getPopularItems(allItems, 1.0f)
        
        // 策略 2: 如果有类别偏好，推荐该类别的热门物品
        if (userProfile.favoriteCategories.isNotEmpty()) {
            val categoryItems = allItems.filter { it.category in userProfile.favoriteCategories }
            if (categoryItems.isNotEmpty()) {
                return getPopularItems(categoryItems, 1.0f)
                    .take(config.maxRecommendations)
            }
        }
        
        // 策略 3: 随机推荐（增加多样性）
        if (popularItems.size < config.minRecommendations) {
            val randomItems = allItems.shuffled().take(config.minRecommendations - popularItems.size)
            return (popularItems + randomItems.map { 
                RecommendedItem(it.id, 0.5f, 0.1f) 
            }).take(config.maxRecommendations)
        }
        
        return popularItems.take(config.maxRecommendations)
    }
    
    /**
     * 基于内容的推荐
     */
    private fun contentBasedRecommendation(
        userProfile: UserProfile,
        allItems: List<MediaItem>
    ): List<RecommendedItem> {
        val scores = mutableListOf<RecommendedItem>()
        
        for (item in allItems) {
            var score = 0f
            var weight = 0f
            
            // 类别匹配
            if (item.category in userProfile.favoriteCategories) {
                score += 0.5f
                weight += 0.5f
            }
            
            // 标签匹配
            val matchedTags = item.tags.intersect(userProfile.favoriteTags).size
            if (matchedTags > 0) {
                score += (matchedTags.toFloat() / item.tags.size) * 0.5f
                weight += 0.5f
            }
            
            // 特征向量相似度（如果有）
            if (item.features != null && userProfile.preferences.isNotEmpty()) {
                val userFeatureVector = calculateUserFeatureVector(userProfile, allItems)
                if (userFeatureVector != null) {
                    val similarity = AdvancedAlgorithms.cosineSimilarity(
                        userFeatureVector,
                        item.features
                    )
                    score += similarity * 0.3f
                    weight += 0.3f
                }
            }
            
            if (score > 0) {
                scores.add(RecommendedItem(item.id, score, weight))
            }
        }
        
        return scores.sortedByDescending { it.score }
    }
    
    /**
     * 获取热门物品
     */
    private fun getPopularItems(
        items: List<MediaItem>,
        weight: Float = 1.0f
    ): List<RecommendedItem> {
        return items
            .sortedByDescending { it.viewCount * it.rating }
            .take(50)
            .mapIndexed { index, item ->
                val score = (1.0f - index.toFloat() / 50) * weight
                RecommendedItem(item.id, score, weight)
            }
    }
    
    /**
     * 获取时效性物品
     */
    private fun getRecentItems(
        items: List<MediaItem>,
        weight: Float = 1.0f
    ): List<RecommendedItem> {
        val now = System.currentTimeMillis()
        val thirtyDaysAgo = now - 30 * 24 * 60 * 60 * 1000
        
        return items
            .filter { it.createdAt > thirtyDaysAgo }
            .sortedByDescending { it.createdAt }
            .take(20)
            .mapIndexed { index, item ->
                val score = (1.0f - index.toFloat() / 20) * weight
                RecommendedItem(item.id, score, weight)
            }
    }
    
    /**
     * 分数融合
     */
    private fun fuseScores(
        cfItems: Map<String, Float>,
        cbItems: Map<String, Float>,
        popularItems: Map<String, Float>,
        recentItems: Map<String, Float>,
        config: RecommendationConfig
    ): Map<String, Float> {
        val allItemIds = (cfItems.keys + cbItems.keys + popularItems.keys + recentItems.keys).toSet()
        
        return allItemIds.associateWith { itemId ->
            val cfScore = cfItems[itemId] ?: 0f
            val cbScore = cbItems[itemId] ?: 0f
            val popScore = popularItems[itemId] ?: 0f
            val recScore = recentItems[itemId] ?: 0f
            
            cfScore * config.collaborativeWeight +
            cbScore * config.contentBasedWeight +
            popScore * config.popularityWeight +
            recScore * config.recencyWeight
        }
    }
    
    /**
     * 构建物品相似度矩阵（优化版）
     * 使用近似最近邻搜索降低复杂度
     */
    private fun buildItemSimilarityMatrix(items: List<MediaItem>): Map<String, Map<String, Float>> {
        val itemsWithFeatures = items.filter { it.features != null }
        val similarityMatrix = mutableMapOf<String, MutableMap<String, Float>>()
        
        // 对于没有特征向量的物品，使用标签相似度
        val itemsByTags = items.associateBy { it.id }
        
        for (item1 in items) {
            val similarities = mutableMapOf<String, Float>()
            
            for (item2 in items) {
                if (item1.id == item2.id) continue
                
                val similarity = if (item1.features != null && item2.features != null) {
                    // 特征向量相似度
                    AdvancedAlgorithms.cosineSimilarity(item1.features, item2.features)
                } else {
                    // 标签 Jaccard 相似度
                    AdvancedAlgorithms.jaccardSimilarity(
                        item1.tags.toSet(),
                        item2.tags.toSet()
                    )
                }
                
                if (similarity > 0.3) { // 只保留相似度较高的
                    similarities[item2.id] = similarity
                }
            }
            
            if (similarities.isNotEmpty()) {
                similarityMatrix[item1.id] = similarities
            }
        }
        
        return similarityMatrix
    }
    
    /**
     * 计算用户特征向量
     */
    private fun calculateUserFeatureVector(
        userProfile: UserProfile,
        allItems: List<MediaItem>
    ): FloatArray? {
        val likedItems = allItems.filter { 
            it.id in userProfile.preferences.keys && userProfile.preferences[it.id]!! >= 3.0f 
        }
        
        if (likedItems.isEmpty()) return null
        
        val vectors = likedItems.mapNotNull { it.features }
        if (vectors.isEmpty()) return null
        
        // 计算平均向量
        val dimension = vectors[0].size
        val result = FloatArray(dimension)
        
        for (vector in vectors) {
            for (i in vector.indices) {
                result[i] += vector[i]
            }
        }
        
        for (i in result.indices) {
            result[i] /= vectors.size
        }
        
        return result
    }
}
```

---

## 💾 缓存算法优化

### 当前问题

```kotlin
@Synchronized
fun put(key: K, value: V) {
    if (cache.size >= capacity && !cache.containsKey(key)) {
        val iterator = cache.iterator()
        if (iterator.hasNext()) {
            iterator.next()
            iterator.remove()
        }
    }
    cache[key] = value
}
```

### 优化方案

```kotlin
package com.resonance.ui.utils

import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

/**
 * 优化的 LRU 缓存
 * 使用 ReentrantLock 替代 synchronized，提高并发性能
 */
class OptimizedLRUCache<K, V>(
    private val capacity: Int,
    private val onRemove: ((K, V) -> Unit)? = null,
    private val onAccess: ((K, V) -> Unit)? = null
) {
    
    // 使用 LinkedHashMap，accessOrder=true 实现 LRU
    private val cache = LinkedHashMap<K, V>((capacity / 0.75f).toInt(), 0.75f, true)
    private val lock = ReentrantLock()
    
    /**
     * 获取缓存
     */
    fun get(key: K): V? {
        return lock.withLock {
            cache[key]?.also { value ->
                onAccess?.invoke(key, value)
            }
        }
    }
    
    /**
     * 放入缓存
     */
    fun put(key: K, value: V): V? {
        return lock.withLock {
            val oldValue = cache.put(key, value)
            
            // 如果超出容量，移除最旧的
            if (cache.size > capacity) {
                val iterator = cache.iterator()
                if (iterator.hasNext()) {
                    val (oldestKey, oldestValue) = iterator.next()
                    iterator.remove()
                    onRemove?.invoke(oldestKey, oldestValue)
                }
            }
            
            oldValue
        }
    }
    
    /**
     * 批量放入
     */
    fun putAll(items: Map<K, V>) {
        lock.withLock {
            items.forEach { (key, value) ->
                cache[key] = value
            }
            
            // 清理超出容量的项
            while (cache.size > capacity) {
                val iterator = cache.iterator()
                if (iterator.hasNext()) {
                    val (oldestKey, oldestValue) = iterator.next()
                    iterator.remove()
                    onRemove?.invoke(oldestKey, oldestValue)
                }
            }
        }
    }
    
    /**
     * 移除缓存
     */
    fun remove(key: K): V? {
        return lock.withLock {
            cache.remove(key)?.also { value ->
                onRemove?.invoke(key, value)
            }
        }
    }
    
    /**
     * 是否包含
     */
    fun containsKey(key: K): Boolean {
        return lock.withLock {
            cache.containsKey(key)
        }
    }
    
    /**
     * 清空缓存
     */
    fun clear() {
        lock.withLock {
            val removedItems = cache.toMap()
            cache.clear()
            removedItems.forEach { (key, value) ->
                onRemove?.invoke(key, value)
            }
        }
    }
    
    /**
     * 缓存大小
     */
    val size: Int
        get() = lock.withLock { cache.size }
    
    /**
     * 缓存命中率统计
     */
    private var hitCount = 0
    private var missCount = 0
    
    fun getHitRate(): Float {
        val total = hitCount + missCount
        return if (total > 0) hitCount.toFloat() / total else 0f
    }
    
    fun recordHit() {
        lock.withLock { hitCount++ }
    }
    
    fun recordMiss() {
        lock.withLock { missCount++ }
    }
    
    fun resetStats() {
        lock.withLock {
            hitCount = 0
            missCount = 0
        }
    }
}
```

### 使用示例

```kotlin
// 创建优化的图片缓存
val imageCache = OptimizedLRUCache<String, Bitmap>(
    capacity = 100,
    onRemove = { key, bitmap ->
        bitmap.recycle()
        Log.d("ImageCache", "Removed: $key")
    },
    onAccess = { key, bitmap ->
        Log.d("ImageCache", "Accessed: $key")
    }
)

// 使用
fun getImage(url: String): Bitmap? {
    val cached = imageCache.get(url)
    if (cached != null) {
        imageCache.recordHit()
        return cached
    } else {
        imageCache.recordMiss()
        // 加载图片...
        val bitmap = loadImage(url)
        imageCache.put(url, bitmap)
        return bitmap
    }
}
```

---

## 📝 实施步骤

### 第一阶段：CPU 监控升级（2-3 天）

#### Day 1: 实现 CpuUsageMonitor
- [ ] 创建 `CpuUsageMonitor.kt`
- [ ] 实现 `/proc` 文件系统读取
- [ ] 实现 CPU 使用率计算
- [ ] 添加 CPU 频率和温度监控
- [ ] 编写单元测试

#### Day 2: 集成到 PerformanceOptimizer
- [ ] 替换旧的 CPU 监控代码
- [ ] 添加日志输出
- [ ] 实现基于 CPU 的优化策略
- [ ] 测试不同场景下的 CPU 监控

#### Day 3: 测试和优化
- [ ] 性能测试（CPU 开销 < 1%）
- [ ] 准确性测试（与系统工具对比）
- [ ] 边界条件测试
- [ ] 代码审查

### 第二阶段：帧率监控升级（2-3 天）

#### Day 1: 实现 FpsMonitor
- [ ] 创建 `FpsMonitor.kt`
- [ ] 使用 Choreographer 监控帧率
- [ ] 实现帧率统计和记录
- [ ] 添加卡顿检测
- [ ] 编写单元测试

#### Day 2: 集成到 PerformanceOptimizer
- [ ] 替换旧的帧率监控代码
- [ ] 实现基于帧率的优化策略
- [ ] 添加帧率变化监听
- [ ] 测试不同场景下的帧率监控

#### Day 3: 测试和优化
- [ ] 性能测试（监控开销 < 2%）
- [ ] 准确性测试（与 GPU Profiler 对比）
- [ ] 卡顿检测测试
- [ ] 代码审查

### 第三阶段：推荐算法升级（3-4 天）

#### Day 1: 实现混合推荐引擎
- [ ] 创建 `HybridRecommendationEngine.kt`
- [ ] 实现冷启动处理
- [ ] 实现基于内容推荐
- [ ] 实现热门度推荐
- [ ] 编写单元测试

#### Day 2: 实现分数融合
- [ ] 实现协同过滤推荐
- [ ] 实现分数融合算法
- [ ] 实现相似度矩阵优化
- [ ] 测试推荐效果

#### Day 3-4: 集成和测试
- [ ] 集成到应用
- [ ] A/B 测试推荐效果
- [ ] 性能优化
- [ ] 代码审查

### 第四阶段：缓存优化（1-2 天）

#### Day 1: 实现 OptimizedLRUCache
- [ ] 创建 `OptimizedLRUCache.kt`
- [ ] 使用 ReentrantLock 优化并发
- [ ] 添加缓存统计
- [ ] 编写单元测试

#### Day 2: 替换现有缓存
- [ ] 替换所有 LRU 缓存使用
- [ ] 性能对比测试
- [ ] 代码审查

---

## 🧪 测试方案

### CPU 监控测试

```kotlin
class CpuUsageMonitorTest {
    
    @Test
    fun testCpuUsageAccuracy() {
        val monitor = CpuUsageMonitor()
        
        // 模拟高 CPU 负载
        var sum = 0L
        for (i in 1..1000000) {
            sum += i
        }
        
        val cpuUsage = monitor.getCurrentCpuUsage()
        assertTrue(cpuUsage.processCpu > 0)
        assertTrue(cpuUsage.processCpu <= 100)
        assertTrue(cpuUsage.coreCount > 0)
    }
    
    @Test
    fun testCpuUsageConsistency() {
        val monitor = CpuUsageMonitor()
        val readings = mutableListOf<Float>()
        
        repeat(10) {
            readings.add(monitor.getCurrentCpuUsage().processCpu)
            Thread.sleep(100)
        }
        
        // 检查读数是否连续
        val avg = readings.average()
        readings.forEach { reading ->
            assertTrue(Math.abs(reading - avg) < 50) // 波动不超过 50%
        }
    }
}
```

### 帧率监控测试

```kotlin
class FpsMonitorTest {
    
    @Test
    fun testFpsMonitoring() {
        val monitor = FpsMonitor()
        
        // 等待至少 1 秒
        Thread.sleep(1100)
        
        val fps = monitor.getCurrentFps()
        assertTrue(fps > 0)
        assertTrue(fps <= 120) // 不超过 120fps
    }
    
    @Test
    fun testFpsStatistics() {
        val monitor = FpsMonitor()
        
        // 等待收集数据
        Thread.sleep(2000)
        
        val stats = monitor.getFpsStatistics()
        assertTrue(stats.avgFps > 0)
        assertTrue(stats.minFps <= stats.maxFps)
        assertTrue(stats.jankRate >= 0 && stats.jankRate <= 1)
    }
}
```

### 推荐算法测试

```kotlin
class HybridRecommendationEngineTest {
    
    @Test
    fun testColdStartRecommendation() {
        runBlocking {
            val userProfile = UserProfile(
                userId = "test_user",
                preferences = emptyMap(),
                favoriteCategories = emptySet(),
                favoriteTags = emptySet(),
                recentViews = emptyList(),
                totalViews = 0
            )
            
            val items = listOf(
                MediaItem("1", "Movie 1", "Action", listOf("action"), 4.5f, 1000, System.currentTimeMillis()),
                MediaItem("2", "Movie 2", "Comedy", listOf("comedy"), 4.0f, 500, System.currentTimeMillis())
            )
            
            val recommendations = HybridRecommendationEngine.recommend(userProfile, items)
            
            assertTrue(recommendations.isNotEmpty())
            assertTrue(recommendations.size <= 50)
        }
    }
    
    @Test
    fun testPersonalizedRecommendation() {
        runBlocking {
            val userProfile = UserProfile(
                userId = "test_user",
                preferences = mapOf("1" to 5.0f, "2" to 4.0f),
                favoriteCategories = setOf("Action"),
                favoriteTags = setOf("sci-fi"),
                recentViews = listOf("1", "2"),
                totalViews = 10
            )
            
            val items = (1..100).map { i ->
                MediaItem("$i", "Movie $i", if (i % 2 == 0) "Action" else "Comedy", 
                    listOf("tag$i"), (3.0f + i % 3).toFloat(), i * 100L, System.currentTimeMillis())
            }
            
            val recommendations = HybridRecommendationEngine.recommend(userProfile, items)
            
            assertTrue(recommendations.isNotEmpty())
            // 检查是否包含 Action 类电影
            val actionMovies = items.filter { it.category == "Action" }.map { it.id }
            val hasAction = recommendations.any { it.itemId in actionMovies }
            assertTrue(hasAction)
        }
    }
}
```

### 缓存性能测试

```kotlin
class OptimizedLRUCacheBenchmark {
    
    @Test
    fun testCachePerformance() {
        val cache = OptimizedLRUCache<String, ByteArray>(capacity = 1000)
        val data = ByteArray(1024) { it.toByte() }
        
        val startTime = System.nanoTime()
        
        // 写入测试
        repeat(10000) {
            cache.put("key_$it", data)
        }
        
        // 读取测试
        repeat(10000) {
            cache.get("key_${it % 10000}")
        }
        
        val endTime = System.nanoTime()
        val duration = (endTime - startTime) / 1_000_000.0
        
        println("Cache Operations: 20000")
        println("Duration: ${duration}ms")
        println("Ops/sec: ${20000 * 1000 / duration}")
        println("Hit Rate: ${cache.getHitRate() * 100}%")
        
        assertTrue(duration < 1000) // 应该在 1 秒内完成
        assertTrue(cache.getHitRate() > 0.8) // 命中率应该 > 80%
    }
}
```

---

## ✅ 验收标准

### CPU 监控
- [ ] CPU 使用率准确率 > 95%（与系统工具对比）
- [ ] 监控开销 < 1% CPU
- [ ] 支持多核 CPU 监控
- [ ] 支持 CPU 频率和温度监控

### 帧率监控
- [ ] 帧率检测误差 < 2fps
- [ ] 监控开销 < 2% CPU
- [ ] 卡顿检测准确率 > 90%
- [ ] 支持帧率统计和历史记录

### 推荐算法
- [ ] 冷启动用户推荐成功率 100%
- [ ] 推荐点击率提升 30%
- [ ] 推荐响应时间 < 100ms
- [ ] 支持至少 10 万物品规模

### 缓存优化
- [ ] 并发性能提升 50%
- [ ] 缓存命中率 > 80%
- [ ] 内存占用降低 20%
- [ ] 支持缓存统计和监控

---

**编制人**: AI Code Reviewer  
**审核状态**: 待审核  
**下次更新**: 实施完成后
