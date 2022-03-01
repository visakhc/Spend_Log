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
import com.google.firebase.database.*


class HomeActivity : AppCompatActivity(), SpendAdapter.OnEachListener {
    private var mBudget = 0

    var modelList = mutableListOf<SpendModel>()
var arraySpendId = arrayListOf<String>()
    private val firebaseDatabase = FirebaseDatabase.getInstance()
    private val rootKey = firebaseDatabase.getReference("user1")
    private val spendKey = rootKey.child("spend")

    private var binding: ActivityHomeBinding? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        init()
    }

    private fun getSpendID() {
        arraySpendId.clear()
        spendKey.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val spendId = snapshot.childrenCount.toString()
                Log.d("as",spendId)
                arraySpendId.add(spendId)
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                val spendId = snapshot.key.toString()
                Log.d("SPENDID",spendId)
                arraySpendId.add(spendId)

            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
                TODO("Not yet implemented")
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                TODO("Not yet implemented")
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        }

        )
        Log.d("as","last")

        getSpendData()
    }

    private fun getSpendData() {
        modelList.clear()

        spendKey.child("0").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {

                if (dataSnapshot.exists()) {

                    val user = dataSnapshot.getValue(SpendModel::class.java)
                    modelList.add(user!!)
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
            }
        })
        setRecycler(modelList)
    }

    //TODO add settings and firebase and nightmode
    private fun init() {
        setBudget()

        binding?.spendRecycler?.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(this@HomeActivity)
        }
        getSpendID()
        handleEvents()
    }

    fun setRecycler(model: MutableList<SpendModel>) {
        binding?.spendRecycler?.adapter = SpendAdapter(model, this@HomeActivity)
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