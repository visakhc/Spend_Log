package com.app.spendlog.utils

import android.content.Context
import android.util.Log
import android.view.View
import com.app.spendlog.firebase.Child.BUDGET
import com.app.spendlog.firebase.Child.SPEND
import com.app.spendlog.firebase.Child.SPENDCOUNT
import com.app.spendlog.firebase.Child.TOTALSPEND
import com.app.spendlog.firebase.Constant.firebaseDatabase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage


class CommonUtil(context: Context, val listener: FirebaseCallbacks) {
    private val userId = SavedSession(context).getSharedString("userId")
    private var rootKey = firebaseDatabase.getReference(userId)


    fun getBudget(year: Int, month: Int) {
        rootKey.child(year.toString()).child(month.toString())
            .child(BUDGET).addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {
                        listener.onSuccess(true, BUDGET, dataSnapshot)
                    } else {
                        listener.onSuccess(false, BUDGET, dataSnapshot)
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    listener.onCancelled(databaseError)
                    Log.w("GAuthResp", "loadPost:onCancelled", databaseError.toException())
                }
            })
    }

    fun getBalance(year: Int, month: Int) {
        rootKey.child(year.toString()).child(month.toString())
            .child(TOTALSPEND).addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {
                        listener.onSuccess(true, TOTALSPEND, dataSnapshot)
                    } else {
                        listener.onSuccess(false, TOTALSPEND, dataSnapshot)
                    }
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }
//todo add cancelled to all
     fun getSpendItemCount(lastID: Int,year: Int, month: Int) {
        rootKey.child(SPEND).child(year.toString()).child(month.toString())
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val spendCount = dataSnapshot.childrenCount.toInt()
                    if (dataSnapshot.exists() && spendCount != lastID) {
                        listener.onSuccess(true, SPENDCOUNT, dataSnapshot)
                    } else {
                        listener.onSuccess(false, SPENDCOUNT, dataSnapshot)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                }
            })
    }

}

interface FirebaseCallbacks {
    fun onSuccess(status: Boolean, result: String, dataSnapshot: DataSnapshot)
    fun onCancelled(databaseError: DatabaseError)
}