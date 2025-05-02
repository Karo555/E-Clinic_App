package com.example.e_clinic_app.ui.home.doctor

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController

@Composable
fun DoctorHomeTabScreen(navController: NavController) {
    Column(modifier = Modifier.fillMaxSize().background(Color(0xFFEFF7F9))) {
        // Doctor Profile Section
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "Hi, Dr. [Name]!", fontSize = 20.sp)
            Spacer(modifier = Modifier.weight(1f))
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(Color.Gray, shape = CircleShape)
            )
        }

        // Navigation Panels
        val navigationItems = listOf(
            "Patients" to "patients",
            "Appointments" to "appointments",
            "Prescriptions" to "prescriptions"
        )

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            navigationItems.forEach { (label, route) ->
                Card(
                    modifier = Modifier
                        .size(120.dp)
                        .clickable { navController.navigate(route) },
                    elevation = 4.dp,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize().padding(8.dp)) {
                        Text(text = label, fontSize = 14.sp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Upcoming Appointments
        Text(text = "Upcoming Appointments", fontSize = 18.sp, modifier = Modifier.padding(16.dp))
        val appointments = listOf("John Doe - 10:00 AM", "Jane Smith - 11:30 AM", "Chris Evans - 1:00 PM")
        LazyRow(modifier = Modifier.padding(16.dp)) {
            items(appointments) { appointment ->
                Card(
                    modifier = Modifier.padding(8.dp).clickable { navController.navigate("appointmentDetail/$appointment") },
                    elevation = 4.dp,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(text = appointment, modifier = Modifier.padding(16.dp), fontSize = 14.sp)
                }
            }
        }

        // Set Availability Button
        Button(
            onClick = { navController.navigate("setAvailability") },
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF4CAF50))
        ) {
            Text(text = "Set Availability", color = Color.White, fontSize = 16.sp)
        }
    }
}

@Composable
fun DoctorHomeTabPreview() {
    val navController = rememberNavController()
    DoctorHomeTabScreen(navController)
}

@Preview(showBackground = true)
@Composable
fun PreviewDoctorHome() {
    DoctorHomeTabPreview()
}