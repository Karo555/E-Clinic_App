package com.example.e_clinic_app.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.e_clinic_app.data.appointment.Appointment
import com.example.e_clinic_app.data.model.CareGap
import com.example.e_clinic_app.data.model.CareGapPriority
import com.example.e_clinic_app.data.model.CareGapStatus
import com.example.e_clinic_app.data.model.FollowUpReminder
import com.example.e_clinic_app.data.model.FollowUpStatus
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
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
     * @property visits The patient's visit history with the current doctor.
     * @property followUps The follow-up reminders for the patient.
     * @property careGaps The care gaps identified for the patient.
     */
    data class Success(
        val profile: PatientProfile,
        val visits: List<Appointment> = emptyList(),
        val followUps: List<FollowUpReminder> = emptyList(),
        val careGaps: List<CareGap> = emptyList()
    ) : PatientDetailState()
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
 * their public profile, email, visit history, follow-up reminders, and care gaps.
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
    
    // Get the current doctor's ID from Firebase Auth
    private val currentDoctorId: String
        get() = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    init {
        loadPatientData()
    }
    
    /**
     * Loads all patient data from Firestore.
     *
     * This method fetches the patient's profile, visit history, follow-up reminders,
     * and care gaps, then updates the UI state accordingly.
     */
    private fun loadPatientData() {
        viewModelScope.launch {
            _uiState.value = PatientDetailState.Loading
            try {
                // Fetch basic patient profile
                val profile = loadPatientProfile()

                // Fetch visit history with the current doctor
                val visits = loadVisitHistory()

                // Fetch follow-up reminders created by the current doctor
                val followUps = loadFollowUpReminders()

                // Fetch care gaps identified by the current doctor
                val careGaps = loadCareGaps()

                _uiState.value = PatientDetailState.Success(
                    profile = profile,
                    visits = visits,
                    followUps = followUps,
                    careGaps = careGaps
                )
            } catch (e: Exception) {
                Log.e("PatientDetailVM", "Error loading patient data", e)
                _uiState.value = PatientDetailState.Error(e.message ?: "Unknown error")
            }
        }
    }

    /**
     * Loads the patient's profile data from Firestore.
     */
    private suspend fun loadPatientProfile(): PatientProfile {
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

        return PatientProfile(
            id = patientId,
            firstName = firstName,
            lastName = lastName,
            bio = bio,
            email = email
        )
    }

    /**
     * Loads the patient's visit history with the current doctor.
     */
    private suspend fun loadVisitHistory(): List<Appointment> {
        return firestore.collection("appointments")
            .whereEqualTo("patientId", patientId)
            .whereEqualTo("doctorId", currentDoctorId)
            .orderBy("date", Query.Direction.DESCENDING)
            .get().await()
            .documents
            .mapNotNull { doc -> doc.toObject(Appointment::class.java) }
    }

    /**
     * Loads follow-up reminders created by the current doctor for the patient.
     */
    private suspend fun loadFollowUpReminders(): List<FollowUpReminder> {
        return firestore.collection("followUpReminders")
            .whereEqualTo("patientId", patientId)
            .whereEqualTo("doctorId", currentDoctorId)
            .orderBy("dueDate", Query.Direction.ASCENDING)
            .get().await()
            .documents
            .mapNotNull { doc -> doc.toObject(FollowUpReminder::class.java) }
    }

    /**
     * Loads care gaps identified by the current doctor for the patient.
     */
    private suspend fun loadCareGaps(): List<CareGap> {
        return firestore.collection("careGaps")
            .whereEqualTo("patientId", patientId)
            .whereEqualTo("doctorId", currentDoctorId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .get().await()
            .documents
            .mapNotNull { doc -> doc.toObject(CareGap::class.java) }
    }

    /**
     * Creates a new follow-up reminder for the patient.
     */
    fun createFollowUpReminder(description: String, dueDate: com.google.firebase.Timestamp) {
        viewModelScope.launch {
            try {
                val reminderId = firestore.collection("followUpReminders").document().id
                val reminder = FollowUpReminder(
                    id = reminderId,
                    patientId = patientId,
                    doctorId = currentDoctorId,
                    dueDate = dueDate,
                    description = description,
                    createdAt = com.google.firebase.Timestamp.now()
                )

                firestore.collection("followUpReminders")
                    .document(reminderId)
                    .set(reminder)
                    .await()

                // Refresh the data to reflect the new reminder
                loadPatientData()
            } catch (e: Exception) {
                Log.e("PatientDetailVM", "Error creating follow-up reminder", e)
            }
        }
    }

    /**
     * Creates a new care gap for the patient.
     */
    fun createCareGap(description: String, recommendedAction: String, priority: CareGapPriority) {
        viewModelScope.launch {
            try {
                val careGapId = firestore.collection("careGaps").document().id
                val careGap = CareGap(
                    id = careGapId,
                    patientId = patientId,
                    doctorId = currentDoctorId,
                    description = description,
                    recommendedAction = recommendedAction,
                    priority = priority,
                    createdAt = com.google.firebase.Timestamp.now()
                )

                firestore.collection("careGaps")
                    .document(careGapId)
                    .set(careGap)
                    .await()

                // Refresh the data to reflect the new care gap
                loadPatientData()
            } catch (e: Exception) {
                Log.e("PatientDetailVM", "Error creating care gap", e)
            }
        }
    }

    /**
     * Updates the status of a follow-up reminder.
     */
    fun updateFollowUpStatus(reminderId: String, newStatus: FollowUpStatus) {
        viewModelScope.launch {
            try {
                val updates = hashMapOf<String, Any>(
                    "status" to newStatus // Store as enum, not string
                )

                if (newStatus == FollowUpStatus.COMPLETED) {
                    updates["completedAt"] = com.google.firebase.Timestamp.now()
                }

                firestore.collection("followUpReminders")
                    .document(reminderId)
                    .update(updates)
                    .await()

                // Refresh the data to reflect the status change
                loadPatientData()
            } catch (e: Exception) {
                Log.e("PatientDetailVM", "Error updating follow-up status", e)
            }
        }
    }

    /**
     * Updates the status of a care gap.
     */
    fun updateCareGapStatus(careGapId: String, newStatus: CareGapStatus) {
        viewModelScope.launch {
            try {
                val updates = hashMapOf<String, Any>(
                    "status" to newStatus // Store as enum, not string
                )

                if (newStatus == CareGapStatus.RESOLVED) {
                    updates["resolvedAt"] = com.google.firebase.Timestamp.now()
                }

                firestore.collection("careGaps")
                    .document(careGapId)
                    .update(updates)
                    .await()

                // Refresh the data to reflect the status change
                loadPatientData()
            } catch (e: Exception) {
                Log.e("PatientDetailVM", "Error updating care gap status", e)
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