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
 *
 * @property id The unique identifier of the patient.
 * @property firstName The first name of the patient.
 * @property lastName The last name of the patient.
 * @property bio A short biography or description of the patient.
 * @property email The email address of the patient.
 */
data class PatientProfile(
    val id: String,
    val firstName: String = "",
    val lastName: String = "",
    val bio: String = "",
    val email: String = ""
)
/**
 * Represents the UI state of the patient detail screen.
 */
sealed class PatientDetailState {
    /** Indicates that the data is currently being loaded. */
    object Loading : PatientDetailState()
    /**
     * Indicates that the data has been successfully loaded.
     *
     * @property profile The patient's profile data.
     */
    data class Success(val profile: PatientProfile) : PatientDetailState()
    /**
     * Indicates that an error occurred while loading the data.
     *
     * @property message The error message describing the issue.
     */
    data class Error(val message: String) : PatientDetailState()
}

/**
 * ViewModel to load and manage a patient's profile for display to the doctor.
 *
 * This ViewModel fetches the patient's profile data from Firestore, including
 * their public profile and email, and exposes the UI state for the patient detail screen.
 *
 * @property firestore The Firestore instance used for database operations.
 * @property patientId The unique identifier of the patient whose profile is being loaded.
 */
class PatientDetailViewModel(
    private val firestore: FirebaseFirestore,
    private val patientId: String
) : ViewModel() {

    private val _uiState = MutableStateFlow<PatientDetailState>(PatientDetailState.Loading)
    /** A state flow containing the current UI state of the patient detail screen. */
    val uiState: StateFlow<PatientDetailState> = _uiState.asStateFlow()

    init {
        loadPatient()
    }
    /**
     * Loads the patient's profile data from Firestore.
     *
     * This method fetches the patient's email and public profile data, including
     * their first name, last name, and bio, and updates the UI state accordingly.
     */
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
        /**
         * Provides a factory for creating instances of `PatientDetailViewModel`.
         *
         * @param firestore The Firestore instance to use.
         * @param patientId The unique identifier of the patient.
         * @return A factory for creating `PatientDetailViewModel` instances.
         */
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