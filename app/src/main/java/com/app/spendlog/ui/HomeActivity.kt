package com.app.spendlog.ui
//TODO Add an mBudget increment variable for easy access

import android.app.Dialog
import android.content.ContentValues.TAG
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.Window
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.app.spendlog.R
import com.app.spendlog.adapter.SpendAdapter
import com.app.spendlog.bottomsheets.AddSpendBottomSheet
import com.app.spendlog.bottomsheets.SettingsBottomSheet
import com.app.spendlog.databinding.ActivityHomeBinding
import com.app.spendlog.model.SpendModel
import com.app.spendlog.utils.SavedSession
import com.bumptech.glide.Glide
import com.google.firebase.database.*


class HomeActivity : AppCompatActivity(), SpendAdapter.OnEachListener {
    private var mBudget = 0
    private var lastID = -1
    private var modelList = mutableListOf<SpendModel>()
    private val firebaseDatabase = FirebaseDatabase.getInstance()
    private var rootKey = firebaseDatabase.getReference()
    private val spendKey = rootKey.child("spend")
    private var binding: ActivityHomeBinding? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding?.root)
        val userId = SavedSession(this).getSharedString("userId")
        rootKey = rootKey.child(userId)

        binding?.spendRecycler?.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(this@HomeActivity)
        }
        init()
    }

    private fun init() {
        setBudget()
        getSpendID()
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
            binding?.inclLayout?.ivBack?.setImageResource(R.drawable.ic_person)
        }
    }

    private fun setBudget() {
        val addedBudget = SavedSession(this).getSharedBoolean("BudgetAdded", false)

        if (addedBudget) {
            rootKey.child("budget").addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val post = dataSnapshot.value.toString()
                    binding?.tvBudgetView?.text = post
                    binding?.tvBudget?.text = post
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
                }
            })
        } else {
            binding?.clBudgetDialog?.visibility = View.VISIBLE
        }
    }
//TODO look if budget dialog click outside works

    fun getSpendID() {
        spendKey.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val spendId = snapshot.childrenCount.toInt()
                if (snapshot.exists() && spendId != lastID) {
                    lastID = spendId
                    getSpendData(spendId)
                } else {
                    Log.d("INNN", "Dont Mind Error")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        }
        )
    }


    private fun getSpendData(spendCount: Int) {
        modelList.clear()
        for (id in 1..spendCount) {
            spendKey.child(id.toString()).addValueEventListener(object : ValueEventListener {

                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {
                        val user = dataSnapshot.getValue(SpendModel::class.java)
                        modelList.asReversed().add(user!!)
                        setRecycler(modelList)

                    }
                }
                override fun onCancelled(databaseError: DatabaseError) {
                    Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
                }
            })
        }

    }

    fun setRecycler(model: MutableList<SpendModel>) {
        binding?.spendRecycler?.adapter = SpendAdapter(model, this@HomeActivity)
    }

    private fun handleEvents() {
        binding?.submitBudget?.setOnClickListener {
            val amount = binding?.etAmount?.text.toString()
            if (amount.isNotEmpty()) {
                rootKey.child("budget").setValue(amount).addOnSuccessListener {
                    SavedSession(this).putSharedBoolean("BudgetAdded", true)
                    setBudget()
                    binding?.clBudgetDialog?.visibility = View.GONE
                }
            } else {
                Log.d("ERROR", "HomeActivity line 48")
            }
        }
        binding?.inclLayout?.ivSettings?.setOnClickListener {
            SettingsBottomSheet().show(supportFragmentManager, "Settings")
        }
        binding?.inclLayout?.ivBack?.setOnClickListener {
            val userName = SavedSession(this).getSharedString("userName")
            Toast.makeText(this, "Coming Soon....$userName", Toast.LENGTH_SHORT).show()
        }

        binding?.cvBudget?.setOnClickListener {
            binding?.cvAdd?.visibility = View.VISIBLE
        }
        binding?.homeLayout?.setOnClickListener {
            if (binding?.cvAdd?.visibility == View.VISIBLE) {
                binding?.cvAdd?.visibility = View.GONE
            }
        }
        binding?.ivPlus?.setOnClickListener {
            mBudget += 500
            rootKey.child("budget").setValue(mBudget.toString())
        }
        binding?.ivMinus?.setOnClickListener {
            while (mBudget >= 500) {
                mBudget -= 500
                rootKey.child("budget").setValue(mBudget.toString())
            }
        }
        binding?.tvAddSpend?.setOnClickListener {
            AddSpendBottomSheet().show(supportFragmentManager, "addSpent")
        }
    }

    override fun onEachClick(position: Int) {
        showPopup(position)
    }

    private fun showPopup(pos: Int) {
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
        when (spendtype) {
            "General" -> dialog.findViewById<ImageView>(R.id.iv_background)
                .setImageResource(R.drawable.ic_rupees)
            "Food" -> dialog.findViewById<ImageView>(R.id.iv_background)
                .setImageResource(R.drawable.ic_food)
            "Fuel" -> dialog.findViewById<ImageView>(R.id.iv_background)
                .setImageResource(R.drawable.ic_fuel)
            "Recharge" -> dialog.findViewById<ImageView>(R.id.iv_background)
                .setImageResource(R.drawable.ic_recharge)
            "Bills" -> dialog.findViewById<ImageView>(R.id.iv_background)
                .setImageResource(R.drawable.ic_bill)
            "Movies" -> dialog.findViewById<ImageView>(R.id.iv_background)
                .setImageResource(R.drawable.ic_movies)
            "Online Shopping" -> dialog.findViewById<ImageView>(R.id.iv_background)
                .setImageResource(R.drawable.ic_shopping)
        }
        dialog.show()
    }
}