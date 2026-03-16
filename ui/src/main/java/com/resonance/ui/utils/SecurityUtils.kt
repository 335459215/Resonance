package com.resonance.ui.utils

import android.security.KeyStoreParams
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/**
 * 安全加密工具类
 * 提供密码加密和解密功能（使用 AES/GCM/NoPadding 模式）
 */
object SecurityUtils {
    
    private const val ANDROID_KEY_STORE = "AndroidKeyStore"
    private const val KEY_ALIAS = "resonance_master_key"
    private const val TRANSFORMATION = "AES/GCM/NoPadding"
    private const val GCM_IV_LENGTH = 12
    private const val GCM_TAG_LENGTH = 128
    
    private val keyStore: KeyStore by lazy {
        KeyStore.getInstance(ANDROID_KEY_STORE).apply {
            load(null)
        }
    }
    
    /**
     * 生成或获取密钥（使用 Android KeyStore）
     */
    private fun getSecretKey(): SecretKey {
        val entry = keyStore.getEntry(KEY_ALIAS, null) as? KeyStore.SecretKeyEntry
        if (entry != null) {
            return entry.secretKey
        }
        
        // 生成新密钥
        val keyGenerator = KeyGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_AES,
            ANDROID_KEY_STORE
        )
        
        val keyGenParameterSpec = KeyGenParameterSpec.Builder(
            KEY_ALIAS,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setKeySize(256)
            .build()
        
        keyGenerator.init(keyGenParameterSpec)
        return keyGenerator.generateKey()
    }
    
    /**
     * 加密密码
     * @param password 原始密码
     * @return Base64 编码的加密字符串（IV + 密文）
     */
    fun encryptPassword(password: String): String {
        return try {
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.ENCRYPT_MODE, getSecretKey())
            
            val iv = cipher.iv
            val encryptedBytes = cipher.doFinal(password.toByteArray())
            
            // IV + 密文一起编码
            val combined = ByteArray(iv.size + encryptedBytes.size)
            System.arraycopy(iv, 0, combined, 0, iv.size)
            System.arraycopy(encryptedBytes, 0, combined, iv.size, encryptedBytes.size)
            
            Base64.encodeToString(combined, Base64.DEFAULT)
        } catch (e: Exception) {
            e.printStackTrace()
            password // 如果加密失败，返回原始密码
        }
    }
    
    /**
     * 解密密码
     * @param encryptedPassword Base64 编码的加密字符串（IV + 密文）
     * @return 解密后的原始密码
     */
    fun decryptPassword(encryptedPassword: String): String {
        return try {
            val combined = Base64.decode(encryptedPassword, Base64.DEFAULT)
            
            // 提取 IV 和密文
            val iv = combined.copyOf(GCM_IV_LENGTH)
            val encryptedBytes = combined.sliceArray(GCM_IV_LENGTH until combined.size)
            
            val cipher = Cipher.getInstance(TRANSFORMATION)
            val spec = GCMParameterSpec(GCM_TAG_LENGTH, iv)
            cipher.init(Cipher.DECRYPT_MODE, getSecretKey(), spec)
            
            val decryptedBytes = cipher.doFinal(encryptedBytes)
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
