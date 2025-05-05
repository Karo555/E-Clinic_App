package com.example.e_clinic_app.ui.admin


import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.e_clinic_app.backend.home.AdminDashboardViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(viewModel: AdminDashboardViewModel) {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Admin Dashboard") })
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "🛠 Admin tools coming soon...",
                style = MaterialTheme.typography.headlineSmall
            )
        }
    }
}
