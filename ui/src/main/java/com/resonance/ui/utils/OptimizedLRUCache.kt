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
    
    /**
     * 获取命中率
     */
    fun getHitRate(): Float {
        val total = hitCount + missCount
        return if (total > 0) hitCount.toFloat() / total else 0f
    }
    
    /**
     * 记录命中
     */
    fun recordHit() {
        lock.withLock { hitCount++ }
    }
    
    /**
     * 记录未命中
     */
    fun recordMiss() {
        lock.withLock { missCount++ }
    }
    
    /**
     * 重置统计
     */
    fun resetStats() {
        lock.withLock {
            hitCount = 0
            missCount = 0
        }
    }
    
    /**
     * 获取命中和未命中次数
     */
    fun getStats(): CacheStats {
        return lock.withLock {
            CacheStats(hitCount, missCount)
        }
    }
    
    /**
     * 缓存统计数据类
     */
    data class CacheStats(
        val hitCount: Int,
        val missCount: Int
    )
}
