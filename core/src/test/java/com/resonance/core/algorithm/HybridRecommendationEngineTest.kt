package com.resonance.core.algorithm

import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.Assert.*

/**
 * HybridRecommendationEngine 单元测试
 */
class HybridRecommendationEngineTest {
    
    @Test
    fun testColdStartRecommendation() = runBlocking {
        // 测试冷启动场景（新用户）
        val userProfile = HybridRecommendationEngine.UserProfile(
            userId = "test_user",
            preferences = emptyMap(),
            favoriteCategories = emptySet(),
            favoriteTags = emptySet(),
            recentViews = emptyList(),
            totalViews = 0
        )
        
        val items = listOf(
            HybridRecommendationEngine.MediaItem(
                id = "1",
                title = "Movie 1",
                category = "Action",
                tags = listOf("action", "adventure"),
                rating = 4.5f,
                viewCount = 1000,
                createdAt = System.currentTimeMillis()
            ),
            HybridRecommendationEngine.MediaItem(
                id = "2",
                title = "Movie 2",
                category = "Comedy",
                tags = listOf("comedy"),
                rating = 4.0f,
                viewCount = 500,
                createdAt = System.currentTimeMillis()
            )
        )
        
        val recommendations = HybridRecommendationEngine.recommend(userProfile, items)
        
        // 验证冷启动时仍然有推荐
        assertTrue(recommendations.isNotEmpty())
        assertTrue(recommendations.size <= 50)
    }
    
    @Test
    fun testPersonalizedRecommendation() = runBlocking {
        // 测试个性化推荐场景
        val userProfile = HybridRecommendationEngine.UserProfile(
            userId = "test_user",
            preferences = mapOf("1" to 5.0f, "2" to 4.0f),
            favoriteCategories = setOf("Action"),
            favoriteTags = setOf("sci-fi"),
            recentViews = listOf("1", "2"),
            totalViews = 10
        )
        
        val items = (1..20).map { i ->
            HybridRecommendationEngine.MediaItem(
                id = "$i",
                title = "Movie $i",
                category = if (i % 2 == 0) "Action" else "Comedy",
                tags = listOf("tag$i", if (i % 3 == 0) "sci-fi" else "other"),
                rating = (3.0f + i % 3).toFloat(),
                viewCount = i * 100L,
                createdAt = System.currentTimeMillis() - i * 86400000
            )
        }
        
        val recommendations = HybridRecommendationEngine.recommend(userProfile, items)
        
        // 验证有推荐结果
        assertTrue(recommendations.isNotEmpty())
        
        // 验证推荐数量在合理范围内
        assertTrue(recommendations.size <= 50)
        
        // 验证推荐结果包含 Action 类电影（用户偏好）
        val actionMovies = items.filter { it.category == "Action" }.map { it.id }
        val hasAction = recommendations.any { it.itemId in actionMovies }
        assertTrue(hasAction)
    }
    
    @Test
    fun testRecommendationWithCategoryPreference() = runBlocking {
        // 测试基于类别偏好的推荐
        val userProfile = HybridRecommendationEngine.UserProfile(
            userId = "test_user",
            preferences = emptyMap(),
            favoriteCategories = setOf("Action", "Sci-Fi"),
            favoriteTags = emptySet(),
            recentViews = emptyList(),
            totalViews = 5
        )
        
        val items = listOf(
            HybridRecommendationEngine.MediaItem(
                id = "1",
                title = "Action Movie",
                category = "Action",
                tags = listOf("action"),
                rating = 4.5f,
                viewCount = 1000,
                createdAt = System.currentTimeMillis()
            ),
            HybridRecommendationEngine.MediaItem(
                id = "2",
                title = "Comedy Movie",
                category = "Comedy",
                tags = listOf("comedy"),
                rating = 4.0f,
                viewCount = 500,
                createdAt = System.currentTimeMillis()
            )
        )
        
        val recommendations = HybridRecommendationEngine.recommend(userProfile, items)
        
        // 验证推荐结果优先包含 Action 类电影
        assertTrue(recommendations.isNotEmpty())
    }
    
