package com.app.spendlog.firebase

import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage

object Constant {
    val firebaseDatabase = FirebaseDatabase.getInstance()
    val storage = Firebase.storage
    val storageRef = storage.reference
}