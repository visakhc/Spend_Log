package com.app.spendlog.bottomsheets

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.app.spendlog.ui.LoginActivity
import com.app.spendlog.R
import com.app.spendlog.databinding.BottomsheetSettingsBinding
import com.app.spendlog.ui.HomeActivity
import com.app.spendlog.utils.SavedSession
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class SettingsBottomSheet : BottomSheetDialogFragment() {
    private var binding: BottomsheetSettingsBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = BottomsheetSettingsBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.BottomSheetDialogStyle)

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        init()
    }

    private fun init() {
        handleEvents()
    }

    private fun handleEvents() {
        binding?.tvLogout?.setOnClickListener {
            SavedSession(requireContext()).clearSharedPrefs()
            clearGAuth()
        }
        binding?.tvAddBudget?.setOnClickListener {
            dialog?.dismiss()
            HomeActivity().showBudgetDialog(requireContext())
        }
    }

    private fun clearGAuth() {

        val gso =
            GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestEmail().build()
       val  mGoogleSignInClient = GoogleSignIn.getClient(requireContext(), gso)

        mGoogleSignInClient.signOut().addOnSuccessListener {
            dialog?.dismiss()
            val intent = Intent(requireContext(), LoginActivity::class.java)
            startActivity(intent)
            requireActivity().finish()
        }.addOnFailureListener {
            Toast.makeText(requireContext(), "Failed to Login with code:- ${it.message.toString()}", Toast.LENGTH_SHORT).show()
        }
    }

}