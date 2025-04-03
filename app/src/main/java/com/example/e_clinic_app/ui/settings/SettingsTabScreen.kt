package com.example.e_clinic_app.ui.settings
import com.example.e_clinic_app.ui.navigation.Routes

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth

@Composable
fun SettingsTabScreen(navController: NavController) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("⚙️ Settings", style = MaterialTheme.typography.headlineSmall)

            Button(onClick = {
                FirebaseAuth.getInstance().signOut()
                navController.navigate(Routes.AUTH) {
                    popUpTo(0) // Clears back stack completely
                }
            }) {
                Text("Log Out")
            }
        }
    }
}