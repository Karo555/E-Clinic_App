package com.example.e_clinic_app.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

/**
 * Represents the patient's public profile data displayed to the doctor.
 */
data class PatientProfile(
    val id: String,
    val firstName: String = "",
    val lastName: String = "",
    val bio: String = "",
    val email: String = ""
)

sealed class PatientDetailState {
    object Loading : PatientDetailState()
    data class Success(val profile: PatientProfile) : PatientDetailState()
    data class Error(val message: String) : PatientDetailState()
}

/**
 * ViewModel to load a patient's profile for display to the doctor.
 */
class PatientDetailViewModel(
    private val firestore: FirebaseFirestore,
    private val patientId: String
) : ViewModel() {

    private val _uiState = MutableStateFlow<PatientDetailState>(PatientDetailState.Loading)
    val uiState: StateFlow<PatientDetailState> = _uiState.asStateFlow()

    init {
        loadPatient()
    }

    private fun loadPatient() {
        viewModelScope.launch {
            _uiState.value = PatientDetailState.Loading
            try {
                // Fetch the user document
                val userSnap = firestore.collection("users")
                    .document(patientId)
                    .get().await()

                val email = userSnap.getString("email") ?: ""

                // Fetch profile sub-doc (if exists)
                val profileSnap = firestore.collection("users")
                    .document(patientId)
                    .collection("profile")
                    .limit(1)
                    .get().await()
                    .documents
                    .firstOrNull()

                val firstName = profileSnap?.getString("firstName") ?: ""
                val lastName  = profileSnap?.getString("lastName") ?: ""
                val bio       = profileSnap?.getString("bio") ?: ""

                val profile = PatientProfile(
                    id = patientId,
                    firstName = firstName,
                    lastName = lastName,
                    bio = bio,
                    email = email
                )

                _uiState.value = PatientDetailState.Success(profile)
            } catch (e: Exception) {
                Log.e("PatientDetailVM", "Error loading patient profile", e)
                _uiState.value = PatientDetailState.Error(e.message ?: "Unknown error")
            }
        }
    }

    companion object {
        fun provideFactory(
            firestore: FirebaseFirestore,
            patientId: String
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return PatientDetailViewModel(firestore, patientId) as T
            }
        }
    }
}