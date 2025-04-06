package com.example.e_clinic_app.ui.admin


import androidx.compose.foundation.layout.*
import androidx.compose.foundation.clickable
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GlobalAdminDashboardScreen() {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("🌍 Global Admin Dashboard") })
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(24.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Welcome, Global Admin 👋", style = MaterialTheme.typography.headlineSmall)

            Divider()

            Text("Quick Access", style = MaterialTheme.typography.titleMedium)

            AdminSectionItem("👩‍⚕️ Manage Doctors") {
                // TODO: Navigate to doctor management screen
            }

            AdminSectionItem("🏥 Manage Clinics") {
                // TODO: Navigate to clinic management screen
            }

            AdminSectionItem("🛡 Manage Admins") {
                // TODO: Navigate to admin list or assignment screen
            }

            AdminSectionItem("⚙️ App Settings") {
                // TODO: Future feature
            }
        }
    }
}

@Composable
fun AdminSectionItem(label: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation()
    ) {
        Box(
            modifier = Modifier.padding(16.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Text(label, style = MaterialTheme.typography.bodyLarge)
        }
    }
}