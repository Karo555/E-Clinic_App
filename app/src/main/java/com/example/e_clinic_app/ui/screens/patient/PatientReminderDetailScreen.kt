package com.example.e_clinic_app.ui.screens.patient

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.e_clinic_app.data.model.FollowUpReminder
import com.example.e_clinic_app.data.model.FollowUpStatus
import com.example.e_clinic_app.data.repository.PatientReminderRepository
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * Screen that displays details for a specific follow-up reminder.
 *
 * @param reminderId The ID of the reminder to display.
 * @param onBackClick Callback for when the back button is clicked.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatientReminderDetailScreen(
    reminderId: String,
    onBackClick: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val repository = remember { PatientReminderRepository() }

    var reminder by remember { mutableStateOf<FollowUpReminder?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var snackbarMessage by remember { mutableStateOf<String?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }

    // Load the reminder data
    LaunchedEffect(reminderId) {
        isLoading = true
        error = null
        try {
            reminder = repository.getReminderById(reminderId)
            if (reminder == null) {
                error = "Reminder not found"
            }
        } catch (e: Exception) {
            error = e.message ?: "An error occurred while loading the reminder"
        } finally {
            isLoading = false
        }
    }

    // Show snackbar when message changes
    LaunchedEffect(snackbarMessage) {
        snackbarMessage?.let {
            snackbarHostState.showSnackbar(it)
            snackbarMessage = null
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Reminder Details") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = androidx.compose.material.icons.Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                error != null -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Error",
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = error ?: "Unknown error",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = onBackClick) {
                            Text("Go Back")
                        }
                    }
                }
                reminder != null -> {
                    ReminderDetailContent(
                        reminder = reminder!!,
                        onMarkCompleted = {
                            coroutineScope.launch {
                                try {
                                    val success = repository.markReminderAsCompleted(reminderId)
                                    snackbarMessage = if (success) {
                                        "Reminder marked as completed"
                                    } else {
                                        "Failed to mark reminder as completed"
                                    }
                                } catch (e: Exception) {
                                    snackbarMessage = e.message ?: "An error occurred"
                                }
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun ReminderDetailContent(
    reminder: FollowUpReminder,
    onMarkCompleted: () -> Unit
) {
    val dateFormatter = SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault())
    val timeFormatter = SimpleDateFormat("hh:mm a", Locale.getDefault())

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Status indicator
        val statusColor = when (reminder.status) {
            FollowUpStatus.PENDING -> {
                if (reminder.dueDate.toDate().before(java.util.Date())) {
                    MaterialTheme.colorScheme.error
                } else {
                    MaterialTheme.colorScheme.primary
                }
            }
            FollowUpStatus.COMPLETED -> MaterialTheme.colorScheme.secondary
            else -> MaterialTheme.colorScheme.outline
        }

        val statusText = when (reminder.status) {
            FollowUpStatus.PENDING -> {
                if (reminder.dueDate.toDate().before(java.util.Date())) {
                    "OVERDUE"
                } else {
                    "PENDING"
                }
            }
            FollowUpStatus.COMPLETED -> "COMPLETED"
            else -> reminder.status.name
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = statusColor.copy(alpha = 0.1f)
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Status",
                    style = MaterialTheme.typography.titleMedium
                )

                Text(
                    text = statusText,
                    style = MaterialTheme.typography.titleMedium,
                    color = statusColor,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Reminder details
        Text(
            text = "Due Date",
            style = MaterialTheme.typography.titleMedium
        )

        Text(
            text = dateFormatter.format(reminder.dueDate.toDate()),
            style = MaterialTheme.typography.bodyLarge
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Description",
            style = MaterialTheme.typography.titleMedium
        )

        Text(
            text = reminder.description,
            style = MaterialTheme.typography.bodyLarge
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Doctor information (if available)
        Text(
            text = "Created by Doctor",
            style = MaterialTheme.typography.titleMedium
        )

        // In a real implementation, you might want to fetch the doctor's name here
        Text(
            text = reminder.doctorId,
            style = MaterialTheme.typography.bodyLarge
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Created on",
            style = MaterialTheme.typography.titleMedium
        )

        Text(
            text = dateFormatter.format(reminder.createdAt.toDate()),
            style = MaterialTheme.typography.bodyLarge
        )

        // Completed date (if applicable)
        reminder.completedAt?.let {
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Completed on",
                style = MaterialTheme.typography.titleMedium
            )

            Text(
                text = dateFormatter.format(it.toDate()),
                style = MaterialTheme.typography.bodyLarge
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        // Action button
        if (reminder.status == FollowUpStatus.PENDING) {
            Button(
                onClick = onMarkCompleted,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
            ) {
                Text("Mark as Completed")
            }
        }
    }
}
