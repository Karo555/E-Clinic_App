package com.example.e_clinic_app.ui.home.patient

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.e_clinic_app.backend.home.PatientDashboardViewModel
import com.example.e_clinic_app.ui.bottomNavBar.BottomNavigationBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatientHomeTabScreen(navController: NavController, viewModel: PatientDashboardViewModel) {
    val colorScheme = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Column {
                        Text("Hello, User!", style = typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text("Find care that fits you", style = typography.bodySmall, color = colorScheme.onSurfaceVariant)
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

            // Search Bar
            OutlinedTextField(
                value = "",
                onValueChange = {},
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
                "Browse Doctors" to "browseDoctors",
                "Your Doctors" to "yourDoctors",
                "Prescriptions" to "prescriptions",
                "Visits" to "visits"
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

            // Specialties
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

            // Recommended Doctors
            Text(
                "Recommended Doctors",
                style = typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(8.dp))
            val doctors = listOf(
                Doctor("Dr. Crick", 3.7, "$25.00/hour"),
                Doctor("Dr. Strain", 3.0, "$22.00/hour"),
                Doctor("Dr. Lachinet", 2.9, "$29.00/hour")
            )
            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                items(doctors) { doctor ->
                    ElevatedCard(
                        modifier = Modifier
                            .width(160.dp)
                            .clickable { navController.navigate("doctorDetail/${doctor.name}") },
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .background(colorScheme.surfaceVariant, shape = CircleShape)
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                doctor.name,
                                style = typography.bodyLarge,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                "⭐ ${doctor.rating}",
                                style = typography.labelSmall,
                                color = Color(0xFFF59E0B)
                            )
                            Text(
                                doctor.price,
                                style = typography.labelSmall,
                                color = colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

// Sample Data Class
data class Doctor(val name: String, val rating: Double, val price: String)