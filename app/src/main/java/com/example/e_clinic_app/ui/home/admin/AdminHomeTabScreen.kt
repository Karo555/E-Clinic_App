package com.example.e_clinic_app.ui.home.admin

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
import com.example.e_clinic_app.ui.bottomNavBar.BottomNavigationBar

@Composable
fun AdminHomeTabScreen(navController: NavController) {
    Column(modifier = Modifier.fillMaxSize().background(Color(0xFFEFF7F9))) {
        // Admin Profile Section
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "Hi, Admin!", fontSize = 20.sp)
            Spacer(modifier = Modifier.weight(1f))
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(Color.Gray, shape = CircleShape)
            )
        }

        // Navigation Panels
        val navigationItems = listOf(
            "Manage Doctors" to "manageDoctors",
            "Manage Patients" to "managePatients",
            "Reports" to "reports",
            "System Alerts" to "systemAlerts"
        )

        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            for (i in navigationItems.chunked(2)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    i.forEach { (label, route) ->
                        Card(
                            modifier = Modifier
                                .size(140.dp)
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
            }
        }

        // System Reports Section
        Text(text = "Recent Reports", fontSize = 18.sp, modifier = Modifier.padding(16.dp))
        // mock list of reports
        val reports = listOf("Report 1 - User Issue", "Report 2 - System Alert", "Report 3 - Doctor Verification")
        LazyRow(modifier = Modifier.padding(16.dp)) {
            items(reports) { report ->
                Card(
                    modifier = Modifier.padding(8.dp).clickable { navController.navigate("reportDetail/$report") },
                    elevation = 4.dp,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(text = report, modifier = Modifier.padding(16.dp), fontSize = 14.sp)
                }
            }
        }
        BottomNavigationBar(navController)
    }
}
