package com.example.e_clinic_app.ui.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun HomeTabScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "🏥 Welcome to E-Clinic!",
                style = MaterialTheme.typography.headlineMedium
            )
            Text(
                text = "You’ve successfully completed onboarding.\nMore features coming soon!",
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}
