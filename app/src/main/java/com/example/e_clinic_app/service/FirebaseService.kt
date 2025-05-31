package com.example.e_clinic_app.service

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

object FirebaseService {

    private const val TAG = "FirebaseService"

    private val auth: FirebaseAuth by lazy {
        FirebaseAuth.getInstance()
    }

    private val firestore: FirebaseFirestore by lazy {
        FirebaseFirestore.getInstance()
    }

    /**
     * Uploads the FCM token to Firestore under the current user's document.
     */
    fun uploadTokenToFirestore(token: String) {
        val currentUser = auth.currentUser

        if (currentUser != null) {
            val userDocRef = firestore.collection("users").document(currentUser.uid)

            userDocRef.update("fcmToken", token)
                .addOnSuccessListener {
                    Log.d(TAG, "FCM token successfully updated in Firestore.")
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Error updating FCM token in Firestore.", e)
                }
        } else {
            Log.w(TAG, "No authenticated user. Cannot upload FCM token.")
        }
    }
}