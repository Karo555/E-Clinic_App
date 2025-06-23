package com.example.e_clinic_app.ui.screens.patient

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.e_clinic_app.data.model.FollowUpReminder
import com.example.e_clinic_app.ui.common.ErrorScreen
import com.example.e_clinic_app.ui.common.LoadingScreen
import com.example.e_clinic_app.viewmodel.PatientReminderViewModel
import java.text.SimpleDateFormat
import java.util.Locale


/**
 * Screen that displays follow-up reminders for patients.
 *
 * This screen shows all pending and completed follow-up reminders for the current patient,
 * allowing them to mark reminders as completed.
 *
 * @param viewModel The ViewModel that provides reminder data.
 * @param onNavigateToDetail Optional callback for navigating to a detail screen.
 */
@Composable
fun PatientRemindersScreen(
    viewModel: PatientReminderViewModel,
    onNavigateToDetail: (String) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val actionStatus by viewModel.actionStatus.collectAsState()

    // Show snackbar for action status
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(actionStatus) {
        when (actionStatus) {
            is PatientReminderViewModel.ActionStatus.Success -> {
                snackbarHostState.showSnackbar(
                    (actionStatus as PatientReminderViewModel.ActionStatus.Success).message
                )
                viewModel.resetActionStatus()
            }
            is PatientReminderViewModel.ActionStatus.Error -> {
                snackbarHostState.showSnackbar(
                    (actionStatus as PatientReminderViewModel.ActionStatus.Error).message
                )
                viewModel.resetActionStatus()
            }
            else -> {}
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }

    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            when (uiState) {
                is PatientReminderViewModel.RemindersUiState.Loading -> {
                    LoadingScreen(message = "Loading reminders...")
                }
                is PatientReminderViewModel.RemindersUiState.Empty -> {
                    EmptyRemindersScreen()
                }
                is PatientReminderViewModel.RemindersUiState.Error -> {
                    ErrorScreen(
                        message = (uiState as PatientReminderViewModel.RemindersUiState.Error).message,
                        onRetry = { viewModel.loadReminders() }
                    )
                }
                is PatientReminderViewModel.RemindersUiState.Success -> {
                    val data = uiState as PatientReminderViewModel.RemindersUiState.Success
                    RemindersList(
                        overdueReminders = data.overdueReminders,
                        activeReminders = data.activeReminders,
                        completedReminders = data.completedReminders,
                        onMarkCompleted = { viewModel.markReminderAsCompleted(it) },
                        onReminderClick = onNavigateToDetail
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyRemindersScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "No Reminders",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "You don't have any follow-up reminders at the moment.",
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun RemindersList(
    overdueReminders: List<FollowUpReminder>,
    activeReminders: List<FollowUpReminder>,
    completedReminders: List<FollowUpReminder>,
    onMarkCompleted: (String) -> Unit,
    onReminderClick: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Overdue Reminders Section
        if (overdueReminders.isNotEmpty()) {
            item {
                Text(
                    text = "Overdue Reminders",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            items(overdueReminders) { reminder ->
                ReminderItem(
                    reminder = reminder,
                    isOverdue = true,
                    onMarkCompleted = onMarkCompleted,
                    onClick = onReminderClick
                )
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }
        }

        // Active Reminders Section
        if (activeReminders.isNotEmpty()) {
            item {
                Text(
                    text = "Upcoming Reminders",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            items(activeReminders) { reminder ->
                ReminderItem(
                    reminder = reminder,
                    isOverdue = false,
                    onMarkCompleted = onMarkCompleted,
                    onClick = onReminderClick
                )
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }
        }

        // Completed Reminders Section
        if (completedReminders.isNotEmpty()) {
            item {
                Text(
                    text = "Completed Reminders",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            items(completedReminders) { reminder ->
                CompletedReminderItem(
                    reminder = reminder,
                    onClick = onReminderClick
                )
            }
        }
    }
}

@Composable
private fun ReminderItem(
    reminder: FollowUpReminder,
    isOverdue: Boolean,
    onMarkCompleted: (String) -> Unit,
    onClick: (String) -> Unit
) {
    val dateFormatter = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = if (isOverdue) {
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f)
            )
        } else {
            CardDefaults.cardColors()
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = dateFormatter.format(reminder.dueDate.toDate()),
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isOverdue) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                )

                if (isOverdue) {
                    Text(
                        text = "OVERDUE",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = reminder.description,
                style = MaterialTheme.typography.bodyLarge
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(
                    onClick = { onClick(reminder.id) }
                ) {
                    Text("View Details")
                }

                Spacer(modifier = Modifier.width(8.dp))

                Button(
                    onClick = { onMarkCompleted(reminder.id) }
                ) {
                    Text("Mark as Completed")
                }
            }
        }
    }
}

@Composable
private fun CompletedReminderItem(
    reminder: FollowUpReminder,
    onClick: (String) -> Unit
) {
    val dateFormatter = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Due: ${dateFormatter.format(reminder.dueDate.toDate())}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.outline
                )

                Text(
                    text = "COMPLETED",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = reminder.description,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            reminder.completedAt?.let {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Completed on: ${dateFormatter.format(it.toDate())}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(
                    onClick = { onClick(reminder.id) }
                ) {
                    Text("View Details")
                }
            }
        }
    }
}
