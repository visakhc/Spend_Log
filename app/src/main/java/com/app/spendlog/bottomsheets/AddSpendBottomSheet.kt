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

    private var mTime = SimpleDateFormat("hh:mm a", Locale.ENGLISH).format(today.time)
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

            val mMonth = binding?.dpDate?.month
            val mYear = binding?.dpDate?.year
            val mDay = binding?.dpDate?.dayOfMonth
            val emonth = DateFormatSymbols().shortMonths[mMonth!!]
            val mDate = "$emonth $mDay, $mYear"
            getSpendId(mDay,mMonth,mYear)
            val spendType = binding?.tlType?.editText?.text.toString()
            val amount = binding?.etAmount?.text.toString()

            if (spendType.isEmpty() || amount.isEmpty() || mDate.isEmpty() || mTime.isEmpty() || mSpendId < 0) {
                Toast.makeText(requireContext(), "Fill All Data", Toast.LENGTH_SHORT).show()
            } else {
                rootKey.child("spend").child(mYear.toString()).child((mMonth).toString())
                    .child(mDay.toString())
                    .child(mSpendId.toString()).apply {
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
            }
        }
    }

    private fun getSpendId(mDay: Int?, mMonth: Int, mYear: Int?) {
        rootKey.child("spend").child(mYear.toString()).child((mMonth).toString())
            .child(mDay.toString()).get().addOnSuccessListener {
                if (it.exists()) {
                    val spendId = it.childrenCount.toInt()
                    mSpendId = spendId + 1
                } else
                    mSpendId = 1
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
            Log.d("Testqwqe",mTime)
        }

    }
}