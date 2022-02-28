package com.app.spendlog.ui
//TODO Add an mBudget increment variable for easy access

import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.app.spendlog.adapter.SpendAdapter
import com.app.spendlog.bottomsheets.AddSpendBottomSheet
import com.app.spendlog.databinding.ActivityHomeBinding
import com.app.spendlog.model.SpendModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


class HomeActivity : AppCompatActivity(), SpendAdapter.OnEachListener {
    private var mBudget = 0
    var modelList = mutableListOf<SpendModel>()

    private val firebaseDatabase = FirebaseDatabase.getInstance()
    private val root = firebaseDatabase.getReference("user1")
    private var binding: ActivityHomeBinding? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding?.root)


        init()
    }

    //TODO add settings and firebase and nightmode
    private fun init() {
        setBudget()
        setModelData()
        modelList.add(SpendModel("minus", "200.0", "21-02-2022", "8:08pm"))
        setRecycler(modelList)
        handleEvents()

    }

    private fun setModelData() {
        var spendType = ""
        var amount = ""
        var date = ""
        var time = ""

        root.child("spendType").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                spendType = dataSnapshot.value.toString()
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
            }
        })
        root.child("amount").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                amount = dataSnapshot.value.toString()
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
            }
        })
        root.child("date").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                date = dataSnapshot.value.toString()
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
            }
        })
        root.child("time").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                time = dataSnapshot.value.toString()
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
            }
        })
        modelList.add(SpendModel(spendType, amount, date, time))
    }


    private fun getSpend() {

    }

    private fun setRecycler(model: MutableList<SpendModel>) {
        binding?.spendRecycler?.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(this@HomeActivity)
            adapter = SpendAdapter(model, this@HomeActivity)
        }
    }

    private fun setBudget() {
        root.child("budget").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val post = dataSnapshot.value.toString()
                binding?.tvBudgetView?.text = post
                binding?.tvBudget?.text = post

            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
            }
        })


    }

    private fun handleEvents() {
        binding?.inclLayout?.ivSettings?.setOnClickListener {
            Toast.makeText(this, "ComingSoon..", Toast.LENGTH_SHORT).show()
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
            root.child("budget").setValue(mBudget.toString())
        }
        binding?.ivMinus?.setOnClickListener {
            while (mBudget >= 500) {
                mBudget -= 500
                root.child("budget").setValue(mBudget.toString())
            }
        }
        binding?.tvAddSpend?.setOnClickListener {
            AddSpendBottomSheet().show(supportFragmentManager, "addSpent")
        }

    }

    override fun onEachClick(position: Int) {
        Toast.makeText(this, "Hi----> $position", Toast.LENGTH_SHORT).show()
    }
}