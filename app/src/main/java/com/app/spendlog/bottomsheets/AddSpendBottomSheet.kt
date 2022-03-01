package com.app.spendlog.bottomsheets

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.app.spendlog.R
import com.app.spendlog.adapter.SpendAdapter
import com.app.spendlog.databinding.BottomsheetAddSpendBinding
import com.app.spendlog.ui.HomeActivity
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.DateFormatSymbols
import java.text.SimpleDateFormat
import java.util.*

class AddSpendBottomSheet : BottomSheetDialogFragment() {
    private var mSpendId = 0
    private val today = Calendar.getInstance()
    private var mTime = SimpleDateFormat("hh:mm a").format(today.time)
    private var mDate = SimpleDateFormat("MMM d, yyyy").format(today.time)
    private val firebaseDatabase = FirebaseDatabase.getInstance()
    private val rootKey = firebaseDatabase.getReference("user1")
    private val spendKey = rootKey.child("spend")
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

        init()
    }

    private fun init() {

        listeners()
        handleEvents()
    }


    private fun handleEvents() {
        binding?.fabSave?.setOnClickListener {
            val spendType = binding?.etSpendType?.text.toString()
            val amount = binding?.etAmount?.text.toString()
            val d = mSpendId

            if (spendType.isEmpty() || amount.isEmpty() || mDate.isEmpty() || mTime.isEmpty()) {
                Toast.makeText(requireContext(), "Fill All Data", Toast.LENGTH_SHORT).show()
            } else {
                /*  spendKey.child(d.toString()).child("spendType").setValue(spendType)
                      .addOnSuccessListener {
                          spendKey.child(d.toString()).child("amount").setValue(amount)
                              .addOnSuccessListener {
                                  spendKey.child(d.toString()).child("date").setValue(mDate)
                                      .addOnSuccessListener {
                                          spendKey.child(d.toString()).child("time")
                                              .setValue(mTime).addOnSuccessListener {
                                              dialog?.dismiss()
                                              Toast.makeText(
                                                  requireContext(),
                                                  "Added",
                                                  Toast.LENGTH_SHORT
                                              ).show()

                                          }

                                      }

                              }

                      }
  */
                spendKey.child(mSpendId.toString()).apply {
                    child("spendType").setValue(spendType)
                    child("amount").setValue(amount)
                    child("date").setValue(mDate)
                    child("time").setValue(mTime)
                }
                dialog?.dismiss()
                Toast.makeText(requireContext(), "Added", Toast.LENGTH_SHORT).show()

            }
        }
    }

    private fun listeners() {
   /*     spendKey.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val spendId = snapshot.childrenCount.toInt()
                if (snapshot.exists()) {
                    Log.d("SpendNN", spendId.toString())
                    mSpendId = spendId + 1
                } else
                    mSpendId = 1
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })*/

spendKey.get().addOnSuccessListener {
   val spendId = it.childrenCount.toInt()
    if (it.exists()) {
        Log.d("SpendNN", spendId.toString())
        mSpendId = spendId + 1
    } else
        mSpendId = 1
}

        binding?.tpTime?.setOnTimeChangedListener { timePicker, hour, minute ->
            var hr = hour
            var amPM = ""
            when {
                hr == 0 -> {
                    hr += 12
                    amPM = "am"
                }
                hr == 12 -> {
                    amPM = "pm"
                }
                hr > 12 -> {
                    hr -= 12
                    amPM = "pm"
                }
                else -> amPM = "am"
            }
            val hourq = if (hr < 10) "0$hr" else hr
            val minq = if (minute < 10) "0$minute" else minute
            mTime = "$hourq:$minq $amPM"

            Log.d("TIME", mTime)
        }

        binding?.dpDate?.init(
            today.get(Calendar.YEAR), today.get(Calendar.MONTH),
            today.get(Calendar.DAY_OF_MONTH)
        ) { _, year, month, day ->
            val emonth = DateFormatSymbols().shortMonths[month]
            mDate = "$emonth $day, $year"
        }
    }
}