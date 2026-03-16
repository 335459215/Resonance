package com.resonance.ui.utils

import android.util.Base64
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec

/**
 * 安全加密工具类
 * 提供密码加密和解密功能
 */
object SecurityUtils {
    
    private const val ALGORITHM = "AES"
    private const val TRANSFORMATION = "AES/ECB/PKCS5Padding"
    private const val KEY_SIZE = 128
    
    private var secretKey: SecretKey? = null
    
    /**
     * 生成或获取密钥
     */
    private fun getSecretKey(): SecretKey {
        if (secretKey == null) {
            val keyGenerator = KeyGenerator.getInstance(ALGORITHM)
            keyGenerator.init(KEY_SIZE)
            secretKey = keyGenerator.generateKey()
        }
        return secretKey!!
    }
    
    /**
     * 加密密码
     * @param password 原始密码
     * @return Base64 编码的加密字符串
     */
    fun encryptPassword(password: String): String {
        return try {
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.ENCRYPT_MODE, getSecretKey())
            val encryptedBytes = cipher.doFinal(password.toByteArray())
            Base64.encodeToString(encryptedBytes, Base64.DEFAULT)
        } catch (e: Exception) {
            e.printStackTrace()
            password // 如果加密失败，返回原始密码
        }
    }
    
    /**
     * 解密密码
     * @param encryptedPassword Base64 编码的加密字符串
     * @return 解密后的原始密码
     */
    fun decryptPassword(encryptedPassword: String): String {
        return try {
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.DECRYPT_MODE, getSecretKey())
            val decryptedBytes = cipher.doFinal(Base64.decode(encryptedPassword, Base64.DEFAULT))
            String(decryptedBytes)
        } catch (e: Exception) {
            e.printStackTrace()
            encryptedPassword // 如果解密失败，返回原始字符串
        }
    }
    
    /**
     * 检查 URL 是否为 HTTPS
     */
    fun isHttpsUrl(url: String): Boolean {
        return url.startsWith("https://", ignoreCase = true)
    }
    
    /**
     * 将 HTTP URL 转换为 HTTPS
     */
    fun convertToHttps(url: String): String {
        return if (url.startsWith("http://", ignoreCase = true)) {
            url.replaceFirst("http://", "https://", ignoreCase = true)
        } else {
            url
        }
    }
    
    /**
     * 验证 URL 安全性
     */
    fun validateUrlSecurity(url: String): Boolean {
        // 强制要求 HTTPS
        return isHttpsUrl(url)
    }
    
    /**
     * 哈希密码（用于存储）
     * 使用简单的 SHA-256 哈希（生产环境应该使用 BCrypt 等更安全的算法）
     */
    fun hashPassword(password: String): String {
        return try {
            val digest = java.security.MessageDigest.getInstance("SHA-256")
            val hashBytes = digest.digest(password.toByteArray())
            Base64.encodeToString(hashBytes, Base64.DEFAULT)
        } catch (e: Exception) {
            e.printStackTrace()
            password
        }
    }
}
