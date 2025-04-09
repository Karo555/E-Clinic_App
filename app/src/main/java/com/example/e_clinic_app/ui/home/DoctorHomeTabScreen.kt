package com.example.e_clinic_app.ui.home

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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Preview
@Composable
fun DoctorHomeScreen() {
    Column(modifier = Modifier.fillMaxSize().background(Color(0xFFEFF7F9))) {
        // Doctor Profile Section
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "Hi, Dr. [Name]!", fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.weight(1f))
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(Color.Gray, shape = CircleShape)
            ) {}
        }

        // Action Panels
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            listOf("Chats", "Appointments", "Prescriptions").forEach { label ->
                Card(
                    modifier = Modifier.size(120.dp).clickable { /* Navigate */ },
                    elevation = 4.dp,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize().padding(8.dp)) {
                        Text(text = label, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Upcoming Appointments
        Text(text = "Upcoming Appointments", fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(16.dp))
        val appointments = listOf("John Doe - 10:00 AM", "Jane Smith - 11:30 AM", "Chris Evans - 1:00 PM")
        LazyRow(modifier = Modifier.padding(16.dp)) {
            items(appointments) { appointment ->
                Card(
                    modifier = Modifier.padding(8.dp).clickable { /* View details */ },
                    elevation = 4.dp,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(text = appointment, modifier = Modifier.padding(16.dp), fontSize = 14.sp)
                }
            }
        }

        // Add Available Hours
        Button(
            onClick = { /* Open scheduling screen */ },
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF4CAF50))
        ) {
            Text(text = "Set Availability", color = Color.White, fontSize = 16.sp)
        }

        // Bottom Navigation
        Spacer(modifier = Modifier.weight(1f))
        BottomNavigation {
            listOf("Home", "Chats", "Appointments").forEach { screen ->
                BottomNavigationItem(
                    icon = { Icon(painterResource(id = android.R.drawable.ic_menu_gallery), contentDescription = null) },
                    label = { Text(screen) },
                    selected = false,
                    onClick = { /* Navigate */ }
                )
            }
        }
    }
}
