package com.example.e_clinic_app.ui.home.patient

import com.example.e_clinic_app.ui.bottomNavBar.BottomNavigationBar
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.e_clinic_app.backend.home.PatientDashboardViewModel
import com.example.e_clinic_app.ui.navigation.Routes

/**
 * A composable function that represents the Patient Home Tab screen in the e-clinic application.
 *
 * This screen serves as the main dashboard for patients, providing access to key features such as
 * browsing doctors, viewing visits, and exploring specialties. It also displays a list of available
 * doctors and allows patients to search for doctors or specialties.
 *
 * The screen includes:
 * - A top app bar with a greeting message and a profile button.
 * - A search bar for finding doctors or specialties.
 * - A navigation grid for quick access to "Browse Doctors" and "Visits" sections.
 * - A horizontally scrollable list of specialties.
 * - A section displaying available doctors with their details, such as name, specialization, and experience.
 * - Error and loading states for the doctors list.
 *
 * @param navController The `NavController` used for navigating to other screens.
 * @param viewModel The `PatientDashboardViewModel` instance used to manage the screen's state and data.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatientHomeTabScreen(
    navController: NavController,
    viewModel: PatientDashboardViewModel
) {
    val colorScheme = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography

    // Collect the doctors list state from ViewModel
    val doctorsState by viewModel.doctorsState.collectAsState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Column {
                        Text(
                            "Hello!",
                            style = typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Find care that fits you",
                            style = typography.bodySmall,
                            color = colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { /* Navigate to profile */ }) {
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = "Profile",
                            tint = colorScheme.primary
                        )
                    }
                }
            )
        },
        bottomBar = { BottomNavigationBar(navController) },
        containerColor = colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .fillMaxSize()
        ) {
            Spacer(Modifier.height(16.dp))

            // Search Bar (logic to be implemented later)
            OutlinedTextField(
                value = viewModel.searchQuery,
                onValueChange = { viewModel.onSearchQueryChanged(it) },
                placeholder = { Text("Search doctors, specialties...") },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = "Search")
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = colorScheme.primary,
                    unfocusedBorderColor = colorScheme.outline,
                    focusedContainerColor = colorScheme.surface,
                    unfocusedContainerColor = colorScheme.surfaceVariant
                )
            )

            Spacer(Modifier.height(24.dp))

            // Navigation Grid
            val navigationItems = listOf(
                "Browse Doctors" to Routes.BROWSE_DOCTORS,
                "Visits" to Routes.VISITS,
                "Prescriptions" to Routes.PRESCRIPTIONS, // Assuming this is a placeholder for future functionality
            )
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.height(220.dp)
            ) {
                items(navigationItems) { (label, route) ->
                    ElevatedCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { navController.navigate(route) },
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                style = typography.bodyLarge,
                                fontWeight = FontWeight.Medium,
                                color = colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // Specialties Section
            Text(
                "Specialties",
                style = typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(8.dp))
            val specialties = listOf("Dentist", "Cardiologist", "Optometrist", "Dietitian")
            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                items(specialties) { specialty ->
                    ElevatedCard(
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.clickable { /* Navigate to Specialty Screen */ }
                    ) {
                        Text(
                            text = specialty,
                            style = typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // Available Doctors Section
            Text(
                "Available Doctors",
                style = typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(8.dp))

            when (doctorsState) {
                is PatientDashboardViewModel.UiState.Loading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(80.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                is PatientDashboardViewModel.UiState.Error -> {
                    Text(
                        text = "Failed to load doctors",
                        color = Color.Red,
                        modifier = Modifier.padding(8.dp)
                    )
                }

                is PatientDashboardViewModel.UiState.Success -> {
                    val doctors =
                        (doctorsState as PatientDashboardViewModel.UiState.Success).doctors
                    if (doctors.isEmpty()) {
                        Text(
                            text = "No doctors available right now.",
                            color = colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(8.dp)
                        )
                    } else {
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            items(doctors) { doctor ->
                                ElevatedCard(
                                    modifier = Modifier
                                        .width(160.dp)
                                        .clickable {
                                            navController.navigate("${Routes.DOCTOR_DETAIL}/${doctor.id}")
                                        },
                                    shape = RoundedCornerShape(16.dp)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(12.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(48.dp)
                                                .background(
                                                    colorScheme.surfaceVariant,
                                                    shape = CircleShape
                                                )
                                        )
                                        Spacer(Modifier.height(8.dp))
                                        Text(
                                            "${doctor.firstName} ${doctor.lastName}",
                                            style = typography.bodyLarge,
                                            fontWeight = FontWeight.Medium
                                        )
                                        Text(
                                            doctor.specialisation,
                                            style = typography.labelSmall,
                                            color = colorScheme.onSurfaceVariant
                                        )
                                        Spacer(Modifier.height(4.dp))
                                        Text(
                                            "${doctor.experienceYears} yrs exp.",
                                            style = typography.labelSmall,
                                            color = colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Reminders Section
            ElevatedCard(
                modifier = Modifier
                    .padding(16.dp)
                    .clickable { navController.navigate(Routes.PATIENT_REMINDERS) },
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "View Reminders",
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}