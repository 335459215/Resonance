package com.resonance.core.algorithm

import java.security.MessageDigest
import kotlin.math.*

/**
 * 高级算法工具类
 * 提供智能推荐、数据压缩、加密算法等功能
 */
object AdvancedAlgorithms {
    
    // ==================== 智能推荐算法 ====================
    
    /**
     * 基于协同过滤的推荐算法
     * @param userPreferences 用户偏好列表
     * @param allItems 所有物品列表
     * @param itemSimilarities 物品相似度矩阵
     * @return 推荐物品列表
     */
    fun collaborativeFiltering(
        userPreferences: Map<String, Float>,
        allItems: List<String>,
        itemSimilarities: Map<String, Map<String, Float>>,
        topN: Int = 10
    ): List<RecommendedItem> {
        val scores = mutableMapOf<String, Float>()
        val weights = mutableMapOf<String, Float>()
        
        // 计算每个物品的推荐分数
        for (item in allItems) {
            if (userPreferences.containsKey(item)) continue
            
            var similaritySum = 0f
            var scoreSum = 0f
            
            for ((userItem, rating) in userPreferences) {
                val similarity = itemSimilarities[item]?.get(userItem) ?: 0f
                if (similarity > 0) {
                    similaritySum += similarity
                    scoreSum += similarity * rating
                }
            }
            
            if (similaritySum > 0) {
                scores[item] = scoreSum / similaritySum
                weights[item] = similaritySum
            }
        }
        
        // 排序并返回前 N 个推荐
        return scores.entries
            .sortedByDescending { it.value }
            .take(topN)
            .map { (item, score) ->
                RecommendedItem(item, score, weights[item] ?: 0f)
            }
    }
    
    /**
     * 基于内容的推荐算法
     * @param itemFeatures 物品特征向量
     * @param userLikedItems 用户喜欢的物品
     * @param allItems 所有物品
     * @return 推荐物品列表
     */
    fun contentBasedRecommendation(
        itemFeatures: Map<String, FloatArray>,
        userLikedItems: List<String>,
        allItems: List<String>,
        topN: Int = 10
    ): List<RecommendedItem> {
        // 计算用户偏好特征向量（平均值）
        val userPreferenceVector = calculateAverageVector(
            userLikedItems.mapNotNull { itemFeatures[it] }
        ) ?: return emptyList()
        
        // 计算每个物品与用户偏好的相似度
        val scores = allItems
            .filter { !userLikedItems.contains(it) }
            .mapNotNull { item ->
                itemFeatures[item]?.let { feature ->
                    val similarity = cosineSimilarity(userPreferenceVector, feature)
                    item to similarity
                }
            }
            .sortedByDescending { it.second }
            .take(topN)
            .map { (item, score) ->
                RecommendedItem(item, score, score)
            }
        
        return scores
    }
    
    /**
     * 计算余弦相似度
     */
    fun cosineSimilarity(vectorA: FloatArray, vectorB: FloatArray): Float {
        if (vectorA.size != vectorB.size) return 0f
        
        var dotProduct = 0f
        var normA = 0f
        var normB = 0f
        
        for (i in vectorA.indices) {
            dotProduct += vectorA[i] * vectorB[i]
            normA += vectorA[i] * vectorA[i]
            normB += vectorB[i] * vectorB[i]
        }
        
        return if (normA > 0 && normB > 0) {
            dotProduct / (sqrt(normA) * sqrt(normB))
        } else {
            0f
        }
    }
    
