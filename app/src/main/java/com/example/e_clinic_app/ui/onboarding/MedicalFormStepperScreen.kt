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
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.rememberModalBottomSheetState
import com.example.e_clinic_app.data.model.DosageUnit
import com.example.e_clinic_app.data.model.Drug
import com.example.e_clinic_app.data.model.Frequency
import com.example.e_clinic_app.presentation.viewmodel.MedicationsViewModel
/**
 * A composable function that represents the Medical Form Stepper screen in the e-clinic application.
 *
 * This screen guides users through a multi-step form to collect their medical information, including
 * personal details, medical conditions, and medications. The form is divided into four steps:
 * - Step 1: Personal Information
 * - Step 2: Medical Conditions
 * - Step 3: Medications
 * - Step 4: Review and Submit
 *
 * The screen includes:
 * - A stepper header to indicate the current step.
 * - A bottom navigation bar for navigating between steps.
 * - Validation for each step to ensure required fields are completed.
 * - A review screen to summarize the entered information before submission.
 *
 * @param viewModel The `MedicalFormStepperViewModel` instance used to manage the form's state and logic.
 * @param onFormCompleted A callback function triggered when the form is successfully submitted.
 */
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
/**
 * A composable function that represents the Medical Form Stepper screen in the e-clinic application.
 *
 * This screen guides users through a multi-step form to collect their medical information, including
 * personal details, medical conditions, and medications. The form is divided into four steps:
 * - Step 1: Personal Information
 * - Step 2: Medical Conditions
 * - Step 3: Medications
 * - Step 4: Review and Submit
 *
 * The screen includes:
 * - A stepper header to indicate the current step.
 * - A bottom navigation bar for navigating between steps.
 * - Validation for each step to ensure required fields are completed.
 * - A review screen to summarize the entered information before submission.
 *
 * @param viewModel The `MedicalFormStepperViewModel` instance used to manage the form's state and logic.
 * @param onFormCompleted A callback function triggered when the form is successfully submitted.
 */
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

