package com.app.spendlog

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
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
import com.bumptech.glide.Glide
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.component1
import com.google.firebase.storage.ktx.component2
import com.google.firebase.storage.ktx.storage
import java.io.ByteArrayOutputStream
import java.util.*

class testing : AppCompatActivity() {
    private val storage = Firebase.storage

    private val storageRef = storage.reference

    private var binding: ActivityTestingBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTestingBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        val imagesRef: StorageReference = storageRef.child("users").child("userId")
        val img = binding?.snappedImg

        binding?.testButton?.setOnClickListener {
            img?.isDrawingCacheEnabled = true
            img?.buildDrawingCache()
            val bitmap = (img?.drawable as BitmapDrawable).bitmap
            val baos = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 50, baos)
            val data = baos.toByteArray()
            val uploadTask = imagesRef.child("timestamp").putBytes(data)
            binding?.pause?.setOnClickListener {

                Glide.with(applicationContext)
                    .load(imagesRef.child("timestamp"))
                    .into(binding?.imgview!!).clearOnDetach()
            }
            binding?.stop?.setOnClickListener {
                uploadTask.cancel()
            }
            binding?.resume?.setOnClickListener {
                uploadTask.resume()
            }
            uploadTask.addOnProgressListener { /*(bytesTransferred, totalByteCount)*/ dd ->
                val progress = (100.0 * dd.bytesTransferred) / dd.totalByteCount
                binding?.progressIndicator?.setIndicatorColor(Color.parseColor("#000080"))
                // binding?.progressIndicator?.max = totalByteCount.toInt()
                binding?.progressIndicator?.progress = progress.toInt()
                LogUtil("Send ${dd.bytesTransferred}%")

            }.addOnPausedListener {
                binding?.progressIndicator?.setIndicatorColor(Color.parseColor("#FFA500"))
            }
            uploadTask.addOnFailureListener {
                LogUtil(it.message.toString())
                binding?.progressIndicator?.setIndicatorColor(Color.RED)

            }.addOnSuccessListener { taskSnapshot ->
                binding?.progressIndicator?.visibility = View.GONE
                //dialog?.dismiss()
                LogUtil(taskSnapshot.metadata.toString())
            }
        }


    }
}