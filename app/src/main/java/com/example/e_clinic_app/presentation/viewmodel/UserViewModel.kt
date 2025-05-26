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

class UserViewModel : ViewModel() {
    private val _role = MutableStateFlow<String?>(null)
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
