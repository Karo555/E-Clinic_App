package com.example.e_clinic_app.ui.home.doctor

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Card
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.e_clinic_app.backend.home.DoctorHomeViewModel

@Composable
fun DoctorHomeTabScreen(navController: NavController, viewModel: DoctorHomeViewModel) {
    LaunchedEffect(Unit) {
        viewModel.fetchAppointments(viewModel.firestore)
    }
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
        val appointments = viewModel.appointmentsList
        LazyRow(modifier = Modifier.padding(16.dp)) {
            items(appointments) { appointment ->
                Card(
                    modifier = Modifier.padding(8.dp).clickable { navController.navigate("appointmentDetail/$appointment") },
                    elevation = 4.dp,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(text = appointment.patient.lastName, modifier = Modifier.padding(16.dp), fontSize = 14.sp)
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
