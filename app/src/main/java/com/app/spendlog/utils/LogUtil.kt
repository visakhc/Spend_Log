package com.app.spendlog.utils

import android.util.Log

class LogUtil(message: String) {
    init {
        Log.d("SpendLog", "------->$message")

    }
}