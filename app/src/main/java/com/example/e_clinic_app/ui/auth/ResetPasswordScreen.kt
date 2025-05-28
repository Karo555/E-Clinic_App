package com.example.e_clinic_app.ui.auth

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
/**
 * A composable function that represents the Reset Password screen in the e-clinic application.
 *
 * This screen allows users to request a password reset by entering their email address. It validates
 * the email format, displays error messages for invalid input, and shows a success message if the reset
 * email is sent successfully. The screen also provides a button to navigate back to the login screen.
 *
 * @param onBackToLogin A callback invoked to navigate back to the login screen.
 * @param viewModel The `AuthViewModel` instance used to handle the password reset logic. Defaults to a local `viewModel`.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResetPasswordScreen(
    onBackToLogin: () -> Unit,
    viewModel: AuthViewModel = viewModel()
) {
    val focusManager = LocalFocusManager.current
    val coroutineScope = rememberCoroutineScope()

    var email by remember { mutableStateOf(TextFieldValue("")) }
    var isLoading by remember { mutableStateOf(false) }
    var showSuccess by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Reset Password", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(16.dp))

        TextField(
            value = email,
            onValueChange = {
                email = it
                errorMessage = null
                showSuccess = false
            },
            label = { Text("Email") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        errorMessage?.let {
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = it, color = MaterialTheme.colorScheme.error)
        }

        if (showSuccess) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "📧 If this email exists, a reset link has been sent. Check your inbox!",
                color = MaterialTheme.colorScheme.primary
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                focusManager.clearFocus()
                errorMessage = null
                showSuccess = false

                val trimmedEmail = email.text.trim()
                if (!android.util.Patterns.EMAIL_ADDRESS.matcher(trimmedEmail).matches()) {
                    errorMessage = "Please enter a valid email address."
                    return@Button
                }

                isLoading = true
                coroutineScope.launch {
                    viewModel.sendPasswordResetEmail(trimmedEmail) { success, message ->
                        isLoading = false
                        if (success) {
                            showSuccess = true
                        } else {
                            errorMessage = message ?: "Something went wrong. Please try again."
                        }
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .size(20.dp)
                        .padding(end = 8.dp),
                    strokeWidth = 2.dp
                )
            }
            Text("Send Reset Email")
        }

        Spacer(modifier = Modifier.height(24.dp))

        TextButton(onClick = onBackToLogin) {
            Text("← Back to Login")
        }
    }
}