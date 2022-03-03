package com.app.spendlog.utils

import android.content.Context
import android.content.SharedPreferences
import android.opengl.ETC1.isValid

class SavedSession(var context: Context) {
    val sharedPref = context.getSharedPreferences("SpendLog", Context.MODE_PRIVATE)
    val editor = sharedPref?.edit()

    fun putSharedString(KEY: String?, value: String?) {
        editor?.putString(KEY, value)
        editor?.apply()
    }

    fun putSharedBoolean(KEY: String?, value: Boolean) {
        editor?.putBoolean(KEY, value)
        editor?.apply()
    }

    fun getSharedBoolean(KEY: String?, default: Boolean): Boolean {
        return sharedPref.getBoolean(KEY, default)
    }

    fun getSharedString(KEY: String?): String {
        return sharedPref.getString(KEY, "notfound").toString()
    }

    fun clearSharedString(KEY: String?) {
        editor?.remove(KEY)
        editor?.apply()
    }

    fun clearSharedPrefs() {
        editor?.clear()
        editor?.apply()
    }
}