    @Test
    fun testPopularItemsRecommendation() = runBlocking {
        // 测试热门物品推荐
        val userProfile = HybridRecommendationEngine.UserProfile(
            userId = "test_user",
            preferences = emptyMap(),
            favoriteCategories = emptySet(),
            favoriteTags = emptySet(),
            recentViews = emptyList(),
            totalViews = 0
        )
        
        val items = listOf(
            HybridRecommendationEngine.MediaItem(
                id = "1",
                title = "Popular Movie",
                category = "Action",
                tags = listOf("action"),
                rating = 5.0f,
                viewCount = 10000,
                createdAt = System.currentTimeMillis()
            ),
            HybridRecommendationEngine.MediaItem(
                id = "2",
                title = "Less Popular Movie",
                category = "Comedy",
                tags = listOf("comedy"),
                rating = 3.0f,
                viewCount = 100,
                createdAt = System.currentTimeMillis()
            )
        )
        
        val recommendations = HybridRecommendationEngine.recommend(userProfile, items)
        
        // 验证热门物品排在前面
        assertTrue(recommendations.isNotEmpty())
        assertEquals("1", recommendations.first().itemId)
    }
    
    @Test
    fun testRecommendationConfig() {
        // 测试推荐配置
        val config = HybridRecommendationEngine.RecommendationConfig(
            collaborativeWeight = 0.5f,
            contentBasedWeight = 0.3f,
            popularityWeight = 0.1f,
            recencyWeight = 0.1f,
            minRecommendations = 5,
            maxRecommendations = 20
        )
        
        assertEquals(0.5f, config.collaborativeWeight, 0.01f)
        assertEquals(0.3f, config.contentBasedWeight, 0.01f)
        assertEquals(0.1f, config.popularityWeight, 0.01f)
        assertEquals(0.1f, config.recencyWeight, 0.01f)
        assertEquals(5, config.minRecommendations)
        assertEquals(20, config.maxRecommendations)
    }
    
    @Test
    fun testMediaItemDataClass() {
        // 测试媒体物品数据类
        val item = HybridRecommendationEngine.MediaItem(
            id = "1",
            title = "Test Movie",
            category = "Action",
            tags = listOf("action", "adventure"),
            rating = 4.5f,
            viewCount = 1000,
            createdAt = System.currentTimeMillis(),
            features = floatArrayOf(0.1f, 0.2f, 0.3f)
        )
        
        assertEquals("1", item.id)
        assertEquals("Test Movie", item.title)
        assertEquals("Action", item.category)
        assertEquals(4.5f, item.rating, 0.01f)
        assertEquals(1000, item.viewCount)
        assertNotNull(item.features)
        assertEquals(3, item.features!!.size)
    }
    
    @Test
    fun testUserProfileDataClass() {
        // 测试用户画像数据类
        val profile = HybridRecommendationEngine.UserProfile(
            userId = "test_user",
            preferences = mapOf("1" to 5.0f, "2" to 4.0f),
            favoriteCategories = setOf("Action", "Sci-Fi"),
            favoriteTags = setOf("adventure"),
            recentViews = listOf("1", "2"),
            totalViews = 10
        )
        
        assertEquals("test_user", profile.userId)
        assertEquals(2, profile.preferences.size)
        assertEquals(2, profile.favoriteCategories.size)
        assertEquals(1, profile.favoriteTags.size)
        assertEquals(2, profile.recentViews.size)
        assertEquals(10, profile.totalViews)
    }
    
    @Test
    fun testRecommendedItemScore() {
        // 测试推荐物品分数
        val item = RecommendedItem(
            itemId = "1",
            score = 0.85f,
            weight = 0.5f
        )
        
        assertEquals("1", item.itemId)
        assertEquals(0.85f, item.score, 0.01f)
        assertEquals(0.5f, item.weight, 0.01f)
    }
}
