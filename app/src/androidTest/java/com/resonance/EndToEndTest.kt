package com.resonance

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class EndToEndTest {

    @get:Rule
    val activityScenarioRule = ActivityScenarioRule(MainActivity::class.java)

    @Test
    fun testFullNavigationFlow() {
        // 1. 验证主屏幕显示
        onView(withText("媒体库")).check(matches(isDisplayed()))
        onView(withText("网络")).check(matches(isDisplayed()))
        onView(withText("最近")).check(matches(isDisplayed()))
        onView(withText("收藏")).check(matches(isDisplayed()))
        onView(withText("设置")).check(matches(isDisplayed()))

        // 2. 导航到设置屏幕
        onView(withText("设置")).perform(click())
        onView(withText("设置")).check(matches(isDisplayed()))
        onView(withText("服务器管理")).check(matches(isDisplayed()))

        // 3. 导航到服务器管理屏幕
        onView(withText("服务器管理")).perform(click())
        onView(withText("服务器管理")).check(matches(isDisplayed()))
        onView(withText("Local Emby")).check(matches(isDisplayed()))
        onView(withText("115 Cloud")).check(matches(isDisplayed()))

        // 4. 返回设置屏幕
        onView(withText("返回")).perform(click())
        onView(withText("设置")).check(matches(isDisplayed()))

        // 5. 返回主屏幕
        onView(withText("返回")).perform(click())
        onView(withText("媒体库")).check(matches(isDisplayed()))
    }

    @Test
    fun testMediaLibraryNavigation() {
        // 1. 验证主屏幕显示
        onView(withText("媒体库")).check(matches(isDisplayed()))

        // 2. 点击媒体库
        onView(withText("媒体库")).perform(click())

        // 3. 验证媒体库内容加载（这里假设会显示一些媒体内容）
        // 注意：实际测试中需要根据具体的媒体库实现调整
        // 例如：onView(withText("电影")).check(matches(isDisplayed()))
        //       onView(withText("电视剧")).check(matches(isDisplayed()))
    }

    @Test
    fun testNetworkSectionNavigation() {
        // 1. 验证主屏幕显示
        onView(withText("网络")).check(matches(isDisplayed()))

        // 2. 点击网络
        onView(withText("网络")).perform(click())

        // 3. 验证网络内容加载
        // 注意：实际测试中需要根据具体的网络实现调整
    }

    @Test
    fun testRecentSectionNavigation() {
        // 1. 验证主屏幕显示
        onView(withText("最近")).check(matches(isDisplayed()))

        // 2. 点击最近
        onView(withText("最近")).perform(click())

        // 3. 验证最近内容加载
        // 注意：实际测试中需要根据具体的最近实现调整
    }

    @Test
    fun testFavoritesSectionNavigation() {
        // 1. 验证主屏幕显示
        onView(withText("收藏")).check(matches(isDisplayed()))

        // 2. 点击收藏
        onView(withText("收藏")).perform(click())

        // 3. 验证收藏内容加载
        // 注意：实际测试中需要根据具体的收藏实现调整
    }
}
