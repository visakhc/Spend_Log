package com.app.spendlog

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast

class testing : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_testing)

        val num = findViewById<EditText>(R.id.test_et).text
        val btn = findViewById<Button>(R.id.test_button)
btn.setOnClickListener {

    val symbols = "0123456789."

        if (num.any {it in symbols}) {
            Toast.makeText(this, "aaaaa", Toast.LENGTH_SHORT).show()
        } else
            Toast.makeText(this, "9999", Toast.LENGTH_SHORT).show()

    }}

}