package com.example.e_clinic_app.ui.home.doctor

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.e_clinic_app.data.appointment.Appointment
import com.example.e_clinic_app.data.model.CareGap
import com.example.e_clinic_app.data.model.CareGapPriority
import com.example.e_clinic_app.data.model.CareGapStatus
import com.example.e_clinic_app.data.model.FollowUpReminder
import com.example.e_clinic_app.data.model.FollowUpStatus
import com.example.e_clinic_app.presentation.viewmodel.PatientDetailState
import com.example.e_clinic_app.presentation.viewmodel.PatientDetailViewModel
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

/**
 * A composable function that represents the Patient Detail screen in the e-clinic application.
 *
 * This screen displays detailed information about a specific patient, including their name, email,
 * and bio. It handles different UI states such as loading, error, and success, ensuring a smooth
 * user experience. The screen also includes a top app bar with a back navigation button.
 *
 * @param navController The `NavController` used for navigating back to the previous screen.
 * @param patientId The unique identifier of the patient whose details are being displayed.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatientDetailScreen(
    navController: NavController,
    patientId: String
) {
    // Obtain the ViewModel with patientId
    val vm: PatientDetailViewModel = viewModel(
        factory = PatientDetailViewModel.provideFactory(
            firestore = FirebaseFirestore.getInstance(),
            patientId = patientId
        )
    )

    val uiState = vm.uiState.collectAsState().value

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Patient Details") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            when (uiState) {
                is PatientDetailState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is PatientDetailState.Error -> {
                    Text(
                        text = uiState.message,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                is PatientDetailState.Success -> {
                    PatientDetailContent(
                        patientState = uiState,
                        viewModel = vm
                    )
                }
            }
        }
    }
}

@Composable
private fun PatientDetailContent(
    patientState: PatientDetailState.Success,
    viewModel: PatientDetailViewModel
) {
    val profile = patientState.profile

    // State for dialogs
    var showFollowUpDialog by remember { mutableStateOf(false) }
    var showCareGapDialog by remember { mutableStateOf(false) }

    // Handle follow-up dialog if shown
    if (showFollowUpDialog) {
        FollowUpReminderDialog(
            onDismiss = { showFollowUpDialog = false },
            onConfirm = { description, dueDate ->
                viewModel.createFollowUpReminder(description, dueDate)
                showFollowUpDialog = false
            }
        )
    }

    // Handle care gap dialog if shown
    if (showCareGapDialog) {
        CareGapDialog(
            onDismiss = { showCareGapDialog = false },
            onConfirm = { description, action, priority ->
                viewModel.createCareGap(description, action, priority)
                showCareGapDialog = false
            }
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Patient Profile Section
        item {
            PatientProfileSection(profile)
        }

        // Visit History Section
        item {
            SectionHeader(
                title = "Visit History",
                icon = Icons.Default.History
            )
        }

        if (patientState.visits.isEmpty()) {
            item {
                EmptyStateMessage(message = "No previous visits with this patient")
            }
        } else {
            items(patientState.visits) { visit ->
                VisitHistoryItem(visit)
            }
        }

        // Follow-up Reminders Section
        item {
            SectionHeader(
                title = "Follow-up Reminders",
                icon = Icons.Default.Notifications,
                actionButton = {
                    IconButton(onClick = { showFollowUpDialog = true }) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "Add Follow-up"
                        )
                    }
                }
            )
        }

        if (patientState.followUps.isEmpty()) {
            item {
                EmptyStateMessage(message = "No follow-up reminders for this patient")
            }
        } else {
            items(patientState.followUps) { followUp ->
                FollowUpReminderItem(
                    followUp = followUp,
                    onStatusChange = { reminderId, newStatus ->
                        viewModel.updateFollowUpStatus(reminderId, newStatus)
                    }
                )
            }
        }

        // Care Gaps Section
        item {
            SectionHeader(
                title = "Care Gaps",
                icon = Icons.Default.MedicalServices,
                actionButton = {
                    IconButton(onClick = { showCareGapDialog = true }) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "Add Care Gap"
                        )
                    }
                }
            )
        }

        if (patientState.careGaps.isEmpty()) {
            item {
                EmptyStateMessage(message = "No care gaps identified for this patient")
            }
        } else {
            items(patientState.careGaps) { careGap ->
                CareGapItem(
                    careGap = careGap,
                    onStatusChange = { careGapId, newStatus ->
                        viewModel.updateCareGapStatus(careGapId, newStatus)
                    }
                )
            }
        }
    }
}

@Composable
private fun PatientProfileSection(profile: com.example.e_clinic_app.presentation.viewmodel.PatientProfile) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "${profile.firstName} ${profile.lastName}",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = 4.dp)
            ) {
                Icon(
                    Icons.Default.Email,
                    contentDescription = "Email",
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = profile.email,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            if (profile.bio.isNotEmpty()) {
                Row(
                    verticalAlignment = Alignment.Top,
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = "Bio",
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = profile.bio,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    actionButton: @Composable (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.weight(1f)
        )
        actionButton?.invoke()
    }
    Divider()
}

@Composable
private fun EmptyStateMessage(message: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun VisitHistoryItem(appointment: Appointment) {
    val dateFormat = SimpleDateFormat("MMM dd, yyyy 'at' hh:mm a", Locale.getDefault())
    val formattedDate = appointment.date?.let { dateFormat.format(it.toDate()) } ?: "Date not available"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = formattedDate,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )

//            Text(
//                text = "Reason: ${appointment.reason ?: "Not specified"}",
//                style = MaterialTheme.typography.bodyMedium
//            )

            Text(
                text = "Status: ${appointment.status?.name ?: "Unknown"}",
                style = MaterialTheme.typography.bodySmall
            )

//            if (!appointment.notes.isNullOrEmpty()) {
//                Text(
//                    text = "Notes: ${appointment.notes}",
//                    style = MaterialTheme.typography.bodySmall
//                )
//            }
        }
    }
}

@Composable
private fun FollowUpReminderItem(
    followUp: FollowUpReminder,
    onStatusChange: (String, FollowUpStatus) -> Unit
) {
    val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    val dueDateFormatted = dateFormat.format(followUp.dueDate.toDate())
    val isCompleted = followUp.status == FollowUpStatus.COMPLETED
    val isPending = followUp.status == FollowUpStatus.PENDING

    var expandedMenu by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        colors = CardDefaults.cardColors(
            containerColor = when (followUp.status) {
                FollowUpStatus.COMPLETED -> MaterialTheme.colorScheme.surfaceVariant
                FollowUpStatus.CANCELLED -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
                else -> MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Due: $dueDateFormatted",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    when (followUp.status) {
                        FollowUpStatus.COMPLETED -> {
                            Surface(
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(4.dp),
                                modifier = Modifier.padding(horizontal = 4.dp)
                            ) {
                                Text(
                                    text = "Completed",
                                    style = MaterialTheme.typography.labelSmall,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                        FollowUpStatus.CANCELLED -> {
                            Surface(
                                color = MaterialTheme.colorScheme.error.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(4.dp),
                                modifier = Modifier.padding(horizontal = 4.dp)
                            ) {
                                Text(
                                    text = "Cancelled",
                                    style = MaterialTheme.typography.labelSmall,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                        FollowUpStatus.PENDING -> {
                            Surface(
                                color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(4.dp),
                                modifier = Modifier.padding(horizontal = 4.dp)
                            ) {
                                Text(
                                    text = "Pending",
                                    style = MaterialTheme.typography.labelSmall,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    color = MaterialTheme.colorScheme.tertiary
                                )
                            }
                        }
                    }
                }

                Text(
                    text = followUp.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isCompleted) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
                )
            }

            Box {
                IconButton(onClick = { expandedMenu = true }) {
                    Icon(
                        Icons.Default.MoreVert,
                        contentDescription = "More options"
                    )
                }

                DropdownMenu(
                    expanded = expandedMenu,
                    onDismissRequest = { expandedMenu = false }
                ) {
                    if (isPending) {
                        DropdownMenuItem(
                            text = { Text("Mark as Completed") },
                            onClick = {
                                onStatusChange(followUp.id, FollowUpStatus.COMPLETED)
                                expandedMenu = false
                            },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.CheckCircle,
                                    contentDescription = null
                                )
                            }
                        )

                        DropdownMenuItem(
                            text = { Text("Cancel") },
                            onClick = {
                                onStatusChange(followUp.id, FollowUpStatus.CANCELLED)
                                expandedMenu = false
                            },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Cancel,
                                    contentDescription = null
                                )
                            }
                        )
                    }

                    if (followUp.status == FollowUpStatus.CANCELLED) {
                        DropdownMenuItem(
                            text = { Text("Mark as Pending") },
                            onClick = {
                                onStatusChange(followUp.id, FollowUpStatus.PENDING)
                                expandedMenu = false
                            },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Refresh,
                                    contentDescription = null
                                )
                            }
                        )
                    }

                    if (followUp.status == FollowUpStatus.COMPLETED) {
                        DropdownMenuItem(
                            text = { Text("Mark as Pending") },
                            onClick = {
                                onStatusChange(followUp.id, FollowUpStatus.PENDING)
                                expandedMenu = false
                            },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Refresh,
                                    contentDescription = null
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CareGapItem(
    careGap: CareGap,
    onStatusChange: (String, CareGapStatus) -> Unit
) {
    val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    val createdAtFormatted = dateFormat.format(careGap.createdAt.toDate())
    val isResolved = careGap.status == CareGapStatus.RESOLVED

    var expandedMenu by remember { mutableStateOf(false) }

    val priorityColor = when (careGap.priority) {
        CareGapPriority.HIGH -> MaterialTheme.colorScheme.error
        CareGapPriority.MEDIUM -> MaterialTheme.colorScheme.tertiary
        CareGapPriority.LOW -> MaterialTheme.colorScheme.primary
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .border(
                width = 1.dp,
                color = priorityColor.copy(alpha = 0.5f),
                shape = RoundedCornerShape(8.dp)
            ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        colors = CardDefaults.cardColors(
            containerColor = when (careGap.status) {
                CareGapStatus.RESOLVED -> MaterialTheme.colorScheme.surfaceVariant
                else -> MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            color = priorityColor.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(4.dp),
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Text(
                                text = "${careGap.priority.name} Priority",
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                color = priorityColor
                            )
                        }

                        when (careGap.status) {
                            CareGapStatus.RESOLVED -> {
                                Surface(
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = "Resolved",
                                        style = MaterialTheme.typography.labelSmall,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                            CareGapStatus.IN_PROGRESS -> {
                                Surface(
                                    color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = "In Progress",
                                        style = MaterialTheme.typography.labelSmall,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                        color = MaterialTheme.colorScheme.secondary
                                    )
                                }
                            }
                            else -> { /* No special UI for OPEN status */ }
                        }
                    }

                    Text(
                        text = "Created: $createdAtFormatted",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Box {
                    IconButton(onClick = { expandedMenu = true }) {
                        Icon(
                            Icons.Default.MoreVert,
                            contentDescription = "More options"
                        )
                    }

                    DropdownMenu(
                        expanded = expandedMenu,
                        onDismissRequest = { expandedMenu = false }
                    ) {
                        if (careGap.status != CareGapStatus.RESOLVED) {
                            DropdownMenuItem(
                                text = { Text("Mark as Resolved") },
                                onClick = {
                                    onStatusChange(careGap.id, CareGapStatus.RESOLVED)
                                    expandedMenu = false
                                },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.CheckCircle,
                                        contentDescription = null
                                    )
                                }
                            )
                        }

                        if (careGap.status == CareGapStatus.OPEN) {
                            DropdownMenuItem(
                                text = { Text("Mark as In Progress") },
                                onClick = {
                                    onStatusChange(careGap.id, CareGapStatus.IN_PROGRESS)
                                    expandedMenu = false
                                },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.Pending,
                                        contentDescription = null
                                    )
                                }
                            )
                        }

                        if (careGap.status == CareGapStatus.RESOLVED || careGap.status == CareGapStatus.IN_PROGRESS) {
                            DropdownMenuItem(
                                text = { Text("Mark as Open") },
                                onClick = {
                                    onStatusChange(careGap.id, CareGapStatus.OPEN)
                                    expandedMenu = false
                                },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.Refresh,
                                        contentDescription = null
                                    )
                                }
                            )
                        }
                    }
                }
            }

            Text(
                text = careGap.description,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = if (isResolved) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = "Recommended Action: ${careGap.recommendedAction}",
                style = MaterialTheme.typography.bodyMedium,
                color = if (isResolved) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FollowUpReminderDialog(
    onDismiss: () -> Unit,
    onConfirm: (description: String, dueDate: Timestamp) -> Unit
) {
    var description by remember { mutableStateOf("") }
    var selectedDate by remember { mutableStateOf(Calendar.getInstance()) }
    var showDatePicker by remember { mutableStateOf(false) }

    val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())

    // Hold a temporary date for the date picker dialog
    var tempDate by remember { mutableStateOf(selectedDate.timeInMillis) }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = selectedDate.timeInMillis
        )

        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let {
                        selectedDate.timeInMillis = it
                    }
                    showDatePicker = false
                }) {
                    Text("Confirm")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create Follow-up Reminder") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    placeholder = { Text("What to follow up on") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = dateFormat.format(selectedDate.time),
                    onValueChange = { },
                    label = { Text("Due Date") },
                    readOnly = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showDatePicker = true },
                    trailingIcon = {
                        Icon(Icons.Default.DateRange, contentDescription = "Select date")
                    }
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (description.isNotBlank()) {
                        onConfirm(
                            description,
                            Timestamp(selectedDate.time)
                        )
                    }
                },
                enabled = description.isNotBlank()
            ) {
                Text("Create")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CareGapDialog(
    onDismiss: () -> Unit,
    onConfirm: (description: String, recommendedAction: String, priority: CareGapPriority) -> Unit
) {
    var description by remember { mutableStateOf("") }
    var recommendedAction by remember { mutableStateOf("") }
    var selectedPriority by remember { mutableStateOf(CareGapPriority.MEDIUM) }
    var showPriorityMenu by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Identify Care Gap") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    placeholder = { Text("Describe the care gap") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = recommendedAction,
                    onValueChange = { recommendedAction = it },
                    label = { Text("Recommended Action") },
                    placeholder = { Text("What action should be taken") },
                    modifier = Modifier.fillMaxWidth()
                )

                Column {
                    Text(
                        text = "Priority",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showPriorityMenu = true }
                    ) {
                        OutlinedTextField(
                            value = selectedPriority.name,
                            onValueChange = { },
                            readOnly = true,
                            enabled = false,
                            modifier = Modifier
                                .fillMaxWidth(),
                            trailingIcon = {
                                Icon(Icons.Default.ArrowDropDown, contentDescription = "Select priority")
                            }
                        )

                        DropdownMenu(
                            expanded = showPriorityMenu,
                            onDismissRequest = { showPriorityMenu = false }
                        ) {
                            CareGapPriority.values().forEach { priority ->
                                DropdownMenuItem(
                                    text = { Text(priority.name) },
                                    onClick = {
                                        selectedPriority = priority
                                        showPriorityMenu = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (description.isNotBlank() && recommendedAction.isNotBlank()) {
                        onConfirm(
                            description,
                            recommendedAction,
                            selectedPriority
                        )
                    }
                },
                enabled = description.isNotBlank() && recommendedAction.isNotBlank()
            ) {
                Text("Create")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}