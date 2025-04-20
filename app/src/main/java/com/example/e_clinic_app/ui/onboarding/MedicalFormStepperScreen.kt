package com.example.e_clinic_app.ui.onboarding

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.e_clinic_app.data.model.MedicalCondition
import com.example.e_clinic_app.data.model.Medication
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.foundation.clickable
import androidx.compose.material.ModalBottomSheetLayout
import kotlinx.coroutines.launch
import androidx.compose.material3.rememberModalBottomSheetState

@Composable
fun StepperBottomNavBar(
    step: Int,
    isStepValid: Boolean,
    onBack: () -> Unit,
    onNext: () -> Unit
) {
    NavigationBar {
        if (step > 0) {
            NavigationBarItem(
                selected = false,
                onClick = onBack,
                icon = {},
                label = { Text("Back") }
            )
        } else {
            Spacer(modifier = Modifier.width(0.dp))
        }
        Spacer(modifier = Modifier.weight(1f))

        NavigationBarItem(
            selected = false,
            onClick = onNext,
            enabled = isStepValid,
            icon = {},
            label = { Text(if (step == 3) "Submit" else "Next") }
        )
    }
}

fun isStepValid(state: MedicalFormState): Boolean {
    return when (state.step) {
        0 -> state.personalInfo.firstName.isNotBlank() && state.personalInfo.lastName.isNotBlank()
        1 -> {
            when (state.hasConditions) {
                false -> true // skip step
                true -> state.conditions.isNotEmpty() // must have at least one condition saved
                else -> false // hasn't selected yes/no yet
            }
        }
        2 -> when (state.hasMedications) {
            false -> true                    // skip meds
            true  -> state.medications.isNotEmpty()
            else  -> false                   // user hasn’t chosen yet
        }
        3 -> true
        else -> false
    }
}

