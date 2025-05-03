package com.example.e_clinic_app.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.Icons.Default
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.AssignmentTurnedIn
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.House
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController

@Composable
fun PatientHomeTabScreen(navController: NavController) {

    Column{
        // searchbar
        TextField(
            value = "",
            onValueChange = {},
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            placeholder = { Text("Find Your Doctor") },
            trailingIcon = {
                IconButton(onClick = { /* clear search */ }) {
                    Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear")
                }
            }
        )

        // navigation panels
        val navigationItems = listOf(
            "Browse Doctors" to "browseDoctors",
            "Your Doctors" to "yourDoctors",
            "Prescriptions" to "prescriptions",
            "Visits" to "visits"
        )

        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                FeatureCard(icon = Icons.Default.House, label = "Browse Doctors") {
                    navController.navigate("browseDoctors")
                }
                FeatureCard(icon = Icons.Default.AccountCircle, label = "Your Doctors") {
                    navController.navigate("yourDoctors")
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                FeatureCard(icon = Icons.Default.AssignmentTurnedIn, label = "Prescriptions") {
                    navController.navigate("prescriptions")
                }
                FeatureCard(icon = Icons.Default.ChatBubble, label = "Visits") {
                    navController.navigate("visits")
                }
            }
        }

        // specialties section
        // for now it's an example list
        val specialties = listOf("Dentist", "Cardiologist", "Optometrist", "Dietitian", "Neurologist", "Pediatrician") // example list
        val specialtyColors = listOf(Color(0xFF4169E1), Color(0xFF00C49A), Color(0xFFFFA500), Color(0xFFEF5350))

        LazyRow(modifier = Modifier.padding(16.dp)) {
            itemsIndexed(specialties) { index, specialty ->
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = specialtyColors[index % specialtyColors.size]),
                    modifier = Modifier
                        .padding(end = 8.dp)
                        .clickable { }
                ) {
                    Text(
                        text = specialty,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        // recommended doctors section
        // for now an example list
        val doctors = listOf(
            Doctor("Dr. A", 3.5, "$25.00/hour"),
            Doctor("Dr. B", 3.0, "$22.00/hour"),
            Doctor("Dr. C", 4.5, "$29.00/hour")
        )
        LazyRow(modifier = Modifier.padding(16.dp)) {
            items(doctors) { doctor ->
                Card(
                    modifier = Modifier
                        .padding(8.dp)
                        .width(140.dp)
                        .clickable { navController.navigate("doctorDetail/${doctor.name}") },
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(60.dp)
                                .background(Color.Gray, shape = CircleShape)
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(doctor.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text("\u2B50 ${doctor.rating}", fontSize = 12.sp)
                        Text(doctor.price, fontSize = 12.sp, color = Color.Gray)
                    }
                }
            }
        }
    }
}

// example doctor data
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

@Composable
fun FeatureCard(icon: ImageVector, label: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .width(140.dp)
            .height(100.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(icon, contentDescription = null, tint = Color.Gray)
            Spacer(modifier = Modifier.height(8.dp))
            Text(label, fontSize = 14.sp)
        }
    }
}