package com.app.spendlog

import android.content.ContentValues
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.app.spendlog.databinding.ActivityTestingBinding
import com.app.spendlog.model.SpendModel
import com.app.spendlog.utils.LogUtil
import com.app.spendlog.utils.SavedSession
import com.google.firebase.database.*
import java.util.*

class testing : AppCompatActivity() {
    private val firebaseDatabase = FirebaseDatabase.getInstance()
    private lateinit var rootKey: DatabaseReference
    private var binding: ActivityTestingBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTestingBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        rootKey = firebaseDatabase.getReference("17578ef010a308a191baeeaf82f9a818")

        val btn = findViewById<Button>(R.id.test_button)
        btn.setOnClickListener {

            getSpendData(0)
        }
    }

    private fun getSpendData(spendCount: Int) {

        val today = Calendar.getInstance()

        val year = today.get(Calendar.YEAR).toString()
        val month = (today.get(Calendar.MONTH)+1).toString()
        val day = today.get(Calendar.DAY_OF_MONTH).toString()
        LogUtil("$day--$month--$year")

        rootKey.child("spend").child(year).child(month)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {
                       //  val user = dataSnapshot.getValue(SpendModel::class.java)
                        LogUtil(dataSnapshot.toString())

                    } else {
                        LogUtil("Error near line 136 HomeActivity")
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Log.w(ContentValues.TAG, "loadPost:onCancelled", databaseError.toException())
                }
            })
    }
}