package com.example.e_clinic_app.ui.home.patient

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.e_clinic_app.presentation.viewmodel.VisitDetailViewModel
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VisitDetailScreen(
    navController: NavController,
    viewModel: VisitDetailViewModel
) {
    val appointment by viewModel.appointment.collectAsState()
    val zone = ZoneId.of("Europe/Warsaw")
    val dateFmt = DateTimeFormatter.ofPattern("MMM d, yyyy")
    val timeFmt = DateTimeFormatter.ofPattern("HH:mm")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Visit Details") },
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
                    text = "Doctor: Dr. ${appt.doctorFirstName} ${appt.doctorLastName}",
                    style = MaterialTheme.typography.bodyLarge
                )
                Divider()
                Text(
                    text = "Preparation Instructions:",
                    style = MaterialTheme.typography.titleSmall
                )
                Text(
                    text = if (appt.fastingRequired)
                        "Fasting required: 24 hours prior to appointment."
                    else
                        "No fasting required.",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "Additional preparation: ${appt.additionalPrep}",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        } ?: Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    }
}