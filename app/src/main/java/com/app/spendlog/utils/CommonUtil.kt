package com.app.spendlog.utils

import android.content.Context
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage

val firebaseDatabase = FirebaseDatabase.getInstance()

val storage = Firebase.storage
val storageRef = storage.reference

class CommonUtil {
}