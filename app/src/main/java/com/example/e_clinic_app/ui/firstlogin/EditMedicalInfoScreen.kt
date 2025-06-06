package com.example.e_clinic_app.ui.firstlogin

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.e_clinic_app.ui.admin.components.MedicalConditionPicker
import com.example.e_clinic_app.ui.onboarding.StepMedications

/**
 * A composable function that represents the Edit Medical Information screen in the e-clinic application.
 *
 * This screen allows users to input or update their medical information, including personal details,
 * known medical conditions, and medications. It provides expandable sections for better organization
 * and supports form validation to ensure required fields are completed.
 *
 * The screen handles loading states, error messages, and unsaved changes through a confirmation dialog.
 * It also provides a callback for successful submission or cancellation of the form.
 *
 * @param viewModel The `EditMedicalInfoViewModel` instance used to manage the screen's state. Defaults to a local `viewModel`.
 * @param isEditing A flag indicating whether the screen is in edit mode or submission mode.
 * @param onSubmitSuccess A callback function invoked when the form is successfully submitted.
 * @param onCancel A callback function invoked when the user cancels the form.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditMedicalInfoScreen(
    viewModel: EditMedicalInfoViewModel = viewModel(),
    isEditing: Boolean = false,
    onSubmitSuccess: () -> Unit,
    onCancel: () -> Unit
) {
    // collect UI state
    val state by viewModel.uiState.collectAsState()

    // local UI flags
    var showDiscardDialog by remember { mutableStateOf(false) }
    var personalExpanded by remember { mutableStateOf(false) }
    var conditionsExpanded by remember { mutableStateOf(false) }
    var medsExpanded by remember { mutableStateOf(false) }

    // initial load
    LaunchedEffect(isEditing) {
        viewModel.loadBasicInfo(isEditing)
    }

    // handle system back
    BackHandler {
        if (viewModel.hasChanges()) showDiscardDialog = true
        else onCancel()
    }

    // when save completes
    LaunchedEffect(state.saveSuccess) {
        if (state.saveSuccess) {
            viewModel.clearSaveSuccess()
            onSubmitSuccess()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = {
                        if (viewModel.hasChanges()) showDiscardDialog = true
                        else onCancel()
                    }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Cancel")
                    }
                },
                title = {
                    Text(if (isEditing) "Update Your Medical Info" else "Patient Medical Form")
                }
            )
        },
        bottomBar = {
            Box(Modifier.fillMaxWidth().padding(16.dp)) {
                Button(
                    onClick = { viewModel.saveBasicInfo() },
                    enabled = !state.isSaving && !state.isLoading,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        when {
                            state.isSaving -> "Saving…"
                            else           -> if (isEditing) "Save" else "Submit"
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(Modifier.fillMaxSize().padding(innerPadding)) {
            // loading overlay
            if (state.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }

            Column(
                Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 24.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Personal Info section
                ExpandableSection(
                    title    = "Personal Information",
                    expanded = personalExpanded,
                    onToggle = { personalExpanded = !personalExpanded }
                ) {
                    OutlinedTextField(
                        value = state.firstName,
                        onValueChange = { viewModel.onFirstNameChange(it) },
                        label = { Text("First Name") },
                        isError = state.firstName.isBlank()
                    )
                    OutlinedTextField(
                        value = state.lastName,
                        onValueChange = { viewModel.onLastNameChange(it) },
                        label = { Text("Last Name") },
                        isError = state.lastName.isBlank()
                    )
                }

                // Conditions section
                ExpandableSection(
                    title    = "Known Conditions",
                    expanded = conditionsExpanded,
                    onToggle = { conditionsExpanded = !conditionsExpanded }
                ) {
                    MedicalConditionPicker(
                        selectedConditions = state.knownConditions,
                        onSelectionChanged = { viewModel.onConditionsChange(it) }
                    )
                }

                // Medications section
                ExpandableSection(
                    title    = "Medications",
                    expanded = medsExpanded,
                    onToggle = { medsExpanded = !medsExpanded }
                ) {
                    StepMedications(
                        medications = state.medications,
                        hasMedications = state.hasMedications ?: false,
                        onHasMedicationsChange = { viewModel.onHasMedicationsChange(it)  },
                        onMedicationsChange = { viewModel.onMedicationsChange(it) }
                    )
                }

                state.errorMessage?.let {
                    Text("Error: $it", color = MaterialTheme.colorScheme.error)
                }
            }
        }

        // Discard changes confirmation
        if (showDiscardDialog) {
            AlertDialog(
                onDismissRequest = { showDiscardDialog = false },
                title   = { Text("Discard changes?") },
                text    = { Text("You have unsaved changes. Discard and exit?") },
                confirmButton = {
                    TextButton(onClick = onCancel) {
                        Text("Discard")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDiscardDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

/**
 * A composable function representing an expandable section with a toggleable header.
 *
 * This section is used to group related form fields, allowing users to expand or collapse the content.
 *
 * @param title The title displayed in the header of the section.
 * @param expanded A flag indicating whether the section is expanded or collapsed.
 * @param onToggle A callback function invoked when the section's header is clicked to toggle its state.
 * @param content The composable content displayed within the section when expanded.
 */
@Composable
fun ExpandableSection(
    title: String,
    expanded: Boolean,
    onToggle: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape     = MaterialTheme.shapes.medium,
        modifier  = Modifier.fillMaxWidth()
    ) {
        Column {
            // header row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onToggle)
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(title, style = MaterialTheme.typography.titleMedium)
                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null
                )
            }

            // body
            if (expanded) {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    content()
                }
            }
        }
    }
}