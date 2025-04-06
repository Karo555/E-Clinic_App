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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext



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
                        CoroutineScope(Dispatchers.IO).launch {
                            try {
                                val doctorQuerySnapshot = db.collection("users")
                                    .whereEqualTo("role", "Doctor")
                                    .whereEqualTo("institutionId", institutionId)
                                    .get()
                                    .await()

                                Log.d("AdminDashboard", "Doctors returned by query: ${doctorQuerySnapshot.size()}")

                                val result = doctorQuerySnapshot.documents.mapNotNull { docSnapshot ->
                                    val uid = docSnapshot.id
                                    val profileSnapshot = db.collection("users")
                                        .document(uid)
                                        .collection("profile")
                                        .document("doctorInfo")
                                        .get()
                                        .await()

                                    if (profileSnapshot.exists()) {
                                        val firstName = profileSnapshot.getString("firstName") ?: "Unknown"
                                        val lastName = profileSnapshot.getString("lastName") ?: ""
                                        val specialization = profileSnapshot.getString("specialization") ?: "Unknown"

                                        Log.d("AdminDashboard", "Fetched profile for $uid: $firstName $lastName ($specialization)")

                                        DoctorDisplayData(
                                            uid = uid,
                                            fullName = "Dr. $firstName $lastName",
                                            specialization = specialization
                                        )
                                    } else {
                                        Log.d("AdminDashboard", "No profile found for UID: $uid")
                                        null
                                    }
                                }

                                withContext(Dispatchers.Main) {
                                    Log.d("AdminDashboard", "Final doctor display list size: ${result.size}")
                                    doctorsDisplayData = result
                                    doctorLoading = false
                                }

                            } catch (e: Exception) {
                                Log.e("AdminDashboard", "Error fetching doctor data: ${e.message}", e)
                                withContext(Dispatchers.Main) {
                                    doctorLoading = false
                                }
                            }
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