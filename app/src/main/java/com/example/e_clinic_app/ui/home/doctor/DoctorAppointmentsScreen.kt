package com.example.e_clinic_app.ui.home.doctor

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.e_clinic_app.data.appointment.Appointment
import com.example.e_clinic_app.data.appointment.AppointmentStatus
import com.example.e_clinic_app.presentation.viewmodel.DoctorAppointmentsViewModel
import com.example.e_clinic_app.ui.bottomNavBar.BottomNavigationBar
import com.example.e_clinic_app.ui.navigation.Routes
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun DoctorAppointmentsScreen(
    navController: NavController,
    viewModel: DoctorAppointmentsViewModel = viewModel()
) {
    val appointments by viewModel.filteredAppointments.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val filterState by viewModel.filterState.collectAsState()
    val zone = ZoneId.of("Europe/Warsaw")
    val dateFmt = DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.getDefault())
    val timeFmt = DateTimeFormatter.ofPattern("HH:mm", Locale.getDefault())

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("All Appointments") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        bottomBar = {
            BottomNavigationBar(navController)
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            AppointmentFilterBar(
                filterState = filterState,
                onFilterChange = viewModel::onFilterChange
            )
            when {
                isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
                error != null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text(error ?: "Unknown error", color = MaterialTheme.colorScheme.error) }
                appointments.isEmpty() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("No appointments found") }
                else -> {
                    val grouped = appointments.groupBy { it.date.toDate().toInstant().atZone(zone).toLocalDate().withDayOfMonth(1) }
                    LazyColumn(
                        modifier = Modifier.fillMaxSize().padding(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        grouped.forEach { (month, appts) ->
                            stickyHeader {
                                Text(
                                    month.format(DateTimeFormatter.ofPattern("MMMM yyyy", Locale.getDefault())),
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(MaterialTheme.colorScheme.background)
                                        .padding(vertical = 4.dp)
                                )
                            }
                            itemsIndexed(appts) { _, appt ->
                                AppointmentRow(
                                    appt = appt,
                                    navController = navController,
                                    dateFmt = dateFmt,
                                    timeFmt = timeFmt,
                                    zone = zone
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AppointmentFilterBar(
    filterState: DoctorAppointmentsViewModel.FilterState,
    onFilterChange: (DoctorAppointmentsViewModel.FilterState) -> Unit
) {
    Column(Modifier.fillMaxWidth().padding(8.dp)) {
        OutlinedTextField(
            value = filterState.patientQuery,
            onValueChange = { onFilterChange(filterState.copy(patientQuery = it)) },
            label = { Text("Search by patient") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            // Date range pickers (pseudo, you may want to use a real date picker dialog)
            OutlinedTextField(
                value = filterState.startDate?.toString() ?: "From",
                onValueChange = {},
                label = { Text("From") },
                readOnly = true,
                modifier = Modifier.weight(1f)
            )
            Spacer(Modifier.width(8.dp))
            OutlinedTextField(
                value = filterState.endDate?.toString() ?: "To",
                onValueChange = {},
                label = { Text("To") },
                readOnly = true,
                modifier = Modifier.weight(1f)
            )
            // You can add date picker dialogs here
        }
        Spacer(Modifier.height(8.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
            AppointmentStatus.values().forEach { status ->
                FilterChip(
                    selected = filterState.statuses.contains(status),
                    onClick = {
                        val newStatuses = if (filterState.statuses.contains(status))
                            filterState.statuses - status else filterState.statuses + status
                        onFilterChange(filterState.copy(statuses = newStatuses))
                    },
                    label = { Text(status.name) },
                    modifier = Modifier.padding(end = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun AppointmentRow(
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
                navController.navigate("${Routes.APPOINTMENT_DETAIL}/${appt.id}")
            }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "${appt.date.toDate().toInstant().atZone(zone).format(dateFmt)} " +
                        "${appt.date.toDate().toInstant().atZone(zone).format(timeFmt)}",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "${appt.patientFirstName} ${appt.patientLastName}",
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = "Status: ${appt.status.name}",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}
