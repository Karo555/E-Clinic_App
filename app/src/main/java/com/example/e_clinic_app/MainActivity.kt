package com.example.e_clinic_app
<<<<<<< HEAD
import android.R
import androidx.navigation.compose.rememberNavController
import com.example.e_clinic_app.ui.navigation.AppNavGraph
=======

>>>>>>> 756ebfd11b740e052517104bb1d4e988af64b7f6
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
<<<<<<< HEAD
import androidx.compose.runtime.Composable
=======
import androidx.compose.runtime.*
>>>>>>> 756ebfd11b740e052517104bb1d4e988af64b7f6
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
<<<<<<< HEAD
import com.example.e_clinic_app.ui.theme.EClinic_AppTheme
import com.google.firebase.FirebaseApp
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
=======
import androidx.navigation.compose.rememberNavController
import com.example.e_clinic_app.ui.navigation.AppNavGraph
import com.example.e_clinic_app.ui.navigation.Routes
import com.example.e_clinic_app.ui.theme.EClinic_AppTheme
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
>>>>>>> 756ebfd11b740e052517104bb1d4e988af64b7f6

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)
        enableEdgeToEdge()
        setContent {
            EClinic_AppTheme {
                val navController = rememberNavController()
<<<<<<< HEAD
                AppNavGraph(
                    navController = navController,
                    startDestination = "home"
                )
=======
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
                                        Routes.FIRST_LOGIN
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
                                        "institution" -> Routes.ADMIN_DASHBOARD_INSTITUTION
                                        "global" -> Routes.ADMIN_DASHBOARD_GLOBAL
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
>>>>>>> 756ebfd11b740e052517104bb1d4e988af64b7f6
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