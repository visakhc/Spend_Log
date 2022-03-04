package com.app.spendlog.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.app.spendlog.databinding.ActivityLoginBinding
import com.app.spendlog.utils.SavedSession
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.SignInButton
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task


class LoginActivity : AppCompatActivity() {

    private val RC_SIGN_IN = 9001
    private var binding: ActivityLoginBinding? = null
    private  var mGoogleSignInClient: GoogleSignInClient?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        val gso =
            GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestEmail().build()
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)
        binding?.signInButton?.setSize(SignInButton.SIZE_WIDE)

        init()
    }

    private fun init() {
        handleEvents()
    }

    private fun handleEvents() {
        binding?.signInButton?.setOnClickListener {
            val intent = mGoogleSignInClient?.signInIntent
            startActivityForResult(intent, RC_SIGN_IN)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            val task: Task<GoogleSignInAccount> = GoogleSignIn.getSignedInAccountFromIntent(data)
            handleSignInResult(task)
        }
        else{
            Toast.makeText(this, "Failed to Login", Toast.LENGTH_SHORT).show()
        }
    }

    private fun handleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            val account = completedTask.getResult(ApiException::class.java)
            if (account != null) {
                SavedSession(this).apply {

                    putSharedString("userId", account.email.toString())
                    putSharedString("userName", account.displayName.toString())
                    putSharedString("userPic", account.photoUrl.toString())
                }

                val intent = Intent(this, HomeActivity::class.java)
                startActivity(intent)
                finish()
            } else
            Toast.makeText(this, "Failed to Login", Toast.LENGTH_SHORT).show()
        } catch (e: ApiException) {
            Log.w("TAG", "signInResult:failed code=" + e.statusCode)
        }
    }

}