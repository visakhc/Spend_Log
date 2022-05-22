package com.app.spendlog.ui

import android.app.Dialog
import android.content.ContentValues.TAG
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.text.InputFilter
import android.util.Log
import android.view.View
import android.view.Window
import android.view.animation.AnimationUtils
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.app.spendlog.MoneyValueFilter
import com.app.spendlog.R
import com.app.spendlog.adapter.SpendAdapter
import com.app.spendlog.bottomsheets.AddSpendBottomSheet
import com.app.spendlog.bottomsheets.SettingsBottomSheet
import com.app.spendlog.databinding.ActivityHomeBinding
import com.app.spendlog.firebase.Childs.BUDGET
import com.app.spendlog.firebase.Childs.SPEND
import com.app.spendlog.firebase.Childs.SPENDCOUNT
import com.app.spendlog.firebase.Childs.TOTALSPEND
import com.app.spendlog.firebase.ApiCallback
import com.app.spendlog.firebase.Childs.LEDGER
import com.app.spendlog.firebase.Constant
import com.app.spendlog.firebase.Constant.storageRef
import com.app.spendlog.firebase.FirebaseCallbacks
import com.app.spendlog.model.SpendModel
import com.app.spendlog.ui.HomeActivity.Companion.totalSpend
import com.app.spendlog.utils.*
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.util.*


class HomeActivity : AppCompatActivity(), SpendAdapter.OnEachListener, FirebaseCallbacks {
    private lateinit var rootKey: DatabaseReference
    var mBudget: Double = 0.0
    private var modelList = mutableListOf<SpendModel>()
    private var snapshotList = mutableListOf<DatabaseReference>()
    private val today = Calendar.getInstance()
    private var mYear = today.get(Calendar.YEAR)
    private var mMonth = today.get(Calendar.MONTH)
    private var userId = ""
    private var commonUtil: ApiCallback? = null

    companion object {
        var totalSpend: Double = 0.0

    }

    // val day = today.get(Calendar.DAY_OF_MONTH)
    private var binding: ActivityHomeBinding? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        commonUtil = ApiCallback(this@HomeActivity, this)
        userId = SavedSession(this).getSharedString("userId")
        rootKey = Constant.firebaseDatabase.getReference(userId)

