package com.example.e_clinic_app.ui.home

import androidx.compose.foundation.layout.Box
import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import com.example.e_clinic_app.ui.navigation.AppNavGraph
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

@Composable
fun HomeTabScreen(navController: NavController) {
    var role by remember { mutableStateOf<String?>(null) }
    var fullName by remember { mutableStateOf<String?>(null) }
    var specialization by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(true) }

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
                    }

                    "Patient" -> {
                        val patInfo = db.collection("users")
                            .document(uid)
                            .collection("profile")
                            .document("basicInfo")
                            .get()
                            .await()

                        fullName = patInfo.getString("fullName")
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

    if (isLoading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = "🏥 Welcome${if (!fullName.isNullOrBlank()) ", $fullName!" else "!"}",
                style = MaterialTheme.typography.headlineMedium
            )

            // change to nav controllers

            when (role) {
                "Doctor" -> navController.navigate("doctor_dashboard")

                "Patient" -> navController.navigate("patient_dashboard")

                else -> {
                    Text("⚠️ Unknown user role.")
                }
            }
        }
    }
}
