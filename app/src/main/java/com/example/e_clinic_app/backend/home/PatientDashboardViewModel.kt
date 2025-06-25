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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.e_clinic_app.data.repository.DoctorRepository
import com.example.e_clinic_app.data.model.Doctor
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

/**
 * ViewModel for managing the patient dashboard.
 *
 * This ViewModel handles fetching upcoming appointments and available doctors
 * for the patient. It also manages the search query state for filtering doctors.
 *
 * @property firestore The Firestore instance used for database operations. Defaults to [FirebaseFirestore.getInstance].
 */
class PatientDashboardViewModel(
    override val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) : StandardDashboard() {
    private val _isBanned = MutableStateFlow<Boolean?>(null)
    val isBanned: StateFlow<Boolean?> = _isBanned
    private var initialized = false

    fun initialize(userId: String) {
        if (initialized) return
        initialized = true

        // Load the list of available doctors
        loadDoctors()

        // Fetch the patient's upcoming appointments
        fetchAppointments(firestore)

        // Check if the patient is banned
        viewModelScope.launch {
            val result = loadBanStatus(userId, firestore)
            _isBanned.value = result.getOrNull()
            // Optional: handle errors
            result.exceptionOrNull()?.let {
                Log.e("BanCheck", "Error checking ban status: ${it.message}")
            }
        }
    }

    /**
     * Represents the UI state for the list of available doctors.
     */
    sealed interface UiState {
        /** Indicates that the data is currently being loaded. */
        object Loading : UiState
        /**
         * Indicates that the data was successfully loaded.
         *
         * @property doctors The list of available doctors.
         */
        data class Success(val doctors: List<Doctor>) : UiState
        /**
         * Indicates that an error occurred while loading the data.
         *
         * @property throwable The exception that caused the error.
         */
        data class Error(val throwable: Throwable) : UiState
    }
    // Backing property for the state of available doctors
    private val _doctorsState = MutableStateFlow<UiState>(UiState.Loading)
    /** Exposes the state of available doctors as a read-only [StateFlow]. */
    val doctorsState: StateFlow<UiState> = _doctorsState.asStateFlow()

    // Holds the current search query for filtering doctors
    var searchQuery by mutableStateOf("")
        private set

    /**
     * Updates the search query.
     *
     * This method currently only updates the state. Filtering logic will be added in the future.
     *
     * @param query The new search query.
     */
    fun onSearchQueryChanged(query: String) {
        searchQuery = query
    }

    init {
        // Load the list of available doctors when the ViewModel is initialized
        loadDoctors()
        viewModelScope.launch {
            val result = loadBanStatus(userId, firestore)
            _isBanned.value = result.getOrNull()
            // Optional: handle errors
            result.exceptionOrNull()?.let {
                Log.e("BanCheck", "Error checking ban status: ${it.message}")
            }
        }

    }

    /**
     * Fetches the list of currently available doctors from Firestore.
     *
     * This method uses the [DoctorRepository] to retrieve the list of doctors
     * and updates the [doctorsState] accordingly.
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
     * Fetches the patient's upcoming appointments from Firestore.
     *
     * This method retrieves appointments where the `patientId` matches the current user's ID
     * and the `status` is either "CONFIRMED" or "PENDING". The appointments are stored in
     * the [appointmentsList] property of the parent [StandardDashboard] class.
     *
     * @param firestore The Firestore instance used for database operations.
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

    override suspend fun loadBanStatus(
        patientId: String?,
        db: FirebaseFirestore
    ): Result<Boolean> = suspendCoroutine { continuation ->
        if (patientId != null) {
            db.collection("users")
                .document(patientId)
                .collection("profile")
                .document("basicInfo")
                .get()
                .addOnSuccessListener { document ->
                    val isBaned = document.getBoolean("isBaned")
                    if (document.exists() && isBaned != null) {
                        continuation.resume(Result.success(isBaned))
                    } else {
                        continuation.resume(Result.failure(Exception("Patient data not found.")))
                    }
                }
                .addOnFailureListener { exception ->
                    continuation.resume(Result.failure(exception))
                }
        } else {
            continuation.resume(Result.failure(Exception("Patient ID is null.")))
        }
    }


}