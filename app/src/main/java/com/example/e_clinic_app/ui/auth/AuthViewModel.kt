package com.example.e_clinic_app.ui.auth

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

enum class AuthMode { LOGIN, REGISTER }
enum class UserRole { Admin, Patient, Doctor }

data class AuthUiState(
    val email: String = "",
    val password: String = "",
    val role: UserRole = UserRole.Patient,
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val errorMessage: String? = null,
)

class AuthViewModel : ViewModel() {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState

    private val _authMode = MutableStateFlow(AuthMode.LOGIN)
    val authMode: StateFlow<AuthMode> = _authMode

    fun toggleAuthMode() {
        _authMode.value = if (_authMode.value == AuthMode.LOGIN) AuthMode.REGISTER else AuthMode.LOGIN
    }

    fun onEmailChange(email: String) {
        _uiState.value = _uiState.value.copy(email = email)
    }

    fun onPasswordChange(password: String) {
        _uiState.value = _uiState.value.copy(password = password)
    }

    fun onRoleChange(role: UserRole) {
        _uiState.value = _uiState.value.copy(role = role)
    }

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

    fun resetState() {
        _uiState.value = AuthUiState()
    }
}