package com.example.e_clinic_app.ui.firstlogin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun DoctorFirstLoginScreen(
    onSubmitSuccess: () -> Unit
) {
    var fullName by remember { mutableStateOf("") }
    var specialization by remember { mutableStateOf("") }
    var experienceYears by remember { mutableStateOf("") }
    var licenseNumber by remember { mutableStateOf("") }
    var clinic by remember { mutableStateOf("") }
    var bio by remember { mutableStateOf("") }
    var availability by remember { mutableStateOf("") }

    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isSubmitting by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Doctor Profile Setup", style = MaterialTheme.typography.headlineSmall)

            TextField(
                value = fullName,
                onValueChange = { fullName = it },
                label = { Text("Full Name") },
                modifier = Modifier.fillMaxWidth()
            )

            TextField(
                value = specialization,
                onValueChange = { specialization = it },
                label = { Text("Specialization") },
                modifier = Modifier.fillMaxWidth()
            )

            TextField(
                value = experienceYears,
                onValueChange = { experienceYears = it },
                label = { Text("Years of Experience") },
                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            TextField(
                value = licenseNumber,
                onValueChange = { licenseNumber = it },
                label = { Text("Medical License Number") },
                modifier = Modifier.fillMaxWidth()
            )

            TextField(
                value = clinic,
                onValueChange = { clinic = it },
                label = { Text("Clinic / Hospital Affiliation") },
                modifier = Modifier.fillMaxWidth()
            )

            TextField(
                value = bio,
                onValueChange = { bio = it },
                label = { Text("Short Bio") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
            )

            TextField(
                value = availability,
                onValueChange = { availability = it },
                label = { Text("Availability (e.g., Mon–Fri 9–17)") },
                modifier = Modifier.fillMaxWidth()
            )

            errorMessage?.let {
                Text(text = it, color = MaterialTheme.colorScheme.error)
            }

            Button(
                onClick = {
                    errorMessage = null

                    // Basic validation
                    if (
                        fullName.isBlank() || specialization.isBlank() ||
                        experienceYears.toIntOrNull() == null || licenseNumber.isBlank()
                    ) {
                        errorMessage = "Please fill all required fields correctly."
                        return@Button
                    }

                    isSubmitting = true
                    val currentUser = FirebaseAuth.getInstance().currentUser
                    val db = FirebaseFirestore.getInstance()

                    currentUser?.let { user ->
                        val uid = user.uid
                        val doctorData = mapOf(
                            "fullName" to fullName,
                            "specialization" to specialization,
                            "experienceYears" to experienceYears.toInt(),
                            "licenseNumber" to licenseNumber,
                            "clinic" to clinic,
                            "bio" to bio,
                            "availability" to availability,
                            "submittedAt" to System.currentTimeMillis()
                        )

                        db.collection("users")
                            .document(uid)
                            .collection("profile")
                            .document("doctorInfo")
                            .set(doctorData)
                            .addOnSuccessListener {
                                isSubmitting = false
                                onSubmitSuccess()
                            }
                            .addOnFailureListener {
                                isSubmitting = false
                                errorMessage = "Failed to save data: ${it.message}"
                            }
                    } ?: run {
                        isSubmitting = false
                        errorMessage = "User not logged in."
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isSubmitting
            ) {
                Text("Save & Continue")
            }
        }
    }
}