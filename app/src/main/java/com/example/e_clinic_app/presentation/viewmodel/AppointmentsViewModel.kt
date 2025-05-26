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
                    .get()
                    .await()

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
        /** Factory that sets filterField = "patientId" **/
        fun factoryForPatient() = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return AppointmentsViewModel(filterField = "patientId") as T
            }
        }
        /** Factory that sets filterField = "doctorId" **/
        fun factoryForDoctor() = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return AppointmentsViewModel(filterField = "doctorId") as T
            }
        }
    }
}