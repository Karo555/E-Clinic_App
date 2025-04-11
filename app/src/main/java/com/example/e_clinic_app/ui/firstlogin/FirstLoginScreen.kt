package com.example.e_clinic_app.ui.firstlogin

import android.util.Log
import com.example.e_clinic_app.ui.admin.components.MedicalConditionPicker
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.e_clinic_app.data.model.MedicalCondition
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

@Composable
fun FirstLoginScreen(
    onSubmitSuccess: () -> Unit,
    isEditing: Boolean = false
) {
    Log.d("FirstLoginScreen", "isEditing: $isEditing")
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var knownConditions by remember { mutableStateOf<List<MedicalCondition>>(emptyList()) }
    var isSaving by remember { mutableStateOf(false) }
    var showSuccessDialog by remember { mutableStateOf(false) }

    var firstNameError by remember { mutableStateOf(false) }
    var lastNameError by remember { mutableStateOf(false) }
    var conditionError by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()
    val db = FirebaseFirestore.getInstance()
    val user = FirebaseAuth.getInstance().currentUser

    // Optional: preload data if editing
    LaunchedEffect(isEditing) {
        Log.d("FirstLoginScreen", "Loading data for editing")
        if (isEditing && user != null) {
            Log.d("FirstLoginScreen", "User ID: ${user.uid}")
            val doc = db.collection("users").document(user.uid)
                .collection("profile").document("basicInfo").get().await()
            if (doc.exists()) {
                Log.d("FirstLoginScreen", "Document exists")
                firstName = doc.getString("firstName") ?: ""
                lastName = doc.getString("lastName") ?: ""
                @Suppress("UNCHECKED_CAST")
                val conditions = doc.get("knownConditions") as? List<Map<String, String>>
                knownConditions = conditions?.map {
                    MedicalCondition(it["category"] ?: "", it["type"])
                } ?: emptyList()
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = if (isEditing) "Update Your Medical Info" else "Patient Medical Form",
            style = MaterialTheme.typography.headlineMedium
        )

        OutlinedTextField(
            value = firstName,
            onValueChange = {
                firstName = it
                firstNameError = false
            },
            label = { Text("First Name") },
            modifier = Modifier.fillMaxWidth(),
            isError = firstNameError
        )
        if (firstNameError) Text("First name cannot be empty", color = MaterialTheme.colorScheme.error)

        OutlinedTextField(
            value = lastName,
            onValueChange = {
                lastName = it
                lastNameError = false
            },
            label = { Text("Last Name") },
            modifier = Modifier.fillMaxWidth(),
            isError = lastNameError
        )
        if (lastNameError) Text("Last name cannot be empty", color = MaterialTheme.colorScheme.error)

        MedicalConditionPicker(
            selectedConditions = knownConditions,
            onSelectionChanged = {
                knownConditions = it
                conditionError = false
            }
        )
        if (conditionError) Text("Please select at least one valid medical condition", color = MaterialTheme.colorScheme.error)

        Button(
            onClick = {
                Log.d("FirstLoginScreen", "Button clicked")
                // Validation
                var hasError = false
                if (firstName.isBlank()) {
                    firstNameError = true
                    hasError = true
                }
                if (lastName.isBlank()) {
                    lastNameError = true
                    hasError = true
                }
                if (knownConditions.isEmpty() || knownConditions.any { it.category == "Allergy" && it.type == null }) {
                    conditionError = true
                    hasError = true
                }

                if (!hasError && user != null) {
                    Log.d("FirstLoginScreen", "Saving data for user: ${user.uid}")
                    isSaving = true
                    val basicInfo = hashMapOf(
                        "firstName" to firstName,
                        "lastName" to lastName,
                        "knownConditions" to knownConditions.map {
                            mapOf("category" to it.category, "type" to it.type)
                        }
                    )

                    db.collection("users").document(user.uid)
                        .collection("profile")
                        .document("basicInfo")
                        .set(basicInfo)
                        .addOnSuccessListener {
                            Log.d("FirstLoginScreen", "Data saved successfully")
                            isSaving = false
                            showSuccessDialog = true
                        }
                        .addOnFailureListener {
                            Log.d("FirstLoginScreen", "Error saving data: ${it.message}")
                            isSaving = false
                        }
                }
            },
            enabled = !isSaving,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (isSaving) "Saving..." else if (isEditing) "Save" else "Submit")
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
                text = {
                    Text(if (isEditing) "Your information has been updated." else "Your medical information has been saved.")
                }
            )
        }
    }
}