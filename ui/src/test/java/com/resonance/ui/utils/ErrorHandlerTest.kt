package com.resonance.ui.utils

import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * ErrorHandler 单元测试
 * 测试错误处理和提示功能
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class ErrorHandlerTest {
    
    @Test
    fun testIsRetryable() {
        // 准备测试数据
        val retryableException = Exception("timeout")
        val nonRetryableException = Exception("401 Unauthorized")
        
        // 执行测试
        val isRetryable = ErrorHandler.isRetryable(retryableException)
        val isNotRetryable = ErrorHandler.isRetryable(nonRetryableException)
        
        // 验证结果
        assertTrue(isRetryable, "超时错误应该可重试")
        assertTrue(!isNotRetryable, "认证错误不应该可重试")
    }
    
    @Test
    fun testShowError() {
        // 准备测试数据
        val context = RuntimeEnvironment.getApplication()
        val errorMessage = "测试错误消息"
        
        // 执行测试（主要测试不会抛出异常）
        ErrorHandler.showError(context, errorMessage)
        
        // 验证：Toast 显示需要通过 UI 测试验证
    }
}
