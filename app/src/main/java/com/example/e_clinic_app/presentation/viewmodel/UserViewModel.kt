package com.example.e_clinic_app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.launch
/**
 * ViewModel for managing the user's role in the e-clinic application.
 *
 * This ViewModel retrieves the role of the currently authenticated user
 * from the Firestore database and exposes it as a state flow.
 */
class UserViewModel : ViewModel() {
    private val _role = MutableStateFlow<String?>(null)
    /** A state flow containing the role of the currently authenticated user. */
    val role: StateFlow<String?> = _role

    init {
        viewModelScope.launch {
            FirebaseAuth.getInstance().currentUser?.uid?.let { uid ->
                try {
                    val snap = Firebase.firestore
                        .collection("users")
                        .document(uid)
                        .get()
                        .await()
                    _role.value = snap.getString("role")
                } catch (e: Exception) {
                    _role.value = null
                }
            }
        }
    }
}
