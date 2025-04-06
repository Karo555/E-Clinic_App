package com.example.e_clinic_app
import androidx.navigation.compose.rememberNavController
import com.example.e_clinic_app.ui.navigation.AppNavGraph
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.e_clinic_app.ui.navigation.Routes
import com.example.e_clinic_app.ui.theme.EClinic_AppTheme
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import androidx.compose.material3.CircularProgressIndicator

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
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
                        val uid = user.uid
                        db.collection("users").document(uid).get()
                            .addOnSuccessListener { doc ->
                                val role = doc.getString("role")

                                when (role) {
                                    "Patient" -> {
                                        db.collection("users").document(uid)
                                            .collection("profile")
                                            .document("basicInfo")
                                            .get()
                                            .addOnSuccessListener { profile ->
                                                startDestination = if (profile.exists()) {
                                                    Routes.HOME
                                                } else {
                                                    Routes.FIRST_LOGIN
                                                }
                                            }
                                    }

                                    "Doctor" -> {
                                        db.collection("users").document(uid)
                                            .collection("profile")
                                            .document("doctorInfo")
                                            .get()
                                            .addOnSuccessListener { profile ->
                                                startDestination = if (profile.exists()) {
                                                    Routes.HOME
                                                } else {
                                                    Routes.DOCTOR_FIRST_LOGIN
                                                }
                                            }
                                    }

                                    "Admin" -> {
                                        val adminLevel = doc.getString("adminLevel") ?: "global"
                                        startDestination = when (adminLevel) {
                                            "institution" -> Routes.ADMIN_DASHBOARD_INSTITUTION
                                            "global" -> Routes.ADMIN_DASHBOARD_GLOBAL
                                            else -> Routes.HOME // fallback
                                        }
                                    }

                                    else -> {
                                        startDestination = Routes.HOME // fallback
                                    }
                                }
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