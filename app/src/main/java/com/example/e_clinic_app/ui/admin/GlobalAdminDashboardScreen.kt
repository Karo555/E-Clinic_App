package com.example.e_clinic_app.ui.admin


import androidx.compose.foundation.layout.*
import androidx.compose.foundation.clickable
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import android.app.Activity
import android.content.Intent
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Logout
import androidx.compose.ui.platform.LocalContext
import com.google.firebase.auth.FirebaseAuth
import com.example.e_clinic_app.MainActivity


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GlobalAdminDashboardScreen() {
    Scaffold(
        topBar = {
            val context = LocalContext.current

            TopAppBar(
                title = { Text("🌍 Global Admin Dashboard") },
                actions = {
                    IconButton(onClick = {
                        FirebaseAuth.getInstance().signOut()
                        val intent = Intent(context, MainActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        }
                        context.startActivity(intent)
                        (context as? Activity)?.finish()
                    }) {
                        Icon(Icons.Default.Logout, contentDescription = "Log out")
                    }
                }
            )

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