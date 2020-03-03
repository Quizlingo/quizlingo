package com.quizlingo.verticalprototype

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.google.firebase.auth.FirebaseAuth

class AuthDemoActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private val RC_SIGN_IN = 123;
    private lateinit var signInButton: Button
    private lateinit var signOutButton: Button
    private lateinit var text: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth_demo)
        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()
        auth.signOut()
        signInButton = findViewById<Button>(R.id.signin)
        signOutButton = findViewById<Button>(R.id.signout)
        text = findViewById<TextView>(R.id.status)
        signInButton.setOnClickListener { launchSignInFlow() }
        signOutButton.setOnClickListener {
            AuthUI.getInstance().signOut(this).addOnCompleteListener {
                text.text = "Signed out!"
                signOutButton.visibility = LinearLayout.INVISIBLE
            }
        }
    }

    private fun launchSignInFlow() {
        // Give users the option to sign in / register with their email or Google account.
        // If users choose to register with their email,
        // they will need to create a password as well.
        val providers = arrayListOf(
            AuthUI.IdpConfig.EmailBuilder().build(), AuthUI.IdpConfig.GoogleBuilder().build()
        )

        // Create and launch the sign-in intent.
        // We listen to the response of this activity with the
        // SIGN_IN_REQUEST_CODE.
        startActivityForResult(
            AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(providers)
                .setIsSmartLockEnabled(false)
                .build(),
            RC_SIGN_IN
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            val response = IdpResponse.fromResultIntent(data)
            if (resultCode == Activity.RESULT_OK) {
                text.text = "Signed in: " + FirebaseAuth.getInstance().currentUser?.displayName
                signOutButton.visibility = LinearLayout.VISIBLE
            } else {
                Log.e("Firebase", "Sign in unsuccessful ${response?.error?.errorCode}")
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        auth.signOut()
    }
}
