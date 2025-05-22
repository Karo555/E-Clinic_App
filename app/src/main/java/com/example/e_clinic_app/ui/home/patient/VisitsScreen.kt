package com.example.e_clinic_app.ui.home.patient

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.e_clinic_app.presentation.viewmodel.AppointmentsViewModel
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import androidx.compose.foundation.lazy.items


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
        topBar = { CenterAlignedTopAppBar(title = { Text("Your Visits") }) }
    ) { padding ->
        Box(Modifier.padding(padding).fillMaxSize()) {
            when {
                isLoading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                error != null -> Text(
                    error!!,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.align(Alignment.Center)
                )

                appointments.isEmpty() -> Text(
                    "No upcoming visits",
                    modifier = Modifier.align(Alignment.Center)
                )

                else -> LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.padding(16.dp)
                ) {
                    items(appointments) { appt ->
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(Modifier.padding(16.dp)) {
                                Text(
                                    text = appt.date
                                        .toDate()
                                        .toInstant()
                                        .atZone(ZoneId.systemDefault())
                                        .format(fmt),
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                Text(
                                    text = appt.status.name,
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}