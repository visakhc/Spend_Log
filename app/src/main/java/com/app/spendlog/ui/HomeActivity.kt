package com.app.spendlog.ui

import android.app.Dialog
import android.content.ContentValues.TAG
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
import androidx.appcompat.app.AppCompatDelegate
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
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.database.*
import com.google.firebase.database.annotations.Nullable
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*


class HomeActivity : AppCompatActivity(), SpendAdapter.OnEachListener {
    var mBudget = 0f
    private var modelList = mutableListOf<SpendModel>()
    private val firebaseDatabase = FirebaseDatabase.getInstance()
    private lateinit var rootKey: DatabaseReference
    private val today = Calendar.getInstance()
    private var mYear = today.get(Calendar.YEAR)
    private var mMonth = today.get(Calendar.MONTH)
    private var userId = ""

    //  val day = today.get(Calendar.DAY_OF_MONTH)
    private val storage = Firebase.storage
    private val storageRef = storage.reference

    private var binding: ActivityHomeBinding? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding?.root)
        userId = SavedSession(this).getSharedString("userId")
        rootKey = firebaseDatabase.getReference(userId)
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
        setBudget()
        getSpendItemCount()
        setNameImg()
        handleEvents()
    }

    private fun setBalance() {
        rootKey.child(mYear.toString()).child(mMonth.toString())
            .child("totalspend").addValueEventListener(object : ValueEventListener {
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
            binding?.inclLayout?.ivBack?.visibility = View.INVISIBLE
        }
    }

    private fun setBudget() {

        rootKey.child(mYear.toString()).child(mMonth.toString())
            .child("budget").addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    Log.d("GAuthResp", dataSnapshot.value.toString())
                    if (dataSnapshot.exists()) {
                        val post = dataSnapshot.value.toString()
                        mBudget = post.toFloat()
                        binding?.tvBudgetView?.text = post.toString()
                        setBalance()
                    } else {
                        mBudget = 0f
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
                        setBudget()
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

    private fun handleEvents() {
        binding?.inclLayout?.dayNightSwitch?.setListener { is_night ->
            SavedSession(this).putSharedBoolean("dark", is_night)
            if (is_night) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                Toast.makeText(this, "night", Toast.LENGTH_SHORT).show()
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                Toast.makeText(this, "day", Toast.LENGTH_SHORT).show()
            }
        }
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
            if (binding?.tvBudgetView?.text == "0" || binding?.tvBudgetView?.text == "0.0") {
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
                    LogUtil("LoadFailed")
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
            getSpendItemCount()
        }
        dialog.show()
    }

    private fun showBudgetDialog() {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.setCanceledOnTouchOutside(false)
        dialog.setContentView(R.layout.dialog_set_budget)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.findViewById<EditText>(R.id.et_add_amount).setText(mBudget.toInt().toString())
        dialog.findViewById<ImageView>(R.id.submit_budget).setOnClickListener {
            val budget = dialog.findViewById<EditText>(R.id.et_add_amount).text.toString()
            //  val budget = String.format("%.2f", amt)
            if (budget.isEmpty()) {
                dialog.dismiss()
            } else {
                if (budget.isNotEmpty()) {
                    rootKey.child(mYear.toString()).child(mMonth.toString())
                        .child("budget").setValue(budget).addOnSuccessListener {
                            setBudget()
                            //  setBalance()
                            dialog.dismiss()

                        }
                } else {
                    Log.d("ERROR", "HomeActivity line 241")
                }
            }
        }
        dialog.show()
    }

    //TOdo add functionality to delete a spend and make some of these functions to a constructor
    fun setRecycler(model: MutableList<SpendModel>) {
        binding?.spendRecycler?.adapter = SpendAdapter(model, this@HomeActivity)
        binding?.spendRecycler?.adapter?.notifyItemRangeChanged(0, model.size)
    }

    override fun onEachClick(position: Int) {
        showRecyclerItemPopup(position)
    }
}