package com.lorenaortiz.arqueodata.auth

import android.app.Activity
import android.content.Context
import android.content.Intent
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.AuthResult
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.OnCompleteListener
import com.lorenaortiz.arqueodata.R

class GoogleAuthUiClient(private val context: Context) {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestIdToken(context.getString(R.string.default_web_client_id))
        .requestEmail()
        .build()
    val googleSignInClient: GoogleSignInClient = GoogleSignIn.getClient(context, gso)

    fun getSignInIntent(): Intent {
        // Forzar la selección de cuenta revocando el acceso
        googleSignInClient.revokeAccess()
        return googleSignInClient.signInIntent
    }

    fun signInWithIntent(data: Intent?, onResult: (Boolean, String?) -> Unit) {
        val task = GoogleSignIn.getSignedInAccountFromIntent(data)
        try {
            val account = task.getResult(ApiException::class.java)
            firebaseAuthWithGoogle(account, onResult)
        } catch (e: Exception) {
            onResult(false, e.localizedMessage)
        }
    }

    private fun firebaseAuthWithGoogle(account: GoogleSignInAccount?, onResult: (Boolean, String?) -> Unit) {
        val credential = GoogleAuthProvider.getCredential(account?.idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    if (user != null && user.email != null) {
                        onResult(true, null)
                    } else {
                        onResult(false, "No se pudo obtener el usuario de Google tras la autenticación.")
                    }
                } else {
                    onResult(false, task.exception?.localizedMessage)
                }
            }
    }

    fun signOut(onComplete: () -> Unit) {
        // Cerrar sesión en Firebase
        auth.signOut()
        
        // Cerrar sesión en Google
        googleSignInClient.signOut().addOnCompleteListener {
            onComplete()
        }
    }
} 