/**
 * A composable function that represents the header for the stepper.
 *
 * This component displays the step titles and highlights the current step.
 *
 * @param currentStep The index of the current step.
 */
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
/**
 * A composable function that represents the personal information step in the form.
 *
 * This step collects the user's first and last name.
 *
 * @param firstName The user's first name.
 * @param lastName The user's last name.
 * @param onInputChange A callback function triggered when the input fields are updated.
 */
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
/**
 * A composable function that represents the medical conditions step in the form.
 *
 * This step allows users to specify their medical conditions, including category and subtype.
 *
 * @param conditions A list of the user's medical conditions.
 * @param hasConditions A boolean indicating whether the user has any medical conditions.
 * @param selectedCategory The selected category of the condition.
 * @param selectedSubtype The selected subtype of the condition.
 * @param onSelectionChange A callback function triggered when the conditions list is updated.
 * @param onHasConditionChange A callback function triggered when the "Has Conditions" state changes.
 * @param onCategoryChange A callback function triggered when the condition category is updated.
 * @param onSubtypeChange A callback function triggered when the condition subtype is updated.
 */
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
/**
 * A composable function that represents the medications step in the form.
 *
 * This step allows users to specify their medications, including drug, dosage, unit, and frequency.
 *
 * @param medications A list of the user's medications.
 * @param hasMedications A boolean indicating whether the user takes any medications.
 * @param onHasMedicationsChange A callback function triggered when the "Has Medications" state changes.
 * @param onMedicationsChange A callback function triggered when the medications list is updated.
 * @param viewModel The `MedicationsViewModel` instance used to manage the medications data.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StepMedications(
    medications: List<Medication>,
    hasMedications: Boolean?,
    onHasMedicationsChange: (Boolean) -> Unit,
    onMedicationsChange: (List<Medication>) -> Unit,
    viewModel: MedicationsViewModel = viewModel()
) {
    val drugs by viewModel.drugs.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    // sheet state
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    var openSheet by remember { mutableStateOf(false) }

    // form state
    var selectedDrug by remember { mutableStateOf<Drug?>(null) }
    var availableUnits by remember { mutableStateOf<List<DosageUnit>>(emptyList()) }
    var selectedUnit by remember { mutableStateOf<DosageUnit?>(null) }
    var commonDosages by remember { mutableStateOf<List<Double>>(emptyList()) }
    var selectedAmount by remember { mutableStateOf<Double?>(null) }
    var expandedDrug by remember { mutableStateOf(false) }
    var expandedUnit by remember { mutableStateOf(false) }
    var expandedAmount by remember { mutableStateOf(false) }
    var selectedFrequency by remember { mutableStateOf<Frequency?>(null) }
    var expandedFrequency by remember { mutableStateOf(false) }
    var showError by remember { mutableStateOf(false) }

    if (openSheet) {
        LaunchedEffect(openSheet) {
            if (openSheet) sheetState.show()
        }
        ModalBottomSheet(
            sheetState = sheetState,
            onDismissRequest = { openSheet = false }
        ) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Add a Medication", style = MaterialTheme.typography.headlineSmall)

                // Drug picker
                ExposedDropdownMenuBox(
                    expanded = expandedDrug,
                    onExpandedChange = { expandedDrug = it }
                ) {
                    OutlinedTextField(
                        value = selectedDrug?.name.orEmpty(),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Medication") },
                        trailingIcon = { Icon(Icons.Filled.ArrowDropDown, null) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                            .clickable { expandedDrug = true }
                    )
                    ExposedDropdownMenu(
                        expanded = expandedDrug,
                        onDismissRequest = { expandedDrug = false }
                    ) {
                        drugs.forEach { drug ->
                            DropdownMenuItem(
                                text = { Text(drug.name) },
                                onClick = {
                                    selectedDrug = drug
                                    availableUnits = drug.availableUnits
                                    selectedUnit = drug.availableUnits.firstOrNull()
                                    commonDosages = drug.commonDosages[selectedUnit] ?: emptyList()
                                    selectedAmount = commonDosages.firstOrNull()
                                    selectedFrequency = drug.defaultFrequency
                                    expandedDrug = false
                                }
                            )
                        }
                    }
                }

                // Unit picker
                ExposedDropdownMenuBox(
                    expanded = expandedUnit,
                    onExpandedChange = { expandedUnit = it }
                ) {
                    OutlinedTextField(
                        value = selectedUnit?.name.orEmpty(),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Unit") },
                        trailingIcon = { Icon(Icons.Filled.ArrowDropDown, null) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                            .clickable { expandedUnit = true }
                    )
                    ExposedDropdownMenu(
                        expanded = expandedUnit,
                        onDismissRequest = { expandedUnit = false }
                    ) {
                        availableUnits.forEach { unit ->
                            DropdownMenuItem(
                                text = { Text(unit.name) },
                                onClick = {
                                    selectedUnit = unit
                                    commonDosages = selectedDrug?.commonDosages?.get(unit) ?: emptyList()
                                    selectedAmount = commonDosages.firstOrNull()
                                    expandedUnit = false
                                }
                            )
                        }
                    }
                }

                // Amount picker
                ExposedDropdownMenuBox(
                    expanded = expandedAmount,
                    onExpandedChange = { expandedAmount = it }
                ) {
                    OutlinedTextField(
                        value = selectedAmount?.toString().orEmpty(),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Dose") },
                        trailingIcon = { Icon(Icons.Filled.ArrowDropDown, null) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                            .clickable { expandedAmount = true }
                    )
                    ExposedDropdownMenu(
                        expanded = expandedAmount,
                        onDismissRequest = { expandedAmount = false }
                    ) {
                        commonDosages.forEach { amount ->
                            DropdownMenuItem(
                                text = { Text(amount.toString()) },
                                onClick = {
                                    selectedAmount = amount
                                    expandedAmount = false
                                }
                            )
                        }
                    }
                }

                // Frequency picker
                ExposedDropdownMenuBox(
                    expanded = expandedFrequency,
                    onExpandedChange = { expandedFrequency = it }
                ) {
                    OutlinedTextField(
                        value = selectedFrequency?.name.orEmpty(),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Frequency") },
                        trailingIcon = { Icon(Icons.Filled.ArrowDropDown, null) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                            .clickable { expandedFrequency = true }
                    )
                    ExposedDropdownMenu(
                        expanded = expandedFrequency,
                        onDismissRequest = { expandedFrequency = false }
                    ) {
                        Frequency.values().forEach { freq ->
                            DropdownMenuItem(
                                text = { Text(freq.name) },
                                onClick = {
                                    selectedFrequency = freq
                                    expandedFrequency = false
                                }
                            )
                        }
                    }
                }

                Button(
                    onClick = {
                        if (selectedDrug != null && selectedUnit != null && selectedAmount != null && selectedFrequency != null) {
                            val newMed = Medication(
                                drug = selectedDrug!!,
                                amount = selectedAmount!!,
                                unit = selectedUnit!!,
                                frequency = selectedFrequency!!
                            )
                            onMedicationsChange(medications + newMed)
                            // reset form
                            selectedDrug = null
                            availableUnits = emptyList()
                            selectedUnit = null
                            commonDosages = emptyList()
                            selectedAmount = null
                            selectedFrequency = null
                            showError = false
                            openSheet = false
                        } else {
                            showError = true
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Save Medication")
                }

                if (showError) {
                    Text("All fields must be selected", color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }

    // Main UI
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Do you take any medications?")
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Button(
                onClick = { onHasMedicationsChange(true) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (hasMedications == true) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = if (hasMedications == true) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                )
            ) { Text("Yes") }

            Button(
                onClick = {
                    onHasMedicationsChange(false)
                    openSheet = false
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (hasMedications == false) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = if (hasMedications == false) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
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
                        Text("• ${med.drug.name}: ${med.amount} ${med.unit.name}, ${med.frequency.name}")
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
                    Icon(Icons.Filled.Add, contentDescription = "Add Medication")
                }
            }
        }

        if (isLoading) {
            CircularProgressIndicator()
        }
        error?.let {
            Text("Error loading drugs: $it", color = MaterialTheme.colorScheme.error)
        }
    }
}

/**
 * A composable function that represents the review and submit step in the form.
 *
 * This step displays a summary of the entered information for the user to review before submission.
 *
 * @param state The `MedicalFormState` containing the form's current state.
 */
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
            state.medications.forEach { med: Medication ->
                Text(
                    "• ${med.drug.name}: ${med.amount} ${med.unit.name.lowercase()} — ${med.frequency.name.replace('_', ' ').lowercase().capitalize()}"
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        state.errorMessage?.let {
            Text("Error: $it", color = MaterialTheme.colorScheme.error)
        }
    }
}
/**
 * A composable function that represents a dropdown menu with a label.
 *
 * This component is used for selecting options in the form, such as condition categories or subtypes.
 *
 * @param label The label for the dropdown menu.
 * @param options A list of options to display in the dropdown menu.
 * @param selectedOption The currently selected option.
 * @param onOptionSelected A callback function triggered when an option is selected.
 */
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