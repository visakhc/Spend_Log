package com.app.spendlog.ui

import android.app.Dialog
import android.content.ContentValues.TAG
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.Window
import android.view.animation.AnimationUtils
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.app.spendlog.R
import com.app.spendlog.adapter.SpendAdapter
import com.app.spendlog.bottomsheets.AddSpendBottomSheet
import com.app.spendlog.bottomsheets.SettingsBottomSheet
import com.app.spendlog.databinding.ActivityHomeBinding
import com.app.spendlog.model.SpendModel
import com.app.spendlog.utils.LogUtil
import com.app.spendlog.utils.SavedSession
import com.bumptech.glide.Glide
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.database.*
import com.mahfa.dnswitch.DayNightSwitch
import com.mahfa.dnswitch.DayNightSwitchListener
import java.text.SimpleDateFormat
import java.util.*


class HomeActivity : AppCompatActivity(), SpendAdapter.OnEachListener {
    var mBudget = 0f

    //    private var lastID = -1
    private var modelList = mutableListOf<SpendModel>()
    private val firebaseDatabase = FirebaseDatabase.getInstance()
    private lateinit var rootKey: DatabaseReference

    private val today = Calendar.getInstance()

    private var mYear = today.get(Calendar.YEAR)
    private var mMonth = today.get(Calendar.MONTH)
    //  val day = today.get(Calendar.DAY_OF_MONTH)

