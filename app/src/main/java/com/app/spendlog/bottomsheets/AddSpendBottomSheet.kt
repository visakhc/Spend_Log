package com.app.spendlog.bottomsheets

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.app.spendlog.R
import com.app.spendlog.databinding.BottomsheetAddSpendBinding
import com.app.spendlog.ui.HomeActivity
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.database.FirebaseDatabase
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
        handleEvents()
    }

    private fun handleEvents() {
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
            val month = DateFormatSymbols().shortMonths[month]
            mDate = "$month $day, $year"
        }


        binding?.fabSave?.setOnClickListener {
            val spendType = binding?.etSpendType?.text.toString()
            val amount = binding?.etAmount?.text.toString()

            if (spendType.isEmpty() || amount.isEmpty() || mDate.isEmpty()  || mTime.isEmpty()) {
                Toast.makeText(requireContext(), "Fill All Data", Toast.LENGTH_SHORT).show()
            } else {
                Log.d("DEBZ",spendType+"----"+amount+"----"+mDate+"----"+mTime)
                spendKey.child(mSpendId.toString()).child("spendType").setValue(spendType)
                spendKey.child(mSpendId.toString()).child("amount").setValue(amount)
                spendKey.child(mSpendId.toString()).child("date").setValue(mDate)
                spendKey.child(mSpendId.toString()).child("time").setValue(mTime)
                dialog?.dismiss()
                Toast.makeText(requireContext(), "Added", Toast.LENGTH_SHORT).show()

            }
        }
    }
}