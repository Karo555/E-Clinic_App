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
    onNavigateToDoctorFirstLogin: () -> Unit
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
                            val userDocRef = db.collection("users").document(uid)

                            userDocRef.get()
                                .addOnSuccessListener { userDoc ->
                                    val role = userDoc.getString("role") ?: "Patient" // default fallback

                                    when (role) {
                                        "Patient" -> {
                                            db.collection("users")
                                                .document(uid)
                                                .collection("profile")
                                                .document("basicInfo")
                                                .get()
                                                .addOnSuccessListener { profile ->
                                                    if (profile.exists()) {
                                                        onNavigateToHome()
                                                    } else {
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
                                                        onNavigateToHome()
                                                    } else {
                                                        onNavigateToDoctorFirstLogin()
                                                    }
                                                }
                                        }

                                        else -> {
                                            // Admin or unknown role
                                            onNavigateToHome()
                                        }
                                    }
                                }
                                .addOnFailureListener {
                                    // Fallback in case userDoc can't be read
                                    onNavigateToFirstLogin()
                                }
                        }
                    }
                } else {
                    viewModel.register {
                        Log.d("AuthScreen", "Register success")
                        onNavigateToHome()
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