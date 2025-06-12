package com.example.e_clinic_app

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts.RequestPermission
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import androidx.core.content.ContextCompat
import com.example.e_clinic_app.presentation.viewmodel.UserViewModel
import com.example.e_clinic_app.ui.navigation.AppNavGraph
import com.example.e_clinic_app.ui.navigation.Routes
import com.example.e_clinic_app.ui.theme.EClinic_AppTheme
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import android.util.Log

/**
 * The main entry point of the e-clinic application.
 *
 * This activity initializes the application, sets up Firebase, and manages the navigation graph.
 * It determines the start destination of the app based on the user's authentication status and role.
 *
 * It also requests the POST_NOTIFICATIONS permission at runtime for Android 13+.
 */
class MainActivity : ComponentActivity() {

    // Launcher for requesting the POST_NOTIFICATIONS permission (Android 13+)
    private val requestNotificationPermissionLauncher =
        registerForActivityResult(RequestPermission()) { isGranted: Boolean ->
            // You can respond to the user’s choice here if needed (e.g., show a toast or log).
            // For MVP, we’ll not block the UI if they deny.
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. Initialize Firebase
        FirebaseApp.initializeApp(this)

        // 2. Request notification permission if needed (Android 13+)
        requestPostNotificationsPermissionIfNeeded()

        // 3. Enable edge-to-edge and set up Compose content
        enableEdgeToEdge()
        setContent {
            Log.d("MainActivity", "setContent recomposed")
            EClinic_AppTheme {
                Log.d("MainActivity", "EClinic_AppTheme recomposed")
                val navController = rememberNavController()
                // Cache user role in a ViewModel to avoid repeated Firestore reads
                val userViewModel: UserViewModel = viewModel()
                val currentRole by userViewModel.role.collectAsState()

                var startDestination by remember { mutableStateOf<String?>(null) }

                LaunchedEffect(Unit) {
                    Log.d("MainActivity", "LaunchedEffect running for startDestination check")
                    val user = FirebaseAuth.getInstance().currentUser
                    val db = FirebaseFirestore.getInstance()

                    if (user == null) {
                        startDestination = Routes.AUTH
                    } else {
                        try {
                            val uid = user.uid
                            val doc = db.collection("users").document(uid).get().await()
                            val role = doc.getString("role")

                            when (role) {
                                "Patient" -> {
                                    val profile = db.collection("users").document(uid)
                                        .collection("profile")
                                        .document("basicInfo")
                                        .get()
                                        .await()
                                    startDestination = if (profile.exists()) {
                                        Routes.HOME
                                    } else {
                                        Routes.MEDICAL_INTRO
                                    }
                                }

                                "Doctor" -> {
                                    val profile = db.collection("users").document(uid)
                                        .collection("profile")
                                        .document("doctorInfo")
                                        .get()
                                        .await()
                                    startDestination = if (profile.exists()) {
                                        Routes.HOME
                                    } else {
                                        Routes.DOCTOR_FIRST_LOGIN
                                    }
                                }

                                "Admin" -> {
                                    val adminLevel = doc.getString("adminLevel") ?: "global"
                                    startDestination = when (adminLevel) {
                                        "institution" -> Routes.INSTITUTION_ADMIN_DASHBOARD
                                        "global" -> Routes.GLOBAL_ADMIN_DASHBOARD
                                        else -> Routes.HOME
                                    }
                                }

                                else -> {
                                    startDestination = Routes.HOME
                                }
                            }
                        } catch (e: Exception) {
                            startDestination = Routes.AUTH
                        }
                    }
                }

                Log.d("MainActivity", "startDestination = $startDestination")
                if (startDestination != null) {
                    AppNavGraph(
                        navController = navController,
                        startDestination = startDestination!!
                    )
                } else {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }
        }
    }

    /**
     * Checks and requests the POST_NOTIFICATIONS permission on Android 13+.
     */
    private fun requestPostNotificationsPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> {
                    // Permission already granted; do nothing.
                }
                shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS) -> {
                    // Optionally, show an in-app explanation before requesting.
                    // For MVP, we directly request.
                    requestNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
                else -> {
                    // Directly request the permission for the first time.
                    requestNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        }
    }
}