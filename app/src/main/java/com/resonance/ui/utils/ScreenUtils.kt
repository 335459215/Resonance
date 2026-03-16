package com.resonance.ui.utils

import android.app.Activity
import android.content.Context
import android.graphics.Rect
import android.util.DisplayMetrics
import android.view.WindowManager

object ScreenUtils {

    fun getScreenWidth(context: Context): Int {
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val displayMetrics = DisplayMetrics()
        @Suppress("DEPRECATION")
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        return displayMetrics.widthPixels
    }

    fun getScreenHeight(context: Context): Int {
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val displayMetrics = DisplayMetrics()
        @Suppress("DEPRECATION")
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        return displayMetrics.heightPixels
    }

    fun getScreenDensity(context: Context): Float {
        return context.resources.displayMetrics.density
    }

    fun getScreenDensityDpi(context: Context): Int {
        return context.resources.displayMetrics.densityDpi
    }

    fun dp2px(context: Context, dp: Float): Int {
        return (dp * getScreenDensity(context) + 0.5f).toInt()
    }

    fun px2dp(context: Context, px: Int): Float {
        return px / getScreenDensity(context)
    }

    fun getStatusBarHeight(context: Context): Int {
        val resources = context.resources
        val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
        return if (resourceId > 0) {
            resources.getDimensionPixelSize(resourceId)
        } else {
            0
        }
    }

    fun getNavigationBarHeight(context: Context): Int {
        val resources = context.resources
        val resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android")
        return if (resourceId > 0) {
            resources.getDimensionPixelSize(resourceId)
        } else {
            0
        }
    }

    fun isNavigationBarVisible(activity: Activity): Boolean {
        val decorView = activity.window.decorView
        val rect = Rect()
        decorView.getWindowVisibleDisplayFrame(rect)
        val screenHeight = getScreenHeight(activity)
        return screenHeight - rect.bottom > 0
    }
}