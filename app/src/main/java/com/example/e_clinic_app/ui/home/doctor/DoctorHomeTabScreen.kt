package com.example.e_clinic_app.ui.home.doctor

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material.icons.outlined.EventAvailable
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.MedicalServices
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.e_clinic_app.backend.home.DoctorHomeViewModel
import com.example.e_clinic_app.data.appointment.Appointment
import com.example.e_clinic_app.data.appointment.AppointmentStatus
import com.example.e_clinic_app.ui.bottomNavBar.BottomNavigationBar
import com.example.e_clinic_app.ui.navigation.Routes
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun DoctorHomeTabScreen(
    navController: NavController,
    viewModel: DoctorHomeViewModel
) {
    // Collect the doctor's first name for personalized greeting
    val doctorName by viewModel.doctorFirstName.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.fetchAppointments(viewModel.firestore)
    }

    // live appointments list
    val appointments = viewModel.appointmentsList

    // Prepare grouping by date
    val zone = ZoneId.of("Europe/Warsaw")
    val grouped = appointments
        .groupBy { appt ->
            appt.date.toDate().toInstant().atZone(zone).toLocalDate()
        }
        .toSortedMap()

    val dateFmt = DateTimeFormatter.ofPattern("MMM d", Locale.getDefault())
    val timeFmt = DateTimeFormatter.ofPattern("HH:mm", Locale.getDefault())

    val colorScheme = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Welcome back,", style = typography.labelMedium)
                        Text("Dr. $doctorName", style = typography.titleLarge)
                    }
                },
                actions = {
                    IconButton(onClick = { /* TODO: profile */ }) {
                        Icon(Icons.Outlined.AccountCircle, contentDescription = "Profile")
                    }
                }
            )
        },
        bottomBar = { BottomNavigationBar(navController) },
        containerColor = colorScheme.background
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Quick Access
            item {
                Text("Quick Access", style = typography.titleMedium)
                Spacer(Modifier.height(8.dp))
                val quickActions = listOf(
                    NavItem("Patients", Icons.Outlined.Group, Routes.DOCTOR_PATIENTS),
                    NavItem("Prescriptions", Icons.Outlined.MedicalServices, "prescriptions"),
                    NavItem("Appointments", Icons.Outlined.Event, Routes.DOCTOR_APPOINTMENTS)
                )
                LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(quickActions) { item ->
                        ElevatedCard(
                            onClick = { navController.navigate(item.route) },
                            shape = MaterialTheme.shapes.medium,
                            colors = CardDefaults.elevatedCardColors(),
                            modifier = Modifier.size(width = 140.dp, height = 100.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(12.dp),
                                verticalArrangement = Arrangement.SpaceBetween
                            ) {
                                Icon(item.icon, contentDescription = item.label, tint = colorScheme.primary)
                                Text(item.label, style = typography.bodyLarge)
                            }
                        }
                    }
                }
            }

            // Upcoming Appointments header
            item {
                Text("Upcoming Appointments", style = typography.titleMedium)
                Spacer(Modifier.height(8.dp))
            }

            // Grouped appointments with sticky headers
            grouped.forEach { (date, appts) ->
                stickyHeader {
                    Surface(
                        tonalElevation = 2.dp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        val label = when {
                            date == java.time.LocalDate.now(zone) -> "Today"
                            date == java.time.LocalDate.now(zone).plusDays(1) -> "Tomorrow"
                            else -> date.format(dateFmt)
                        }
                        Text(
                            text = label,
                            style = typography.titleSmall,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }
                items(appts) { appt: Appointment ->
                    ElevatedCard(
                        shape = MaterialTheme.shapes.medium,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                navController.navigate("${Routes.PATIENT_DETAIL}/${appt.patientId}")
                            }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Outlined.EventAvailable,
                                contentDescription = null,
                                tint = colorScheme.primary
                            )
                            Spacer(Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "${appt.patientFirstName} ${appt.patientLastName}",
                                    style = typography.bodyLarge
                                )
                                Spacer(Modifier.height(4.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = date.format(dateFmt),
                                        style = typography.bodyMedium
                                    )
                                    Text(" • ", style = typography.bodyMedium)
                                    Text(
                                        text = appt.date
                                            .toDate()
                                            .toInstant()
                                            .atZone(zone)
                                            .format(timeFmt),
                                        style = typography.bodyMedium
                                    )
                                    Text(" • ", style = typography.bodyMedium)
                                    val dotColor = when (appt.status) {
                                        AppointmentStatus.CONFIRMED -> Color(0xFF4CAF50)
                                        AppointmentStatus.PENDING   -> Color(0xFFFFC107)
                                        else                        -> Color.Gray
                                    }
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(dotColor)
                                    )
                                    Spacer(Modifier.width(4.dp))
                                    Text(
                                        text = appt.status.name.lowercase().replaceFirstChar { it.uppercase() },
                                        style = typography.bodyMedium
                                    )
                                }
                            }
                            Icon(
                                imageVector = Icons.Outlined.ArrowForward,
                                contentDescription = "View Details",
                                tint = colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Set availability button
            item {
                Button(
                    onClick = { navController.navigate(Routes.SET_AVAILABILITY) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Icon(Icons.Outlined.Schedule, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Set Availability", style = typography.labelLarge)
                }
            }
        }
    }
}

data class NavItem(val label: String, val icon: ImageVector, val route: String)