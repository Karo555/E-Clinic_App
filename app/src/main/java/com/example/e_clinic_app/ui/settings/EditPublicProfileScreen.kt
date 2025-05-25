package com.example.e_clinic_app.ui.settings

import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.example.e_clinic_app.data.institutionsByCity
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditPublicProfileScreen(
    onSubmitSuccess: () -> Unit,
    onCancel: () -> Unit
) {
    val scrollState = rememberScrollState()
    val daysOfWeek = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")
    val specializationOptions = listOf(
        "Cardiology", "Dermatology", "Endocrinology", "Gastroenterology", "General Practice",
        "Geriatrics", "Gynecology", "Hematology", "Neurology", "Oncology", "Ophthalmology",
        "Orthopedics", "Pediatrics", "Psychiatry", "Pulmonology", "Radiology", "Rheumatology",
        "Surgery", "Urology"
    )

    // UI state
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var specialization by remember { mutableStateOf("") }
    var experienceYears by remember { mutableStateOf("") }
    var licenseNumber by remember { mutableStateOf("") }
    var bio by remember { mutableStateOf("") }
    var availableDays by remember { mutableStateOf(setOf<String>()) }

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

    var isLoading by remember { mutableStateOf(true) }
    var isSubmitting by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val coroutineScope = rememberCoroutineScope()

    // Load existing data
    LaunchedEffect(Unit) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser == null) {
            onCancel()
            return@LaunchedEffect
        }
        try {
            val db = FirebaseFirestore.getInstance()
            val rootSnap = db.collection("users").document(currentUser.uid).get().await()
            val profileSnap = db.collection("users")
                .document(currentUser.uid)
                .collection("profile")
                .document("doctorInfo")
                .get()
                .await()

            // Populate fields
            firstName = profileSnap.getString("firstName") ?: ""
            lastName = profileSnap.getString("lastName") ?: ""
            specialization = profileSnap.getString("specialisation") ?: ""
            experienceYears = profileSnap.getLong("experienceYears")?.toString() ?: ""
            licenseNumber = profileSnap.getString("licenseNumber") ?: ""
            bio = profileSnap.getString("bio") ?: ""
            availableDays = (profileSnap.get("weeklySchedule") as? Map<String, List<String>>)?.keys?.toSet() ?: emptySet()

            selectedInstitutionName = profileSnap.getString("institutionName") ?: ""
            // Find institutionId and city
            val instId = rootSnap.getString("institutionId")
            if (instId != null) {
                selectedInstitutionId = instId
                institutionsByCity.forEach { (city, list) ->
                    list.firstOrNull { it.id == instId }?.let {
                        selectedCity = city
                    }
                }
            }
        } catch (e: Exception) {
            errorMessage = "Error loading profile: ${e.message}"
        } finally {
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onCancel) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Cancel")
                    }
                },
                title = { Text("Edit Public Profile") }
            )
        },
        bottomBar = {
            Box(Modifier.fillMaxWidth().padding(16.dp)) {
                Button(
                    onClick = {
                        errorMessage = null
                        // Validate
                        if (firstName.isBlank() || lastName.isBlank() || specialization.isBlank() ||
                            experienceYears.toIntOrNull()?.let { it < 0 } != false ||
                            licenseNumber.isBlank() || selectedInstitutionId == null ||
                            bio.isBlank() || availableDays.isEmpty()
                        ) {
                            errorMessage = "Please correct all required fields."
                            return@Button
                        }

                        isSubmitting = true
                        coroutineScope.launch {
                            try {
                                val currentUser = FirebaseAuth.getInstance().currentUser!!
                                val db = FirebaseFirestore.getInstance()
                                // Root update
                                val rootData = mapOf(
                                    "institutionId" to selectedInstitutionId
                                )
                                db.collection("users").document(currentUser.uid)
                                    .update(rootData)
                                    .await()

                                // Profile update
                                val doctorInfo = mapOf(
                                    "firstName" to firstName.trim(),
                                    "lastName" to lastName.trim(),
                                    "specialisation" to specialization.trim(),
                                    "experienceYears" to experienceYears.toInt(),
                                    "licenseNumber" to licenseNumber.trim(),
                                    "institutionName" to selectedInstitutionName,
                                    "bio" to bio.trim(),
                                    "availability" to availableDays.isNotEmpty(),
                                    "weeklySchedule" to availableDays.associateWith { emptyList<String>() }
                                )
                                db.collection("users")
                                    .document(currentUser.uid)
                                    .collection("profile")
                                    .document("doctorInfo")
                                    .set(doctorInfo)
                                    .await()

                                onSubmitSuccess()
                            } catch (e: Exception) {
                                errorMessage = "Error saving profile: ${e.message}"
                            } finally {
                                isSubmitting = false
                            }
                        }
                    },
                    enabled = !isSubmitting,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (isSubmitting) "Saving…" else "Save Profile")
                }
            }
        }
    ) { innerPadding ->
        Box(Modifier.fillMaxSize().padding(innerPadding)) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
            Column(
                modifier = Modifier
                    .padding(horizontal = 24.dp, vertical = 16.dp)
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // First/Last Name
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

                // Specialisation dropdown
                ExposedDropdownMenuBox(
                    expanded = specializationExpanded,
                    onExpandedChange = { specializationExpanded = !specializationExpanded }
                ) {
                    OutlinedTextField(
                        value = specialization,
                        onValueChange = { specialization = it; specializationExpanded = true },
                        label = { Text("Specialisation *") },
                        modifier = Modifier.fillMaxWidth().menuAnchor(),
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = specializationExpanded) },
                        singleLine = true,
                        isError = specialization.isBlank()
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

                // Experience & License
                OutlinedTextField(
                    value = experienceYears,
                    onValueChange = { experienceYears = it },
                    label = { Text("Years of Experience *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
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

                // Institution selection
                ExposedDropdownMenuBox(
                    expanded = isCityDropdownExpanded,
                    onExpandedChange = { isCityDropdownExpanded = !isCityDropdownExpanded }
                ) {
                    OutlinedTextField(
                        value = selectedCity,
                        onValueChange = { selectedCity = it; isCityDropdownExpanded = true },
                        label = { Text("Select City *") },
                        modifier = Modifier.fillMaxWidth().menuAnchor(),
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isCityDropdownExpanded) },
                        readOnly = true,
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
                                    selectedInstitutionName = ""
                                    selectedInstitutionId = null
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
                            label = { Text("Select Institution *") },
                            modifier = Modifier.fillMaxWidth().menuAnchor(),
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isInstitutionDropdownExpanded) },
                            readOnly = true,
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

                // Bio
                OutlinedTextField(
                    value = bio,
                    onValueChange = { bio = it },
                    label = { Text("Short Bio *") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    isError = bio.isBlank()
                )

                // Available Days
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

                // Error message
                errorMessage?.let {
                    Text(it, color = MaterialTheme.colorScheme.error)
                }

                Spacer(modifier = Modifier.height(48.dp))
            }
        }
    }
}