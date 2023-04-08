package com.example.voicerecorddemo.untils

import android.content.Context
import android.content.res.Resources

/**
 * @Description: 屏幕高度/宽度获取，尺寸转换工具类
 * @Author: zouji
 * @CreateDate: 2023/4/8 12:10
 */
object ScreenUntil {
    // 屏幕宽度
    val widthPixels: Int
        get() = Resources.getSystem().displayMetrics.widthPixels

    // 屏幕高度
    val heightPixels: Int
        get() = Resources.getSystem().displayMetrics.heightPixels

    fun dip2px(context: Context?, dipValue: Float): Int {
        if (context == null) {
            return dipValue.toInt()
        }
        val scale = context.resources.displayMetrics.density
        return (dipValue * scale + 0.5f).toInt()
    }
}