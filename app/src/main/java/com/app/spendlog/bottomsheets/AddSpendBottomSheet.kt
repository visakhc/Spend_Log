package com.app.spendlog.bottomsheets

import android.app.Dialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.ImageView
import android.widget.Toast
import androidx.core.net.toFile
import com.app.spendlog.R
import com.app.spendlog.databinding.BottomsheetAddSpendBinding
import com.app.spendlog.utils.LogUtil
import com.app.spendlog.utils.SavedSession
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import java.io.ByteArrayOutputStream
import java.text.DateFormatSymbols
import java.text.SimpleDateFormat
import java.util.*


class AddSpendBottomSheet : BottomSheetDialogFragment() {
    // private var mSpendId = 1
    private val today = Calendar.getInstance()
    private val PicID = 69
    private var mTime = SimpleDateFormat("hh:mm a", Locale.ENGLISH).format(today.time)
    private val storage = Firebase.storage
    private var storageRef = storage.reference

    private val firebaseDatabase = FirebaseDatabase.getInstance()
    private lateinit var rootKey: DatabaseReference
    private lateinit var imagesRef: StorageReference
    private var binding: BottomsheetAddSpendBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = BottomsheetAddSpendBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.BottomSheetDialogStyle)

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val userId = SavedSession(requireContext()).getSharedString("userId")
        rootKey = firebaseDatabase.getReference(userId)
        imagesRef = storageRef.child(userId)


        init()
    }

    private fun init() {
        val items =
            listOf("General", "Food", "Fuel", "Recharge", "Bills", "Movies", "Online Shopping")
        val adapter = ArrayAdapter(requireContext(), R.layout.item_spend_type, items)
        (binding?.tlType?.editText as? AutoCompleteTextView)?.setAdapter(adapter)
        listeners()
        handleEvents()
    }

    //add camera permission todo
    private fun showProfilePic(picUrl: Drawable?) {
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(true)
        dialog.setCanceledOnTouchOutside(true)
        dialog.setContentView(R.layout.dialog_image_view)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val imgView = dialog.findViewById<ImageView>(R.id.profilePic)
        Glide.with(this)
            .load(picUrl)
            .placeholder(R.drawable.ic_imageview_placeholder)
            .into(imgView)
        dialog.show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        // Match the request 'pic id with requestCode
        if (requestCode == PicID) {
            // BitMap is data structure of image file
            // which stor the image in memory
            val photo = data?.extras?.get("data") as Bitmap?
            binding?.cvSnapped?.visibility = View.VISIBLE
            binding?.tvSnapAPicture?.text = "retake"
            // Set the image in imageview for display
            binding?.snappedImg?.setImageBitmap(photo)
        }
    }

    private fun handleEvents() {
        binding?.snappedImg?.setOnClickListener {
            showProfilePic(binding?.snappedImg?.drawable)
        }
        binding?.addImg?.setOnClickListener {
            val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            startActivityForResult(cameraIntent, PicID)
        }
        binding?.fabSave?.setOnClickListener {
            val mMonth = binding?.dpDate?.month.toString()
            val mYear = binding?.dpDate?.year.toString()
            val mDay = binding?.dpDate?.dayOfMonth.toString()
            val month = DateFormatSymbols().shortMonths[mMonth.toInt()]
            val mDate = "$month $mDay, $mYear"
            val spendType = binding?.tlType?.editText?.text.toString()
            val amount = binding?.etAmount?.text.toString()
            var description = binding?.etDesc?.text.toString()
            var img = binding?.snappedImg
            var downloadUri = "null"
            if (spendType.isEmpty() || amount.isEmpty() || mDate.isEmpty() || mTime.isEmpty()) {
                Toast.makeText(requireContext(), "Fill All Data", Toast.LENGTH_SHORT).show()
            } else {
                if (description.isEmpty()) {
                    description = "null"
                }
                rootKey.child("spend").child(mYear).child(mMonth).get().addOnSuccessListener {
                    val spendId = it.childrenCount.toInt()

                    img?.isDrawingCacheEnabled = true
                    img?.buildDrawingCache()
                    val bitmap = (img?.drawable as BitmapDrawable).bitmap
                    val baos = ByteArrayOutputStream()
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 40, baos)
                    val data = baos.toByteArray()
                    val timestamp = System.currentTimeMillis().toString()
                    val uploadTask =
                        imagesRef.child(timestamp).putBytes(data)
                    uploadTask.continueWithTask { task ->
                        if (!task.isSuccessful) {
                            task.exception?.let {
                                throw it
                            }
                        }
                        imagesRef.child(timestamp).downloadUrl
                    }.addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            downloadUri = task.result.toString()
                            LogUtil(downloadUri)
                        } else {
                            Toast.makeText(
                                requireContext(),
                                "Failed Please try again",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                    uploadTask.addOnFailureListener {
                        LogUtil(it.message.toString())
                    }.addOnSuccessListener { taskSnapshot ->
                        Toast.makeText(requireContext(), "Added", Toast.LENGTH_SHORT).show()
                        LogUtil(taskSnapshot.metadata.toString())
                    }
//here todo
                    rootKey.child("spend")
                        .child(mYear)
                        .child(mMonth)
                        .child((spendId + 1).toString()).apply {
                            child("spendType").setValue(spendType)
                            child("amount").setValue(amount)
                            child("description").setValue(description)
                            child("snapid").setValue(downloadUri)
                            child("date").setValue(mDate)
                            child("time").setValue(mTime)
                            child("day").setValue(mDay)
                            child("month").setValue(mMonth)
                            child("year").setValue(mYear).addOnSuccessListener {
                                rootKey.child("totalspend").get().addOnSuccessListener {
                                    if (it.exists()) {
                                        val lastTotal = it.value.toString()
                                        rootKey.child("totalspend")
                                            .setValue(lastTotal.toFloat() + amount.toFloat())
                                    } else {
                                        rootKey.child("totalspend").setValue(amount)
                                    }
                                }
                                dialog?.dismiss()

                            }
                        }
                }

            }
        }
    }


    private fun listeners() {

        binding?.tpTime?.setOnTimeChangedListener { timePicker, hour, minute ->
            var hr = hour
            var amPM = ""
            when {
                hr == 0 -> {
                    hr += 12
                    amPM = "AM"
                }
                hr == 12 -> {
                    amPM = "PM"
                }
                hr > 12 -> {
                    hr -= 12
                    amPM = "PM"
                }
                else -> amPM = "AM"
            }
            val hourq = if (hr < 10) "0$hr" else hr
            val minq = if (minute < 10) "0$minute" else minute
            mTime = "$hourq:$minq $amPM"
        }

    }
}