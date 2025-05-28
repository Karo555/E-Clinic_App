package com.example.e_clinic_app.ui.admin


import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.e_clinic_app.backend.home.AdminDashboardViewModel
/**
 * A composable function that represents the Admin Dashboard screen in the e-clinic application.
 *
 * This screen provides a scaffold with a top app bar and a placeholder message indicating
 * that admin tools are under development.
 *
 * @param viewModel The `AdminDashboardViewModel` instance used to manage the state and logic of the admin dashboard.
 */
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
