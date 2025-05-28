package com.example.e_clinic_app.ui.login

import androidx.compose.foundation.clickable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.e_clinic_app.ui.navigation.Routes
import com.google.firebase.auth.FirebaseAuth
/**
 * A composable function that represents the Login screen in the e-clinic application.
 *
 * This screen allows users to log in to their accounts using their email and password.
 * It provides error handling for invalid credentials and displays a loading state during the login process.
 * Upon successful login, the user is navigated to the Home screen.
 *
 * The screen includes:
 * - Input fields for email and password.
 * - A login button that triggers Firebase authentication.
 * - Error messages for invalid input or failed login attempts.
 * - A "Forgot password?" link for password recovery.
 * - A link to navigate to the registration screen for new users.
 *
 * @param navController The `NavController` used for navigating to other screens.
 * @param onLoginSuccess A callback function triggered upon successful login, which navigates the user to the Home screen.
 */
@Composable
fun LoginScreen(
    navController: NavController,
    onLoginSuccess: () -> Unit = {
        navController.navigate(Routes.HOME) {
            popUpTo(Routes.AUTH) { inclusive = true }
        }
    }
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .wrapContentSize()
                .align(Alignment.Center)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Welcome Back", style = MaterialTheme.typography.headlineMedium)

            OutlinedTextField(
                value = email,
                onValueChange = {
                    email = it
                    errorMessage = null
                },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = password,
                onValueChange = {
                    password = it
                    errorMessage = null
                },
                label = { Text("Password") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )

            errorMessage?.let {
                Text(it, color = MaterialTheme.colorScheme.error)
            }

            Button(
                onClick = {
                    if (email.isBlank() || password.isBlank()) {
                        errorMessage = "Please provide your email address and password"
                        return@Button
                    }

                    isLoading = true
                    errorMessage = null

                    FirebaseAuth.getInstance()
                        .signInWithEmailAndPassword(email.trim(), password)
                        .addOnSuccessListener {
                            isLoading = false
                            onLoginSuccess()
                        }
                        .addOnFailureListener {
                            isLoading = false
                            errorMessage = it.message ?: "Login failed"
                        }
                },
                enabled = !isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00C9A7))
            ) {
                Text(if (isLoading) "Logging In..." else "Login", fontSize = 16.sp)
            }

            Text(
                text = "Forgot password?",
                color = Color(0xFF00C9A7),
                modifier = Modifier.clickable { TODO() },
                textAlign = TextAlign.Center
            )
        }

        Text(
            text = "Don't have an account? Join us here",
            color = Color(0xFF00C9A7),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp)
                .clickable { navController.navigate(Routes.FIRST_LOGIN) },
            textAlign = TextAlign.Center
        )
    }
}
/**
 * A composable function that provides a preview of the Login screen.
 *
 * This function is used for development purposes to visualize the Login screen in isolation.
 */
@Composable
fun LoginScreenPreview() {
    val navController = rememberNavController()
    LoginScreen(
        navController,
        onLoginSuccess = { }
    )
}
/**
 * A composable function that displays a preview of the Login screen in the Android Studio Preview tool.
 *
 * This function is annotated with `@Preview` to enable rendering in the design editor.
 */
@Preview(showBackground = true)
@Composable
fun PreviewLoginScreen() {
    LoginScreenPreview()
}