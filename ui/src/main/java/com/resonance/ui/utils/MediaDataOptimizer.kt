package com.resonance.ui.utils

import com.resonance.data.model.MediaItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * 媒体数据处理优化工具类
 * 提供媒体数据的搜索、排序、过滤等优化功能
 */
object MediaDataOptimizer {
    
    /**
     * 媒体数据搜索（支持模糊搜索）
     * @param items 媒体项列表
     * @param query 搜索关键词
     * @param searchFields 搜索字段（名称、描述、标签等）
     */
    suspend fun searchMedia(
        items: List<MediaItem>,
        query: String,
        searchFields: Set<SearchField> = setOf(SearchField.NAME, SearchField.DESCRIPTION)
    ): List<MediaItem> = withContext(Dispatchers.Default) {
        if (query.isBlank()) return@withContext items
        
        val normalizedQuery = query.lowercase().trim()
        
        items.filter { item ->
            searchFields.any { field ->
                when (field) {
                    SearchField.NAME -> {
                        val name = item.name.lowercase()
                        name.contains(normalizedQuery) || 
                        AlgorithmOptimizer.fuzzySearch(listOf(name), normalizedQuery).isNotEmpty()
                    }
                    SearchField.DESCRIPTION -> {
                        item.overview?.lowercase()?.contains(normalizedQuery) == true
                    }
                    SearchField.TAGS -> {
                        item.tags.any { tag -> 
                            tag.lowercase().contains(normalizedQuery) 
                        }
                    }
                    SearchField.YEAR -> {
                        item.releaseYear?.toString() == normalizedQuery
                    }
                }
            }
        }.sortedByDescending { item ->
            // 相关性排序
            calculateRelevance(item, normalizedQuery)
        }
    }
    
    /**
     * 计算相关性得分
     */
    private fun calculateRelevance(item: MediaItem, query: String): Int {
        var score = 0
        
        // 完全匹配名称
        if (item.name.lowercase() == query) {
            score += 100
        }
        
        // 名称包含查询
        if (item.name.lowercase().contains(query)) {
            score += 50
        }
        
        // 描述包含查询
        if (item.overview?.lowercase()?.contains(query) == true) {
            score += 30
        }
        
        // 标签匹配
        if (item.tags.any { it.lowercase().contains(query) }) {
            score += 20
        }
        
        return score
    }
    
    /**
     * 媒体数据排序
     * @param items 媒体项列表
     * @param sortBy 排序字段
     * @param ascending 是否升序
     */
    suspend fun sortMedia(
        items: List<MediaItem>,
        sortBy: SortBy = SortBy.NAME,
        ascending: Boolean = true
    ): List<MediaItem> = withContext(Dispatchers.Default) {
        when (sortBy) {
            SortBy.NAME -> {
                if (ascending) {
                    items.sortedBy { it.name.lowercase() }
                } else {
                    items.sortedByDescending { it.name.lowercase() }
                }
            }
            SortBy.YEAR -> {
                if (ascending) {
                    items.sortedBy { it.releaseYear ?: 0 }
                } else {
                    items.sortedByDescending { it.releaseYear ?: 0 }
                }
            }
            SortBy.RATING -> {
                if (ascending) {
                    items.sortedBy { it.rating ?: 0f }
                } else {
                    items.sortedByDescending { it.rating ?: 0f }
                }
            }
            SortBy.DATE_ADDED -> {
                // MediaItem 没有 dateCreated，暂时使用 id 作为替代
                if (ascending) {
                    items.sortedBy { it.id }
                } else {
                    items.sortedByDescending { it.id }
                }
            }
            SortBy.RUNTIME -> {
                if (ascending) {
                    items.sortedBy { it.runTimeTicks ?: 0L }
                } else {
                    items.sortedByDescending { it.runTimeTicks ?: 0L }
                }
            }
        }
    }
    
    /**
     * 媒体数据过滤
     * @param items 媒体项列表
     * @param filters 过滤条件
     */
    suspend fun filterMedia(
        items: List<MediaItem>,
        filters: MediaFilters
    ): List<MediaItem> = withContext(Dispatchers.Default) {
        items.filter { item ->
            // 类型过滤
            if (filters.types.isNotEmpty() && item.mediaType.name !in filters.types) {
                return@filter false
            }
            
            // 年份过滤
            if (filters.minYear != null && (item.releaseYear ?: 0) < filters.minYear) {
                return@filter false
            }
            if (filters.maxYear != null && (item.releaseYear ?: 0) > filters.maxYear) {
                return@filter false
            }
            
            // 评分过滤
            if (filters.minRating != null && (item.rating ?: 0f) < filters.minRating) {
                return@filter false
            }
            
            // 标签过滤
            if (filters.tags.isNotEmpty()) {
                val hasMatchingTag = filters.tags.any { tag ->
                    item.tags.any { itemTag -> itemTag.equals(tag, ignoreCase = true) }
                }
                if (!hasMatchingTag) return@filter false
            }
            
            true
        }
    }
    
    /**
     * 媒体数据分组
     * @param items 媒体项列表
     * @param groupBy 分组字段
     */
    suspend fun groupMedia(
        items: List<MediaItem>,
        groupBy: GroupBy
    ): Map<String, List<MediaItem>> = withContext(Dispatchers.Default) {
        when (groupBy) {
            GroupBy.YEAR -> {
                items.groupBy { item -> 
                    (item.releaseYear ?: 0).toString() 
                }
            }
            GroupBy.GENRE -> {
                items.flatMap { media ->
                    media.genres.map { tag -> media to tag }
                }.groupBy(
                    { it.second },
                    { it.first }
                )
            }
            GroupBy.TYPE -> {
                items.groupBy { it.mediaType.name }
            }
            GroupBy.FIRST_LETTER -> {
                items.groupBy { 
                    it.name.firstOrNull()?.uppercase() ?: "#" 
                }
            }
        }
    }
    
    /**
     * 搜索字段枚举
     */
    enum class SearchField {
        NAME,
        DESCRIPTION,
        TAGS,
        YEAR
    }
    
    /**
     * 排序字段枚举
     */
    enum class SortBy {
        NAME,
        YEAR,
        RATING,
        DATE_ADDED,
        RUNTIME
    }
    
    /**
     * 分组字段枚举
     */
    enum class GroupBy {
        YEAR,
        GENRE,
        TYPE,
        FIRST_LETTER
    }
    
    /**
     * 媒体过滤条件
     */
    data class MediaFilters(
        val types: Set<String> = emptySet(),
        val minYear: Int? = null,
        val maxYear: Int? = null,
        val minRating: Float? = null,
        val tags: Set<String> = emptySet()
    )
}
