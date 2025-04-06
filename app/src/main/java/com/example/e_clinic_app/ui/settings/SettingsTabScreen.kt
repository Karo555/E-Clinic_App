package com.example.e_clinic_app.ui.settings

import android.app.Activity
import android.content.Intent
import androidx.compose.ui.platform.LocalContext
import com.google.firebase.auth.FirebaseAuth
import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.compose.foundation.layout.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Alignment
import androidx.compose.material3.*
import com.example.e_clinic_app.MainActivity
import com.example.e_clinic_app.ui.navigation.Routes

@Composable
fun SettingsTabScreen(navController: NavController) {
    val context = LocalContext.current

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

                // Launch a fresh MainActivity
                val intent = Intent(context, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                context.startActivity(intent)

                // Finish current activity
                (context as? Activity)?.finish()
            }) {
                Text("Log Out")
            }
        }
    }
}