package com.resonance.ui.utils

import org.junit.Test
import org.junit.Assert.*
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread

/**
 * OptimizedLRUCache 单元测试
 */
class OptimizedLRUCacheTest {
    
    @Test
    fun testBasicPutAndGet() {
        val cache = OptimizedLRUCache<String, Int>(capacity = 3)
        
        // 测试基本放入和获取
        cache.put("key1", 1)
        cache.put("key2", 2)
        cache.put("key3", 3)
        
        assertEquals(1, cache.get("key1"))
        assertEquals(2, cache.get("key2"))
        assertEquals(3, cache.get("key3"))
    }
    
    @Test
    fun testLRUEviction() {
        val cache = OptimizedLRUCache<String, Int>(capacity = 3)
        
        // 测试 LRU 淘汰机制
        cache.put("key1", 1)
        cache.put("key2", 2)
        cache.put("key3", 3)
        
        // 放入第 4 个元素，应该淘汰最旧的 key1
        cache.put("key4", 4)
        
        assertNull(cache.get("key1")) // 应该被淘汰
        assertNotNull(cache.get("key2"))
        assertNotNull(cache.get("key3"))
        assertNotNull(cache.get("key4"))
    }
    
    @Test
    fun testAccessUpdatesLRU() {
        val cache = OptimizedLRUCache<String, Int>(capacity = 3)
        
        cache.put("key1", 1)
        cache.put("key2", 2)
        cache.put("key3", 3)
        
        // 访问 key1，使其变为最近使用
        cache.get("key1")
        
        // 放入新元素，应该淘汰 key2（最久未使用）
        cache.put("key4", 4)
        
        assertNotNull(cache.get("key1")) // 不应该被淘汰
        assertNull(cache.get("key2")) // 应该被淘汰
        assertNotNull(cache.get("key3"))
        assertNotNull(cache.get("key4"))
    }
    
    @Test
    fun testContainsKey() {
        val cache = OptimizedLRUCache<String, Int>(capacity = 3)
        
        cache.put("key1", 1)
        cache.put("key2", 2)
        
        assertTrue(cache.containsKey("key1"))
        assertTrue(cache.containsKey("key2"))
        assertFalse(cache.containsKey("key3"))
    }
    
    @Test
    fun testRemove() {
        val cache = OptimizedLRUCache<String, Int>(capacity = 3)
        
        cache.put("key1", 1)
        cache.put("key2", 2)
        
        val removed = cache.remove("key1")
        
        assertEquals(1, removed)
        assertNull(cache.get("key1"))
        assertNotNull(cache.get("key2"))
    }
    
    @Test
    fun testClear() {
        val cache = OptimizedLRUCache<String, Int>(capacity = 3)
        
        cache.put("key1", 1)
        cache.put("key2", 2)
        cache.put("key3", 3)
        
        cache.clear()
        
        assertEquals(0, cache.size)
        assertNull(cache.get("key1"))
        assertNull(cache.get("key2"))
        assertNull(cache.get("key3"))
    }
    
    @Test
    fun testSize() {
        val cache = OptimizedLRUCache<String, Int>(capacity = 3)
        
        assertEquals(0, cache.size)
        
        cache.put("key1", 1)
        assertEquals(1, cache.size)
        
        cache.put("key2", 2)
        assertEquals(2, cache.size)
        
        cache.put("key3", 3)
        assertEquals(3, cache.size)
        
        cache.put("key4", 4)
        assertEquals(3, cache.size) // 容量限制为 3
    }
    
    @Test
    fun testHitRate() {
        val cache = OptimizedLRUCache<String, Int>(capacity = 3)
        
        cache.put("key1", 1)
        cache.put("key2", 2)
        
        // 3 次命中，2 次未命中
        cache.get("key1")
        cache.get("key2")
        cache.get("key1")
        cache.get("key3") // 未命中
        cache.get("key4") // 未命中
        
        val hitRate = cache.getHitRate()
        assertTrue(hitRate > 0.5f) // 命中率应该大于 50%
    }
    
