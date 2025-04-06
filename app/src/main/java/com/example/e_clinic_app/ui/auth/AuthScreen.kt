package com.example.e_clinic_app.ui.auth

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreen(
    onNavigateToHome: () -> Unit,
    onNavigateToFirstLogin: () -> Unit,
    onNavigateToDoctorFirstLogin: () -> Unit,
    onNavigateToGlobalAdminDashboard: () -> Unit,
    onNavigateToInstitutionAdminDashboard: () -> Unit
) {
    val viewModel: AuthViewModel = viewModel()
    val state by viewModel.uiState.collectAsState()
    val authMode by viewModel.authMode.collectAsState()

    var expanded by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            TextField(
                value = state.email,
                onValueChange = { viewModel.onEmailChange(it) },
                label = { Text("Email") }
            )
            TextField(
                value = state.password,
                onValueChange = { viewModel.onPasswordChange(it) },
                label = { Text("Password") },
                visualTransformation = PasswordVisualTransformation()
            )

            if (authMode == AuthMode.REGISTER) {
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    TextField(
                        value = state.role.name,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Role") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        UserRole.entries.forEach { role ->
                            DropdownMenuItem(
                                text = { Text(role.name) },
                                onClick = {
                                    viewModel.onRoleChange(role)
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }

            Button(onClick = {
                if (authMode == AuthMode.LOGIN) {
                    viewModel.login {
                        val currentUser = FirebaseAuth.getInstance().currentUser
                        val db = FirebaseFirestore.getInstance()

                        currentUser?.let { user ->
                            val uid = user.uid

                            db.collection("users").document(uid).get()
                                .addOnSuccessListener { userDoc ->
                                    val role = userDoc.getString("role")
                                    val adminLevel = userDoc.getString("adminLevel") ?: "global"

                                    if (role == null) {
                                        Log.e("AuthScreen", "❌ Missing role in Firestore for user: $uid")
                                        return@addOnSuccessListener
                                    }

                                    Log.d("AuthScreen", "✅ Logged in as role: $role")

                                    when (role) {
                                        "Patient" -> {
                                            db.collection("users")
                                                .document(uid)
                                                .collection("profile")
                                                .document("basicInfo")
                                                .get()
                                                .addOnSuccessListener { profile ->
                                                    if (profile.exists()) {
                                                        Log.d("AuthScreen", "🎯 Patient profile exists. Navigating to Home.")
                                                        onNavigateToHome()
                                                    } else {
                                                        Log.d("AuthScreen", "📝 Patient profile missing. Navigating to FirstLogin.")
                                                        onNavigateToFirstLogin()
                                                    }
                                                }
                                        }

                                        "Doctor" -> {
                                            db.collection("users")
                                                .document(uid)
                                                .collection("profile")
                                                .document("doctorInfo")
                                                .get()
                                                .addOnSuccessListener { profile ->
                                                    if (profile.exists()) {
                                                        Log.d("AuthScreen", "🎯 Doctor profile exists. Navigating to Home.")
                                                        onNavigateToHome()
                                                    } else {
                                                        Log.d("AuthScreen", "📝 Doctor profile missing. Navigating to DoctorFirstLogin.")
                                                        onNavigateToDoctorFirstLogin()
                                                    }
                                                }
                                        }

                                        "Admin" -> {
                                            when (adminLevel) {
                                                "institution" -> {
                                                    Log.d("AuthScreen", "🏥 Institution Admin login")
                                                    onNavigateToInstitutionAdminDashboard()
                                                }
                                                "global" -> {
                                                    Log.d("AuthScreen", "🌍 Global Admin login")
                                                    onNavigateToGlobalAdminDashboard()
                                                }
                                                else -> {
                                                    Log.w("AuthScreen", "⚠️ Unknown admin level: $adminLevel — defaulting to Home")
                                                    onNavigateToHome()
                                              }
                                            }
                                        }

                                        else -> {
                                            Log.w("AuthScreen", "⚠️ Unknown role: $role — defaulting to Home")
                                            onNavigateToHome()
                                        }
                                    }
                                }
                                .addOnFailureListener {
                                    Log.e("AuthScreen", "❌ Failed to fetch user document: ${it.message}")
                                }
                        }
                    }
                } else {
                    viewModel.register {
                        val currentUser = FirebaseAuth.getInstance().currentUser
                        val db = FirebaseFirestore.getInstance()

                        currentUser?.let { user ->
                            val uid = user.uid

                            db.collection("users").document(uid).get()
                                .addOnSuccessListener { userDoc ->
                                    val role = userDoc.getString("role") ?: "Patient"

                                    when (role) {
                                        "Patient" -> {
                                            onNavigateToFirstLogin()
                                        }

                                        "Doctor" -> {
                                            db.collection("users").document(uid)
                                                .collection("profile")
                                                .document("doctorInfo")
                                                .get()
                                                .addOnSuccessListener { profile ->
                                                    if (profile.exists()) {
                                                        onNavigateToHome()
                                                    } else {
                                                        onNavigateToDoctorFirstLogin()
                                                    }
                                                }
                                        }

                                        else -> onNavigateToHome()
                                    }
                                }
                                .addOnFailureListener {
                                    onNavigateToFirstLogin() // fallback
                                }
                        }
                    }
                }
            }) {
                Text(if (authMode == AuthMode.LOGIN) "Login" else "Register")
            }

            OutlinedButton(onClick = { viewModel.toggleAuthMode() }) {
                Text("Switch to ${if (authMode == AuthMode.LOGIN) "Register" else "Login"}")
            }

            state.errorMessage?.let {
                Text("Error: $it", color = MaterialTheme.colorScheme.error)
            }

            if (state.isSuccess) {
                Text("Success!", color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}