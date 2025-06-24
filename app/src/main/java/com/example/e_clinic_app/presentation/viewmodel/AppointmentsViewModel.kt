package com.example.e_clinic_app.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.e_clinic_app.data.appointment.Appointment
import com.example.e_clinic_app.data.appointment.AppointmentStatus
import com.example.e_clinic_app.data.model.Doctor
import com.google.firebase.Timestamp
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import com.example.e_clinic_app.data.model.Patient
/**
 * ViewModel for managing and loading appointments in the e-clinic application.
 *
 * This ViewModel fetches appointments from Firestore based on the current user's role
 * (patient or doctor) and provides state management for the UI.
 *
 * @property firestore The Firestore instance used to fetch appointment data.
 * @property filterField The field used to filter appointments (e.g., "patientId" or "doctorId").
 */
class AppointmentsViewModel(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val filterField: String
) : ViewModel() {

    private val _appointments = MutableStateFlow<List<Appointment>>(emptyList())
    /** A state flow containing the list of appointments. */
    val appointments: StateFlow<List<Appointment>> = _appointments.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    /** A state flow indicating whether the data is currently being loaded. */
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    /** A state flow containing any error messages encountered during data loading. */
    val error: StateFlow<String?> = _error.asStateFlow()

    private val currentUserId: String?
        /** The unique identifier of the currently authenticated user. */
        get() = Firebase.auth.currentUser?.uid

    init {
        loadAppointments()
    }
    /**
     * Loads appointments from Firestore based on the current user's role and filters.
     *
     * This method fetches confirmed appointments with a date greater than or equal to the current time.
     * The results are ordered by date.
     */
    fun loadAppointments() {
        val uid = currentUserId ?: return
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val now = Timestamp.now()
                val query = firestore.collection("appointments")
                    .whereEqualTo(filterField, uid)
                    .whereGreaterThanOrEqualTo("date", now)
                    .orderBy("date")

                val snaps = query.get().await()

                val list = snaps.documents.map { doc ->
                    // Appointment ID (Firestore auto-ID as String)
                    val apptId = doc.id

                        // Read embedded doctor name fields
                    val docUid   = doc.getString("doctorId")!!
                    val docFirst = doc.getString("doctorFirstName") ?: ""
                    val docLast  = doc.getString("doctorLastName")  ?: ""
                    val doctor = Doctor(
                        id              = docUid,
                        firstName       = docFirst,
                        lastName        = docLast,
                        specialisation  = "",
                        institutionName = "",
                        experienceYears = 0,
                        availability    = false,
                        weeklySchedule  = emptyMap()
                    )

                        // Patient holds only the ID for now
                    val patUid = doc.getString("patientId")!!
                    val patient = Patient(id = patUid)

                        // Date and status
                    val date   = doc.getTimestamp("date")!!
                    val status = AppointmentStatus.valueOf(doc.getString("status")!!)

                    Appointment(
                        id        = apptId,
                        doctorId  = doctor.id,
                        patientId = patient.id,
                        date      = date,
                        status    = status,
                        doctorFirstName = doctor.firstName,
                        doctorLastName = doctor.lastName,
                        patientFirstName = patient.firstName,
                        patientLastName = patient.lastName
                    )
                }

                _appointments.value = list
            } catch (e: Exception) {
                Log.e("ApptVM", "Error loading appointments", e)
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    companion object {
        /**
         * Factory for creating an `AppointmentsViewModel` with `filterField` set to "patientId".
         *
         * @return A `ViewModelProvider.Factory` instance for patient-specific appointments.
         */        fun factoryForPatient() = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return AppointmentsViewModel(filterField = "patientId") as T
            }
        }
        /**
         * Factory for creating an `AppointmentsViewModel` with `filterField` set to "doctorId".
         *
         * @return A `ViewModelProvider.Factory` instance for doctor-specific appointments.
         */        fun factoryForDoctor() = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return AppointmentsViewModel(filterField = "doctorId") as T
            }
        }
    }
}