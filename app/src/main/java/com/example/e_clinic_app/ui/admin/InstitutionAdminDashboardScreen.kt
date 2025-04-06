package com.example.e_clinic_app.ui.admin


import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InstitutionAdminDashboardScreen() {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Institution Admin Dashboard") })
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "🏥 Tools for managing your clinic\n(Coming soon...)",
                style = MaterialTheme.typography.headlineSmall
            )
        }
    }
}
