package com.example.e_clinic_app.ui.firstlogin

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.input.KeyboardType
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.example.e_clinic_app.data.institutionsByCity
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DoctorFirstLoginScreen(
    onSubmitSuccess: () -> Unit
) {
    val scrollState = rememberScrollState()
    val daysOfWeek = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")

    val specializationOptions = listOf(
        "Cardiology", "Dermatology", "Endocrinology", "Gastroenterology", "General Practice",
        "Geriatrics", "Gynecology", "Hematology", "Neurology", "Oncology", "Ophthalmology",
        "Orthopedics", "Pediatrics", "Psychiatry", "Pulmonology", "Radiology", "Rheumatology",
        "Surgery", "Urology"
    )

    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var specialization by remember { mutableStateOf("") }
    var experienceYears by remember { mutableStateOf("") }
    var licenseNumber by remember { mutableStateOf("") }
    var bio by remember { mutableStateOf("") }
    var availableDays by remember { mutableStateOf(setOf<String>()) }

    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isSubmitting by remember { mutableStateOf(false) }

    var selectedCity by remember { mutableStateOf("") }
    var isCityDropdownExpanded by remember { mutableStateOf(false) }
    val cityList = institutionsByCity.keys.toList()

    var selectedInstitutionName by remember { mutableStateOf("") }
    var selectedInstitutionId by remember { mutableStateOf<String?>(null) }
    var isInstitutionDropdownExpanded by remember { mutableStateOf(false) }
    val institutionsInSelectedCity = institutionsByCity[selectedCity] ?: emptyList()

    var specializationExpanded by remember { mutableStateOf(false) }
    val filteredSpecializations = specializationOptions.filter {
        it.contains(specialization, ignoreCase = true)
    }

    fun isValid(): Boolean {
        return firstName.isNotBlank() &&
                lastName.isNotBlank() &&
                specialization.isNotBlank() &&
                experienceYears.toIntOrNull()?.let { it >= 0 } == true &&
                licenseNumber.isNotBlank() &&
                selectedInstitutionId != null &&
                bio.isNotBlank() &&
                availableDays.isNotEmpty()
    }

    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        topBar = { TopAppBar(title = { Text("Doctor Profile Setup") }) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(horizontal = 24.dp, vertical = 16.dp)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = { /* future implementation */ },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("📷 Upload ID to auto-fill (coming soon)")
            }

            OutlinedTextField(
                value = firstName,
                onValueChange = { firstName = it },
                label = { Text("First Name *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = firstName.isBlank()
            )

            OutlinedTextField(
                value = lastName,
                onValueChange = { lastName = it },
                label = { Text("Last Name *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = lastName.isBlank()
            )

            ExposedDropdownMenuBox(
                expanded = specializationExpanded,
                onExpandedChange = { specializationExpanded = !specializationExpanded }
            ) {
                OutlinedTextField(
                    value = specialization,
                    onValueChange = {
                        specialization = it
                        specializationExpanded = true
                    },
                    label = { Text("Specialisation *") },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth(),
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = specializationExpanded)
                    },
                    isError = specialization.isBlank(),
                    singleLine = true
                )

                ExposedDropdownMenu(
                    expanded = specializationExpanded && filteredSpecializations.isNotEmpty(),
                    onDismissRequest = { specializationExpanded = false }
                ) {
                    filteredSpecializations.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option) },
                            onClick = {
                                specialization = option
                                specializationExpanded = false
                            }
                        )
                    }
                }
            }

            OutlinedTextField(
                value = experienceYears,
                onValueChange = { experienceYears = it },
                label = { Text("Years of Experience *") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = experienceYears.toIntOrNull()?.let { it < 0 } != false
            )

            OutlinedTextField(
                value = licenseNumber,
                onValueChange = { licenseNumber = it },
                label = { Text("Medical License Number *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = licenseNumber.isBlank()
            )

            ExposedDropdownMenuBox(
                expanded = isCityDropdownExpanded,
                onExpandedChange = { isCityDropdownExpanded = !isCityDropdownExpanded }
            ) {
                OutlinedTextField(
                    value = selectedCity,
                    onValueChange = {
                        selectedCity = it
                        isCityDropdownExpanded = true
                    },
                    readOnly = true,
                    label = { Text("Select City *") },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = isCityDropdownExpanded)
                    },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth(),
                    isError = selectedCity.isBlank()
                )

                ExposedDropdownMenu(
                    expanded = isCityDropdownExpanded,
                    onDismissRequest = { isCityDropdownExpanded = false }
                ) {
                    cityList.forEach { city ->
                        DropdownMenuItem(
                            text = { Text(city) },
                            onClick = {
                                selectedCity = city
                                isCityDropdownExpanded = false
                            }
                        )
                    }
                }
            }

            if (selectedCity.isNotBlank()) {
                ExposedDropdownMenuBox(
                    expanded = isInstitutionDropdownExpanded,
                    onExpandedChange = { isInstitutionDropdownExpanded = !isInstitutionDropdownExpanded }
                ) {
                    OutlinedTextField(
                        value = selectedInstitutionName,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Select Institution *") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = isInstitutionDropdownExpanded)
                        },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth(),
                        isError = selectedInstitutionId == null
                    )

                    ExposedDropdownMenu(
                        expanded = isInstitutionDropdownExpanded,
                        onDismissRequest = { isInstitutionDropdownExpanded = false }
                    ) {
                        institutionsInSelectedCity.forEach { institution ->
                            DropdownMenuItem(
                                text = { Text(institution.name) },
                                onClick = {
                                    selectedInstitutionName = institution.name
                                    selectedInstitutionId = institution.id
                                    isInstitutionDropdownExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            OutlinedTextField(
                value = bio,
                onValueChange = { bio = it },
                label = { Text("Short Bio *") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp),
                isError = bio.isBlank()
            )

            Text("Available Days (typical) *", style = MaterialTheme.typography.labelLarge)
            daysOfWeek.forEach { day ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .toggleable(
                            value = day in availableDays,
                            onValueChange = {
                                availableDays = if (it) availableDays + day else availableDays - day
                            }
                        )
                ) {
                    Checkbox(checked = day in availableDays, onCheckedChange = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(day)
                }
            }

            errorMessage?.let {
                Text(it, color = MaterialTheme.colorScheme.error)
            }
            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    errorMessage = null
                    if (!isValid()) {
                        errorMessage = "Please correct all required fields."
                        return@Button
                    }
                    isSubmitting = true
                    val currentUser = FirebaseAuth.getInstance().currentUser
                    val db = FirebaseFirestore.getInstance()
                    if (currentUser == null) {
                        isSubmitting = false
                        errorMessage = "User session expired."
                        return@Button
                    }
                    coroutineScope.launch {
                        try {
                            val doctorInfo = mapOf(
                                "firstName"       to firstName.trim(),
                                "lastName"        to lastName.trim(),
                                "specialisation"  to specialization.trim(),
                                "experienceYears" to experienceYears.toInt(),
                                "licenseNumber"   to licenseNumber.trim(),
                                "institutionName" to selectedInstitutionName,
                                "bio"             to bio.trim(),
                                "availability"    to availableDays.isNotEmpty(),
                                "weeklySchedule"  to availableDays.associateWith { emptyList<String>() },
                                "submittedAt"     to System.currentTimeMillis()
                            )

                            val rootData = mapOf(
                                "institutionId" to selectedInstitutionId,
                                "updatedAt"     to System.currentTimeMillis()
                            )

                            db.collection("users").document(currentUser.uid)
                                .update(rootData)
                                .await()

                            db.collection("users")
                                .document(currentUser.uid)
                                .collection("profile")
                                .document("doctorInfo")
                                .set(doctorInfo)
                                .await()

                            isSubmitting = false
                            onSubmitSuccess()
                        } catch (e: Exception) {
                            isSubmitting = false
                            errorMessage = "Error saving data: ${e.message}"
                        }
                    }
                },
                enabled = !isSubmitting,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save & Continue")
            }

            Divider(modifier = Modifier.padding(vertical = 16.dp))

            Text("🔍 Preview", style = MaterialTheme.typography.titleMedium)

            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Dr. $firstName $lastName", style = MaterialTheme.typography.titleLarge)
                    Text("Specialisation: $specialization")
                    Text("Institution: $selectedInstitutionName")
                    Text("Experience: ${experienceYears.toIntOrNull() ?: "-"} years")
                    Text("Available Days: ${availableDays.joinToString()}")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(bio)
                }
            }

            Spacer(modifier = Modifier.height(48.dp))
        }
    }
}