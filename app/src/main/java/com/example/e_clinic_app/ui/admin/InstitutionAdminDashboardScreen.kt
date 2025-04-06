package com.example.e_clinic_app.ui.admin

import android.app.Activity
import android.content.Intent
import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.e_clinic_app.MainActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.example.e_clinic_app.ui.admin.components.DoctorListCard
import com.example.e_clinic_app.ui.admin.model.DoctorDisplayData
import com.google.firebase.firestore.QueryDocumentSnapshot
import com.google.android.gms.tasks.Tasks


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InstitutionAdminDashboardScreen() {
    val context = LocalContext.current
    val user = FirebaseAuth.getInstance().currentUser
    val db = FirebaseFirestore.getInstance()

    var institutionName by remember { mutableStateOf<String?>(null) }
    var doctors by remember { mutableStateOf<List<QueryDocumentSnapshot>>(emptyList()) }
    var doctorLoading by remember { mutableStateOf(true) }
    var loading by remember { mutableStateOf(true) }
    var doctorsDisplayData by remember { mutableStateOf<List<DoctorDisplayData>>(emptyList()) }


    LaunchedEffect(Unit) {
        val user = FirebaseAuth.getInstance().currentUser
        val db = FirebaseFirestore.getInstance()

        user?.uid?.let { uid ->
            db.collection("users").document(uid).get()
                .addOnSuccessListener { doc ->
                    val institutionId = doc.getString("institutionId")
                    Log.d("AdminDashboard", "Loaded admin institutionId: $institutionId") // ✅ ADD THIS


                    if (institutionId != null) {
                        // Fetch institution name
                        db.collection("institutions").document(institutionId)
                            .get()
                            .addOnSuccessListener { instDoc ->
                                val name = instDoc.getString("name")
                                Log.d("AdminDashboard", "Institution name fetched: $name") // ✅ Log what was retrieved
                                institutionName = name ?: "Unknown Institution"
                                loading = false
                            }
                            .addOnFailureListener {
                                institutionName = "Error loading institution"
                                loading = false
                            }

                        // Fetch doctors assigned to this institution
                        db.collection("users")
                            .whereEqualTo("role", "Doctor")
                            .whereEqualTo("institutionId", institutionId)
                            .get()
                            .addOnSuccessListener { doctorDocs ->
                                Log.d("AdminDashboard", "Doctors returned by query: ${doctorDocs.size()}") // ✅ ADD THIS
                                doctorDocs.forEach {
                                    Log.d("AdminDashboard", "Doctor UID: ${it.id}, institutionId: ${it.getString("institutionId")}") // ✅ ADD THIS
                                }
                                val doctorUids = doctorDocs.map { it.id }

                                val profileFetches = doctorUids.map { doctorUid ->
                                    db.collection("users").document(doctorUid)
                                        .collection("profile")
                                        .document("doctorInfo")
                                        .get()
                                        .continueWith { task ->
                                            val profile = task.result
                                            Log.d("AdminDashboard", "Fetched doctor profile for UID ${uid}: ${profile?.getString("firstName")}") // ✅ ADD THIS
                                            DoctorDisplayData(
                                                uid = doctorUid,
                                                fullName = "Dr. ${profile?.getString("firstName") ?: "Unknown"} ${profile?.getString("lastName") ?: ""}",
                                                specialization = profile?.getString("specialization") ?: "Unknown"
                                            )
                                        }
                                }

                                Tasks.whenAllSuccess<DoctorDisplayData>(profileFetches)
                                    .addOnSuccessListener { result ->
                                        doctorsDisplayData = result
                                        doctorLoading = false
                                        Log.d("AdminDashboard", "Successfully built doctor display list. Size: ${result.size}") // ✅ ADD THIS

                                    }
                                    .addOnFailureListener {
                                        doctorLoading = false
                                    }
                            }
                            .addOnFailureListener {
                                doctorLoading = false
                            }
                    } else {
                        institutionName = "No institution assigned"
                        loading = false
                        doctorLoading = false
                    }
                }
                .addOnFailureListener {
                    institutionName = "Error loading user info"
                    loading = false
                    doctorLoading = false
                }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("🏥 Institution Admin Dashboard")
                },
                actions = {
                    IconButton(onClick = {
                        FirebaseAuth.getInstance().signOut()
                        val intent = Intent(context, MainActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        }
                        context.startActivity(intent)
                        (context as? Activity)?.finish()
                    }) {
                        Icon(Icons.Default.Logout, contentDescription = "Log out")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (loading) {
                CircularProgressIndicator()
            } else {
                Text(
                    text = "Welcome, Institution Admin 👋",
                    style = MaterialTheme.typography.headlineSmall
                )
                Text(
                    text = "Institution: ${institutionName ?: "Unknown"}",
                    style = MaterialTheme.typography.bodyLarge
                )

                Spacer(modifier = Modifier.height(16.dp))

                Divider()

                Text("👩‍⚕️ Doctors in ${institutionName ?: "your institution"}", style = MaterialTheme.typography.titleMedium)

                if (doctorLoading) {
                    CircularProgressIndicator()
                } else if (doctors.isEmpty()) {
                    Text("No doctors found in this institution.")
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        doctorsDisplayData.forEach { doctor ->
                            DoctorListCard(
                                fullName = doctor.fullName,
                                specialization = doctor.specialization,
                                onClick = { /* placeholder for future actions */ }
                            )
                        }
                    }
                }

            }
        }
    }
}