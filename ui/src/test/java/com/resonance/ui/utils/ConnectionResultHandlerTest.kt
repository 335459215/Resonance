package com.resonance.ui.utils

import com.resonance.data.model.ConnectionTestResult
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * ConnectionResultHandler 单元测试
 * 测试连接结果处理功能
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class ConnectionResultHandlerTest {
    
    @Test
    fun testIsSuccess() {
        // 准备测试数据
        val successResult = ConnectionTestResult.Success
        val failureResult = ConnectionTestResult.Failure("连接失败", 1001)
        
        // 执行测试
        val isSuccess = ConnectionResultHandler.isSuccess(successResult)
        val isFailure = ConnectionResultHandler.isSuccess(failureResult)
        
        // 验证结果
        assertTrue(isSuccess, "成功结果应该返回 true")
        assertTrue(!isFailure, "失败结果应该返回 false")
    }
    
    @Test
    fun testGetErrorCode() {
        // 准备测试数据
        val successResult = ConnectionTestResult.Success
        val failureResult = ConnectionTestResult.Failure("连接失败", 1001)
        
        // 执行测试
        val successCode = ConnectionResultHandler.getErrorCode(successResult)
        val failureCode = ConnectionResultHandler.getErrorCode(failureResult)
        
        // 验证结果
        assertEquals(0, successCode, "成功错误码应该为 0")
        assertEquals(1001, failureCode, "失败错误码应该匹配")
    }
    
    @Test
    fun testGetErrorDetails() {
        // 准备测试数据
        val successResult = ConnectionTestResult.Success
        val failureResult = ConnectionTestResult.Failure("测试错误信息", 1002)
        
        // 执行测试
        val successDetails = ConnectionResultHandler.getErrorDetails(successResult)
        val failureDetails = ConnectionResultHandler.getErrorDetails(failureResult)
        
        // 验证结果
        assertEquals(null, successDetails, "成功应该没有错误详情")
        assertEquals("测试错误信息", failureDetails, "失败应该返回错误信息")
    }
    
    @Test
    fun testShowResult() {
        // 准备测试数据
        val context = RuntimeEnvironment.getApplication()
        val successResult = ConnectionTestResult.Success
        val failureResult = ConnectionTestResult.Failure("连接失败", 1001)
        
        // 执行测试
        ConnectionResultHandler.showResult(context, successResult)
        ConnectionResultHandler.showResult(context, failureResult)
        
        // 验证：这里主要测试方法不会抛出异常
        // 实际的 Toast 显示需要通过 UI 测试验证
    }
}
