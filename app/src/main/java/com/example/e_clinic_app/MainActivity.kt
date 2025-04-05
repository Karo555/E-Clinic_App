package com.example.e_clinic_app
import androidx.navigation.compose.rememberNavController
import com.example.e_clinic_app.ui.navigation.AppNavGraph
import android.os.Bundle
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

                LaunchedEffect(Unit) {
                    val user = FirebaseAuth.getInstance().currentUser
                    if (user == null) {
                        startDestination = Routes.AUTH
                    } else {
                        try {
                            val uid = user.uid
                            val userDoc = FirebaseFirestore.getInstance()
                                .collection("users")
                                .document(uid)
                                .get()
                                .await()

                            val role = userDoc.getString("role") ?: "Patient"
                            val profilePath = when (role) {
                                "Doctor" -> "doctorInfo"
                                "Patient" -> "basicInfo"
                                else -> "basicInfo" // fallback
                            }

                            val profileSnapshot = FirebaseFirestore.getInstance()
                                .collection("users")
                                .document(uid)
                                .collection("profile")
                                .document(profilePath)
                                .get()
                                .await()

                            startDestination = if (profileSnapshot.exists()) {
                                Routes.HOME
                            } else {
                                when (role) {
                                    "Doctor" -> Routes.DOCTOR_FIRST_LOGIN
                                    "Patient" -> Routes.FIRST_LOGIN
                                    else -> Routes.FIRST_LOGIN
                                }
                            }

                        } catch (e: Exception) {
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