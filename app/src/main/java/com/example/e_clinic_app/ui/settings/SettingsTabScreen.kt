package com.example.e_clinic_app.ui.settings

import android.app.Activity
import android.content.Intent
import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.e_clinic_app.MainActivity
import com.example.e_clinic_app.ui.navigation.Routes
import com.google.firebase.auth.FirebaseAuth

@Composable
fun SettingsTabScreen(navController: NavController) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("⚙️ Settings", style = MaterialTheme.typography.headlineMedium)

        Button(
            onClick = {
                Log.d("Logout", "User tapped log out")
                FirebaseAuth.getInstance().signOut()

                val intent = Intent(context, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
                context.startActivity(intent)
                (context as? Activity)?.finish()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Log Out")
        }

        Button(
            onClick = {
                Log.d("Settings", "Navigating to Edit Medical Info")
                navController.navigate(Routes.EDIT_MEDICAL_INFO)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Edit Medical Information")
        }
    }
}