    private var binding: ActivityHomeBinding? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        val userId = SavedSession(this).getSharedString("userId")
        rootKey = firebaseDatabase.getReference(userId)
        binding?.spendRecycler?.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(this@HomeActivity)
        }
        //todo fix setrefreshlayout
        binding?.swipeHomeLayout?.setDistanceToTriggerSync(800)
        val dateFormat = SimpleDateFormat("MMMM", Locale.ENGLISH)
        val currentMonth = dateFormat.format(Date()).uppercase(Locale.ENGLISH)
        binding?.tvMonth?.text = currentMonth
        init()
    }

    private fun init() {
        setTheme()
        setBudget()
        getSpendItemCount()
        setNameImg()
        handleEvents()
    }

    private fun setTheme() {

    }

    private fun setBalance() {
        rootKey.child("totalspend").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    val totalSpend = dataSnapshot.value.toString()
                    binding?.tvBalance?.text = (mBudget - totalSpend.toFloat()).toString()
                } else {
                    binding?.tvBalance?.text = mBudget.toString()
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun setNameImg() {
        val userPicture = SavedSession(this).getSharedString("userPic")

        if (userPicture.length > 5) {
            Glide.with(this)
                .load(userPicture)
                .into(binding?.inclLayout?.ivBack!!)
        } else {
            binding?.inclLayout?.ivBack?.setImageResource(R.drawable.ic_person)
        }
    }

    private fun setBudget() {

        rootKey.child("budget").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                Log.d("GAuthResp", dataSnapshot.value.toString())
                if (dataSnapshot.exists()) {
                    val post = dataSnapshot.value.toString()
                    mBudget = post.toFloat()
                    binding?.tvBudgetView?.text = post.toString()
                    setBalance()
                } else {
                    showBudgetDialog()
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.w("GAuthResp", "loadPost:onCancelled", databaseError.toException())
            }
        })
    }

    private fun getSpendItemCount() {
        var lastID = -1
        LogUtil("dpend $mYear --- $mMonth")
        rootKey.child("spend").child(mYear.toString()).child(mMonth.toString())
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val spendCount = snapshot.childrenCount.toInt()
                    if (snapshot.exists() && spendCount != lastID) {
                        if (binding?.lottieView?.visibility == View.VISIBLE) {
                            binding?.lottieView?.visibility = View.GONE
                            binding?.tvMessage?.visibility = View.GONE
                        }
                        lastID = spendCount
                        getSpendData(spendCount)
                    } else {
                        modelList.clear()
                        binding?.spendRecycler?.adapter?.notifyDataSetChanged()
                        if (binding?.lottieView?.visibility == View.GONE) {
                            binding?.lottieView?.visibility = View.VISIBLE
                            binding?.tvMessage?.visibility = View.VISIBLE
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }
            }
            )
    }

    private fun getSpendData(spendCount: Int) {
        modelList.clear()
        for (id in 1..spendCount) {
            rootKey.child("spend")
                .child(mYear.toString())
                .child(mMonth.toString())
                .child(id.toString())
                .addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        if (dataSnapshot.exists()) {
                            val user = dataSnapshot.getValue(SpendModel::class.java)
                            modelList.asReversed().add(user!!)
                            if (binding?.lottieView?.visibility == View.VISIBLE) {
                                binding?.lottieView?.visibility = View.GONE
                                binding?.tvMessage?.visibility = View.GONE
                            }
                            if (modelList.size == spendCount) {
                                setRecycler(modelList)
                            }


                        } else {
                            Log.w("ERROR", "Error near line 136 HomeActivity")
                        }
                    }

                    override fun onCancelled(databaseError: DatabaseError) {
                        Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
                    }
                })

        }

    }

    //TOdo add functionality to delete a spend and make some of these functions to a constructor
    fun setRecycler(model: MutableList<SpendModel>) {
        binding?.spendRecycler?.adapter = SpendAdapter(model, this@HomeActivity)
        binding?.spendRecycler?.adapter?.notifyItemRangeChanged(0, model.size)
    }

    private fun handleEvents() {
        binding?.inclLayout?.dayNightSwitch?.setListener { is_night ->
            SavedSession(this).putSharedBoolean("dark",is_night)
            if (is_night) {
                Toast.makeText(this, "night", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "day", Toast.LENGTH_SHORT).show()
            }
        }


        //  day_night_switch.setDuration(450)


        binding?.swipeHomeLayout?.setOnRefreshListener {
            recreate()
        }
        binding?.inclLayout?.ivSettings?.setOnClickListener {
            SettingsBottomSheet().show(supportFragmentManager, "Settings")
        }
        binding?.inclLayout?.ivBack?.setOnClickListener {
            val userName = SavedSession(this).getSharedString("userName")
            Toast.makeText(this, "Coming Soon....$userName", Toast.LENGTH_SHORT).show()
        }
        binding?.tvMonth?.setOnClickListener {
            showDatePickerDialog()
        }

        binding?.cvBudget?.setOnClickListener {
            showBudgetDialog()
        }

        binding?.tvAddSpend?.setOnClickListener {
            if (binding?.tvBudgetView?.text == "0") {
                Snackbar.make(this, it, "Add your Monthly Budget First !!", Snackbar.LENGTH_SHORT)
                    .show()
            } else {
                AddSpendBottomSheet().show(supportFragmentManager, "addSpent")
            }
        }
    }

    override fun onEachClick(position: Int) {
        showRecyclerItemPopup(position)
    }

    private fun showRecyclerItemPopup(pos: Int) {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(true)
        dialog.setCanceledOnTouchOutside(true)
        dialog.setContentView(R.layout.dialog_item_layout)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.findViewById<TextView>(R.id.tv_amount).text = modelList[pos].amount
        dialog.findViewById<TextView>(R.id.tv_date).text = modelList[pos].date
        dialog.findViewById<TextView>(R.id.tv_time).text = modelList[pos].time
        val spendtype = modelList[pos].spendType
        dialog.findViewById<TextView>(R.id.tv_type).text = spendtype
        val imgBack = dialog.findViewById<ImageView>(R.id.iv_background)
        when (spendtype) {
            "General" -> imgBack.setImageResource(R.drawable.ic_rupees)
            "Food" -> imgBack.setImageResource(R.drawable.ic_food)
            "Fuel" -> imgBack.setImageResource(R.drawable.ic_fuel)
            "Recharge" -> imgBack.setImageResource(R.drawable.ic_recharge)
            "Bills" -> imgBack.setImageResource(R.drawable.ic_bill)
            "Movies" -> imgBack.setImageResource(R.drawable.ic_movies)
            "Online Shopping" -> imgBack.setImageResource(R.drawable.ic_shopping)
        }
        imgBack.startAnimation(AnimationUtils.loadAnimation(this, R.anim.rotate_left))
        dialog.show()
    }

    private fun showDatePickerDialog() {
        val items = resources.getStringArray(R.array.months_array)
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(true)
        dialog.setCanceledOnTouchOutside(true)
        dialog.setContentView(R.layout.dialog_date_selector)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val monthPicker = dialog.findViewById<NumberPicker>(R.id.picker_month)
        monthPicker.apply {
            minValue = 0
            maxValue = items.size - 1
            displayedValues = items
            value = mMonth
        }

        val yearPicker = dialog.findViewById<NumberPicker>(R.id.picker_year)
        yearPicker.apply {
            minValue = 1900
            maxValue = 2900
            value = mYear
        }

        monthPicker.setOnValueChangedListener { picker, _, _ ->
            mMonth = picker.value
            binding?.tvMonth?.text = picker.displayedValues[mMonth].toString()
        }

        yearPicker.setOnValueChangedListener { picker, _, _ ->
            mYear = picker.value
        }

        dialog.setOnCancelListener {
            getSpendItemCount()
        }
        dialog.show()
    }

    private fun showBudgetDialog() {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(true)
        dialog.setCanceledOnTouchOutside(true)
        dialog.setContentView(R.layout.dialog_set_budget)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.findViewById<EditText>(R.id.et_add_amount).setText(mBudget.toString())
        dialog.findViewById<ImageView>(R.id.submit_budget).setOnClickListener {
            val amt = dialog.findViewById<EditText>(R.id.et_add_amount).text.toString()
            if (amt.isNullOrEmpty()) {
                dialog.dismiss()
            } else {
                if (amt.isNotEmpty()) {
                    rootKey.child("budget").setValue(amt).addOnSuccessListener {
                        dialog.dismiss()
                        setBudget()
                        setBalance()
                    }
                } else {
                    Log.d("ERROR", "HomeActivity line 241")
                }
            }
        }
        dialog.show()
    }
}