package com.example.e_clinic_app.ui.home

import android.util.Log
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.navigation.NavController
import com.example.e_clinic_app.ui.navigation.Routes
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
/**
 * A composable function that represents the Home Tab screen in the e-clinic application.
 *
 * This screen determines the user's role (Doctor, Patient, or Admin) and navigates them to the appropriate
 * dashboard based on their role. It fetches user-specific data from Firebase Firestore and displays a loading
 * state while the data is being retrieved.
 *
 * The screen includes:
 * - Firebase authentication to identify the current user.
 * - Firestore queries to fetch user role and profile information.
 * - Navigation to the appropriate dashboard based on the user's role.
 * - Error handling for failed data retrieval.
 *
 * @param navController The `NavController` used for navigating to other screens.
 */
@Composable
fun HomeTabScreen(navController: NavController) {
    val role = remember { mutableStateOf<String?>(null) }
    val fullName = remember { mutableStateOf<String?>(null) }
    val specialization = remember { mutableStateOf<String?>(null) }
    val isLoading = remember { mutableStateOf(true) }
    val hasNavigated = remember { mutableStateOf(false) }

    val user = FirebaseAuth.getInstance().currentUser
    if (user != null) {
        val uid = user.uid
        val db = FirebaseFirestore.getInstance()

        LaunchedEffect(user) {
            // Fetch user role
            db.collection("users").document(uid).get()
                .addOnSuccessListener { userDoc ->
                    role.value = userDoc.getString("role")

                    // Fetch role-specific profile data
                    when (role.value) {
                        "Doctor" -> {
                            db.collection("users")
                                .document(uid)
                                .collection("profile")
                                .document("doctorInfo")
                                .get()
                                .addOnSuccessListener { docInfo ->
                                    fullName.value = docInfo.getString("fullName") ?: "Unknown Doctor"
                                    specialization.value = docInfo.getString("specialization") ?: "No Specialization"
                                    isLoading.value = false
                                }
                                .addOnFailureListener { e ->
                                    Log.e("HomeTabScreen", "Error loading doctor info: ${e.message}")
                                    fullName.value = "Error loading doctor"
                                    isLoading.value = false
                                }
                        }
                        "Patient" -> {
                            db.collection("users")
                                .document(uid)
                                .collection("profile")
                                .document("basicInfo")
                                .get()
                                .addOnSuccessListener { patInfo ->
                                    fullName.value = patInfo.getString("fullName") ?: "Unknown Patient"
                                    isLoading.value = false
                                }
                                .addOnFailureListener { e ->
                                    Log.e("HomeTabScreen", "Error loading patient info: ${e.message}")
                                    fullName.value = "Error loading patient"
                                    isLoading.value = false
                                }
                        }
                        else -> {
                            fullName.value = "Unknown User"
                            isLoading.value = false
                        }
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("HomeTabScreen", "Error loading user info: ${e.message}")
                    fullName.value = "Error loading user"
                    isLoading.value = false
                }
        }
    } else {
        // Handle case where user is not logged in
        isLoading.value = false
        fullName.value = "No user logged in"
    }

    // Navigate based on role once data is loaded
    if (!isLoading.value && role.value != null && !hasNavigated.value) {
        hasNavigated.value = true
        Log.d("HomeTabScreen", "Navigating to dashboard for role: ${role.value}")
        when (role.value) {
            "Doctor" -> navController.navigate("doctor_dashboard") {
                popUpTo(Routes.HOME) { inclusive = true }
                launchSingleTop = true
            }
            "Patient" -> navController.navigate("patient_dashboard") {
                popUpTo(Routes.HOME) { inclusive = true }
                launchSingleTop = true
            }
            "Admin" -> navController.navigate("admin_dashboard") {
                popUpTo(Routes.HOME) { inclusive = true }
                launchSingleTop = true
            }
        }
    }
    // Optionally, show a loading spinner while navigating
    if (isLoading.value || !hasNavigated.value) {
        CircularProgressIndicator()
    }
}
