package com.app.spendlog

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.app.spendlog.databinding.ActivityTestingBinding
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage

class testing : AppCompatActivity() {
    private var binding: ActivityTestingBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTestingBinding.inflate(layoutInflater)
        setContentView(binding?.root)

    }
}