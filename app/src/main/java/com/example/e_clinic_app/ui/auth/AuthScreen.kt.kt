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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreen(
    onAuthSuccess: () -> Unit
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
                        UserRole.values().forEach { role ->
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
                        Log.d("AuthScreen", "Login success")
                        onAuthSuccess()
                    }
                } else {
                    viewModel.register {
                        Log.d("AuthScreen", "Register success")
                        onAuthSuccess()
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