package com.example.e_clinic_app.presentation.screen.patient

import android.app.Application
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Chip
import androidx.compose.material.ChipDefaults
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.e_clinic_app.data.model.FollowUpReminder
import com.example.e_clinic_app.data.model.FollowUpStatus
import com.example.e_clinic_app.presentation.viewmodel.PatientRemindersViewModel
import com.example.e_clinic_app.presentation.viewmodel.PatientRemindersViewModelFactory
import java.text.SimpleDateFormat
import java.util.*

/**
 * A composable screen that displays follow-up reminders for a patient.
 *
 * This screen shows a list of reminders created by doctors for the patient,
 * allowing them to view details and mark reminders as completed.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatientRemindersScreen(
    viewModel: PatientRemindersViewModel = viewModel(
        factory = PatientRemindersViewModelFactory(LocalContext.current.applicationContext as Application)
    ),
    navigateBack: () -> Unit
) {
    val reminders by viewModel.reminders.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    // Track selected filter
    var selectedFilter by remember { mutableStateOf<FollowUpStatus?>(null) }

    // Track the currently selected reminder for detail view
    var selectedReminder by remember { mutableStateOf<FollowUpReminder?>(null) }

    // Show create reminder dialog
    var showCreateDialog by remember { mutableStateOf(false) }

    LaunchedEffect(key1 = selectedFilter) {
        viewModel.filterRemindersByStatus(selectedFilter)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Follow-up Reminders") },
                navigationIcon = {
                    IconButton(onClick = navigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.loadReminders() }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh"
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Filter chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = selectedFilter == null,
                    onClick = { selectedFilter = null },
                    label = { Text("All") }
                )
                FilterChip(
                    selected = selectedFilter == FollowUpStatus.PENDING,
                    onClick = { selectedFilter = FollowUpStatus.PENDING },
                    label = { Text("Pending") }
                )
                FilterChip(
                    selected = selectedFilter == FollowUpStatus.COMPLETED,
                    onClick = { selectedFilter = FollowUpStatus.COMPLETED },
                    label = { Text("Completed") }
                )
            }

            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (error != null) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = error ?: "An unknown error occurred",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            } else if (reminders.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No reminders found")
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(reminders) { reminder ->
                        ReminderCard(
                            reminder = reminder,
                            onMarkAsCompleted = { viewModel.markReminderAsCompleted(reminder.id) },
                            onViewDetails = { selectedReminder = reminder }
                        )
                    }
                }
            }

            // Floating action button to create new reminder
            FloatingActionButton(
                onClick = { showCreateDialog = true },
                modifier = Modifier
                    .align(Alignment.End)
                    .padding(16.dp)
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Create Reminder")
            }
        }
    }

    // Show reminder detail dialog if a reminder is selected
    selectedReminder?.let { reminder ->
        ReminderDetailDialog(
            reminder = reminder,
            onDismiss = { selectedReminder = null },
            onMarkAsCompleted = {
                viewModel.markReminderAsCompleted(reminder.id)
                selectedReminder = null
            }
        )
    }

    // Show create reminder dialog
    if (showCreateDialog) {
        CreateFollowUpReminderDialog(
            onDismiss = { showCreateDialog = false },
            onCreate = { description, dueDate ->
                viewModel.createFollowUpReminder(description, com.google.firebase.Timestamp(dueDate))
                showCreateDialog = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReminderCard(
    reminder: FollowUpReminder,
    onMarkAsCompleted: () -> Unit,
    onViewDetails: () -> Unit
) {
    val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    val dueDate = reminder.dueDate.toDate()

    val isPastDue = dueDate.before(Date()) && reminder.status == FollowUpStatus.PENDING

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = if (isPastDue) {
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.errorContainer,
                contentColor = MaterialTheme.colorScheme.onErrorContainer
            )
        } else {
            CardDefaults.cardColors()
        },
        onClick = onViewDetails
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = dateFormat.format(dueDate),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                @OptIn(ExperimentalMaterialApi::class)
                Chip(
                    colors = ChipDefaults.chipColors(
                        backgroundColor = when (reminder.status) {
                            FollowUpStatus.PENDING -> if (isPastDue) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                            FollowUpStatus.COMPLETED -> MaterialTheme.colorScheme.tertiary
                            FollowUpStatus.CANCELLED -> MaterialTheme.colorScheme.surfaceVariant
                        }
                    ),
                    onClick = {}
                ) {
                    Text(
                        text = if (isPastDue) "OVERDUE" else reminder.status.name,
                        color = when (reminder.status) {
                            FollowUpStatus.PENDING -> if (isPastDue) MaterialTheme.colorScheme.onError else MaterialTheme.colorScheme.onPrimary
                            FollowUpStatus.COMPLETED -> MaterialTheme.colorScheme.onTertiary
                            FollowUpStatus.CANCELLED -> MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = reminder.description,
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (reminder.status == FollowUpStatus.PENDING) {
                Button(
                    onClick = onMarkAsCompleted,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Mark as Completed")
                }
            } else if (reminder.status == FollowUpStatus.COMPLETED && reminder.completedAt != null) {
                Text(
                    text = "Completed on: ${dateFormat.format(reminder.completedAt.toDate())}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun ReminderDetailDialog(
    reminder: FollowUpReminder,
    onDismiss: () -> Unit,
    onMarkAsCompleted: () -> Unit
) {
    val dateFormat = SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault())
    val dueDate = reminder.dueDate.toDate()
    val isPastDue = dueDate.before(Date()) && reminder.status == FollowUpStatus.PENDING

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
            ) {
                Text(
                    text = "Follow-up Reminder Details",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row {
                    Text(
                        text = "Due Date: ",
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = dateFormat.format(dueDate),
                        color = if (isPastDue) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row {
                    Text(
                        text = "Status: ",
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (isPastDue && reminder.status == FollowUpStatus.PENDING)
                            "OVERDUE"
                        else
                            reminder.status.name
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Description:",
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = reminder.description
                )

                Spacer(modifier = Modifier.height(16.dp))

                if (reminder.status == FollowUpStatus.COMPLETED && reminder.completedAt != null) {
                    Row {
                        Text(
                            text = "Completed on: ",
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = dateFormat.format(reminder.completedAt.toDate())
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Close")
                    }

                    if (reminder.status == FollowUpStatus.PENDING) {
                        Spacer(modifier = Modifier.width(8.dp))

                        Button(onClick = onMarkAsCompleted) {
                            Text("Mark as Completed")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CreateFollowUpReminderDialog(
    onDismiss: () -> Unit,
    onCreate: (String, Date) -> Unit
) {
    var description by remember { mutableStateOf("") }
    var selectedDate by remember { mutableStateOf(Date()) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
            ) {
                Text(
                    text = "Create Follow-up Reminder",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Replace DatePicker with a simpler TextField for date input
                OutlinedTextField(
                    value = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(selectedDate),
                    onValueChange = { /* Handle date input as text */ },
                    label = { Text("Select Due Date") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        onCreate(description, selectedDate)
                        // Removed onDismiss() here to avoid double closing
                    },
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Create")
                }
            }
        }
    }
}