    /**
     * 计算平均向量
     */
    private fun calculateAverageVector(vectors: List<FloatArray>): FloatArray? {
        if (vectors.isEmpty()) return null
        
        val size = vectors[0].size
        val result = FloatArray(size)
        
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
    
    // ==================== 数据压缩算法 ====================
    
    /**
     * 游程编码压缩（RLE）
     */
    fun runLengthEncode(data: String): String {
        if (data.isEmpty()) return ""
        
        val result = StringBuilder()
        var count = 1
        
        for (i in 1 until data.length) {
            if (data[i] == data[i - 1]) {
                count++
            } else {
                result.append(count).append(data[i - 1])
                count = 1
            }
        }
        
        result.append(count).append(data.last())
        return result.toString()
    }
    
    /**
     * 游程编码解码
     */
    fun runLengthDecode(encoded: String): String {
        val result = StringBuilder()
        var i = 0
        
        while (i < encoded.length) {
            var count = 0
            while (i < encoded.length && encoded[i].isDigit()) {
                count = count * 10 + (encoded[i] - '0')
                i++
            }
            
            if (i < encoded.length) {
                result.append(encoded[i].toString().repeat(count))
                i++
            }
        }
        
        return result.toString()
    }
    
    /**
     * 霍夫曼编码压缩
     */
    fun huffmanEncode(data: String): Pair<String, Map<Char, String>> {
        if (data.isEmpty()) return "" to emptyMap()
        
        // 统计频率
        val frequency = data.groupingBy { it }.eachCount()
        
        // 构建霍夫曼树
        val priorityQueue = frequency.entries
            .map { HuffmanNode(it.key, it.value) }
            .toMutableList()
            .sortedBy { it.frequency }
            .toMutableList()
        
        while (priorityQueue.size > 1) {
            val left = priorityQueue.removeFirst()
            val right = priorityQueue.removeFirst()
            val parent = HuffmanNode(null, left.frequency + right.frequency, left, right)
            
            // 插入到正确位置
            var insertIndex = priorityQueue.indexOfFirst { it.frequency > parent.frequency }
            if (insertIndex == -1) insertIndex = priorityQueue.size
            priorityQueue.add(insertIndex, parent)
        }
        
        // 生成编码
        val codes = mutableMapOf<Char, String>()
        priorityQueue.firstOrNull()?.generateCodes(codes, "")
        
        // 编码数据
        val encoded = data.map { codes[it] }.joinToString("")
        
        return encoded to codes
    }
    
    /**
     * 霍夫曼解码
     */
    fun huffmanDecode(encoded: String, codes: Map<Char, String>): String {
        val reverseCodes = codes.entries.associate { it.value to it.key }
        val result = StringBuilder()
        var currentCode = ""
        
        for (bit in encoded) {
            currentCode += bit
            reverseCodes[currentCode]?.let { char ->
                result.append(char)
                currentCode = ""
            }
        }
        
        return result.toString()
    }
    
    // ==================== 加密算法 ====================
    
    /**
     * SHA-256 哈希
     */
    fun sha256(input: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(input.toByteArray())
        return hashBytes.joinToString("") { "%02x".format(it) }
    }
    
    /**
     * MD5 哈希（不推荐用于安全场景）
     */
    fun md5(input: String): String {
        val digest = MessageDigest.getInstance("MD5")
        val hashBytes = digest.digest(input.toByteArray())
        return hashBytes.joinToString("") { "%02x".format(it) }
    }
    
    /**
     * Base64 编码
     */
    fun base64Encode(input: String): String {
        return android.util.Base64.encodeToString(input.toByteArray(), android.util.Base64.NO_WRAP)
    }
    
    /**
     * Base64 解码
     */
    fun base64Decode(input: String): String {
        return String(android.util.Base64.decode(input, android.util.Base64.NO_WRAP))
    }
    
    /**
     * XOR 加密
     */
    fun xorEncrypt(data: String, key: String): String {
        val result = StringBuilder()
        for ((index, char) in data.withIndex()) {
            val keyChar = key[index % key.length]
            result.append((char.code xor keyChar.code).toChar())
        }
        return result.toString()
    }
    
    /**
     * XOR 解密（与加密相同）
     */
    fun xorDecrypt(data: String, key: String): String {
        return xorEncrypt(data, key)
    }
    
    // ==================== 相似度计算 ====================
    
    /**
     * 莱文斯坦距离（编辑距离）
     */
    fun levenshteinDistance(s1: String, s2: String): Int {
        val m = s1.length
        val n = s2.length
        
        val dp = Array(m + 1) { IntArray(n + 1) }
        
        for (i in 0..m) dp[i][0] = i
        for (j in 0..n) dp[0][j] = j
        
        for (i in 1..m) {
            for (j in 1..n) {
                val cost = if (s1[i - 1] == s2[j - 1]) 0 else 1
                dp[i][j] = minOf(
                    dp[i - 1][j] + 1, // 删除
                    dp[i][j - 1] + 1, // 插入
                    dp[i - 1][j - 1] + cost // 替换
                )
                
                // 允许转置操作
                if (i > 1 && j > 1 && s1[i - 1] == s2[j - 2] && s1[i - 2] == s2[j - 1]) {
                    dp[i][j] = minOf(dp[i][j], dp[i - 2][j - 2] + cost)
                }
            }
        }
        
        return dp[m][n]
    }
    
    /**
     * Jaccard 相似度
     */
    fun jaccardSimilarity(set1: Set<String>, set2: Set<String>): Float {
        val intersection = set1.intersect(set2).size
        val union = set1.union(set2).size
        return if (union == 0) 0f else intersection.toFloat() / union
    }
    
    /**
     * 皮尔逊相关系数
     */
    fun pearsonCorrelation(x: FloatArray, y: FloatArray): Float {
        if (x.size != y.size) return 0f
        
        val n = x.size
        val sumX = x.sum()
        val sumY = y.sum()
        val sumXY = x.zip(y).sumOf { (it.first * it.second).toDouble() }
        val sumX2 = x.sumOf { (it * it).toDouble() }
        val sumY2 = y.sumOf { (it * it).toDouble() }
        
        val numerator = n * sumXY - sumX * sumY
        val denominator = kotlin.math.sqrt((n * sumX2 - sumX * sumX) * (n * sumY2 - sumY * sumY))
        
        return if (denominator == 0.0) 0f else (numerator / denominator).toFloat()
    }
}

/**
 * 推荐物品数据类
 */
data class RecommendedItem(
    val itemId: String,
    val score: Float,
    val weight: Float
)

/**
 * 霍夫曼树节点
 */
private class HuffmanNode(
    val char: Char?,
    val frequency: Int,
    val left: HuffmanNode? = null,
    val right: HuffmanNode? = null
) {
    fun generateCodes(codes: MutableMap<Char, String>, code: String) {
        if (char != null) {
            codes[char] = code.ifEmpty { "0" }
        } else {
            left?.generateCodes(codes, code + "0")
            right?.generateCodes(codes, code + "1")
        }
    }
}
