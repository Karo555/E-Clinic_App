package com.example.e_clinic_app.presentation.viewmodel


import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import com.example.e_clinic_app.data.model.Doctor
import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.ViewModelProvider

/**
 * ViewModel for the Doctor Detail screen.
 * Fetches a single doctor's profile and exposes UI state.
 */
class DoctorDetailViewModel(
    private val firestore: FirebaseFirestore,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    /**
     * UI states for doctor detail.
     */
    sealed interface UiState {
        object Loading : UiState
        data class Success(val doctor: Doctor) : UiState
        data class Error(val throwable: Throwable) : UiState
    }

    private val _uiState = MutableStateFlow<UiState>(UiState.Loading)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private val doctorId: String = checkNotNull(savedStateHandle.get<String>("doctorId"))

    init {
        loadDoctor()
    }

    /**
     * Fetches the doctor document and its profile subcollection entry.
     */
    private fun loadDoctor() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            runCatching {
                // Fetch main user doc
                val userSnap = firestore.collection("users")
                    .document(doctorId)
                    .get()
                    .await()

                if (!userSnap.exists()) {
                    error("Doctor document not found: $doctorId")
                }

                // Fetch profile sub-doc
                val profileSnap = firestore.collection("users")
                    .document(doctorId)
                    .collection("profile")
                    .limit(1)
                    .get()
                    .await()
                    .documents
                    .firstOrNull()
                    ?: error("Profile for doctor $doctorId not found")

                // Map to Doctor model
                Doctor(
                    id = doctorId,
                    firstName       = profileSnap.getString("firstName") ?: "",
                    lastName        = profileSnap.getString("lastName") ?: "",
                    specialisation  = profileSnap.getString("specialisation") ?: "",
                    institutionName = profileSnap.getString("institutionName") ?: "",
                    experienceYears = profileSnap.getLong("experienceYears")?.toInt() ?: 0,
                    availability    = profileSnap.getBoolean("availability") == true
                )
            }.onSuccess { doctor ->
                _uiState.value = UiState.Success(doctor)
            }.onFailure { error ->
                Log.e("DoctorDetailVM", "Error loading doctor profile", error)
                _uiState.value = UiState.Error(error)
            }
        }
    }

    companion object {
        /**
         * Factory to create ViewModel with SavedStateHandle.
         */
        fun provideFactory(
            firestore: FirebaseFirestore,
            savedStateHandle: SavedStateHandle
        ): ViewModelProvider.Factory = object : AbstractSavedStateViewModelFactory() {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(
                key: String,
                modelClass: Class<T>,
                handle: SavedStateHandle
            ): T {
                return DoctorDetailViewModel(firestore, handle) as T
            }
        }
    }
}

