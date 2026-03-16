package com.resonance.ui.utils

/**
 * 算法优化工具类
 * 提供优化的搜索算法、排序算法和缓存策略
 */
object AlgorithmOptimizer {
    
    /**
     * 二分查找算法
     * 适用于已排序列表的快速查找
     * 时间复杂度：O(log n)
     */
    fun <T : Comparable<T>> binarySearch(list: List<T>, target: T): Int {
        var left = 0
        var right = list.size - 1
        
        while (left <= right) {
            val mid = left + (right - left) / 2
            when {
                list[mid] == target -> return mid
                list[mid] < target -> left = mid + 1
                else -> right = mid - 1
            }
        }
        
        return -1
    }
    
    /**
     * 快速排序算法
     * 时间复杂度：平均 O(n log n)，最坏 O(n²)
     */
    fun <T : Comparable<T>> quickSort(list: List<T>): List<T> {
        if (list.size <= 1) return list
        
        val pivot = list[list.size / 2]
        val less = list.filter { it < pivot }
        val equal = list.filter { it == pivot }
        val greater = list.filter { it > pivot }
        
        return quickSort(less) + equal + quickSort(greater)
    }
    
    /**
     * 归并排序算法
     * 时间复杂度：稳定 O(n log n)
     * 适用于大数据集
     */
    fun <T : Comparable<T>> mergeSort(list: List<T>): List<T> {
        if (list.size <= 1) return list
        
        val mid = list.size / 2
        val left = mergeSort(list.subList(0, mid))
        val right = mergeSort(list.subList(mid, list.size))
        
        return merge(left, right)
    }
    
    /**
     * 合并两个有序列表
     */
    private fun <T : Comparable<T>> merge(left: List<T>, right: List<T>): List<T> {
        val result = mutableListOf<T>()
        var i = 0
        var j = 0
        
        while (i < left.size && j < right.size) {
            when {
                left[i] <= right[j] -> result.add(left[i++])
                else -> result.add(right[j++])
            }
        }
        
        result.addAll(left.subList(i, left.size))
        result.addAll(right.subList(j, right.size))
        
        return result
    }
    
    /**
     * 模糊搜索算法
     * 支持拼音、简拼搜索
     */
    fun fuzzySearch(items: List<String>, query: String): List<String> {
        if (query.isBlank()) return items
        
        val normalizedQuery = query.lowercase().trim()
        
        return items.filter { item ->
            val normalizedItem = item.lowercase()
            
            // 完全匹配
            if (normalizedItem == normalizedQuery) return@filter true
            
            // 包含匹配
            if (normalizedItem.contains(normalizedQuery)) return@filter true
            
            // 拼音首字母匹配（简化版）
            if (matchesPinyinInitials(normalizedItem, normalizedQuery)) return@filter true
            
            false
        }
    }
    
    /**
     * 拼音首字母匹配（简化实现）
     */
    private fun matchesPinyinInitials(text: String, initials: String): Boolean {
        // 这里应该使用完整的拼音库
        // 简化实现：只匹配英文首字母
        val textInitials = text.split(" ").joinToString("") { it.firstOrNull()?.toString() ?: "" }
        return textInitials.startsWith(initials)
    }
    
    /**
     * LRU 缓存实现
     * 最近最少使用缓存策略
     */
    class LRUCache<K, V>(private val capacity: Int) {
        private val cache = LinkedHashMap<K, V>(capacity, 0.75f, true)
        
        @Synchronized
        fun get(key: K): V? {
            return cache[key]
        }
        
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
        
        @Synchronized
        fun remove(key: K): V? {
            return cache.remove(key)
        }
        
        @Synchronized
        fun clear() {
            cache.clear()
        }
        
        @Synchronized
        fun containsKey(key: K): Boolean {
            return cache.containsKey(key)
        }
        
        val size: Int
            @Synchronized get() = cache.size
    }
    
    /**
     * 带过期时间的缓存
     */
    class ExpiringCache<K, V>(
        private val capacity: Int,
        private val expiryTimeMs: Long
    ) {
        data class CacheEntry<V>(val value: V, val timestamp: Long)
        
        private val cache = LinkedHashMap<K, CacheEntry<V>>(capacity, 0.75f, true)
        
        @Synchronized
        fun get(key: K): V? {
            val entry = cache[key] ?: return null
            
            // 检查是否过期
            if (System.currentTimeMillis() - entry.timestamp > expiryTimeMs) {
                cache.remove(key)
                return null
            }
            
            return entry.value
        }
        
        @Synchronized
        fun put(key: K, value: V) {
            if (cache.size >= capacity && !cache.containsKey(key)) {
                val iterator = cache.iterator()
                if (iterator.hasNext()) {
                    iterator.next()
                    iterator.remove()
                }
            }
            cache[key] = CacheEntry(value, System.currentTimeMillis())
        }
        
        @Synchronized
        fun remove(key: K): V? {
            return cache.remove(key)?.value
        }
        
        @Synchronized
        fun clear() {
            cache.clear()
        }
        
        /**
         * 清理所有过期的缓存项
         */
        @Synchronized
        fun cleanup() {
            val now = System.currentTimeMillis()
            val iterator = cache.iterator()
            
            while (iterator.hasNext()) {
                val entry = iterator.next()
                if (now - entry.value.timestamp > expiryTimeMs) {
                    iterator.remove()
                }
            }
        }
    }
    
    /**
     * 布隆过滤器（简化实现）
     * 用于快速判断元素是否存在
     */
    class BloomFilter<T>(
        private val size: Int = 10000,
        private val hashCount: Int = 3
    ) {
        private val bitSet = BooleanArray(size)
        
        private fun hash(element: T, seed: Int): Int {
            val hashCode = element.hashCode()
            return kotlin.math.abs((hashCode + seed) % size)
        }
        
        fun add(element: T) {
            for (i in 0 until hashCount) {
                val index = hash(element, i)
                bitSet[index] = true
            }
        }
        
        fun mightContain(element: T): Boolean {
            for (i in 0 until hashCount) {
                val index = hash(element, i)
                if (!bitSet[index]) return false
            }
            return true
        }
        
        fun clear() {
            bitSet.fill(false)
        }
    }
}
