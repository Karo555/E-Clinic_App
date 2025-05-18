package com.example.e_clinic_app.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.e_clinic_app.data.users.Role
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await


data class AuthUiState(
    val email: String = "",
    val password: String = "",
    val role: Role = Role.PATIENT,
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val errorMessage: String? = null,
)

class AuthViewModel () : ViewModel() {


    private val db = FirebaseFirestore.getInstance()

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()


    private val _passwordVisible = MutableStateFlow(false)
    val passwordVisible: StateFlow<Boolean> = _passwordVisible.asStateFlow()


    fun onEmailChange(email: String) {
        _uiState.value = _uiState.value.copy(email = email)
    }

    fun onPasswordChange(password: String) {
        _uiState.value = _uiState.value.copy(password = password)
    }

    fun togglePasswordVisibility() {
        _passwordVisible.value = !_passwordVisible.value
    }

    fun login(
    ) {
        viewModelScope.launch {
            try {
                val user = auth.currentUser ?: return@launch
                val uid = user.uid

                val rolePaths = listOf("patients", "doctors", "admins")
                var role: String? = null

                for (path in rolePaths) {
                    val doc = db.collection("users_test").document(path)
                        .collection(path).document(uid).get().await()
                    if (doc.exists()) {
                        role = path.removeSuffix("s")
                        break
                    }


                }
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = "Login failed: ${e.message}") }
            }
        }
    }

    fun register(
    ) {
        viewModelScope.launch {
            try {
                val user = auth.currentUser ?: return@launch
                val uid = user.uid
                val role = _uiState.value.role.name.lowercase()

                val userData = mapOf(
                    "email" to user.email,
                    "role" to role.replaceFirstChar { it.uppercase() }
                )

                db.collection("users_test").document("${role}s")
                    .collection("${role}s").document(uid).set(userData).await()

            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = "Registration failed: ${e.message}") }
            }
        }
    }

    private fun signInWithEmailAndPassword(onSuccess: () -> Unit, onError: (String) -> Unit) {
        val email = _uiState.value.email
        val password = _uiState.value.password

        viewModelScope.launch {
            try {
                auth.signInWithEmailAndPassword(email, password).await()
                onSuccess()
            } catch (e: Exception) {
                onError(e.message ?: "Sign-in failed")
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