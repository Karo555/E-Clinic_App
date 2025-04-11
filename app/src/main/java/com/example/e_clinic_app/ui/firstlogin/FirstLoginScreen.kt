package com.example.e_clinic_app.ui.firstlogin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var knownConditions by remember { mutableStateOf<List<MedicalCondition>>(emptyList()) }
    var isSaving by remember { mutableStateOf(false) }
    var dob by remember { mutableStateOf("") }
    var genderExpanded by remember { mutableStateOf(false) }
    var gender by remember { mutableStateOf("Select Gender") }
    var height by remember { mutableStateOf("") }
    var weight by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showSuccessDialog by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()
    val db = FirebaseFirestore.getInstance()
    val currentUser = FirebaseAuth.getInstance().currentUser

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        //beginning of the column
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
                .verticalScroll(scrollState)
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
                value = firstName,
                onValueChange = { firstName = it },
                label = { Text("First Name") },
                modifier = Modifier.fillMaxWidth()
            )

            TextField(
                value = lastName,
                onValueChange = { lastName = it },
                label = { Text("Last Name") },
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
                    if (firstName.isBlank() || lastName.isBlank() || dob.isBlank() || gender == "Select Gender") {
                        errorMessage = "Please fill in all required fields."
                        return@Button
                    }

                    val heightVal = height.toFloatOrNull()
                    val weightVal = weight.toFloatOrNull()
                    if (heightVal == null || heightVal <= 0 || weightVal == null || weightVal <= 0) {
                        errorMessage = "Height and weight must be valid numbers."
                        return@Button
                    }

                    if (currentUser != null) {
                        val uid = currentUser.uid
                        val data = mapOf(
                            "firstName" to firstName,
                            "lastName" to lastName,
                            "dob" to dob,
                            "gender" to gender,
                            "height" to heightVal,
                            "weight" to weightVal,
                            "knownConditions" to knownConditions.map {
                                mapOf("category" to it.category, "type" to it.type)
                            },
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
        //end of the column


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