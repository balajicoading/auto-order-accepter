package com.autoaccept.app

import android.content.Context
import android.content.SharedPreferences

object Prefs {
    private const val PREFS_NAME = "autoaccept_prefs"
    private const val KEY_ENABLED = "enabled"
    private const val KEY_START_X = "start_x"
    private const val KEY_START_Y = "start_y"
    private const val KEY_END_X = "end_x"
    private const val KEY_END_Y = "end_y"
    private const val KEY_DURATION = "duration"
    private const val KEY_CLOSE_X = "close_x"
    private const val KEY_CLOSE_Y = "close_y"
    private const val KEY_THRESHOLD = "threshold"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun isEnabled(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_ENABLED, false)
    }

    fun setEnabled(context: Context, enabled: Boolean) {
        getPrefs(context).edit().putBoolean(KEY_ENABLED, enabled).apply()
    }

    fun getStartX(context: Context): Int {
        return getPrefs(context).getInt(KEY_START_X, 198)
    }

    fun setStartX(context: Context, value: Int) {
        getPrefs(context).edit().putInt(KEY_START_X, value).apply()
    }

    fun getStartY(context: Context): Int {
        return getPrefs(context).getInt(KEY_START_Y, 2212)
    }

    fun setStartY(context: Context, value: Int) {
        getPrefs(context).edit().putInt(KEY_START_Y, value).apply()
    }

    fun getEndX(context: Context): Int {
        return getPrefs(context).getInt(KEY_END_X, 904)
    }

    fun setEndX(context: Context, value: Int) {
        getPrefs(context).edit().putInt(KEY_END_X, value).apply()
    }

    fun getEndY(context: Context): Int {
        return getPrefs(context).getInt(KEY_END_Y, 2189)
    }

    fun setEndY(context: Context, value: Int) {
        getPrefs(context).edit().putInt(KEY_END_Y, value).apply()
    }

    fun getDuration(context: Context): Long {
        return getPrefs(context).getInt(KEY_DURATION, 400).toLong()
    }

    fun setDuration(context: Context, value: Long) {
        getPrefs(context).edit().putInt(KEY_DURATION, value.toInt()).apply()
    }

    fun getCloseX(context: Context): Int {
        return getPrefs(context).getInt(KEY_CLOSE_X, 924)
    }

    fun setCloseX(context: Context, value: Int) {
        getPrefs(context).edit().putInt(KEY_CLOSE_X, value).apply()
    }

    fun getCloseY(context: Context): Int {
        return getPrefs(context).getInt(KEY_CLOSE_Y, 239)
    }

    fun setCloseY(context: Context, value: Int) {
        getPrefs(context).edit().putInt(KEY_CLOSE_Y, value).apply()
    }

    fun getThreshold(context: Context): Double {
        return getPrefs(context).getFloat(KEY_THRESHOLD, 2.0f).toDouble()
    }

    fun setThreshold(context: Context, value: Double) {
        getPrefs(context).edit().putFloat(KEY_THRESHOLD, value.toFloat()).apply()
    }
}
