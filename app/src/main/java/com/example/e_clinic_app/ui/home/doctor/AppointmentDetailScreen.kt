package com.example.e_clinic_app.ui.home.doctor

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.e_clinic_app.data.appointment.AppointmentStatus
import com.example.e_clinic_app.data.model.DosageUnit
import com.example.e_clinic_app.data.model.Prescription
import com.example.e_clinic_app.presentation.viewmodel.AppointmentDetailViewModel
import com.example.e_clinic_app.ui.navigation.Routes
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppointmentDetailScreen(
    navController: NavController,
    viewModel: AppointmentDetailViewModel,
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
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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
                HorizontalDivider()
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

                // Status management section
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Appointment Status: ${appt.status.name}",
                            style = MaterialTheme.typography.titleMedium
                        )

                        // Different action buttons based on current status
                        when (appt.status) {
                            AppointmentStatus.PENDING -> {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Button(
                                        onClick = { viewModel.updateAppointmentStatus(AppointmentStatus.CONFIRMED) },
                                        modifier = Modifier.weight(1f),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = MaterialTheme.colorScheme.primary
                                        )
                                    ) {
                                        Icon(Icons.Filled.CheckCircle, contentDescription = null)
                                        Spacer(Modifier.width(4.dp))
                                        Text("Approve")
                                    }

                                    Button(
                                        onClick = { viewModel.updateAppointmentStatus(AppointmentStatus.CANCELLED) },
                                        modifier = Modifier.weight(1f),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = MaterialTheme.colorScheme.error
                                        )
                                    ) {
                                        Icon(Icons.Filled.Close, contentDescription = null)
                                        Spacer(Modifier.width(4.dp))
                                        Text("Cancel")
                                    }
                                }
                            }
                            AppointmentStatus.CONFIRMED -> {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Button(
                                        onClick = { viewModel.updateAppointmentStatus(AppointmentStatus.COMPLETED) },
                                        modifier = Modifier.weight(1f),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = MaterialTheme.colorScheme.secondary
                                        )
                                    ) {
                                        Icon(Icons.Filled.Done, contentDescription = null)
                                        Spacer(Modifier.width(4.dp))
                                        Text("Mark Completed")
                                    }

                                    Button(
                                        onClick = { viewModel.updateAppointmentStatus(AppointmentStatus.CANCELLED) },
                                        modifier = Modifier.weight(1f),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = MaterialTheme.colorScheme.error
                                        )
                                    ) {
                                        Icon(Icons.Filled.Close, contentDescription = null)
                                        Spacer(Modifier.width(4.dp))
                                        Text("Cancel")
                                    }
                                }
                            }
                            AppointmentStatus.CANCELLED -> {
                                // For cancelled appointments, allow restoring to pending
                                Button(
                                    onClick = { viewModel.updateAppointmentStatus(AppointmentStatus.PENDING) },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Restore to Pending")
                                }
                            }
                            AppointmentStatus.COMPLETED -> {
                                // Completed appointments don't need status change buttons
                                Text(
                                    "This appointment has been completed.",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(24.dp))
                Button(onClick = { showDialog = true }) {
                    Text("Create Prescription")
                }
                Spacer(Modifier.height(24.dp))
                Button(
                    onClick = {
                        // build pairId from sorted UIDs
                        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
                        currentUserId?.let { uid ->
                            val pairId = listOf(uid, appt.patientId).sorted().joinToString("_")
                            navController.navigate("${Routes.CHAT_DETAIL}/$pairId")
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
                        patientName = "${appt.patientFirstName} ${appt.patientLastName}",
                        prescriberName = "${appt.doctorFirstName} ${appt.doctorLastName}",
                        onDismiss = { showDialog = false },
                        onSave = { prescription ->
                            viewModel.addPrescription(appt.id, prescription)
                            showDialog = false
                        },
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
    patientName: String,
    prescriberId: String,
    prescriberName: String,
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
                onSave(
                    Prescription(
                        id = UUID.randomUUID().toString(),
                        authorId = prescriberId,
                        doctorName = prescriberName,
                        dateIssued = Timestamp.now(),
                        dosage = amount,
                        frequency = frequency,
                        medication = drugName,
                        notes = notes,
                        patientId = patientId,
                        patientName = patientName,
                    )
                )
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
