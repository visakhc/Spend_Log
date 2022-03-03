package com.app.spendlog

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.app.spendlog.ui.HomeActivity
import com.app.spendlog.ui.LoginActivity
import com.app.spendlog.utils.SavedSession

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        val userId = SavedSession(this).getSharedString("userId")

        if (userId.isEmpty() || userId == "notfound" || userId.isBlank()) {
            Log.d("ERROR", "Error---->SharedPreferences is empty")
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        } else {
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}