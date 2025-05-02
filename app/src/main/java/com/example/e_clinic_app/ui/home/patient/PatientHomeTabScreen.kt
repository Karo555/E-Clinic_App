package com.example.e_clinic_app.ui.home.patient

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController

@Composable
fun PatientHomeTabScreen(navController: NavController) { // Pass NavController
    Column(modifier = Modifier.fillMaxSize().background(Color(0xFFEFF7F9))) {
        // User Info Section
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "Hi [name]!", fontSize = 20.sp)
            Spacer(modifier = Modifier.weight(1f))
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(Color.Gray, shape = CircleShape)
            )
        }

        // Search Bar
        TextField(
            value = "",
            onValueChange = {},
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            placeholder = { Text("Find Your Doctor") },
            trailingIcon = {
                IconButton(onClick = { /* Clear search */ }) {
                    Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear")
                }
            }
        )

        // Navigation Panels
        val navigationItems = listOf(
            "Browse Doctors" to "browseDoctors",
            "Your Doctors" to "yourDoctors",
            "Prescriptions" to "prescriptions",
            "Visits" to "visits"
        )

        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            for (i in navigationItems.chunked(2)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    i.forEach { (label, route) ->
                        androidx.compose.material.Card(
                            modifier = Modifier
                                .size(140.dp)
                                .clickable { navController.navigate(route) },
                            elevation = 4.dp,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.fillMaxSize().padding(8.dp)
                            ) {
                                Text(text = label, fontSize = 14.sp)
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Specialties Section
        val specialties = listOf("Dentist", "Cardiologist", "Optometrist", "Dietitian")
        LazyRow(modifier = Modifier.padding(16.dp)) {
            items(specialties) { specialty ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.Blue),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .padding(8.dp)
                        .clickable { /* Navigate to Specialty Screen */ }
                ) {
                    Text(
                        text = specialty,
                        color = Color.White,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }

        // Recommended Doctors
        val doctors = listOf(
            Doctor("Dr. Crick", 3.7, "$25.00/hour"),
            Doctor("Dr. Strain", 3.0, "$22.00/hour"),
            Doctor("Dr. Lachinet", 2.9, "$29.00/hour")
        )
        LazyRow(modifier = Modifier.padding(16.dp)) {
            items(doctors) { doctor ->
                Card(
                    modifier = Modifier
                        .padding(8.dp)
                        .clickable { navController.navigate("doctorDetail/${doctor.name}") }, // Navigate to doctor details
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier.size(50.dp).background(Color.Gray, shape = CircleShape)
                        )
                        Text(text = doctor.name, fontWeight = FontWeight.Bold)
                        Text(text = "⭐ ${doctor.rating}")
                        Text(text = doctor.price, fontSize = 12.sp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))
    }
}

// Sample Data Class
data class Doctor(val name: String, val rating: Double, val price: String)

@Composable
fun PatientHomeTabPreview() {
    val navController = rememberNavController()
    PatientHomeTabScreen(navController)
}

@Preview(showBackground = true)
@Composable
fun PreviewPatientHome() {
    PatientHomeTabPreview()
}