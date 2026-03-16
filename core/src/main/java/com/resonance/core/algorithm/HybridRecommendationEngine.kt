package com.resonance.core.algorithm

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

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
    ): List<RecommendedItem> = withContext(Dispatchers.Default) {
        // 策略 1: 推荐最热门的物品
        val popularItems = getPopularItems(allItems, 1.0f)
        
        // 策略 2: 如果有类别偏好，推荐该类别的热门物品
        if (userProfile.favoriteCategories.isNotEmpty()) {
            val categoryItems = allItems.filter { it.category in userProfile.favoriteCategories }
            if (categoryItems.isNotEmpty()) {
                return@withContext getPopularItems(categoryItems, 1.0f)
                    .take(config.maxRecommendations)
            }
        }
        
        // 策略 3: 随机推荐（增加多样性）
        if (popularItems.size < config.minRecommendations) {
            val randomItems = allItems.shuffled().take(config.minRecommendations - popularItems.size)
            return@withContext (popularItems + randomItems.map { 
                RecommendedItem(it.id, 0.5f, 0.1f) 
            }).take(config.maxRecommendations)
        }
        
        return@withContext popularItems.take(config.maxRecommendations)
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
        val similarityMatrix = mutableMapOf<String, MutableMap<String, Float>>()
        
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
                
                if (similarity > 0.3f) { // 只保留相似度较高的
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
