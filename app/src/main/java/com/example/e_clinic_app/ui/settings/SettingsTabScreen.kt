package com.example.e_clinic_app.ui.settings

import android.app.Activity
import android.content.Intent
import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.e_clinic_app.MainActivity
import com.example.e_clinic_app.ui.bottomNavBar.BottomNavigationBar
import com.example.e_clinic_app.ui.navigation.Routes
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsTabScreen(
    navController: NavController,
    currentUserRole: String?
) {
    val context = LocalContext.current
    val isDoctor = currentUserRole == "Doctor"

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Settings",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            BottomNavigationBar(navController)
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp, vertical = 32.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = "Settings Icon",
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Text(
                text = "Manage your account and preferences",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            FilledTonalButton(
                onClick = {
                    Log.d("Settings", "Navigating to ${if (isDoctor) "Edit Public Profile" else "Edit Medical Info"}")
                    navController.navigate(
                        if (isDoctor) Routes.EDIT_PUBLIC_PROFILE
                        else         Routes.EDIT_MEDICAL_INFO
                    )
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    if (isDoctor) "Edit Public Profile"
                    else         "Edit Medical Information"
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            FilledTonalButton(
                onClick = { navController.navigate(Routes.MY_DOCUMENTS) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("My Documents")
            }

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
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                )
            ) {
                Text("Log Out")
            }
        }
    }
}