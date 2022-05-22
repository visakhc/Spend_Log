package com.app.spendlog.utils

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.permissionx.guolindev.PermissionX

fun doubleToString(d: Double): String {
    return String.format("%.2f", d)
}
fun logThis(message: String){
    Log.d("SpendLog","----->$message")
}
fun Context.shortToast(message: String){
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
}
fun Fragment.shortToast(message: String){
    Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
}

fun Context.longToast(message: String){
    Toast.makeText(this, message, Toast.LENGTH_LONG).show()
}
fun Fragment.longToast(message: String){
    Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
}

fun FragmentActivity.permissionX(permissions: List<String>): Boolean {
    var bool = false
    PermissionX.init(this)
        .permissions(
            permissions
        )
        .onExplainRequestReason { scope, deniedList ->
            scope.showRequestReasonDialog(
                deniedList,
                "This feature needs these permissions to work.",
                "OK",
                "Cancel"
            )
        }
        .onForwardToSettings { scope, deniedList ->
            scope.showForwardToSettingsDialog(
                deniedList,
                "You need to allow necessary permissions in Settings manually",
                "OK",
                "Cancel"
            )
        }
        .request { allGranted, grantedList, deniedList ->
            bool = allGranted
        }
    return bool
}
fun Fragment.permissionX(permissions: List<String>): Boolean {
    var bool = false
    PermissionX.init(this)
        .permissions(
            permissions
        )
        .onExplainRequestReason { scope, deniedList ->
            scope.showRequestReasonDialog(
                deniedList,
                "This feature needs these permissions to work.",
                "OK",
                "Cancel"
            )
        }
        .onForwardToSettings { scope, deniedList ->
            scope.showForwardToSettingsDialog(
                deniedList,
                "You need to allow necessary permissions in Settings manually",
                "OK",
                "Cancel"
            )
        }
        .request { allGranted, grantedList, deniedList ->
            bool = allGranted
        }
    return bool
}