@Composable
fun MedicalFormStepperScreen(
    viewModel: MedicalFormStepperViewModel = viewModel(),
    onFormCompleted: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        bottomBar = {
            StepperBottomNavBar(
                step = state.step,
                isStepValid = isStepValid(state),
                onBack = { viewModel.goToPreviousStep() },
                onNext = {
                    when (state.step) {
                        0 -> viewModel.goToNextStep()
                        1 -> {
                            viewModel.applyConditionSelection()
                            viewModel.goToNextStep()
                        }
                        2 -> viewModel.goToNextStep()
                        3 -> viewModel.submitForm()
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            StepperHeader(currentStep = state.step)

            when (state.step) {
                0 -> StepPersonalInfo(
                    firstName = state.personalInfo.firstName,
                    lastName = state.personalInfo.lastName,
                    onInputChange = { first, last ->
                        viewModel.updatePersonalInfo(first, last)
                    }
                )

                1 -> StepMedicalConditions(
                    conditions = state.conditions,
                    hasConditions = state.hasConditions,
                    selectedCategory = state.selectedCategory,
                    selectedSubtype = state.selectedSubtype,
                    onSelectionChange = { viewModel.updateConditions(it) },
                    onHasConditionChange = { viewModel.setHasConditions(it) },
                    onCategoryChange = { viewModel.updateConditionCategory(it) },
                    onSubtypeChange = { viewModel.updateConditionSubtype(it) }
                )


                2 -> StepMedications(
                    medications            = state.medications,
                    hasMedications         = state.hasMedications,
                    onHasMedicationsChange = { viewModel.setHasMedications(it) },
                    onMedicationsChange    = { viewModel.updateMedications(it) }
                )

                3 -> StepReviewAndSubmit(state = state)
            }

            if (state.saveSuccess) {
                LaunchedEffect(Unit) {
                    onFormCompleted()
                }
            }
        }
    }




}


@Composable
fun StepperHeader(currentStep: Int) {
    val steps = listOf("Personal Info", "Conditions", "Medications", "Review")

    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth()
    ) {
        steps.forEachIndexed { index, title ->
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Surface(
                    color = if (index <= currentStep) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                    shape = CircleShape,
                    modifier = Modifier.size(24.dp)
                ) {}
                Text(text = title, style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}

@Composable
fun StepPersonalInfo(
    firstName: String,
    lastName: String,
    onInputChange: (String, String) -> Unit
) {
    var firstNameInput by remember { mutableStateOf(firstName) }
    var lastNameInput by remember { mutableStateOf(lastName) }
    var showError by remember { mutableStateOf(false) }

    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        OutlinedTextField(
            value = firstNameInput,
            onValueChange = {
                firstNameInput = it
                showError = false
                onInputChange(it, lastNameInput)
            },
            label = { Text("First Name") },
            isError = showError && firstNameInput.isBlank(),
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = lastNameInput,
            onValueChange = {
                lastNameInput = it
                showError = false
                onInputChange(firstNameInput, it)
            },
            label = { Text("Last Name") },
            isError = showError && lastNameInput.isBlank(),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun StepMedicalConditions(
    conditions: List<MedicalCondition>,
    hasConditions: Boolean?,
    selectedCategory: String,
    selectedSubtype: String,
    onSelectionChange: (List<MedicalCondition>) -> Unit,
    onHasConditionChange: (Boolean) -> Unit,
    onCategoryChange: (String) -> Unit,
    onSubtypeChange: (String) -> Unit
) {
    // your static lists
    val categories = listOf("Allergy", "Chronic", "Other")
    val subtypesMap = mapOf(
        "Allergy" to listOf("Food", "Pollen", "Medication"),
        "Chronic" to listOf("Diabetes", "Hypertension", "Asthma"),
        "Other" to listOf("Migraines", "Epilepsy", "None")
    )

    var showError by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Do you have any known allergies or conditions?")

        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Button(
                onClick = { onHasConditionChange(true) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (hasConditions == true) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                    contentColor   = if (hasConditions == true) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                )
            ) { Text("Yes") }

            Button(
                onClick = { onHasConditionChange(false) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (hasConditions == false) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                    contentColor   = if (hasConditions == false) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                )
            ) { Text("No") }
        }

        if (hasConditions == true) {
            // Category dropdown
            DropdownMenuWithLabel(
                label = "Category",
                options = categories,
                selectedOption = selectedCategory,
                onOptionSelected = {
                    showError = false
                    onCategoryChange(it)      // ViewModel will also reset subtype
                }
            )

            // Subtype dropdown
            DropdownMenuWithLabel(
                label = "Subtype",
                options = subtypesMap[selectedCategory] ?: emptyList(),
                selectedOption = selectedSubtype,
                onOptionSelected = {
                    showError = false
                    onSubtypeChange(it)
                }
            )

            // simple validation
            if (showError && selectedSubtype.isBlank()) {
                Text(
                    "Please select a specific condition.",
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StepMedications(
    medications: List<Medication>,
    hasMedications: Boolean?,
    onHasMedicationsChange: (Boolean) -> Unit,
    onMedicationsChange: (List<Medication>) -> Unit
) {
    // 1) picker options
    val doseOptions      = listOf("50 mg", "100 mg", "200 mg", "500 mg")
    val frequencyOptions = listOf("Daily", "Twice daily", "Weekly", "As needed")

    // 2) local sheet state + control flag
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope      = rememberCoroutineScope()
    var openSheet  by remember { mutableStateOf(false) }

    // 3) form‑field state
    var medName      by remember { mutableStateOf("") }
    var medDose      by remember { mutableStateOf("") }
    var medFrequency by remember { mutableStateOf("") }
    var doseExpanded by remember { mutableStateOf(false) }
    var freqExpanded by remember { mutableStateOf(false) }
    var showError    by remember { mutableStateOf(false) }

    // 4) only compose the sheet when openSheet == true
    if (openSheet) {
        // animate it open when it enters composition
        LaunchedEffect(openSheet) {
            if (openSheet) {
                scope.launch { sheetState.show() }
            }
        }

        ModalBottomSheet(
            sheetState       = sheetState,
            onDismissRequest = { openSheet = false }
        ) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Add a Medication", style = MaterialTheme.typography.headlineSmall)

                // Name field
                OutlinedTextField(
                    value         = medName,
                    onValueChange = { medName = it; showError = false },
                    label         = { Text("Name") },
                    modifier      = Modifier.fillMaxWidth()
                )

                // Dose picker
                ExposedDropdownMenuBox(
                    expanded         = doseExpanded,
                    onExpandedChange = { doseExpanded = it }
                ) {
                    OutlinedTextField(
                        value         = medDose,
                        readOnly      = true,
                        onValueChange = {},
                        label         = { Text("Dose") },
                        trailingIcon  = { ExposedDropdownMenuDefaults.TrailingIcon(doseExpanded) },
                        modifier      = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                            .clickable { doseExpanded = true }
                    )
                    ExposedDropdownMenu(
                        expanded         = doseExpanded,
                        onDismissRequest = { doseExpanded = false }
                    ) {
                        doseOptions.forEach { option ->
                            DropdownMenuItem(
                                text    = { Text(option) },
                                onClick = {
                                    medDose = option
                                    doseExpanded = false
                                }
                            )
                        }
                    }
                }

                // Frequency picker
                ExposedDropdownMenuBox(
                    expanded         = freqExpanded,
                    onExpandedChange = { freqExpanded = it }
                ) {
                    OutlinedTextField(
                        value         = medFrequency,
                        readOnly      = true,
                        onValueChange = {},
                        label         = { Text("Frequency") },
                        trailingIcon  = { ExposedDropdownMenuDefaults.TrailingIcon(freqExpanded) },
                        modifier      = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                            .clickable { freqExpanded = true }
                    )
                    ExposedDropdownMenu(
                        expanded         = freqExpanded,
                        onDismissRequest = { freqExpanded = false }
                    ) {
                        frequencyOptions.forEach { option ->
                            DropdownMenuItem(
                                text    = { Text(option) },
                                onClick = {
                                    medFrequency = option
                                    freqExpanded = false
                                }
                            )
                        }
                    }
                }

                // Rx scan placeholder
                Button(
                    onClick  = { /* TODO: integrate scanner */ },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("📷 Scan Rx (coming soon)")
                }

                if (showError) {
                    Text("All fields are required", color = MaterialTheme.colorScheme.error)
                }

                // Save and close
                Button(
                    onClick = {
                        if (medName.isNotBlank() && medDose.isNotBlank() && medFrequency.isNotBlank()) {
                            onMedicationsChange(medications + Medication(medName, medDose, medFrequency))
                            medName      = ""
                            medDose      = ""
                            medFrequency = ""
                            showError    = false
                            openSheet    = false
                        } else {
                            showError = true
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Save Medication")
                }
            }
        }
    }

    // 5) The main “Yes/No + list + FAB” screen:
    Column(
        Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Do you take any medications?")
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Button(
                onClick = {
                    onHasMedicationsChange(true)
                },
                colors  = ButtonDefaults.buttonColors(
                    containerColor = if (hasMedications == true) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                    contentColor   = if (hasMedications == true) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                )
            ) { Text("Yes") }

            Button(
                onClick = {
                    onHasMedicationsChange(false)
                    openSheet = false   // make sure sheet is closed if user re‑toggles No
                },
                colors  = ButtonDefaults.buttonColors(
                    containerColor = if (hasMedications == false) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                    contentColor   = if (hasMedications == false) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                )
            ) { Text("No") }
        }

        if (hasMedications == true) {
            Text("Tap + to add a medication", style = MaterialTheme.typography.bodySmall)

            if (medications.isEmpty()) {
                Text("No medications added yet.")
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    medications.forEach { med ->
                        Text("• ${med.name}, ${med.dose}, ${med.frequency}")
                    }
                }
            }

            Box(Modifier.fillMaxSize()) {
                FloatingActionButton(
                    onClick = { openSheet = true },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(16.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Medication")
                }
            }
        }
    }
}

@Composable
fun StepReviewAndSubmit(
    state: MedicalFormState
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.Start,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text("Review Your Information", style = MaterialTheme.typography.headlineSmall)

        Divider()

        Text("\uD83D\uDC64 Personal Info", style = MaterialTheme.typography.titleMedium)
        Text("First Name: ${state.personalInfo.firstName}")
        Text("Last Name: ${state.personalInfo.lastName}")

        Spacer(modifier = Modifier.height(8.dp))

        Text("\uD83E\uDDEA Medical Conditions", style = MaterialTheme.typography.titleMedium)
        if (state.conditions.isEmpty()) {
            Text("None provided")
        } else {
            state.conditions.forEach {
                Text("• ${it.category} - ${it.type}")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text("\uD83D\uDC8A Medications", style = MaterialTheme.typography.titleMedium)
        if (state.medications.isEmpty()) {
            Text("None provided")
        } else {
            state.medications.forEach {
                Text("• ${it.name}, ${it.dose}, ${it.frequency}")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (state.errorMessage != null) {
            Text("Error: ${state.errorMessage}", color = MaterialTheme.colorScheme.error)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DropdownMenuWithLabel(
    label: String,
    options: List<String>,
    selectedOption: String,
    onOptionSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = Modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = selectedOption,
            onValueChange = { /* readOnly */ },
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            modifier = Modifier
                .menuAnchor() // 📌 anchor the menu to this field
                .fillMaxWidth()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onOptionSelected(option)
                        expanded = false
                    }
                )
            }
        }
    }
}