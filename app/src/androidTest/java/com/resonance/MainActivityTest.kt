package com.resonance

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import org.junit.Rule
import org.junit.Test

class MainActivityTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun testHomeScreenDisplaysCorrectly() {
        // 验证主屏幕是否正确显示
        composeTestRule.onNodeWithText("媒体库").assertIsDisplayed()
        composeTestRule.onNodeWithText("网络").assertIsDisplayed()
        composeTestRule.onNodeWithText("最近").assertIsDisplayed()
        composeTestRule.onNodeWithText("收藏").assertIsDisplayed()
        composeTestRule.onNodeWithText("设置").assertIsDisplayed()
    }

    @Test
    fun testNavigateToSettingsScreen() {
        // 点击设置按钮
        composeTestRule.onNodeWithText("设置").performClick()
        
        // 验证设置屏幕是否显示
        composeTestRule.onNodeWithText("设置").assertIsDisplayed()
        composeTestRule.onNodeWithText("服务器管理").assertIsDisplayed()
    }

    @Test
    fun testNavigateToServerManagementScreen() {
        // 先导航到设置屏幕
        composeTestRule.onNodeWithText("设置").performClick()
        
        // 点击服务器管理
        composeTestRule.onNodeWithText("服务器管理").performClick()
        
        // 验证服务器管理屏幕是否显示
        composeTestRule.onNodeWithText("服务器管理").assertIsDisplayed()
        composeTestRule.onNodeWithText("Local Emby").assertIsDisplayed()
        composeTestRule.onNodeWithText("115 Cloud").assertIsDisplayed()
    }

    @Test
    fun testNavigateBackFromSettingsToHome() {
        // 先导航到设置屏幕
        composeTestRule.onNodeWithText("设置").performClick()
        
        // 点击返回按钮
        composeTestRule.onNodeWithText("返回").performClick()
        
        // 验证是否返回主屏幕
        composeTestRule.onNodeWithText("媒体库").assertIsDisplayed()
    }

    @Test
    fun testNavigateBackFromServerManagementToSettings() {
        // 先导航到设置屏幕
        composeTestRule.onNodeWithText("设置").performClick()
        
        // 点击服务器管理
        composeTestRule.onNodeWithText("服务器管理").performClick()
        
        // 点击返回按钮
        composeTestRule.onNodeWithText("返回").performClick()
        
        // 验证是否返回设置屏幕
        composeTestRule.onNodeWithText("设置").assertIsDisplayed()
    }
}
