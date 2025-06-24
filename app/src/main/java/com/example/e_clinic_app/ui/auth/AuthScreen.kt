package com.example.e_clinic_app.ui.auth

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.e_clinic_app.service.FirebaseService
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

/**
 * A composable function that represents the authentication screen in the e-clinic application.
 *
 * This screen provides functionality for users to log in or register, depending on the selected mode.
 * It includes fields for email and password, a role selection dropdown (for registration), and navigation
 * to other screens based on the user's role and authentication status.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreen(
    onNavigateToHome: () -> Unit,
    onNavigateToFirstLogin: () -> Unit,
    onNavigateToDoctorFirstLogin: () -> Unit,
    onNavigateToGlobalAdminDashboard: () -> Unit,
//    onNavigateToInstitutionAdminDashboard: () -> Unit,
    onNavigateToResetPassword: () -> Unit
) {
    val viewModel: AuthViewModel = viewModel()
    val state by viewModel.uiState.collectAsState()
    val authMode by viewModel.authMode.collectAsState()
    val passwordVisible by viewModel.passwordVisible.collectAsState()

    var expanded by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

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
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { viewModel.togglePasswordVisibility() }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                            contentDescription = if (passwordVisible) "Hide password" else "Show password"
                        )
                    }
                }
            )

            TextButton(onClick = { onNavigateToResetPassword() }) {
                Text("Forgot Password?")
            }

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
                coroutineScope.launch {
                    if (authMode == AuthMode.LOGIN) {
                        viewModel.login {
                            coroutineScope.launch {
                                // 1. Fetch the FCM token (suspend) and upload it
                                try {
                                    val token = FirebaseMessaging.getInstance().token.await()
                                    val uploadTask = FirebaseService.uploadTokenToFirestore(token)
                                    if (uploadTask != null) {
                                        try {
                                            uploadTask?.let {
                                                uploadTask?.let {
                                                    if (it is Task<*>) {
                                                        it.await()
                                                        Log.d("AuthScreen", "FCM token written successfully.")
                                                    } else {
                                                        Log.e("AuthScreen", "uploadTask is not a valid Task.")
                                                    }
                                                }
                                            }
                                        } catch (e: Exception) {
                                            Log.e("AuthScreen", "Failed to write FCM token", e)
                                        }
                                    } else {
                                        Log.w("AuthScreen", "uploadTokenToFirestore returned null (no authenticated user).")
                                    }
                                } catch (e: Exception) {
                                    Log.e("AuthScreen", "Failed to retrieve FCM token", e)
                                }

                                // 2. Role-based navigation logic
                                val currentUser = FirebaseAuth.getInstance().currentUser
                                val db = FirebaseFirestore.getInstance()

                                currentUser?.let { user ->
                                    val uid = user.uid
                                    try {
                                        val userDoc = db.collection("users").document(uid).get().await()
                                        val role = userDoc.getString("role")

                                        when (role) {
                                            "Patient" -> {
                                                val profile = db.collection("users").document(uid)
                                                    .collection("profile").document("basicInfo")
                                                    .get().await()
                                                if (profile.exists()) onNavigateToHome()
                                                else onNavigateToFirstLogin()
                                            }

                                            "Doctor" -> {
                                                val profile = db.collection("users").document(uid)
                                                    .collection("profile").document("doctorInfo")
                                                    .get().await()
                                                if (profile.exists()) onNavigateToHome()
                                                else onNavigateToDoctorFirstLogin()
                                            }

                                            "Admin" -> {
                                                onNavigateToGlobalAdminDashboard()
                                            }

                                            else -> onNavigateToHome()
                                        }
                                    } catch (e: Exception) {
                                        Log.e("AuthScreen", "Login flow failed: ${e.message}")
                                    }
                                }
                            }
                        }
                    } else {
                        viewModel.register {
                            coroutineScope.launch {
                                val currentUser = FirebaseAuth.getInstance().currentUser
                                val db = FirebaseFirestore.getInstance()

                                currentUser?.let { user ->
                                    val uid = user.uid
                                    try {
                                        val userDoc = db.collection("users").document(uid).get().await()
                                        val role = userDoc.getString("role") ?: "Patient"

                                        when (role) {
                                            "Patient" -> onNavigateToFirstLogin()
                                            "Doctor" -> {
                                                val profile = db.collection("users").document(uid)
                                                    .collection("profile").document("doctorInfo")
                                                    .get().await()
                                                if (profile.exists()) onNavigateToHome()
                                                else onNavigateToDoctorFirstLogin()
                                            }
                                            "Admin" -> onNavigateToGlobalAdminDashboard()

                                        }
                                    } catch (e: Exception) {
                                        onNavigateToFirstLogin()
                                    }
                                }
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