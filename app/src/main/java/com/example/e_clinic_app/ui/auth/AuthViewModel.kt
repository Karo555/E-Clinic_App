package com.example.e_clinic_app.ui.auth

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

/**
 * Represents the authentication mode (login or register).
 */
enum class AuthMode { LOGIN, REGISTER }
/**
 * Represents the user roles available in the application.
 */
enum class UserRole { Admin, Patient, Doctor }
/**
 * A data class representing the UI state of the authentication screen.
 *
 * @property email The email entered by the user.
 * @property password The password entered by the user.
 * @property role The selected user role (for registration).
 * @property isLoading Indicates whether an authentication operation is in progress.
 * @property isSuccess Indicates whether the last operation was successful.
 * @property errorMessage An optional error message for the last operation.
 */
data class AuthUiState(
    val email: String = "",
    val password: String = "",
    val role: UserRole = UserRole.Patient,
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val errorMessage: String? = null,
)
/**
 * A ViewModel class that manages the authentication logic for the e-clinic application.
 *
 * This class handles user login, registration, password reset, and state management for the authentication screen.
 * It interacts with Firebase Authentication and Firestore to perform these operations.
 */
class AuthViewModel : ViewModel() {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    private val _authMode = MutableStateFlow(AuthMode.LOGIN)
    val authMode: StateFlow<AuthMode> = _authMode.asStateFlow()

    private val _passwordVisible = MutableStateFlow(false)
    val passwordVisible: StateFlow<Boolean> = _passwordVisible.asStateFlow()
    /**
     * Toggles the authentication mode between login and registration.
     */
    fun toggleAuthMode() {
        _authMode.value = if (_authMode.value == AuthMode.LOGIN) AuthMode.REGISTER else AuthMode.LOGIN
    }
    /**
     * Updates the email in the UI state.
     *
     * @param email The new email value.
     */
    fun onEmailChange(email: String) {
        _uiState.value = _uiState.value.copy(email = email)
    }
    /**
     * Updates the password in the UI state.
     *
     * @param password The new password value.
     */
    fun onPasswordChange(password: String) {
        _uiState.value = _uiState.value.copy(password = password)
    }
    /**
     * Updates the user role in the UI state.
     *
     * @param role The new user role.
     */
    fun onRoleChange(role: UserRole) {
        _uiState.value = _uiState.value.copy(role = role)
    }
    /**
     * Toggles the visibility of the password field.
     */
    fun togglePasswordVisibility() {
        _passwordVisible.value = !_passwordVisible.value
    }
    /**
     * Performs the login operation using Firebase Authentication.
     *
     * @param onSuccess A callback invoked when the login is successful.
     */
    fun login(onSuccess: () -> Unit) {
        val email = _uiState.value.email.trim()
        val password = _uiState.value.password

        if (email.isBlank() || password.isBlank()) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "Email and password must not be empty."
            )
            return
        }

        _uiState.value = _uiState.value.copy(
            errorMessage = null,
            isSuccess = false,
            isLoading = true
        )

        viewModelScope.launch {
            try {
                auth.signInWithEmailAndPassword(email, password).await()
                _uiState.value = _uiState.value.copy(isLoading = false, isSuccess = true)
                onSuccess()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = e.message)
            }
        }
    }
    /**
     * Performs the registration operation using Firebase Authentication and Firestore.
     *
     * @param onSuccess A callback invoked when the registration is successful.
     */
    fun register(onSuccess: () -> Unit) {
        val email = _uiState.value.email.trim()
        val password = _uiState.value.password

        if (email.isBlank() || password.isBlank()) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "Email and password must not be empty."
            )
            return
        }

        _uiState.value = _uiState.value.copy(
            errorMessage = null,
            isSuccess = false,
            isLoading = true
        )

        viewModelScope.launch {
            try {
                val result = auth.createUserWithEmailAndPassword(email, password).await()
                Log.d("AuthViewModel", "Firebase user created")

                val uid = result.user?.uid
                if (uid == null) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "User ID is null. Registration failed."
                    )
                    return@launch
                }

                val userMap = mapOf(
                    "email" to email,
                    "role" to _uiState.value.role.name,
                    "createdAt" to System.currentTimeMillis()
                )

                firestore.collection("users").document(uid).set(userMap).await()

                Log.d("AuthViewModel", "User data saved to Firestore")
                _uiState.value = _uiState.value.copy(isLoading = false, isSuccess = true)
                onSuccess()

            } catch (e: Exception) {
                Log.e("AuthViewModel", "Registration failed: ${e.message}")
                _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = e.message)
            }
        }
    }
    /**
     * Sends a password reset email to the specified email address.
     *
     * @param email The email address to send the reset link to.
     * @param onComplete A callback invoked with the success status and an optional error message.
     */
    fun sendPasswordResetEmail(email: String, onComplete: (Boolean, String?) -> Unit) {
        if (email.isBlank()) {
            onComplete(false, "Please enter your email address.")
            return
        }

        viewModelScope.launch {
            try {
                auth.sendPasswordResetEmail(email).await()
                onComplete(true, null)
            } catch (e: Exception) {
                onComplete(false, e.message)
            }
        }
    }
    /**
     * Resets the UI state to its initial values.
     */
    fun resetState() {
        _uiState.value = AuthUiState()
    }
}