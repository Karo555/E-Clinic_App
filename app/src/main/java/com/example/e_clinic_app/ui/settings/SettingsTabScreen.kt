package com.example.e_clinic_app.ui.settings

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
                Log.d("Logout", "User tapped log out")

                FirebaseAuth.getInstance().signOut()
                Log.d("Logout", "Firebase user signed out")

                // Instead of recreate(), use safe back stack reset
                navController.navigate(Routes.AUTH) {
                    popUpTo(0) { inclusive = true }
                    launchSingleTop = true
                }
            }) {
                Text("Log Out")
            }
        }
    }
}