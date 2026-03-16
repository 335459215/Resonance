package com.resonance.ui.utils

import android.content.Context
import android.widget.Toast
import com.resonance.data.model.ConnectionTestResult

/**
 * 连接结果提示工具类
 * 提供服务器连接测试结果的友好提示
 */
object ConnectionResultHandler {
    
    /**
     * 显示连接测试结果
     */
    fun showResult(context: Context, result: ConnectionTestResult) {
        when (result) {
            is ConnectionTestResult.Success -> {
                Toast.makeText(context, "连接成功", Toast.LENGTH_SHORT).show()
            }
            is ConnectionTestResult.Failure -> {
                val message = getFriendlyMessage(result)
                Toast.makeText(context, message, Toast.LENGTH_LONG).show()
            }
        }
    }
    
    /**
     * 获取友好的错误消息
     */
    private fun getFriendlyMessage(result: ConnectionTestResult.Failure): String {
        return when (result.errorCode) {
            1001 -> "无法连接到服务器，请检查：\n1. 网络连接是否正常\n2. 服务器地址是否正确\n3. 服务器是否在线"
            1002 -> "HTTP 连接失败，服务器可能未响应或地址错误"
            1003 -> "认证失败，请检查用户名和密码是否正确"
            else -> "连接失败：${result.message}"
        }
    }
    
    /**
     * 检查连接是否成功
     */
    fun isSuccess(result: ConnectionTestResult): Boolean {
        return result is ConnectionTestResult.Success
    }
    
    /**
     * 获取错误详情
     */
    fun getErrorDetails(result: ConnectionTestResult): String? {
        return when (result) {
            is ConnectionTestResult.Success -> null
            is ConnectionTestResult.Failure -> result.message
        }
    }
    
    /**
     * 获取错误码
     */
    fun getErrorCode(result: ConnectionTestResult): Int {
        return when (result) {
            is ConnectionTestResult.Success -> 0
            is ConnectionTestResult.Failure -> result.errorCode
        }
    }
}
