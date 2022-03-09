package com.app.spendlog.bottomsheets

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import com.app.spendlog.R
import com.app.spendlog.adapter.SpendAdapter
import com.app.spendlog.databinding.BottomsheetAddSpendBinding
import com.app.spendlog.ui.HomeActivity
import com.app.spendlog.utils.LogUtil
import com.app.spendlog.utils.SavedSession
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.database.*
import java.text.DateFormatSymbols
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt

class AddSpendBottomSheet : BottomSheetDialogFragment() {
    private var mSpendId = 0
    private val today = Calendar.getInstance()
    private var mMonth = ""
    private var mDay = ""
    private var mYear = ""
    private var mTime = SimpleDateFormat("hh:mm a", Locale.ENGLISH).format(today.time)
    private var mDate = SimpleDateFormat("MMM d, yyyy", Locale.ENGLISH).format(today.time)
    private val firebaseDatabase = FirebaseDatabase.getInstance()
    private lateinit var rootKey: DatabaseReference
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


    private fun handleEvents() {
        binding?.fabSave?.setOnClickListener {

            mMonth = binding?.dpDate?.month.toString()
            mYear = binding?.dpDate?.year.toString()
            mDay = binding?.dpDate?.dayOfMonth.toString()
            Log.d("Testqwqe", "month--> $mMonth    day---> $mDay   year----> $mYear")

            /* val spendType = binding?.tlType?.editText?.text.toString()
             val amount = binding?.etAmount?.text.toString()

             if (spendType.isEmpty() || amount.isEmpty() || mDate.isEmpty() || mTime.isEmpty()) {
                 Toast.makeText(requireContext(), "Fill All Data", Toast.LENGTH_SHORT).show()
             } else {
                 rootKey.child("spend").child(mSpendId.toString()).apply {
                     child("spendType").setValue(spendType)
                     child("amount").setValue(amount)
                     child("date").setValue(mDate)
                     child("time").setValue(mTime)
                 }
                 rootKey.child("totalspend").get().addOnSuccessListener {
                     if (it.exists()) {
                         val lastTotal = it.value.toString()
                         rootKey.child("totalspend").setValue(lastTotal.toFloat() + amount.toFloat())
                     } else {
                         rootKey.child("totalspend").setValue(amount)
                     }
                 }
                 dialog?.dismiss()
                 Toast.makeText(requireContext(), "Added", Toast.LENGTH_SHORT).show()
             }*/
        }
    }

    private fun listeners() {
        rootKey.child("spend").get().addOnSuccessListener {
            if (it.exists()) {
                val spendId = it.childrenCount.toInt()
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