package com.example.e_clinic_app.ui.home.patient

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.e_clinic_app.data.institutionsByCity
import com.example.e_clinic_app.presentation.viewmodel.BrowseDoctorsViewModel
import com.example.e_clinic_app.ui.navigation.Routes

/**
 * A composable function that represents the Browse Doctors screen in the e-clinic application.
 *
 * This screen allows patients to search for and filter doctors based on various criteria such as name,
 * specialization, city, and years of experience. It displays a list of doctors matching the search
 * and filter criteria, and clicking on a doctor navigates to the Doctor Detail screen.
 *
 * The screen includes:
 * - A search bar for searching doctors by name.
 * - Expandable filters for selecting specialization, city, and minimum years of experience.
 * - A scrollable list of doctors with their details such as name, specialization, institution, and bio.
 *
 * @param navController The `NavController` used for navigation to other screens.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BrowseDoctorsScreen(navController: NavController) {
    val vm: BrowseDoctorsViewModel = viewModel()
    val doctors by vm.filteredDoctors.collectAsState()
    val query by vm.searchQuery.collectAsState()
    val spec by vm.selectedSpecialisation.collectAsState()
    val city by vm.selectedCity.collectAsState()
    val minExp by vm.minExperience.collectAsState()

    var filtersExpanded by remember { mutableStateOf(false) }
    val specializationOptions = listOf(
        "Cardiology", "Dermatology", "Endocrinology", "Gastroenterology", "General Practice",
        "Geriatrics", "Gynecology", "Hematology", "Neurology", "Oncology", "Ophthalmology",
        "Orthopedics", "Pediatrics", "Psychiatry", "Pulmonology", "Radiology", "Rheumatology",
        "Surgery", "Urology"
    )
    val cityList = institutionsByCity.keys.toList()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Search bar
        item {
            OutlinedTextField(
                value = query,
                onValueChange = vm::onSearchQueryChange,
                label = { Text("Search doctors by name") },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true,
                keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { /* Handle search action */ })
            )
        }
        // Filter toggle
        item {
            TextButton(onClick = { filtersExpanded = !filtersExpanded }) {
                Text(if (filtersExpanded) "Hide Filters" else "Show Filters")
            }
        }
        // Filters section
        if (filtersExpanded) {
            item {
                DropdownMenuBox(
                    selected = spec,
                    options = specializationOptions,
                    label = "Specialisation",
                    onOptionSelected = { vm.onSpecialisationChange(it) }
                )
            }
            item { Spacer(Modifier.height(8.dp)) }
            item {
                DropdownMenuBox(
                    selected = city,
                    options = cityList,
                    label = "City",
                    onOptionSelected = { vm.onCityChange(it) }
                )
            }
            item { Spacer(Modifier.height(8.dp)) }
            item {
                OutlinedTextField(
                    value = if (minExp > 0) minExp.toString() else "",
                    onValueChange = { it.toIntOrNull()?.let(vm::onMinExperienceChange) },
                    label = { Text("Min Years of Experience") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        }
        // List of doctors
        items(doctors) { doc ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        navController.navigate("${Routes.DOCTOR_DETAIL}/${doc.id}")
                    }
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Dr. ${doc.firstName} ${doc.lastName}", style = MaterialTheme.typography.titleMedium)
                    Text(doc.specialisation, style = MaterialTheme.typography.bodyMedium)
                    Text(doc.institutionName, style = MaterialTheme.typography.bodyMedium)
                    Text("Experience: ${doc.experienceYears} years", style = MaterialTheme.typography.bodySmall)
                    Text(doc.bio, style = MaterialTheme.typography.bodySmall, maxLines = 2)
                }
            }
        }
    }
}
/**
 * A composable function that represents a dropdown menu for selecting an option from a list.
 *
 * This component is used in the filters section of the Browse Doctors screen to allow users to
 * select a specialization or city from a predefined list of options.
 *
 * @param T The type of the options in the dropdown menu.
 * @param selected The currently selected option.
 * @param options The list of options to display in the dropdown menu.
 * @param label The label for the dropdown menu.
 * @param onOptionSelected A callback invoked when an option is selected.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun <T> DropdownMenuBox(
    selected: T?,
    options: List<T>,
    label: String,
    onOptionSelected: (T) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        OutlinedTextField(
            value = selected?.toString() ?: "",
            onValueChange = {},
            label = { Text(label) },
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = true },
            readOnly = true,
            trailingIcon = {
                Icon(
                    imageVector = if (expanded) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                    contentDescription = if (expanded) "Collapse" else "Expand"
                )
            }
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option.toString()) },
                    onClick = {
                        onOptionSelected(option)
                        expanded = false
                    }
                )
            }
        }
    }
}