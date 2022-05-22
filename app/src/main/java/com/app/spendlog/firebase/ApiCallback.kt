package com.app.spendlog.firebase

import android.content.Context
import android.util.Log
import com.app.spendlog.firebase.Childs.BUDGET
import com.app.spendlog.firebase.Childs.LEDGER
import com.app.spendlog.firebase.Childs.SPEND
import com.app.spendlog.firebase.Childs.SPENDCOUNT
import com.app.spendlog.firebase.Childs.TOTALSPEND
import com.app.spendlog.firebase.Constant.firebaseDatabase
import com.app.spendlog.utils.SavedSession
import com.app.spendlog.utils.logThis
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener


class ApiCallback(context: Context, val listener: FirebaseCallbacks) {
    private val userId = SavedSession(context).getSharedString("userId")
    private var rootKey = firebaseDatabase.getReference(userId)


    fun getBudget(year: Int, month: Int) {
         rootKey.child(LEDGER).child(year.toString()).child(month.toString())
                .child(BUDGET).addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        logThis("HEEEE   "+dataSnapshot.toString())
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

    fun getTotalspend(year: Int, month: Int) {
        rootKey.child(LEDGER).child(year.toString()).child(month.toString())
            .child(TOTALSPEND).addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {
                        //todo this is calling twice fix it
                        listener.onSuccess(true, TOTALSPEND, dataSnapshot)
                    } else {
                        listener.onSuccess(false, TOTALSPEND, dataSnapshot)
                    }
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }

    //todo add cancelled to all
    fun getSpendData( year: Int, month: Int) {
        rootKey.child(SPEND)
            .child(year.toString())
            .child(month.toString())
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {
                        listener.onSuccess(true, SPEND, dataSnapshot)
                    } else {
                        listener.onSuccess(false, SPEND, dataSnapshot)
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