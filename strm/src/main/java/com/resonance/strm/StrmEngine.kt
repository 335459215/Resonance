package com.resonance.strm

import android.content.Context
import com.resonance.core.PlayerManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.*
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

class StrmEngine(private val context: Context) {
    private val cache = ConcurrentHashMap<String, String>() // STRM文件路径 -> 解析后的URL
    private val executor: ScheduledExecutorService = Executors.newSingleThreadScheduledExecutor()
    private val httpClient = okhttp3.OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    companion object {
        private const val CACHE_KEY_PREFIX = "strm_url_"
        private const val MAX_CACHE_SIZE = 100

        @Volatile
        private var instance: StrmEngine? = null

        fun getInstance(context: Context): StrmEngine {
            return instance ?: synchronized(this) {
                instance ?: StrmEngine(context.applicationContext).also {
                    instance = it
                }
            }
        }
    }

    suspend fun parseStrmFile(strmPath: String): String {
        // 先从内存缓存获取
        val cachedUrl = cache[strmPath]
        if (cachedUrl != null) {
            return cachedUrl
        }

        // 从磁盘缓存获取
        val diskCachedUrl = getFromDiskCache(strmPath)
        if (diskCachedUrl != null) {
            cache[strmPath] = diskCachedUrl
            return diskCachedUrl
        }

        // 解析STRM文件
        val url = withContext(Dispatchers.IO) {
            val file = File(strmPath)
            if (!file.exists()) {
                throw FileNotFoundException("STRM file not found: $strmPath")
            }

            // 读取文件内容，支持不同编码
            val content = readFileWithEncoding(file)
            // 提取URL（取第一行非空行）
            val lines = content.lines()
            for (line in lines) {
                val trimmedLine = line.trim()
                if (trimmedLine.isNotEmpty() && (trimmedLine.startsWith("http://") || trimmedLine.startsWith("https://"))) {
                    return@withContext trimmedLine
                }
            }
            throw IllegalArgumentException("No valid URL found in STRM file: $strmPath")
        }

        // 缓存解析结果
        cache[strmPath] = url
        saveToDiskCache(strmPath, url)
        
        // 限制缓存大小
        if (cache.size > MAX_CACHE_SIZE) {
            val oldestKey = cache.keys.firstOrNull()
            oldestKey?.let { cache.remove(it) }
        }

        return url
    }

    suspend fun preloadStrmFile(strmPath: String) {
        withContext(Dispatchers.IO) {
            try {
                parseStrmFile(strmPath)
                // 预热HTTP连接
                warmUpConnection(parseStrmFile(strmPath))
            } catch (e: Exception) {
                // 预加载失败不影响正常播放
            }
        }
    }

    fun warmUpConnection(url: String) {
        executor.submit { 
            try {
                val request = okhttp3.Request.Builder()
                    .url(url)
                    .method("HEAD", null)
                    .build()
                httpClient.newCall(request).execute().close()
            } catch (e: Exception) {
                // 连接预热失败不影响正常播放
            }
        }
    }

    fun clearCache() {
        cache.clear()
        clearDiskCache()
    }

    private fun readFileWithEncoding(file: File): String {
        val buffers = mutableListOf<Byte>()
        FileInputStream(file).use { fis ->
            val buffer = ByteArray(1024)
            var bytesRead: Int
            while (fis.read(buffer).also { bytesRead = it } != -1) {
                for (i in 0 until bytesRead) {
                    buffers.add(buffer[i])
                }
            }
        }

        val bytes = buffers.toByteArray()
        // 检测编码
        return when {
            bytes.size >= 3 && bytes[0] == 0xEF.toByte() && bytes[1] == 0xBB.toByte() && bytes[2] == 0xBF.toByte() -> {
                // UTF-8 BOM
                String(bytes, 3, bytes.size - 3, charset("UTF-8"))
            }
            else -> {
                // 默认UTF-8或ANSI
                try {
                    String(bytes, charset("UTF-8"))
                } catch (e: Exception) {
                    String(bytes, charset("ISO-8859-1"))
                }
            }
        }
    }

    private fun getFromDiskCache(strmPath: String): String? {
        val mmkv = com.tencent.mmkv.MMKV.defaultMMKV()
        return mmkv.decodeString(CACHE_KEY_PREFIX + strmPath)
    }

    private fun saveToDiskCache(strmPath: String, url: String) {
        val mmkv = com.tencent.mmkv.MMKV.defaultMMKV()
        mmkv.encode(CACHE_KEY_PREFIX + strmPath, url)
    }

    private fun clearDiskCache() {
        val mmkv = com.tencent.mmkv.MMKV.defaultMMKV()
        val allKeys = mmkv.allKeys()
        allKeys?.forEach { key ->
            if (key.startsWith(CACHE_KEY_PREFIX)) {
                mmkv.removeValueForKey(key)
            }
        }
    }

    fun shutdown() {
        executor.shutdown()
    }
}