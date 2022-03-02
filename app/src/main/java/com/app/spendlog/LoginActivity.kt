package com.app.spendlog

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.annotation.Nullable
import androidx.appcompat.app.AppCompatActivity
import com.app.spendlog.databinding.ActivityMainBinding
import com.app.spendlog.ui.HomeActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.SignInButton
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task


class LoginActivity : AppCompatActivity() {
    private val RC_SIGN_IN = 9001
    private var mGoogleSignInClient: GoogleSignInClient? = null
    private var binding: ActivityMainBinding? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        init()
    }

    private fun init() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .build()
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)
        binding?.signInButton?.setSize(SignInButton.SIZE_WIDE)

        handleEvents()
    }

    private fun handleEvents() {
        binding?.signInButton?.setOnClickListener {
            startActivityForResult(mGoogleSignInClient?.signInIntent, RC_SIGN_IN)
        }
    }
    public fun signOut(context: Context) {
        mGoogleSignInClient?.signOut()
            ?.addOnCompleteListener(this) {
                val intent = Intent(context,LoginActivity::class.java)
                startActivity(intent)
                finish()
                updateUI(null)

                Log.w("TAhiuiG", "account sign out ")

            }
    }

//Do signout TODO
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            val task: Task<GoogleSignInAccount> = GoogleSignIn.getSignedInAccountFromIntent(data)
            handleSignInResult(task)
        }
    }

    private fun handleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            val account = completedTask.getResult(ApiException::class.java)
            if (account != null) {
                val intent = Intent(this, HomeActivity::class.java)
                intent.putExtra("userId", account.email.toString())
                intent.putExtra("userName", account.displayName.toString())
                intent.putExtra("userPic", account.photoUrl.toString())
                startActivity(intent)
                finish()
            } else
                updateUI(null)
        } catch (e: ApiException) {
            Log.w("TAG", "signInResult:failed code=" + e.statusCode)
        }
    }

    override fun onStart() {
        super.onStart()
        val account = GoogleSignIn.getLastSignedInAccount(this)
        if (account != null) {
            val intent = Intent(this, HomeActivity::class.java)
            intent.putExtra("userId", account.email.toString())
            intent.putExtra("userName", account.displayName.toString())
            intent.putExtra("userPic", account.photoUrl.toString())
            startActivity(intent)
            finish()
        } else
            updateUI(null)
    }

    private fun updateUI(@Nullable account: GoogleSignInAccount?) {
        if (account != null) {
            findViewById<View>(R.id.sign_in_button).visibility = View.GONE
        } else {
            findViewById<View>(R.id.sign_in_button).visibility = View.VISIBLE
        }
    }
}