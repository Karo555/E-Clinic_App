package com.example.e_clinic_app.ui.home.patient

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.e_clinic_app.data.appointment.Appointment
import com.example.e_clinic_app.data.appointment.AppointmentStatus
import com.example.e_clinic_app.presentation.viewmodel.AppointmentsViewModel
import com.example.e_clinic_app.ui.navigation.Routes
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun VisitsScreen(
    navController: NavController,
    viewModel: AppointmentsViewModel
) {
    val appointments by viewModel.appointments.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val zone = ZoneId.of("Europe/Warsaw")
    val dateFmt = DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.getDefault())
    val timeFmt = DateTimeFormatter.ofPattern("HH:mm", Locale.getDefault())

    var selectedDoctorId by remember { mutableStateOf<String?>(null) }
    val doctorOptions = appointments
        .map { it.doctorId to "Dr. ${it.doctorFirstName} ${it.doctorLastName}" }
        .distinct()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Your Visits") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        when {
            isLoading -> {
                Box(
                    Modifier
                        .padding(padding)
                        .fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            error != null -> {
                Box(
                    Modifier
                        .padding(padding)
                        .fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(error ?: "Unknown error", color = MaterialTheme.colorScheme.error)
                }
            }
            appointments.isEmpty() -> {
                Box(
                    Modifier
                        .padding(padding)
                        .fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No visits found")
                }
            }
            else -> {
                LazyColumn(
                    Modifier
                        .padding(padding)
                        .padding(16.dp)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        var expanded by remember { mutableStateOf(false) }
                        ExposedDropdownMenuBox(
                            expanded = expanded,
                            onExpandedChange = { expanded = !expanded }
                        ) {
                            OutlinedTextField(
                                value = doctorOptions.firstOrNull { it.first == selectedDoctorId }?.second
                                    ?: "All Doctors",
                                onValueChange = {},
                                label = { Text("Filter by Doctor") },
                                readOnly = true,
                                trailingIcon = {
                                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                                },
                                modifier = Modifier.fillMaxWidth()
                            )
                            ExposedDropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("All Doctors") },
                                    onClick = {
                                        selectedDoctorId = null
                                        expanded = false
                                    }
                                )
                                doctorOptions.forEach { (id, name) ->
                                    DropdownMenuItem(
                                        text = { Text(name) },
                                        onClick = {
                                            selectedDoctorId = id
                                            expanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    val filtered = appointments.filter {
                        selectedDoctorId == null || it.doctorId == selectedDoctorId
                    }
                    val upcoming = filtered.filter { it.status == AppointmentStatus.PENDING || it.status == AppointmentStatus.CONFIRMED }
                    val completed = filtered.filter { it.status == AppointmentStatus.COMPLETED }
                    val cancelled = filtered.filter { it.status == AppointmentStatus.CANCELLED }

                    if (upcoming.isNotEmpty()) {
                        stickyHeader {
                            Text(
                                "Upcoming",
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(MaterialTheme.colorScheme.background)
                                    .padding(vertical = 4.dp)
                            )
                        }
                        items(upcoming) { appt ->
                            VisitRow(appt, navController, dateFmt, timeFmt, zone)
                        }
                    }
                    if (completed.isNotEmpty()) {
                        stickyHeader {
                            Text(
                                "History",
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(MaterialTheme.colorScheme.background)
                                    .padding(vertical = 4.dp)
                            )
                        }
                        items(completed) { appt ->
                            VisitRow(appt, navController, dateFmt, timeFmt, zone)
                        }
                    }
                    if (cancelled.isNotEmpty()) {
                        stickyHeader {
                            Text(
                                "Cancelled",
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(MaterialTheme.colorScheme.background)
                                    .padding(vertical = 4.dp)
                            )
                        }
                        items(cancelled) { appt ->
                            VisitRow(appt, navController, dateFmt, timeFmt, zone)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun VisitRow(
    appt: Appointment,
    navController: NavController,
    dateFmt: DateTimeFormatter,
    timeFmt: DateTimeFormatter,
    zone: ZoneId
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                navController.navigate("${Routes.VISIT_DETAIL}/${appt.id}")
            }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "${appt.date.toDate().toInstant().atZone(zone).format(dateFmt)} " +
                        "${appt.date.toDate().toInstant().atZone(zone).format(timeFmt)}",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "Dr. ${appt.doctorFirstName} ${appt.doctorLastName}",
                style = MaterialTheme.typography.bodyLarge
            )
            Spacer(Modifier.height(4.dp))
            if (appt.fastingRequired) {
                Text("Fasting required: 24h prior", style = MaterialTheme.typography.bodySmall)
            } else {
                Text("No fasting required", style = MaterialTheme.typography.bodySmall)
            }
            if (appt.additionalPrep.isNotBlank()) {
                Text("Additional prep: ${appt.additionalPrep}", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}