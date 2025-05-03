package com.example.e_clinic_app.ui.home

import androidx.compose.foundation.layout.Box
import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.example.e_clinic_app.ui.navigation.Routes
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

@Composable
fun HomeTabScreen(navController: NavController) {
    var role by remember { mutableStateOf<String?>(null) }
    var fullName by remember { mutableStateOf<String?>(null) }
    var specialization by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var navigateTo by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            val uid = user.uid
            val db = FirebaseFirestore.getInstance()

            try {
                val userDoc = db.collection("users").document(uid).get().await()
                role = userDoc.getString("role")

                when (role) {
                    "Doctor" -> {
                        val docInfo = db.collection("users")
                            .document(uid)
                            .collection("profile")
                            .document("doctorInfo")
                            .get()
                            .await()

                        fullName = docInfo.getString("fullName")
                        specialization = docInfo.getString("specialization")
                        navigateTo = Routes.DOCTOR_HOME
                    }

                    "Patient" -> {
                        val patInfo = db.collection("users")
                            .document(uid)
                            .collection("profile")
                            .document("basicInfo")
                            .get()
                            .await()

                        fullName = patInfo.getString("fullName")
                        navigateTo = Routes.PATIENT_HOME
                    }

                    "Admin" -> {
                        fullName = "Admin"
                        navigateTo = Routes.ADMIN_HOME
                    }

                    else -> {
                        fullName = "Unknown User"
                    }
                }

            } catch (e: Exception) {
                Log.e("HomeTabScreen", "Error loading user info: ${e.message}")
                fullName = "Error loading user"
            } finally {
                isLoading = false
            }
        }
    }

    LaunchedEffect(navigateTo) {
        navigateTo?.let {
            navController.navigate(it) {
                popUpTo(Routes.HOME) { inclusive = true }
            }
        }
    }

    if (isLoading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    }
}
