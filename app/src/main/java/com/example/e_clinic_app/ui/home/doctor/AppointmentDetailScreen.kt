package com.example.e_clinic_app.ui.home.doctor

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.e_clinic_app.data.model.*
import com.example.e_clinic_app.presentation.viewmodel.AppointmentDetailViewModel
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import com.example.e_clinic_app.data.model.Frequency
import androidx.compose.foundation.clickable

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppointmentDetailScreen(
    navController: NavController,
    viewModel: AppointmentDetailViewModel,
    onCreatePrescription: () -> Unit
) {
    val appointment by viewModel.appointment.collectAsState()
    val zone = ZoneId.of("Europe/Warsaw")
    val dateFmt = DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.getDefault())
    val timeFmt = DateTimeFormatter.ofPattern("HH:mm", Locale.getDefault())

    var showDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Appointment Details") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        appointment?.let { appt ->
            Column(
                Modifier
                    .padding(padding)
                    .padding(16.dp)
                    .fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = appt.date.toDate()
                        .toInstant()
                        .atZone(zone)
                        .format(dateFmt) + " at " +
                            appt.date.toDate()
                                .toInstant()
                                .atZone(zone)
                                .format(timeFmt),
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "Patient: ${appt.patientFirstName} ${appt.patientLastName}",
                    style = MaterialTheme.typography.bodyLarge
                )
                Divider()
                Text(
                    text = "Preparation Instructions:",
                    style = MaterialTheme.typography.titleSmall
                )
                if (appt.fastingRequired) {
                    Text("Fasting required: 24 hours prior to appointment.", style = MaterialTheme.typography.bodyMedium)
                } else {
                    Text("No fasting required.", style = MaterialTheme.typography.bodyMedium)
                }
                if (appt.additionalPrep.isNotBlank()) {
                    Text("Additional preparation: ${appt.additionalPrep}", style = MaterialTheme.typography.bodyMedium)
                }
                Spacer(Modifier.height(24.dp))
                Button(onClick = { showDialog = true }) {
                    Text("Create Prescription")
                }
                Spacer(Modifier.height(24.dp))
                Button(
                    onClick = {
                        // build pairId from sorted UIDs
                        val currentUserId = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid
                        currentUserId?.let { uid ->
                            val pairId = listOf(uid, appt.patientId).sorted().joinToString("_")
                            navController.navigate("${com.example.e_clinic_app.ui.navigation.Routes.CHAT_DETAIL}/$pairId")
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Filled.ChatBubble, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Chat with Patient")
                }
                Spacer(Modifier.height(24.dp))
                if (showDialog) {
                    CreatePrescriptionDialog(
                        patientId = appt.patientId,
                        prescriberId = appt.doctorId,
                        onDismiss = { showDialog = false },
                        onSave = { prescription ->
                            viewModel.addPrescription(appt.id, prescription)
                            showDialog = false
                        }
                    )
                }
            }
        } ?: Box(
            Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    }
}

@Composable
fun CreatePrescriptionDialog(
    patientId: String,
    prescriberId: String,
    onDismiss: () -> Unit,
    onSave: (Prescription) -> Unit
) {
    var drugName by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var unit by remember { mutableStateOf(DosageUnit.MG) }
    var frequency by remember { mutableStateOf("Once daily") }
    var notes by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create Prescription") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = drugName,
                    onValueChange = { drugName = it },
                    label = { Text("Drug Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Amount") },
                    modifier = Modifier.fillMaxWidth()
                )
                DropdownMenuBox(
                    value = unit,
                    onValueChange = { unit = it },
                    label = "Unit"
                )
                OutlinedTextField(
                    value = frequency,
                    onValueChange = { frequency = it },
                    label = { Text("Frequency") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Instructions/Notes") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                val medication = Medication(
                    drug = Drug(
                        id = drugName.lowercase().replace(" ", "_"),
                        name = drugName,
                        formulation = "",
                        availableUnits = listOf(unit),
                        commonDosages = mapOf(unit to listOf(amount.toDoubleOrNull() ?: 0.0)),
                        defaultFrequency = Frequency.ONCE_DAILY
                    ),
                    amount = amount.toDoubleOrNull() ?: 0.0,
                    unit = unit,
                    frequency = Frequency.valueOf(frequency.replace(" ", "_").uppercase())
                )
                val prescription = Prescription(
                    id = java.util.UUID.randomUUID().toString(),
                    patientId = patientId,
                    prescriberId = prescriberId,
                    medications = listOf(medication),
                    dosageUnit = unit,
                    startDate = Instant.now(),
                    notes = notes
                )
                onSave(prescription)
            }) {
                Text("Save")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun DropdownMenuBox(
    value: DosageUnit,
    onValueChange: (DosageUnit) -> Unit,
    label: String
) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        OutlinedTextField(
            value = value.name,
            onValueChange = {},
            label = { Text(label) },
            readOnly = true,
            modifier = Modifier.fillMaxWidth().clickable { expanded = true }
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            DosageUnit.values().forEach { unit ->
                DropdownMenuItem(
                    text = { Text(unit.name) },
                    onClick = {
                        onValueChange(unit)
                        expanded = false
                    }
                )
            }
        }
    }
}
