package com.example.e_clinic_app.ui.home.patient

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.outlined.ChatBubble
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.e_clinic_app.data.appointment.Appointment
import com.example.e_clinic_app.presentation.viewmodel.AppointmentsViewModel
import com.example.e_clinic_app.ui.navigation.Routes
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VisitsScreen(
    navController: NavController,
    viewModel: AppointmentsViewModel
) {
    val appointments by viewModel.appointments.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val fmt = DateTimeFormatter.ofPattern("EEE, MMM d  HH:mm")

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
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            when {
                isLoading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                error != null -> {
                    Text(
                        text = error ?: "Unknown error",
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                appointments.isEmpty() -> {
                    Text(
                        text = "No upcoming visits",
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                else -> {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth()
                    ) {
                        items(appointments) { appt: Appointment ->
                            Card(
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Dr. ${appt.doctorFirstName} ${appt.doctorLastName}",
                                            style = MaterialTheme.typography.bodyLarge
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = appt.date
                                                .toDate()
                                                .toInstant()
                                                .atZone(ZoneId.systemDefault())
                                                .format(fmt),
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                        Text(
                                            text = appt.status.name,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    IconButton(onClick = {
                                        navController.navigate(Routes.CHAT_TAB)
                                    }) {
                                        Icon(
                                            imageVector = Icons.Outlined.ChatBubble,
                                            contentDescription = "Chat with doctor"
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}