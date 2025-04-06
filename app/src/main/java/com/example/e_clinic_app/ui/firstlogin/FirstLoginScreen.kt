package com.example.e_clinic_app.ui.firstlogin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.e_clinic_app.data.model.MedicalCondition
import com.example.e_clinic_app.ui.admin.components.MedicalConditionPicker
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FirstLoginScreen(
    onSubmitSuccess: () -> Unit
) {
    var fullName by remember { mutableStateOf("") }
    var dob by remember { mutableStateOf("") }
    var genderExpanded by remember { mutableStateOf(false) }
    var gender by remember { mutableStateOf("Select Gender") }
    var height by remember { mutableStateOf("") }
    var weight by remember { mutableStateOf("") }
    var conditions by remember { mutableStateOf("") }

    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showSuccessDialog by remember { mutableStateOf(false) }

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
            Text("Enter Your Medical Information", style = MaterialTheme.typography.headlineSmall)

            Button(
                onClick = {
                    // TODO: implement image upload & OCR to extract PESEL/DOB/etc
                    println("Upload placeholder clicked")
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("📷 Upload ID / Medical Record")
            }

            TextField(
                value = fullName,
                onValueChange = { fullName = it },
                label = { Text("Full Name") },
                modifier = Modifier.fillMaxWidth()
            )

            TextField(
                value = dob,
                onValueChange = { dob = it },
                label = { Text("Date of Birth (YYYY-MM-DD)") },
                modifier = Modifier.fillMaxWidth()
            )

            ExposedDropdownMenuBox(
                expanded = genderExpanded,
                onExpandedChange = { genderExpanded = !genderExpanded }
            ) {
                TextField(
                    value = gender,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Gender") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = genderExpanded) },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = genderExpanded,
                    onDismissRequest = { genderExpanded = false }
                ) {
                    listOf("Male", "Female", "Other").forEach {
                        DropdownMenuItem(
                            text = { Text(it) },
                            onClick = {
                                gender = it
                                genderExpanded = false
                            }
                        )
                    }
                }
            }

            TextField(
                value = height,
                onValueChange = { height = it },
                label = { Text("Height (cm)") },
                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            TextField(
                value = weight,
                onValueChange = { weight = it },
                label = { Text("Weight (kg)") },
                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            var knownConditions by remember { mutableStateOf<List<MedicalCondition>>(emptyList()) }

            MedicalConditionPicker(
                selectedConditions = knownConditions,
                onSelectionChanged = { knownConditions = it }
            )


            errorMessage?.let {
                Text(text = it, color = MaterialTheme.colorScheme.error)
            }

            Button(
                onClick = {
                    errorMessage = null

                    // Validation
                    if (fullName.isBlank() || dob.isBlank() || gender == "Select Gender") {
                        errorMessage = "Please fill in all required fields."
                        return@Button
                    }

                    val heightVal = height.toFloatOrNull()
                    val weightVal = weight.toFloatOrNull()
                    if (heightVal == null || heightVal <= 0 || weightVal == null || weightVal <= 0) {
                        errorMessage = "Height and weight must be valid numbers."
                        return@Button
                    }

                    val currentUser = FirebaseAuth.getInstance().currentUser
                    val db = FirebaseFirestore.getInstance()

                    if (currentUser != null) {
                        val uid = currentUser.uid
                        val data = mapOf(
                            "fullName" to fullName,
                            "dob" to dob,
                            "gender" to gender,
                            "height" to heightVal,
                            "weight" to weightVal,
                            "conditions" to conditions,
                            "submittedAt" to System.currentTimeMillis()
                        )

                        db.collection("users")
                            .document(uid)
                            .collection("profile")
                            .document("basicInfo")
                            .set(data)
                            .addOnSuccessListener {
                                showSuccessDialog = true
                            }
                            .addOnFailureListener {
                                errorMessage = "Failed to save data: ${it.message}"
                            }
                    } else {
                        errorMessage = "User not logged in."
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Submit")
            }
        }

        if (showSuccessDialog) {
            AlertDialog(
                onDismissRequest = {},
                confirmButton = {
                    TextButton(onClick = {
                        showSuccessDialog = false
                        onSubmitSuccess()
                    }) {
                        Text("Continue")
                    }
                },
                title = { Text("Success") },
                text = { Text("Your medical information has been saved.") }
            )
        }
    }
}