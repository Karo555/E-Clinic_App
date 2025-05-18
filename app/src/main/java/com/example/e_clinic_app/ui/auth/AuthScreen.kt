//package com.example.e_clinic_app.ui.auth
//
//import android.util.Log
//import androidx.compose.foundation.layout.*
//import androidx.compose.material3.*
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.filled.Visibility
//import androidx.compose.material.icons.filled.VisibilityOff
//import androidx.compose.runtime.*
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.text.input.PasswordVisualTransformation
//import androidx.compose.ui.text.input.VisualTransformation
//import androidx.compose.ui.unit.dp
//import androidx.lifecycle.viewmodel.compose.viewModel
//import com.google.firebase.auth.FirebaseAuth
//import com.google.firebase.firestore.FirebaseFirestore
//import kotlinx.coroutines.launch
//import kotlinx.coroutines.tasks.await
//
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun AuthScreen(
//    onNavigateToHome: () -> Unit,
//    onNavigateToFirstLogin: () -> Unit,
//    onNavigateToDoctorFirstLogin: () -> Unit,
//    onNavigateToGlobalAdminDashboard: () -> Unit,
//    onNavigateToInstitutionAdminDashboard: () -> Unit,
//    onNavigateToResetPassword: () -> Unit
//) {
//    val viewModel: AuthViewModel = viewModel()
//    val state by viewModel.uiState.collectAsState()
//    val authMode by viewModel.authMode.collectAsState()
//    val passwordVisible by viewModel.passwordVisible.collectAsState()
//
//    var expanded by remember { mutableStateOf(false) }
//    val coroutineScope = rememberCoroutineScope()
//
//    Box(
//        modifier = Modifier
//            .fillMaxSize()
//            .padding(24.dp),
//        contentAlignment = Alignment.Center
//    ) {
//        Column(
//            verticalArrangement = Arrangement.spacedBy(16.dp),
//            horizontalAlignment = Alignment.CenterHorizontally
//        ) {
//            TextField(
//                value = state.email,
//                onValueChange = { viewModel.onEmailChange(it) },
//                label = { Text("Email") }
//            )
//            TextField(
//                value = state.password,
//                onValueChange = { viewModel.onPasswordChange(it) },
//                label = { Text("Password") },
//                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
//                trailingIcon = {
//                    IconButton(onClick = { viewModel.togglePasswordVisibility() }) {
//                        Icon(
//                            imageVector = if (passwordVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
//                            contentDescription = if (passwordVisible) "Hide password" else "Show password"
//                        )
//                    }
//                }
//            )
//
//            TextButton(onClick = { onNavigateToResetPassword() }) {
//                Text("Forgot Password?")
//            }
//
//            if (authMode == AuthMode.REGISTER) {
//                ExposedDropdownMenuBox(
//                    expanded = expanded,
//                    onExpandedChange = { expanded = !expanded }
//                ) {
//                    TextField(
//                        value = state.role.name,
//                        onValueChange = {},
//                        readOnly = true,
//                        label = { Text("Role") },
//                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
//                        modifier = Modifier
//                            .menuAnchor()
//                            .fillMaxWidth()
//                    )
//                    ExposedDropdownMenu(
//                        expanded = expanded,
//                        onDismissRequest = { expanded = false }
//                    ) {
//                        UserRole.entries.forEach { role ->
//                            DropdownMenuItem(
//                                text = { Text(role.name) },
//                                onClick = {
//                                    viewModel.onRoleChange(role)
//                                    expanded = false
//                                }
//                            )
//                        }
//                    }
//                }
//            }
////TODO( "transfer the button logic to viewModel part")
//            Button(onClick = {
//                coroutineScope.launch {
//                    if (authMode == AuthMode.LOGIN) {
//                        viewModel.login {
//                            coroutineScope.launch {
//                                val currentUser = FirebaseAuth.getInstance().currentUser
//                                val db = FirebaseFirestore.getInstance()
//
//                                currentUser?.let { user ->
//                                    val uid = user.uid
//                                    try {
//                                        val userDoc = db.collection("users").document(uid).get().await()
//                                        val role = userDoc.getString("role")
//                                        val adminLevel = userDoc.getString("adminLevel") ?: "global"
//
//                                        when (role) {
//                                            "Patient" -> {
//                                                val profile = db.collection("users").document(uid)
//                                                    .collection("profile").document("basicInfo")
//                                                    .get().await()
//
//                                                if (profile.exists()) onNavigateToHome()
//                                                else onNavigateToFirstLogin()
//                                            }
//
//                                            "Doctor" -> {
//                                                val profile = db.collection("users").document(uid)
//                                                    .collection("profile").document("doctorInfo")
//                                                    .get().await()
//
//                                                if (profile.exists()) onNavigateToHome()
//                                                else onNavigateToDoctorFirstLogin()
//                                            }
//
//                                            "Admin" -> {
//                                                if (adminLevel == "institution") {
//                                                    onNavigateToInstitutionAdminDashboard()
//                                                } else {
//                                                    onNavigateToGlobalAdminDashboard()
//                                                }
//                                            }
//
//                                            else -> onNavigateToHome()
//                                        }
//                                    } catch (e: Exception) {
//                                        Log.e("AuthScreen", "Login flow failed: ${e.message}")
//                                    }
//                                }
//                            }
//                        }
//                    } else {
//                        viewModel.register {
//                            coroutineScope.launch {
//                                val currentUser = FirebaseAuth.getInstance().currentUser
//                                val db = FirebaseFirestore.getInstance()
//
//                                currentUser?.let { user ->
//                                    val uid = user.uid
//                                    try {
//                                        val userDoc = db.collection("users").document(uid).get().await()
//                                        val role = userDoc.getString("role") ?: "Patient"
//
//                                        when (role) {
//                                            "Patient" -> onNavigateToFirstLogin()
//                                            "Doctor" -> {
//                                                val profile = db.collection("users").document(uid)
//                                                    .collection("profile").document("doctorInfo")
//                                                    .get().await()
//
//                                                if (profile.exists()) onNavigateToHome()
//                                                else onNavigateToDoctorFirstLogin()
//                                            }
//                                            else -> onNavigateToHome()
//                                        }
//                                    } catch (e: Exception) {
//                                        onNavigateToFirstLogin()
//                                    }
//                                }
//                            }
//                        }
//                    }
//                }
//            }) {
//                Text(if (authMode == AuthMode.LOGIN) "Login" else "Register")
//            }
//
//            OutlinedButton(onClick = { viewModel.toggleAuthMode() }) {
//                Text("Switch to ${if (authMode == AuthMode.LOGIN) "Register" else "Login"}")
//            }
//
//            state.errorMessage?.let {
//                Text("Error: $it", color = MaterialTheme.colorScheme.error)
//            }
//
//            if (state.isSuccess) {
//                Text("Success!", color = MaterialTheme.colorScheme.primary)
//            }
//        }
//    }
//}