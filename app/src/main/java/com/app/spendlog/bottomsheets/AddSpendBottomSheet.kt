package com.app.spendlog.bottomsheets

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.app.spendlog.MainActivity
import com.app.spendlog.R
import com.app.spendlog.databinding.BottomsheetAddSpendBinding
import com.app.spendlog.model.SpendModel
import com.app.spendlog.ui.HomeActivity
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.database.FirebaseDatabase

class AddSpendBottomSheet : BottomSheetDialogFragment() {
    private var mSpendId = 3
    private val firebaseDatabase = FirebaseDatabase.getInstance()
    private val root = firebaseDatabase.getReference("user1")
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
        binding?.fabSave?.setOnClickListener {
            val spendType = binding?.etSpendType?.text.toString()
            val amount = binding?.etAmount?.text.toString()
            val date = binding?.tvDate?.text.toString()
            val time = binding?.tvTime?.text.toString()

            if (spendType.isEmpty() || amount.isEmpty() || date.isEmpty() || time.isEmpty()) {
                Toast.makeText(requireContext(), "Fill All Data", Toast.LENGTH_SHORT).show()
            } else {
                root.child(mSpendId.toString()).child("spendType").setValue(spendType)
                root.child(mSpendId.toString()).child("amount").setValue(amount)
                root.child(mSpendId.toString()).child("date").setValue(date)
                root.child(mSpendId.toString()).child("time").setValue(time)
                dialog?.dismiss()
                Toast.makeText(requireContext(), "Added", Toast.LENGTH_SHORT).show()

            }
        }
    }
}