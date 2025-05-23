package com.example.e_clinic_app.ui.home.doctor

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material.icons.outlined.EventAvailable
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.MedicalServices
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.e_clinic_app.backend.home.DoctorHomeViewModel
import com.example.e_clinic_app.ui.bottomNavBar.BottomNavigationBar
import com.example.e_clinic_app.ui.navigation.Routes

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DoctorHomeTabScreen(
    navController: NavController,
    viewModel: DoctorHomeViewModel
) {
    LaunchedEffect(Unit) {
        viewModel.fetchAppointments(viewModel.firestore)
    }

    val appointments = viewModel.appointmentsList
    val colorScheme = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Welcome back,", style = typography.labelMedium)
                        Text("Dr. [Name]", style = typography.titleLarge)
                    }
                },
                actions = {
                    IconButton(onClick = { /* TODO: profile */ }) {
                        Icon(Icons.Outlined.AccountCircle, contentDescription = "Profile")
                    }
                }
            )
        },
        bottomBar = {
            BottomNavigationBar(navController)
        },
        containerColor = colorScheme.background
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {

            item {
                Text("Quick Access", style = typography.titleMedium)
                Spacer(Modifier.height(8.dp))

                val quickActions = listOf(
                    NavItem("Patients", Icons.Outlined.Group, "patients"),
                    NavItem("Prescriptions", Icons.Outlined.MedicalServices, "prescriptions"),
                    NavItem("Appointments", Icons.Outlined.Event, Routes.DOCTOR_APPOINTMENTS)
                )

                LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(quickActions) { item ->
                        ElevatedCard(
                            onClick = { navController.navigate(item.route) },
                            shape = MaterialTheme.shapes.medium,
                            colors = CardDefaults.elevatedCardColors(),
                            modifier = Modifier
                                .size(width = 140.dp, height = 100.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(12.dp),
                                verticalArrangement = Arrangement.SpaceBetween
                            ) {
                                Icon(
                                    imageVector = item.icon,
                                    contentDescription = item.label,
                                    tint = colorScheme.primary
                                )
                                Text(item.label, style = typography.bodyLarge)
                            }
                        }
                    }
                }
            }

            item {
                Text("Upcoming Appointments", style = typography.titleMedium)
                Spacer(Modifier.height(8.dp))
            }

            items(appointments) { appointment ->
                ElevatedCard(
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { navController.navigate("appointmentDetail/${appointment.id}") }
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
                        Column {
                            Text(appointment.patientId, style = typography.bodyLarge)
                            Text(
                                appointment.date.toDate().toString(), // show actual date
                                style = typography.labelSmall,
                                color = colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(modifier = Modifier.weight(1f))
                        IconButton(onClick = {
                            navController.navigate("${Routes.PATIENT_DETAIL}/${appointment.patientId}")
                        }) {
                            Icon(Icons.AutoMirrored.Outlined.ArrowForward, contentDescription = "View Patient Details")
                        }
                    }
                }
            }

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