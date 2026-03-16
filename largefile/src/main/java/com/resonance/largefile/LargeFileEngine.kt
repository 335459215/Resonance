package com.resonance.largefile

import android.content.Context
import android.os.ParcelFileDescriptor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.RandomAccessFile
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import java.util.concurrent.ConcurrentHashMap

class LargeFileEngine(private val context: Context) {
    companion object {
        private const val DEFAULT_CHUNK_SIZE = 4 * 1024 * 1024 // 4MB
        private const val MAX_MEMORY_USAGE = 80 * 1024 * 1024 // 80MB
        private const val TV_MAX_BUFFER_SIZE = 24 * 1024 * 1024 // TV端最大缓冲24MB
        private const val CACHE_KEY_PREFIX = "large_file_"
    }

    private val chunkCache = ConcurrentHashMap<String, MappedByteBuffer>()
    private val keyframeIndexCache = ConcurrentHashMap<String, List<Long>>()

    suspend fun initFile(file: File): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                // 检查文件是否存在
                if (!file.exists()) {
                    return@withContext false
                }

                // 检查文件大小
                val fileSize = file.length()
                if (fileSize == 0L) {
                    return@withContext false
                }

                // 预热关键帧索引
                preloadKeyframeIndex(file)
                true
            } catch (e: Exception) {
                false
            }
        }
    }

    suspend fun readChunk(file: File, offset: Long, size: Int = DEFAULT_CHUNK_SIZE): ByteArray? {
        return withContext(Dispatchers.IO) {
            try {
                val raf = RandomAccessFile(file, "r")
                val channel = raf.channel
                
                // 计算实际读取大小
                val actualSize = kotlin.math.min(size.toLong(), raf.length() - offset).toInt()
                if (actualSize <= 0) {
                    channel.close()
                    raf.close()
                    return@withContext null
                }

                // 使用mmap内存映射
                val buffer = channel.map(FileChannel.MapMode.READ_ONLY, offset, actualSize.toLong())
                val data = ByteArray(actualSize)
                buffer.get(data)
                
                channel.close()
                raf.close()
                data
            } catch (e: Exception) {
                null
            }
        }
    }

    suspend fun seekTo(file: File, position: Long): Long {
        return withContext(Dispatchers.IO) {
            try {
                // 使用关键帧索引加速seek
                val keyframes = getKeyframeIndex(file)
                if (keyframes.isNotEmpty()) {
                    // 找到小于等于目标位置的最大关键帧
                    val targetKeyframe = keyframes.lastOrNull { it <= position } ?: 0L
                    return@withContext targetKeyframe
                }
                position
            } catch (e: Exception) {
                position
            }
        }
    }

    private suspend fun preloadKeyframeIndex(file: File) {
        withContext(Dispatchers.IO) {
            try {
                val cacheKey = "${CACHE_KEY_PREFIX}${file.absolutePath}_keyframes"
                
                // 先从缓存获取
                val cachedKeyframes = getKeyframeIndexFromCache(cacheKey)
                if (cachedKeyframes != null) {
                    keyframeIndexCache[file.absolutePath] = cachedKeyframes
                    return@withContext
                }
                
                // 这里实现关键帧索引的提取逻辑
                // 由于不同格式的媒体文件关键帧提取方式不同，这里只是返回模拟数据
                val keyframes = generateMockKeyframes(file.length())
                
                // 缓存关键帧索引
                keyframeIndexCache[file.absolutePath] = keyframes
                saveKeyframeIndexToCache(cacheKey, keyframes)
            } catch (e: Exception) {
                // 关键帧索引提取失败
            }
        }
    }

    private fun getKeyframeIndex(file: File): List<Long> {
        return keyframeIndexCache.getOrPut(file.absolutePath) { emptyList() }
    }

    private fun generateMockKeyframes(fileSize: Long): List<Long> {
        // 生成模拟的关键帧索引
        val keyframes = mutableListOf<Long>()
        val step = fileSize / 100 // 每1%生成一个关键帧
        for (i in 0..100) {
            keyframes.add(i * step)
        }
        return keyframes
    }

    private fun getKeyframeIndexFromCache(key: String): List<Long>? {
        val mmkv = com.tencent.mmkv.MMKV.defaultMMKV()
        val serialized = mmkv.decodeString(key)
        return serialized?.split(",")?.mapNotNull { it.toLongOrNull() }
    }

    private fun saveKeyframeIndexToCache(key: String, keyframes: List<Long>) {
        val mmkv = com.tencent.mmkv.MMKV.defaultMMKV()
        val serialized = keyframes.joinToString(",")
        mmkv.encode(key, serialized)
    }

    fun getOptimalChunkSize(isTv: Boolean): Int {
        return if (isTv) {
            // TV端使用较小的分片大小
            2 * 1024 * 1024 // 2MB
        } else {
            // 移动端使用默认分片大小
            DEFAULT_CHUNK_SIZE
        }
    }

    fun getMaxBufferSize(isTv: Boolean): Int {
        return if (isTv) {
            TV_MAX_BUFFER_SIZE
        } else {
            MAX_MEMORY_USAGE
        }
    }

    fun clearCache() {
        // 清理内存缓存
        chunkCache.clear()
        keyframeIndexCache.clear()
        
        // 清理磁盘缓存
        val mmkv = com.tencent.mmkv.MMKV.defaultMMKV()
        val allKeys = mmkv.allKeys()
        allKeys?.forEach {
            if (it.startsWith(CACHE_KEY_PREFIX)) {
                mmkv.removeValueForKey(it)
            }
        }
    }

    fun getFileInfo(file: File): FileInfo {
        return FileInfo(
            fileName = file.name,
            fileSize = file.length(),
            chunkSize = DEFAULT_CHUNK_SIZE,
            totalChunks = (file.length() + DEFAULT_CHUNK_SIZE - 1) / DEFAULT_CHUNK_SIZE
        )
    }
}

// 文件信息数据类
data class FileInfo(
    val fileName: String,
    val fileSize: Long,
    val chunkSize: Int,
    val totalChunks: Long
)