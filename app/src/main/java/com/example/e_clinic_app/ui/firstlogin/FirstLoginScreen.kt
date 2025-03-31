package com.example.e_clinic_app.ui.firstlogin

import android.util.Log
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


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FirstLoginScreen() {
    var fullName by remember { mutableStateOf("") }
    var dob by remember { mutableStateOf("") } // we can use date picker later
    var genderExpanded by remember { mutableStateOf(false) }
    var gender by remember { mutableStateOf("Select Gender") }
    var height by remember { mutableStateOf("") }
    var weight by remember { mutableStateOf("") }
    var conditions by remember { mutableStateOf("") }

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

            TextField(
                value = conditions,
                onValueChange = { conditions = it },
                label = { Text("Known Medical Conditions") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
            )

            Button(
                onClick = {
                    val currentUser = FirebaseAuth.getInstance().currentUser
                    val db = FirebaseFirestore.getInstance()

                    if (currentUser != null) {
                        val uid = currentUser.uid
                        Log.d("FirstLogin", "User ID: $uid")

                        val data = mapOf(
                            "fullName" to fullName,
                            "dob" to dob,
                            "gender" to gender,
                            "height" to height,
                            "weight" to weight,
                            "conditions" to conditions,
                            "submittedAt" to System.currentTimeMillis()
                        )

                        Log.d("FirstLogin", "Attempting to save data: $data")

                        db.collection("users")
                            .document(uid)
                            .collection("profile")
                            .document("basicInfo")
                            .set(data)
                            .addOnSuccessListener {
                                Log.d("FirstLogin", "Medical data saved successfully.")
                            }
                            .addOnFailureListener {
                                Log.e("FirstLogin", "Failed to save medical data", it)
                            }
                    } else {
                        Log.e("FirstLogin", "No current user found. Cannot save medical data.")
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Submit")
            }
        }
    }
}