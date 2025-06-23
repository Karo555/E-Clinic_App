package com.example.e_clinic_app.ui.admin.tools

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.e_clinic_app.data.model.Doctor
import com.example.e_clinic_app.data.repository.DoctorRepository
import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageDoctorsScreen(navController: NavController) {
    val db = Firebase.firestore
    var doctors by remember { mutableStateOf<List<Doctor>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        try {
            doctors = DoctorRepository.fetchAvailableDoctors()
            isLoading = false
        } catch (e: Exception) {
            errorMessage = e.message ?: "Unknown error occurred"
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Manage Doctors", fontWeight = FontWeight.Bold) },
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
                            // Retry loading
                            // This could be extracted to a function
                            // LaunchedEffect can't be called directly here
                        },
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    ) {
                        Text("Retry")
                    }
                }

                doctors.isEmpty() -> {
                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "No doctors found",
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
                        items(doctors, key = { it.id }) { doctor ->
                            DoctorCard(
                                doctor = doctor,
                                onBanClick = { doctorId ->
                                    banDoctor(doctorId, db) { success, message ->
                                        if (success) {
                                            doctors = doctors.map {
                                                if (it.id == doctorId) it.copy(isBaned = true) else it
                                            }
                                        } else {
                                            errorMessage = message
                                        }
                                    }
                                },
                                onUnbanClick = { doctorId ->
                                    unbanDoctor(doctorId, db) { success, message ->
                                        if (success) {
                                            doctors = doctors.map {
                                                if (it.id == doctorId) it.copy(isBaned = false) else it
                                            }
                                        } else {
                                            errorMessage = message
                                        }
                                    }
                                },
                                onDeleteClick = { doctorId ->
                                    deleteDoctor(doctorId, db) { success, message ->
                                        if (success) {
                                            doctors = doctors.filter { it.id != doctorId }
                                        } else {
                                            errorMessage = message
                                        }
                                    }
                                },
                                onApproveClick = { doctorId ->
                                    approveDoctor(doctorId, db) { success, message ->
                                        if (success) {
                                            // Reload doctors after successful approval
                                            Log.d("ManageDoctorsScreen", "Doctor $doctorId approved successfully")
                                            }
                                        } else {
                                            errorMessage = message
                                            Log.e("ManageDoctorsScreen", "Failed to approve doctor $doctorId: $message")
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

@Composable
fun DoctorCard(
    doctor: Doctor,
    onBanClick: (String) -> Unit,
    onUnbanClick: (String) -> Unit,
    onDeleteClick: (String) -> Unit,
    onApproveClick: (String) -> Unit
) {
    var showBanDialog by remember { mutableStateOf(false) }
    var showUnbanDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showApproveDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "${doctor.firstName.orEmpty()} ${doctor.lastName.orEmpty()}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "Specialization: ${doctor.specialisation.orEmpty()}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Text(
                        text = "Institution: ${doctor.institutionName.orEmpty()}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Text(
                        text = "Experience: ${doctor.experienceYears ?: 0} years",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Text(
                        text = "License: ${doctor.licenseNumber.orEmpty()}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Row(
                        modifier = Modifier.padding(top = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        StatusChip(
                            text = if (doctor.isVerified) "Verified" else "Pending",
                            backgroundColor = if (doctor.isVerified)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.secondary,
                            textColor = if (doctor.isVerified)
                                MaterialTheme.colorScheme.onPrimary
                            else
                                MaterialTheme.colorScheme.onSecondary
                        )

                        if (doctor.isBaned) {
                            StatusChip(
                                text = "Banned",
                                backgroundColor = MaterialTheme.colorScheme.error,
                                textColor = MaterialTheme.colorScheme.onError
                            )
                        }

                        StatusChip(
                            text = if (doctor.availability) "Available" else "Unavailable",
                            backgroundColor = if (doctor.availability)
                                Color.Green
                            else
                                Color.Gray,
                            textColor = Color.White
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (!doctor.isVerified) {
                    Button(
                        onClick = { showApproveDialog = true },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Approve", style = MaterialTheme.typography.bodySmall)
                    }
                }

                if (!doctor.isBaned) {
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
            title = "Ban Doctor",
            message = "Are you sure you want to ban ${doctor.firstName.orEmpty()} ${doctor.lastName.orEmpty()}? This will prevent them from accessing the system.",
            confirmText = "Ban",
            onConfirm = {
                onBanClick(doctor.id)
                showBanDialog = false
            },
            onDismiss = { showBanDialog = false },
            isDestructive = true
        )
    }

    if (showUnbanDialog) {
        ConfirmationDialog(
            title = "Unban Doctor",
            message = "Are you sure you want to unban ${doctor.firstName.orEmpty()} ${doctor.lastName.orEmpty()}? This will restore their access to the system.",
            confirmText = "Unban",
            onConfirm = {
                onUnbanClick(doctor.id)
                showUnbanDialog = false
            },
            onDismiss = { showUnbanDialog = false },
            isDestructive = false
        )
    }

    if (showDeleteDialog) {
        ConfirmationDialog(
            title = "Delete Doctor",
            message = "Are you sure you want to permanently delete ${doctor.firstName.orEmpty()} ${doctor.lastName.orEmpty()}? This action cannot be undone.",
            confirmText = "Delete",
            onConfirm = {
                onDeleteClick(doctor.id)
                showDeleteDialog = false
            },
            onDismiss = { showDeleteDialog = false },
            isDestructive = true
        )
    }

    if (showApproveDialog) {
        ConfirmationDialog(
            title = "Approve Doctor",
            message = "Are you sure you want to approve ${doctor.firstName.orEmpty()} ${doctor.lastName.orEmpty()}? This will verify them as a licensed doctor.",
            confirmText = "Approve",
            onConfirm = {
                onApproveClick(doctor.id)
                showApproveDialog = false
            },
            onDismiss = { showApproveDialog = false },
            isDestructive = false
        )
    }
}


@Composable
fun StatusChip(
    text: String,
    backgroundColor: Color,
    textColor: Color
) {
    Box(
        modifier = Modifier
            .background(backgroundColor, RoundedCornerShape(12.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = text,
            color = textColor,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun ConfirmationDialog(
    title: String,
    message: String,
    confirmText: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    isDestructive: Boolean = false
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, style = MaterialTheme.typography.titleMedium) },
        text = { Text(message, style = MaterialTheme.typography.bodyMedium) },
        confirmButton = {
            TextButton(
                onClick = onConfirm
            ) {
                Text(
                    text = confirmText,
                    color = if (isDestructive)
                        MaterialTheme.colorScheme.error
                    else
                        MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        containerColor = MaterialTheme.colorScheme.surface
    )
}

fun banDoctor(doctorId: String, db: FirebaseFirestore, callback: (Boolean, String?) -> Unit) {
    db.collection("users")
        .document(doctorId)
        .update("isBaned", true)
        .addOnSuccessListener {
            callback(true, null)
        }
        .addOnFailureListener { exception ->
            callback(false, exception.message ?: "Failed to ban doctor")
        }
}

fun unbanDoctor(doctorId: String, db: FirebaseFirestore, callback: (Boolean, String?) -> Unit) {
    db.collection("users")
        .document(doctorId)
        .update("isBaned", false)
        .addOnSuccessListener {
            callback(true, null)
        }
        .addOnFailureListener { exception ->
            callback(false, exception.message ?: "Failed to unban doctor")
        }
}

fun deleteDoctor(doctorId: String, db: FirebaseFirestore, callback: (Boolean, String?) -> Unit) {
    db.collection("users")
        .document(doctorId)
        .delete()
        .addOnSuccessListener {
            callback(true, null)
        }
        .addOnFailureListener { exception ->
            callback(false, exception.message ?: "Failed to delete doctor")
        }
}

fun approveDoctor(doctorId: String, db: FirebaseFirestore, callback: (Boolean, String?) -> Unit) {
    db.collection("users")
        .document(doctorId)
        .update("isVerified", true)
        .addOnSuccessListener {
            callback(true, null)
        }
        .addOnFailureListener { exception ->
            callback(false, exception.message ?: "Failed to approve doctor")
        }
}

suspend fun getDoctors(db: FirebaseFirestore): List<Doctor> {
    return try {
        val snapshot = db.collection("users")
            .whereEqualTo("role", "Doctor")
            .get()
            .await()

        snapshot.documents.mapNotNull { document ->
            try {
                document.toObject(Doctor::class.java)?.copy(
                    id = document.id
                )
            } catch (e: Exception) {
                null
            }
        }
    } catch (e: Exception) {
        throw Exception("Failed to fetch doctors: ${e.message}")
    }
}