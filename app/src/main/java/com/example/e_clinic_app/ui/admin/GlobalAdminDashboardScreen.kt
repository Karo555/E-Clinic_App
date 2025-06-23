package com.example.e_clinic_app.ui.admin
import android.app.Activity
import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.e_clinic_app.MainActivity
import com.google.firebase.auth.FirebaseAuth

/**
 * A composable function that represents the Global Admin Dashboard screen in the e-clinic application.
 *
 * This screen provides a scaffold with a top app bar, a welcome message, and quick access sections
 * for managing doctors, clinics, admins, and app settings.
 *
 * @param navController The `NavController` used for navigation between screens.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GlobalAdminDashboardScreen(navController: NavController) {
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

            AdminSectionItem("🛡 Manage Institution Admins") {
                navController.navigate("institution_admins_screen")
            }

            AdminSectionItem("⚙️ App Settings") {
                // TODO: Future feature
            }
        }
    }
}
/**
 * A composable function that represents a clickable card for a specific admin section item.
 *
 * This card is used to navigate to different sections of the Global Admin Dashboard.
 *
 * @param label The label to display on the card.
 * @param onClick A lambda function to execute when the card is clicked.
 */
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
