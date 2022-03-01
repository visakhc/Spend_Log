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
import com.firebase.ui.database.SnapshotParser
import com.google.firebase.database.*


class HomeActivity : AppCompatActivity(), SpendAdapter.OnEachListener {
    private var mBudget = 0
    private var lastID = -1
    var modelList = mutableListOf<SpendModel>()
    private val firebaseDatabase = FirebaseDatabase.getInstance()
    private val rootKey = firebaseDatabase.getReference("user1")
    private val spendKey = rootKey.child("spend")

    private var binding: ActivityHomeBinding? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding?.root)



        binding?.spendRecycler?.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(this@HomeActivity)
        }
        init()
    }

    private fun init() {
        setBudget()
        getSpendID()
        handleEvents()
    }


    private fun setBudget() {
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
    }


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
/*
        spendKey.get().addOnSuccessListener {
            Log.d("GETFunc", it.childrenCount.toString())
            val spendId = it.childrenCount.toInt()
            if (it.exists()) {
                getSpendData(spendId)
            }
        }*/
    }


    private fun getSpendData(spendCount: Int) {
        modelList.clear()
        for (i in 1..spendCount) {
            Log.d("spendSIze", "For loop $i")
            spendKey.child(i.toString()).addValueEventListener(object : ValueEventListener {

                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {
                        Log.d("spendSIze", dataSnapshot.toString())

                        val user = dataSnapshot.getValue(SpendModel::class.java)
                        modelList.add(user!!)
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
        binding?.inclLayout?.ivSettings?.setOnClickListener {
            getSpendID()
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
        Toast.makeText(this, "Hi----> $position", Toast.LENGTH_SHORT).show()
    }
}