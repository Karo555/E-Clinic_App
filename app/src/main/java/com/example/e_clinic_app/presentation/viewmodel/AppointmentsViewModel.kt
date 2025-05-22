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
 * ViewModel to load upcoming appointments for patient or doctor.
 */
class AppointmentsViewModel(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val filterField: String
) : ViewModel() {

    private val _appointments = MutableStateFlow<List<Appointment>>(emptyList())
    val appointments: StateFlow<List<Appointment>> = _appointments.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val currentUserId: String?
        get() = Firebase.auth.currentUser?.uid

    init {
        loadAppointments()
    }

    /**
     * Fetches all confirmed, future appointments for current user.
     */
    fun loadAppointments() {
        val uid = currentUserId ?: return
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val now = Timestamp.now()
                val snaps = firestore.collection("appointments")
                    .whereEqualTo(filterField, uid)
                    .whereEqualTo("status", "CONFIRMED")
                    .whereGreaterThanOrEqualTo("date", now)
                    .orderBy("date")
                    .get().await()

                val list = snaps.documents.map { doc ->
                    // Appointment ID
                    val apptId = doc.id
                    Log.d("ApptVM", "Appointment doc.id = ${doc.id}")
                    Log.d("ApptVM", "patientId field    = ${doc.getString("patientId")}")



                    // Doctor object (fetch profile)
                    val docUid = doc.getString("doctorId")!!
                    val docProfileSnap = firestore.collection("users")
                        .document(docUid)
                        .collection("profile")
                        .limit(1)
                        .get().await()
                        .documents.firstOrNull()
                        ?: error("Doctor profile not found for $docUid")
                    val doctor = Doctor(
                        id = docUid,
                        firstName = docProfileSnap.getString("firstName") ?: "",
                        lastName = docProfileSnap.getString("lastName") ?: "",
                        specialisation = docProfileSnap.getString("specialisation") ?: "",
                        institutionName = docProfileSnap.getString("institutionName") ?: "",
                        experienceYears = docProfileSnap.getLong("experienceYears")?.toInt() ?: 0,
                        availability = docProfileSnap.getBoolean("availability") == true,
                        weeklySchedule = emptyMap()
                    )

                    // Patient object (minimal)
                    val patUid = doc.getString("patientId")!!
                    val patient = Patient(id = patUid)

                    // Date and status
                    val date = doc.getTimestamp("date")!!
                    val status = AppointmentStatus.valueOf(doc.getString("status")!!)

                    Appointment(
                        id = apptId,
                        doctorId = doctor.id,
                        patientId = patient.id,
                        date = date,
                        status = status,
                        extraData = emptyMap()
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
         * Factory for patient view (filter by patientId)
         */
        fun factoryForPatient() = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return AppointmentsViewModel(filterField = "patientId") as T
            }
        }

        /**
         * Factory for doctor view (filter by doctorId)
         */
        fun factoryForDoctor() = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return AppointmentsViewModel(filterField = "doctorId") as T
            }
        }
    }
}