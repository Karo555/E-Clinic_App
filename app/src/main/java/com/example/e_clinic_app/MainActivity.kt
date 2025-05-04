package com.example.e_clinic_app
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController
import com.example.e_clinic_app.data.model.DrugSeeder
import com.example.e_clinic_app.data.repository.DrugRepository
import com.example.e_clinic_app.ui.navigation.AppNavGraph
import com.example.e_clinic_app.ui.navigation.Routes
import com.example.e_clinic_app.ui.theme.EClinic_AppTheme
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        DrugSeeder.seed(DrugRepository())
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)
        enableEdgeToEdge()
        setContent {
            EClinic_AppTheme {
                val navController = rememberNavController()
                var startDestination by remember { mutableStateOf<String?>(null) }

                LaunchedEffect(true) {
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
                                        else -> Routes.HOME // fallback
                                    }
                                }

                                else -> {
                                    startDestination = Routes.HOME // fallback
                                }
                            }
                        } catch (e: Exception) {
                            // In case of error, fallback to auth screen
                            startDestination = Routes.AUTH
                        }
                    }
                }

                if (startDestination != null) {
                    AppNavGraph(navController = navController, startDestination = startDestination!!)
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
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    EClinic_AppTheme {
        Greeting("Android")
    }
}