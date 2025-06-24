package com.example.e_clinic_app.ui.admin.tools

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.e_clinic_app.data.users.patient.Patient
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManagePatientsScreen(navController: NavController) {
    val db = Firebase.firestore
    var patients by remember { mutableStateOf<List<Patient>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        try {
            patients = PatientResponse.fetchPatients()
            isLoading = false
        } catch (e: Exception) {
            errorMessage = e.message ?: "Unknown error occurred"
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Manage Patients", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Go back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            when {
                isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                errorMessage != null -> {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                    ) {
                        Text(
                            text = errorMessage!!,
                            modifier = Modifier.padding(16.dp),
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            isLoading = true
                            errorMessage = null
                        },
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    ) {
                        Text("Retry")
                    }
                }

                patients.isEmpty() -> {
                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "No patients found",
                            modifier = Modifier
                                .padding(32.dp)
                                .fillMaxWidth(),
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }

                else -> {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        items(patients, key = { it.id }) { patient ->
                            PatientCard(
                                patient = patient,
                                onBanClick = { patientId ->
                                    banPatient(patientId, db) { success, message ->
                                        if (success) {
                                            patients = patients.map {
                                                if (it.id == patientId) it.copy(isBaned = true) else it
                                            }
                                        } else {
                                            errorMessage = message
                                        }
                                    }
                                },
                                onUnbanClick = { patientId ->
                                    unbanPatient(patientId, db) { success, message ->
                                        if (success) {
                                            patients = patients.map {
                                                if (it.id == patientId) it.copy(isBaned = false) else it
                                            }
                                        } else {
                                            errorMessage = message
                                        }
                                    }
                                },
                                onDeleteClick = { patientId ->
                                    deletePatient(patientId, db) { success, message ->
                                        if (success) {
                                            patients = patients.filter { it.id != patientId }
                                        } else {
                                            errorMessage = message
                                        }
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
fun banPatient(patientId: String, db: FirebaseFirestore, callback: (Boolean, String?) -> Unit) {
    db.collection("users")
        .document(patientId)
        .collection("profile")
        .document("basicInfo")
        .update("isBaned", true)
        .addOnSuccessListener {
            callback(true, null)
        }
        .addOnFailureListener { e ->
            callback(false, "Failed to ban patient: ${e.message}")
        }
}


fun unbanPatient(patientId: String, db: FirebaseFirestore,
                 callback: (Boolean, String?) -> Unit) {

    db.collection("users")
        .document(patientId)
        .collection("profile")
        .document("basicInfo")
        .update("isBaned", false)
        .addOnSuccessListener {
            callback(true, null)
        }
        .addOnFailureListener { e ->
            callback(false, "Failed to unban patient: ${e.message}")
        }
}

fun deletePatient(patientId: String, db: FirebaseFirestore, callback: (Boolean, String?) -> Unit) {
    db.collection("users").document(patientId).delete()
        .addOnSuccessListener {
            callback(true, null)
        }
        .addOnFailureListener { e ->
            callback(false, "Failed to delete patient: ${e.message}")
        }
}


@Composable
fun PatientCard(
    patient: Patient,
    onBanClick: (String) -> Unit,
    onUnbanClick: (String) -> Unit,
    onDeleteClick: (String) -> Unit
) {
    var showBanDialog by remember { mutableStateOf(false) }
    var showUnbanDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "${patient.firstName} ${patient.lastName}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Email: ${patient.email}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text(
                text = "Date of Birth: ${patient.dateOfBirth}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Row(
                modifier = Modifier.padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatusChip(
                    text = if (patient.isVerified) "Verified" else "Pending",
                    backgroundColor = if (patient.isVerified)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.secondary,
                    textColor = if (patient.isVerified)
                        MaterialTheme.colorScheme.onPrimary
                    else
                        MaterialTheme.colorScheme.onSecondary
                )

                if (patient.isBaned) {
                    StatusChip(
                        text = "Banned",
                        backgroundColor = MaterialTheme.colorScheme.error,
                        textColor = MaterialTheme.colorScheme.onError
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (!patient.isBaned) {
                    Button(
                        onClick = { showBanDialog = true },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            Icons.Default.Block,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Ban", style = MaterialTheme.typography.bodySmall)
                    }
                } else {
                    OutlinedButton(
                        onClick = { showUnbanDialog = true },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Unban", style = MaterialTheme.typography.bodySmall)
                    }
                }

                Button(
                    onClick = { showDeleteDialog = true },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    ),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Delete")
                }
            }
        }
    }

    if (showBanDialog) {
        ConfirmationDialog(
            title = "Ban Patient",
            message = "Are you sure you want to ban ${patient.firstName} ${patient.lastName}? This will prevent them from accessing the system.",
            confirmText = "Ban",
            onConfirm = {
                onBanClick(patient.id)
                showBanDialog = false
            },
            onDismiss = { showBanDialog = false },
            isDestructive = true
        )
    }

    if (showUnbanDialog) {
        ConfirmationDialog(
            title = "Unban Patient",
            message = "Are you sure you want to unban ${patient.firstName} ${patient.lastName}? This will restore their access to the system.",
            confirmText = "Unban",
            onConfirm = {
                onUnbanClick(patient.id)
                showUnbanDialog = false
            },
            onDismiss = { showUnbanDialog = false },
            isDestructive = false
        )
    }

    if (showDeleteDialog) {
        ConfirmationDialog(
            title = "Delete Patient",
            message = "Are you sure you want to permanently delete ${patient.firstName} ${patient.lastName}? This action cannot be undone.",
            confirmText = "Delete",
            onConfirm = {
                onDeleteClick(patient.id)
                showDeleteDialog = false
            },
            onDismiss = { showDeleteDialog = false },
            isDestructive = true
        )
    }
}