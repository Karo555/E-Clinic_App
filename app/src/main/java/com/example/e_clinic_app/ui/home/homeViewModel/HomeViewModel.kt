package com.example.e_clinic_app.ui.home.homeViewModel

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.tasks.await

//class HomeViewModel: ViewModel() {
//    val user = FirebaseAuth.getInstance().currentUser
//    var role = MutableStateFlow<String?>(null)
//
//
//    fun getRole(): String {
//        val db = FirebaseFirestore.getInstance()
//        val userDoc = db.collection("users").document(uid).get().await()
//        role = userDoc.getString("role")
//    }
//
//}