    @Test
    fun testGetStats() {
        val cache = OptimizedLRUCache<String, Int>(capacity = 3)
        
        cache.put("key1", 1)
        cache.get("key1")
        cache.get("key2")
        
        val stats = cache.getStats()
        
        assertTrue(stats.hitCount > 0)
        assertTrue(stats.missCount > 0)
    }
    
    @Test
    fun testResetStats() {
        val cache = OptimizedLRUCache<String, Int>(capacity = 3)
        
        cache.put("key1", 1)
        cache.get("key1")
        cache.get("key2")
        
        cache.resetStats()
        
        val stats = cache.getStats()
        assertEquals(0, stats.hitCount)
        assertEquals(0, stats.missCount)
    }
    
    @Test
    fun testPutAll() {
        val cache = OptimizedLRUCache<String, Int>(capacity = 5)
        
        val items = mapOf(
            "key1" to 1,
            "key2" to 2,
            "key3" to 3
        )
        
        cache.putAll(items)
        
        assertEquals(3, cache.size)
        assertEquals(1, cache.get("key1"))
        assertEquals(2, cache.get("key2"))
        assertEquals(3, cache.get("key3"))
    }
    
    @Test
    fun testConcurrentAccess() {
        val cache = OptimizedLRUCache<String, Int>(capacity = 100)
        val threadCount = 10
        val operationsPerThread = 100
        val latch = CountDownLatch(threadCount)
        
        repeat(threadCount) { threadId ->
            thread {
                try {
                    repeat(operationsPerThread) { i ->
                        val key = "key_${threadId}_$i"
                        cache.put(key, i)
                        cache.get(key)
                    }
                } finally {
                    latch.countDown()
                }
            }
        }
        
        assertTrue(latch.await(10, TimeUnit.SECONDS))
        
        // 验证缓存大小不超过容量
        assertTrue(cache.size <= 100)
    }
    
    @Test
    fun testOnRemoveCallback() {
        var removedKey: String? = null
        var removedValue: Int? = null
        
        val cache = OptimizedLRUCache<String, Int>(
            capacity = 2,
            onRemove = { key, value ->
                removedKey = key
                removedValue = value
            }
        )
        
        cache.put("key1", 1)
        cache.put("key2", 2)
        cache.put("key3", 3) // 应该触发 onRemove
        
        assertNotNull(removedKey)
        assertNotNull(removedValue)
        assertEquals("key1", removedKey)
        assertEquals(1, removedValue)
    }
    
    @Test
    fun testOnAccessCallback() {
        var accessedKey: String? = null
        var accessedValue: Int? = null
        
        val cache = OptimizedLRUCache<String, Int>(
            capacity = 3,
            onAccess = { key, value ->
                accessedKey = key
                accessedValue = value
            }
        )
        
        cache.put("key1", 1)
        cache.get("key1") // 应该触发 onAccess
        
        assertNotNull(accessedKey)
        assertNotNull(accessedValue)
        assertEquals("key1", accessedKey)
        assertEquals(1, accessedValue)
    }
    
    @Test
    fun testNullValue() {
        val cache = OptimizedLRUCache<String, String?>(capacity = 3)
        
        cache.put("key1", null)
        
        assertNull(cache.get("key1"))
        assertTrue(cache.containsKey("key1"))
    }
    
    @Test
    fun testLargeCapacity() {
        val cache = OptimizedLRUCache<Int, ByteArray>(capacity = 1000)
        val data = ByteArray(1024) { it.toByte() }
        
        repeat(1000) { i ->
            cache.put(i, data)
        }
        
        assertEquals(1000, cache.size)
        
        // 验证所有数据都在缓存中
        repeat(1000) { i ->
            assertNotNull(cache.get(i))
        }
    }
}
