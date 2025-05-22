package com.example.e_clinic_app.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.google.firebase.auth.ktx.auth
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
 * ViewModel for the Doctor Detail screen.
 * Fetches a single doctor's profile, expands its weekly schedule
 * into concrete slots for the next 7 days, and allows booking.
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
        data class Success(
            val doctor: Doctor,
            val slots: List<LocalDateTime>
        ) : UiState
        data class Error(val throwable: Throwable) : UiState
    }

    private val _uiState = MutableStateFlow<UiState>(UiState.Loading)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private val doctorId: String = checkNotNull(
        savedStateHandle.get<String>("doctorId")
    )

    init {
        loadDoctor()
    }

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
     * Books an appointment at the given slot.
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
                val userId = Firebase.auth.currentUser?.uid
                    ?: error("Not authenticated")
                val instant = slot.atZone(ZoneId.of("Europe/Warsaw")).toInstant()
                val timestamp = Timestamp(instant.epochSecond, instant.nano)
                val appt = mapOf(
                    "doctorId"          to doctor.id,
                    "doctorFirstName"   to doctor.firstName,
                    "doctorLastName"    to doctor.lastName,
                    "patientId"         to userId,
                    "date"              to timestamp,
                    "status"            to "CONFIRMED"
                )
                firestore.collection("appointments")
                    .add(appt).await()
                Log.d("DoctorDetailVM", "Booked slot: $slot for user: $userId")
            } catch (e: Exception) {
                Log.e("DoctorDetailVM", "Error booking appointment", e)
            }
        }
    }

    companion object {
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
