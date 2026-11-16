package com.example.practical.ui.auth

import android.content.Context
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider

class AuthManager(private val context: Context) {

    private var firebaseAuth: FirebaseAuth? = null

    init {
        try {
            if (FirebaseApp.getApps(context).isEmpty()) {
                FirebaseApp.initializeApp(context)
            }
            firebaseAuth = FirebaseAuth.getInstance()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getCurrentUser(): FirebaseUser? {
        return try {
            firebaseAuth?.currentUser
        } catch (e: Exception) {
            null
        }
    }

    fun getGoogleSignInClient(webClientId: String? = null): GoogleSignInClient {
        val gsoBuilder = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestProfile()

        if (!webClientId.isNullOrEmpty()) {
            gsoBuilder.requestIdToken(webClientId)
        }

        return GoogleSignIn.getClient(context, gsoBuilder.build())
    }

    fun handleGoogleAuth(idToken: String, callback: (Boolean, String?) -> Unit) {
        val auth = firebaseAuth
        if (auth == null) {
            callback(false, "Firebase Auth is not initialized. Please ensure google-services.json is configured.")
            return
        }

        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    callback(true, null)
                } else {
                    callback(false, task.exception?.localizedMessage ?: "Authentication failed")
                }
            }
    }

    fun signOut(client: GoogleSignInClient, onComplete: () -> Unit) {
        try {
            firebaseAuth?.signOut()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        client.signOut().addOnCompleteListener {
            onComplete()
        }
    }
}
