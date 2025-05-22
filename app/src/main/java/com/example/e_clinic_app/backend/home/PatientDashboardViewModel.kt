package com.example.e_clinic_app.backend.home

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.example.e_clinic_app.data.appointment.Appointment
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.e_clinic_app.data.repository.DoctorRepository
import com.example.e_clinic_app.data.model.Doctor

/**
 * ViewModel for the patient dashboard.
 * Handles fetching upcoming appointments and available doctors.
 */
class PatientDashboardViewModel(
    override val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) : StandardDashboard() {

    // UI state for the list of available doctors
    sealed interface UiState {
        object Loading : UiState
        data class Success(val doctors: List<Doctor>) : UiState
        data class Error(val throwable: Throwable) : UiState
    }

    private val _doctorsState = MutableStateFlow<UiState>(UiState.Loading)
    val doctorsState: StateFlow<UiState> = _doctorsState.asStateFlow()

    // Search query state for filtering doctors (stubbed for future use)
    var searchQuery by mutableStateOf("")
        private set

    /**
     * Call to update the search query.
     * Currently only stored; filtering logic to be added later.
     */
    fun onSearchQueryChanged(query: String) {
        searchQuery = query
    }

    init {
        // Load doctors on initialization
        loadDoctors()
    }

    /**
     * Fetches all currently available doctors from Firestore.
     */
    private fun loadDoctors() {
        viewModelScope.launch {
            _doctorsState.value = UiState.Loading
            runCatching {
                DoctorRepository.fetchAvailableDoctors()
            }.onSuccess { list ->
                _doctorsState.value = UiState.Success(list)
            }.onFailure { error ->
                _doctorsState.value = UiState.Error(error)
                Log.e("PatientDashboardViewModel", "Error fetching doctors", error)
            }
        }
    }

    /**
     * Existing method for fetching patient appointments.
     */
    override fun fetchAppointments(firestore: FirebaseFirestore) {
        firestore.collection("appointments")
            .whereEqualTo("patientId", userId)
            .whereIn("status", listOf("CONFIRMED", "PENDING"))
            .get()
            .addOnSuccessListener { documents ->
                val appointments = documents.toObjects(Appointment::class.java)
                super.appointmentsList.clear()
                super.appointmentsList.addAll(appointments)
                // Notify observers or update UI as needed
            }
            .addOnFailureListener { exception ->
                Log.e("PatientDashboardViewModel", "Error fetching appointments", exception)
            }
    }
}