        binding?.spendRecycler?.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(this@HomeActivity)
        }
        //todo fix setrefreshlayout
        val dateFormat = SimpleDateFormat("MMMM", Locale.ENGLISH)
        val currentMonth = dateFormat.format(Date()).uppercase(Locale.ENGLISH)
        binding?.tvMonth?.text = currentMonth
        init()
    }

    private fun init() {
        //  getSpendItemCount(lastID, mYear, mMonth)
        commonUtil?.getSpendData(mYear, mMonth)
        setNameImg()
        handleEvents()
    }


    private fun setNameImg() {
        val userPicture = SavedSession(this).getSharedString("userPic")
        if (userPicture.length > 5) {
            Glide.with(this)
                .load(userPicture)
                .into(binding?.inclLayout?.ivBack!!)
        } else {
            binding?.inclLayout?.ivBack?.visibility = View.INVISIBLE
        }
    }


    private fun handleEvents() {

        binding?.inclLayout?.ivSettings?.setOnClickListener {
            SettingsBottomSheet().show(supportFragmentManager, "Settings")
        }
        binding?.inclLayout?.ivBack?.setOnClickListener {
            val userPicture = SavedSession(this).getSharedString("userPic")
            showProfilePic(userPicture)
        }
        binding?.tvMonth?.setOnClickListener {
            showDatePickerDialog()
        }
        binding?.cvBudget?.setOnClickListener {
            showBudgetDialog()
        }
        binding?.tvAddSpend?.setOnClickListener {
            if (mBudget <= 0) {
                Snackbar.make(this, it, "Add your Monthly Budget First !!", Snackbar.LENGTH_SHORT)
                    .show()
            } else {
                AddSpendBottomSheet().show(supportFragmentManager, "addSpent")
            }
        }
    }

    private fun showProfilePic(picUrl: String) {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(true)
        dialog.setCanceledOnTouchOutside(true)
        dialog.setContentView(R.layout.dialog_image_view)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val imgView = dialog.findViewById<ImageView>(R.id.profilePic)
        if (picUrl.length > 5) {
            Glide.with(this)
                .load(picUrl)
                .into(imgView)
        } else {
            imgView.setImageResource(R.drawable.ic_imageview_placeholder)
        }
        dialog.show()
    }

    private fun showRecyclerItemPopup(pos: Int) {
        val imagesRef =
            storageRef.child("users").child(userId).child((modelList[pos].snapimageid).toString())
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
        val imgBg = dialog.findViewById<ImageView>(R.id.iv_background)
        when (spendtype) {
            "General" -> imgBg.setImageResource(R.drawable.ic_rupees)
            "Food" -> imgBg.setImageResource(R.drawable.ic_food)
            "Fuel" -> imgBg.setImageResource(R.drawable.ic_fuel)
            "Recharge" -> imgBg.setImageResource(R.drawable.ic_recharge)
            "Bills" -> imgBg.setImageResource(R.drawable.ic_bill)
            "Movies" -> imgBg.setImageResource(R.drawable.ic_movies)
            "Online Shopping" -> imgBg.setImageResource(R.drawable.ic_shopping)
        }
        val rotate = AnimationUtils.loadAnimation(this, R.anim.rotate_left)
        imgBg.startAnimation(rotate)

        if (modelList[pos].description.isNullOrEmpty() || modelList[pos].description.toString() == "null") {
            dialog.findViewById<TextView>(R.id.tv_description).visibility = View.GONE
            dialog.findViewById<TextView>(R.id.tv_Desc).visibility = View.GONE
        } else {
            dialog.findViewById<TextView>(R.id.tv_description).text = modelList[pos].description
        }


        Glide.with(this@HomeActivity)
            .load(imagesRef)
            // .placeholder(R.drawable.ic_imageview_placeholder)
            .listener(object : RequestListener<Drawable> {
                override fun onLoadFailed(
                    p0: GlideException?,
                    p1: Any?,
                    p2: Target<Drawable>?,
                    p3: Boolean
                ): Boolean {
                    logThis("LoadFailed")
                    return false
                }

                override fun onResourceReady(
                    p0: Drawable?,
                    p1: Any?,
                    p2: Target<Drawable>?,
                    p3: DataSource?,
                    p4: Boolean
                ): Boolean {
                    dialog.findViewById<ImageView>(R.id.snap_img).visibility = View.VISIBLE
                    return false
                }
            })
            .into(dialog.findViewById(R.id.snap_img))

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

        val yearPicker = dialog.findViewById<NumberPicker>(R.id.picker_year)
        yearPicker.apply {
            minValue = 2000
            maxValue = mYear
            value = mYear
        }

        val monthPicker = dialog.findViewById<NumberPicker>(R.id.picker_month)
        monthPicker.apply {
            minValue = 0
            maxValue = items.lastIndex
            displayedValues = items
            value = mMonth
        }


        monthPicker.setOnValueChangedListener { picker, _, _ ->
            mMonth = picker.value
            binding?.tvMonth?.text = picker.displayedValues[mMonth].toString()
        }

        yearPicker.setOnValueChangedListener { picker, _, _ ->
            mYear = picker.value
        }

        dialog.setOnCancelListener {
            commonUtil?.getSpendData(mYear, mMonth)
        }
        dialog.show()
    }

    fun showBudgetDialog(context: Context = this) {
        logThis("thissss")
        val dialog = Dialog(context)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.setCanceledOnTouchOutside(false)
        dialog.setContentView(R.layout.dialog_set_budget)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.findViewById<EditText>(R.id.et_add_amount).setText(doubleToString(mBudget))
        val budgetView = dialog.findViewById<EditText>(R.id.et_add_amount)
        budgetView.filters = arrayOf<InputFilter>(MoneyValueFilter())
        dialog.findViewById<ImageView>(R.id.submit_budget).setOnClickListener {
            val budget = budgetView.text.toString()
            if (budget.isEmpty()) {
                budgetView.error = "this field cannot be empty"
            } else {
                if (budget.toDouble() < 1) {
                    budgetView.error = "minimum value is 1"
                } else {
                    //mBudget =budget.toDouble()
                    rootKey.child(LEDGER).child(mYear.toString()).child(mMonth.toString())
                        .child(BUDGET).setValue(budget.toDouble()).addOnSuccessListener {
                            dialog.dismiss()
                        }
                }
            }
        }
        dialog.show()
    }

    //TOdo add functionality to delete a spend and make some of these functions to a constructor
    private fun setRecycler(model: MutableList<SpendModel>) {
        binding?.spendRecycler?.adapter = SpendAdapter(model, this@HomeActivity)
        binding?.spendRecycler?.adapter?.notifyItemRangeChanged(0, model.size)
    }

    override fun onEachClick(position: Int) {
        showRecyclerItemPopup(position)
    }

    override fun onLongPress(position: Int) {
        snapshotList[position].removeValue()
    }

    override fun onSuccess(status: Boolean, result: String, dataSnapshot: DataSnapshot) {
        when (result) {
            BUDGET -> {
                if (status) {
                    val post = dataSnapshot.value.toString().toDouble()
                    mBudget = post
                    binding?.tvBudgetView?.text = post.toString()
                    commonUtil?.getTotalspend(mYear, mMonth)
                } else {
                    binding?.tvBalance?.text = "0"
                    binding?.tvSpendView?.text = "0"
                    showBudgetDialog()
                }
            }
            TOTALSPEND -> {
                if (status) {
                    totalSpend = dataSnapshot.value.toString().toDouble()
                    binding?.tvSpendView?.text = "Rs.$totalSpend"
                    val balance = mBudget - totalSpend
                    binding?.tvBalance?.text = "Rs.${balance}"
                } else {
                    totalSpend = 0.0
                    binding?.tvSpendView?.text = "0"
                    binding?.tvBalance?.text = mBudget.toString()
                }
            }
            SPEND -> {
                if (status) {
                    modelList.clear()
                    snapshotList.clear()
                    commonUtil?.getBudget(mYear, mMonth) // may this will fuck up todo
                    for (spends in dataSnapshot.children) {
                        snapshotList.asReversed().add(spends.ref)
                        logThis("spendRef" + spends.ref.toString())
                        val user = spends.getValue(SpendModel::class.java)
                        user?.let {
                            modelList.asReversed().add(it)
                        }
                        if (binding?.lottieView?.visibility == View.VISIBLE) {
                            binding?.lottieView?.visibility = View.GONE
                            binding?.tvMessage?.visibility = View.GONE
                        }
                        if (modelList.size == dataSnapshot.children.count()) {
                            setRecycler(modelList)
                        }
                    }

                } else {
                    totalSpend = 0.0
                    mBudget = 0.0
                    modelList.clear()
                    commonUtil?.getBudget(mYear, mMonth)
                    binding?.spendRecycler?.adapter?.notifyDataSetChanged()
                    if (binding?.lottieView?.visibility == View.GONE) {
                        binding?.lottieView?.visibility = View.VISIBLE
                        binding?.tvMessage?.visibility = View.VISIBLE
                    }
                }
            }
        }
    }

    override fun onCancelled(databaseError: DatabaseError) {
        Toast.makeText(
            this,
            "Error : ${databaseError.code}\n${databaseError.message}",
            Toast.LENGTH_SHORT
        )
            .show()
    }
}