package com.example.e_clinic_app.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import com.example.e_clinic_app.data.model.Doctor
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId


/**
 * ViewModel for the Doctor Detail screen in the e-clinic application.
 *
 * This ViewModel fetches a doctor's profile, expands their weekly schedule into
 * available slots for the next 7 days, and allows booking appointments.
 *
 * @property firestore The Firestore instance used for database operations.
 * @property savedStateHandle The state handle for accessing saved state, including the doctor ID.
 */
class DoctorDetailViewModel(
    private val firestore: FirebaseFirestore,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    /**
     * Represents the UI state of the Doctor Detail screen.
     */
    sealed interface UiState {
        /** Indicates that the data is currently being loaded. */
        object Loading : UiState
        /**
         * Indicates that the data has been successfully loaded.
         *
         * @property doctor The doctor's profile data.
         * @property slots The list of available appointment slots.
         */
        data class Success(
            val doctor: Doctor,
            val slots: List<LocalDateTime>
        ) : UiState
        /**
         * Indicates that an error occurred while loading the data.
         *
         * @property throwable The exception that caused the error.
         */
        data class Error(val throwable: Throwable) : UiState
    }

    private val _uiState = MutableStateFlow<UiState>(UiState.Loading)
    /** A state flow containing the current UI state of the Doctor Detail screen. */
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private val doctorId: String = checkNotNull(
        savedStateHandle.get<String>("doctorId")
    )

    init {
        loadDoctor()
    }
    /**
     * Loads the doctor's profile and expands their weekly schedule into available slots.
     *
     * This method fetches the doctor's profile from Firestore, processes their weekly schedule
     * to generate appointment slots for the next 7 days, and filters out already-booked slots.
     */
    private fun loadDoctor() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            runCatching {
                // Fetch profile sub-doc
                val profileSnap = firestore.collection("users")
                    .document(doctorId)
                    .collection("profile")
                    .limit(1)
                    .get().await()
                    .documents
                    .firstOrNull()
                    ?: error("Profile for doctor $doctorId not found")


                // Map to Doctor model
                val doctor = Doctor(
                    id               = doctorId,
                    firstName        = profileSnap.getString("firstName") ?: "",
                    lastName         = profileSnap.getString("lastName") ?: "",
                    specialisation   = profileSnap.getString("specialisation") ?: "",
                    institutionName  = profileSnap.getString("institutionName") ?: "",
                    experienceYears  = profileSnap.getLong("experienceYears")?.toInt() ?: 0,
                    availability     = profileSnap.getBoolean("availability") == true,
                    weeklySchedule   = profileSnap.get("weeklySchedule") as? Map<String, ArrayList<String>> ?: emptyMap()
                )

                // Expand into slots for next 7 days
                val zone = ZoneId.of("Europe/Warsaw")
                val now = LocalDateTime.now(zone)
                val today = LocalDate.now(zone)
                val slots = mutableListOf<LocalDateTime>()
                val rawSchedule = doctor.weeklySchedule
                for (offset in 0..6) {
                    val date = today.plusDays(offset.toLong())
                    val dayKey = date.dayOfWeek.name.lowercase().replaceFirstChar { it.uppercase() }
                    val times = rawSchedule[dayKey].orEmpty()
                    for (timeStr in times) {
                        runCatching {
                            val lt = LocalTime.parse(timeStr)
                            val dt = LocalDateTime.of(date, lt)
                            if (dt.isAfter(now)) slots += dt
                        }.onFailure { e ->
                            Log.e("DoctorDetailVM", "Invalid time $timeStr for $dayKey", e)
                        }
                    }
                }
                slots.sort()

                // Subtract already-booked slots
                val nowTs = Timestamp.now()
                val apptsSnap = firestore.collection("appointments")
                    .whereEqualTo("doctorId", doctorId)
                    .whereEqualTo("status", "CONFIRMED")
                    .whereGreaterThanOrEqualTo("date", nowTs)
                    .get().await()

                val bookedDates = apptsSnap.documents.mapNotNull { doc ->
                    doc.getTimestamp("date")
                        ?.toDate()
                        ?.toInstant()
                        ?.atZone(zone)
                        ?.toLocalDateTime()
                }.toSet()

                val availableSlots = slots.filterNot { it in bookedDates }.sorted()
                doctor to availableSlots
            }.onSuccess { (doctor, slots) ->
                _uiState.value = UiState.Success(doctor, slots)
            }.onFailure { error ->
                Log.e("DoctorDetailVM", "Error loading doctor profile", error)
                _uiState.value = UiState.Error(error)
            }
        }
    }

    /**
     * Books an appointment at the specified slot.
     *
     * This method creates a new appointment in Firestore, embedding both doctor and patient information.
     *
     * @param slot The selected appointment slot to book.
     */
    fun bookAppointment(slot: LocalDateTime) {
        viewModelScope.launch {
            Log.d("DoctorDetailVM", "bookAppointment() called with $slot")
            val state = _uiState.value
            if (state !is UiState.Success) {
                Log.e("DoctorDetailVM", "Cannot book slot before doctor is loaded")
                return@launch
            }
            val doctor = state.doctor
            try {
                // Get authenticated patient ID
                val userId = Firebase.auth.currentUser?.uid
                    ?: error("Not authenticated")

                // Fetch patient profile sub-doc
                val profileSnap = firestore.collection("users")
                    .document(userId)
                    .collection("profile")
                    .limit(1)
                    .get().await()
                    .documents
                    .firstOrNull()

                val patientFirst = profileSnap?.getString("firstName") ?: ""
                val patientLast  = profileSnap?.getString("lastName")  ?: ""

                // Build timestamp
                val instant   = slot.atZone(ZoneId.of("Europe/Warsaw")).toInstant()
                val timestamp = Timestamp(instant.epochSecond, instant.nano)

                // Prepare appointment data
                val appt = mapOf(
                    "doctorId"          to doctor.id,
                    "doctorFirstName"   to doctor.firstName,
                    "doctorLastName"    to doctor.lastName,
                    "patientId"         to userId,
                    "patientFirstName"  to patientFirst,
                    "patientLastName"   to patientLast,
                    "date"              to timestamp,
                    "status"            to "CONFIRMED"
                )

                // Write to Firestore
                firestore.collection("appointments")
                    .add(appt)
                    .await()

                Log.d("DoctorDetailVM", "Booked slot: $slot for user: $userId")
            } catch (e: Exception) {
                Log.e("DoctorDetailVM", "Error booking appointment", e)
            }
        }
    }

    companion object {
        /**
         * Provides a factory for creating instances of `DoctorDetailViewModel`.
         *
         * @param firestore The Firestore instance to use.
         * @param savedStateHandle The state handle for accessing saved state.
         * @return A factory for creating `DoctorDetailViewModel` instances.
         */
        fun provideFactory(
            firestore: FirebaseFirestore,
            savedStateHandle: SavedStateHandle
        ) = object : androidx.lifecycle.AbstractSavedStateViewModelFactory() {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(
                key: String,
                modelClass: Class<T>,
                handle: SavedStateHandle
            ) = DoctorDetailViewModel(firestore, handle) as T
        }
    }
}
