package com.example.e_clinic_app.ui.home.patient

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubble
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
import com.example.e_clinic_app.ui.navigation.Routes
import com.google.firebase.auth.FirebaseAuth
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VisitDetailScreen(
    navController: NavController,
    viewModel: VisitDetailViewModel
) {
    val appointment by viewModel.appointment.collectAsState()
    val zone = ZoneId.of("Europe/Warsaw")
    val dateFmt = DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.getDefault())
    val timeFmt = DateTimeFormatter.ofPattern("HH:mm", Locale.getDefault())
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

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
                if (appt.fastingRequired) {
                    Text("Fasting required: 24 hours prior to appointment.", style = MaterialTheme.typography.bodyMedium)
                } else {
                    Text("No fasting required.", style = MaterialTheme.typography.bodyMedium)
                }
                if (appt.additionalPrep.isNotBlank()) {
                    Text("Additional preparation: ${appt.additionalPrep}", style = MaterialTheme.typography.bodyMedium)
                }
                Spacer(Modifier.height(24.dp))
                Button(
                    onClick = {
                        // build pairId from sorted UIDs
                        currentUserId?.let { uid ->
                            val pairId = listOf(uid, appt.doctorId).sorted().joinToString("_")
                            navController.navigate("${Routes.CHAT_DETAIL}/$pairId")
                        } ?: Log.e("VisitDetail", "No current user ID for chat navigation")
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Filled.ChatBubble, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Chat with Doctor")
                }
            }
        } ?: Box(
            Modifier
                .padding(padding)
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    }
}