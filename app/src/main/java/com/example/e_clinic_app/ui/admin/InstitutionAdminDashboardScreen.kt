package com.example.e_clinic_app.ui.admin

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.e_clinic_app.ui.admin.model.DoctorDisplayData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import com.example.e_clinic_app.ui.admin.components.DoctorListCard
/**
 * A composable function that represents the Institution Admin Dashboard screen in the e-clinic application.
 *
 * This screen displays the institution's name and a list of doctors associated with the institution.
 * It fetches data from Firestore, including the institution's details and the doctors' profiles,
 * and provides a logout button for the admin.
 *
 * @param navController The `NavController` used for navigation between screens.
 */
@Composable
fun InstitutionAdminDashboardScreen(navController: NavController) {
    var loading by remember { mutableStateOf(true) }
    var doctorLoading by remember { mutableStateOf(true) }
    var institutionName by remember { mutableStateOf<String?>(null) }
    var doctorsDisplayData by remember { mutableStateOf<List<DoctorDisplayData>>(emptyList()) }

    val db = FirebaseFirestore.getInstance()
    val user = FirebaseAuth.getInstance().currentUser

    LaunchedEffect(Unit) {
        user?.uid?.let { uid ->
            try {
                val userDoc = db.collection("users").document(uid).get().await()
                val institutionId = userDoc.getString("institutionId")
                Log.d("AdminDashboard", "Loaded admin institutionId: $institutionId")

                if (institutionId != null) {
                    try {
                        val instDoc = db.collection("institutions").document(institutionId).get().await()
                        val name = instDoc.getString("name")
                        Log.d("AdminDashboard", "Institution name fetched: $name")
                        institutionName = name ?: "Unknown Institution"
                    } catch (e: Exception) {
                        institutionName = "Error loading institution"
                        Log.e("AdminDashboard", "Institution fetch failed: ${e.message}", e)
                    }

                    try {
                        val doctorQuerySnapshot = db.collection("users")
                            .whereEqualTo("role", "Doctor")
                            .whereEqualTo("institutionId", institutionId)
                            .get()
                            .await()

                        Log.d("AdminDashboard", "Doctors returned by query: ${doctorQuerySnapshot.size()}")

                        val result = doctorQuerySnapshot.documents.mapNotNull { docSnapshot ->
                            val doctorUid = docSnapshot.id
                            val profileSnapshot = db.collection("users")
                                .document(doctorUid)
                                .collection("profile")
                                .document("doctorInfo")
                                .get()
                                .await()

                            if (profileSnapshot.exists()) {
                                val firstName = profileSnapshot.getString("firstName") ?: "Unknown"
                                val lastName = profileSnapshot.getString("lastName") ?: ""
                                val specialization = profileSnapshot.getString("specialization") ?: "Unknown"

                                Log.d(
                                    "AdminDashboard",
                                    "Fetched profile for $doctorUid: $firstName $lastName ($specialization)"
                                )

                                DoctorDisplayData(
                                    uid = doctorUid,
                                    fullName = "Dr. $firstName $lastName",
                                    specialization = specialization
                                )
                            } else {
                                Log.d("AdminDashboard", "No profile found for UID: $doctorUid")
                                null
                            }
                        }

                        doctorsDisplayData = result
                        doctorLoading = false

                    } catch (e: Exception) {
                        Log.e("AdminDashboard", "Error fetching doctor data: ${e.message}", e)
                        doctorLoading = false
                    }

                } else {
                    institutionName = "No institution assigned"
                    doctorLoading = false
                }
            } catch (e: Exception) {
                institutionName = "Error loading user info"
                doctorLoading = false
                Log.e("AdminDashboard", "User fetch failed: ${e.message}", e)
            }

            loading = false
        }
    }

    if (loading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Institution Admin Dashboard",
                style = MaterialTheme.typography.headlineMedium
            )

            Text(
                text = "👨‍⚕️ Doctors in ${institutionName ?: "your institution"}",
                style = MaterialTheme.typography.titleMedium
            )

            if (doctorLoading) {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (doctorsDisplayData.isEmpty()) {
                Text("No doctors found in this institution.")
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    doctorsDisplayData.forEach { doctor ->
                        DoctorListCard(
                            fullName = doctor.fullName,
                            specialization = doctor.specialization,
                            onClick = {}
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            FirebaseAuth.getInstance().signOut()
                            navController.navigate("auth") {
                                popUpTo("home") { inclusive = true }
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Log Out")
                    }
                }
            }
        }
    }
}