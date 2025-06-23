package com.example.e_clinic_app.ui.home.doctor
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.e_clinic_app.helpers.PatientListViewModelFactory
import com.example.e_clinic_app.presentation.viewmodel.PatientListViewModel
import com.example.e_clinic_app.ui.navigation.Routes
import java.text.SimpleDateFormat
import java.util.Locale
import androidx.compose.foundation.lazy.items
/**
 * A composable function that represents the Patients screen in the e-clinic application.
 *
 * This screen displays a searchable and sortable list of patients associated with a specific doctor.
 * It allows the doctor to search for patients by name and view their last visit date. Clicking on a
 * patient navigates to the Patient Detail screen for more information.
 *
 * The screen includes:
 * - A search bar for filtering patients by name.
 * - A toggle button for sorting the list (currently a placeholder).
 * - A scrollable list of patients with their names and last visit dates.
 *
 * @param navController The `NavController` used for navigation to other screens.
 * @param doctorId The unique identifier of the doctor whose patients are being displayed.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatientsScreen(
    navController: NavController,
    doctorId: String
) {
    val vm: PatientListViewModel = viewModel(
        factory = PatientListViewModelFactory(doctorId)
    )
    val patients by vm.filteredPatients.collectAsState()
    val query by vm.searchQuery.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Patients") })
        },
        bottomBar = {
            com.example.e_clinic_app.ui.bottomNavBar.BottomNavigationBar(navController)
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = query,
                onValueChange = vm::onSearchQueryChange,
                label = { Text("Search patients") },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { /* no-op */ })
            )
            Spacer(Modifier.height(8.dp))
            TextButton(onClick = { /* placeholder for sort toggle */ }) {
                Text("Sort: Recent")
            }
            Spacer(Modifier.height(8.dp))
            LazyColumn {
                items(patients) { item ->
                    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault()) }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                navController.navigate("${Routes.PATIENT_DETAIL}/${item.patient.id}")
                            }
                            .padding(vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "${item.patient.firstName} ${item.patient.lastName}")
                        Text(text = dateFormat.format(item.lastVisit.toDate()))
                    }
                }
            }
        }
    }
}