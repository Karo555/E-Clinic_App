package com.example.e_clinic_app.service

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

object FirebaseService {

    private const val TAG = "FirebaseService"

    private val auth: FirebaseAuth by lazy {
        FirebaseAuth.getInstance()
    }

    private val firestore: FirebaseFirestore by lazy {
        FirebaseFirestore.getInstance()
    }

    /**
     * Writes (or merges) the given FCM token into Firestore under users/{uid}.fcmToken.
     * Uses set(..., SetOptions.merge()) to avoid any "document does not exist" or missing-field issues.
     */
    fun uploadTokenToFirestore(token: String) {
        val currentUser = auth.currentUser

        if (currentUser == null) {
            Log.w(TAG, "uploadTokenToFirestore: no authenticated user; skipping token upload")
            return
        }

        val userDocRef = firestore.collection("users").document(currentUser.uid)

        // Using set with merge to add/overwrite only the fcmToken field,
        // preserving any other fields already present.
        val data = mapOf("fcmToken" to token)
        userDocRef.set(data, SetOptions.merge())
            .addOnSuccessListener {
                Log.d(TAG, "FCM token successfully written to Firestore.")
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error writing FCM token to Firestore.", e)
            }
    }
}