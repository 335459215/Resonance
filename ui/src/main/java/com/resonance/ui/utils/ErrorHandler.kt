package com.resonance.ui.utils

import android.content.Context
import android.widget.Toast

/**
 * 错误处理工具类
 * 提供统一的错误处理和用户友好的错误提示
 */
object ErrorHandler {
    
    /**
     * 错误码定义
     */
    sealed class ErrorCode(val messageId: Int) {
        // 网络错误 (1000-1999)
        object NETWORK_TIMEOUT : ErrorCode(1001)
        object NETWORK_UNAVAILABLE : ErrorCode(1002)
        object NETWORK_NO_CONNECTION : ErrorCode(1003)
        
        // 服务器错误 (2000-2999)
        object SERVER_ERROR : ErrorCode(2001)
        object SERVER_UNAVAILABLE : ErrorCode(2002)
        object SERVER_AUTH_FAILED : ErrorCode(2003)
        
        // 客户端错误 (3000-3999)
        object INVALID_INPUT : ErrorCode(3001)
        object INVALID_URL : ErrorCode(3002)
        object INVALID_CREDENTIALS : ErrorCode(3003)
        
        // 业务错误 (4000-4999)
        object SERVER_NOT_FOUND : ErrorCode(4001)
        object SERVER_CONNECTION_FAILED : ErrorCode(4002)
        object SERVER_AUTH_ERROR : ErrorCode(4003)
        
        // 未知错误
        object UNKNOWN : ErrorCode(-1)
    }
    
    /**
     * 错误信息数据类
     */
    data class ErrorInfo(
        val code: ErrorCode,
        val message: String,
        val cause: Throwable? = null,
        val isRetryable: Boolean = false
    )
    
    /**
     * 显示错误提示（Toast）
     */
    fun showError(context: Context, error: Throwable) {
        val errorInfo = parseError(error)
        val message = getErrorMessage(context, errorInfo)
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }
    
    /**
     * 显示错误提示（Toast）- 自定义消息
     */
    fun showError(context: Context, message: String) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }
    
    /**
     * 显示错误提示（Toast）- 资源 ID
     */
    fun showError(context: Context, messageId: Int) {
        Toast.makeText(context, messageId, Toast.LENGTH_LONG).show()
    }
    
    /**
     * 解析错误
     */
    private fun parseError(error: Throwable): ErrorInfo {
        val message = error.message ?: "未知错误"
        
        return when {
            // 网络超时
            message.contains("timeout", ignoreCase = true) ||
            message.contains("Timeout", ignoreCase = true) -> {
                ErrorInfo(
                    code = ErrorCode.NETWORK_TIMEOUT,
                    message = message,
                    isRetryable = true
                )
            }
            
            // 网络连接失败
            message.contains("Unable to resolve host", ignoreCase = true) ||
            message.contains("Network is unreachable", ignoreCase = true) ||
            message.contains("Connection refused", ignoreCase = true) -> {
                ErrorInfo(
                    code = ErrorCode.NETWORK_NO_CONNECTION,
                    message = message,
                    isRetryable = true
                )
            }
            
            // 连接失败
            message.contains("Connection failed", ignoreCase = true) ||
            message.contains("connect failed", ignoreCase = true) -> {
                ErrorInfo(
                    code = ErrorCode.SERVER_CONNECTION_FAILED,
                    message = message,
                    isRetryable = true
                )
            }
            
            // 认证失败
            message.contains("401", ignoreCase = true) ||
            message.contains("Unauthorized", ignoreCase = true) ||
            message.contains("认证失败", ignoreCase = true) -> {
                ErrorInfo(
                    code = ErrorCode.SERVER_AUTH_FAILED,
                    message = message,
                    isRetryable = false
                )
            }
            
            // 服务器错误
            message.contains("500", ignoreCase = true) ||
            message.contains("502", ignoreCase = true) ||
            message.contains("503", ignoreCase = true) -> {
                ErrorInfo(
                    code = ErrorCode.SERVER_ERROR,
                    message = message,
                    isRetryable = true
                )
            }
            
            // 未找到
            message.contains("404", ignoreCase = true) ||
            message.contains("Not Found", ignoreCase = true) -> {
                ErrorInfo(
                    code = ErrorCode.SERVER_NOT_FOUND,
                    message = message,
                    isRetryable = false
                )
            }
            
            // 默认未知错误
            else -> {
                ErrorInfo(
                    code = ErrorCode.UNKNOWN,
                    message = message,
                    cause = error,
                    isRetryable = false
                )
            }
        }
    }
    
    /**
     * 获取友好的错误消息
     */
    private fun getErrorMessage(context: Context, errorInfo: ErrorInfo): String {
        return when (errorInfo.code) {
            is ErrorCode.NETWORK_TIMEOUT -> "网络连接超时，请检查网络设置"
            is ErrorCode.NETWORK_UNAVAILABLE -> "网络不可用，请检查网络连接"
            is ErrorCode.NETWORK_NO_CONNECTION -> "无法连接到网络，请检查网络设置"
            is ErrorCode.SERVER_ERROR -> "服务器错误，请稍后重试"
            is ErrorCode.SERVER_UNAVAILABLE -> "服务器不可用，请稍后重试"
            is ErrorCode.SERVER_AUTH_FAILED -> "认证失败，请检查用户名和密码"
            is ErrorCode.INVALID_INPUT -> "输入无效，请检查输入内容"
            is ErrorCode.INVALID_URL -> "URL 无效，请检查服务器地址"
            is ErrorCode.INVALID_CREDENTIALS -> "凭据无效，请重新登录"
            is ErrorCode.SERVER_NOT_FOUND -> "服务器未找到，请检查地址是否正确"
            is ErrorCode.SERVER_CONNECTION_FAILED -> "连接服务器失败，请检查网络或服务器状态"
            is ErrorCode.SERVER_AUTH_ERROR -> "认证错误，请重新登录"
            is ErrorCode.UNKNOWN -> "发生错误：${errorInfo.message}"
        }
    }
    
    /**
     * 检查错误是否可重试
     */
    fun isRetryable(error: Throwable): Boolean {
        return parseError(error).isRetryable
    }
    
    /**
     * 记录错误日志
     */
    fun logError(tag: String, error: Throwable) {
        android.util.Log.e(tag, "Error: ${error.message}", error)
    }
    
    /**
     * 记录错误日志
     */
    fun logError(tag: String, message: String) {
        android.util.Log.e(tag, message)
    }
}
