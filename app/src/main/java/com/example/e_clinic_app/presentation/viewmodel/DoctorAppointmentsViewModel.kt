package com.example.e_clinic_app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.e_clinic_app.data.appointment.Appointment
import com.example.e_clinic_app.data.appointment.AppointmentStatus
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId
import java.util.Date

class DoctorAppointmentsViewModel : ViewModel() {
    data class FilterState(
        val patientQuery: String = "",
        val startDate: LocalDate? = null,
        val endDate: LocalDate? = null,
        val statuses: Set<AppointmentStatus> = AppointmentStatus.values().toSet()
    )

    private val _appointments = MutableStateFlow<List<Appointment>>(emptyList())
    private val _filteredAppointments = MutableStateFlow<List<Appointment>>(emptyList())
    private val _isLoading = MutableStateFlow(true)
    private val _error = MutableStateFlow<String?>(null)
    private val _filterState = MutableStateFlow(FilterState())

    val appointments: StateFlow<List<Appointment>> = _appointments.asStateFlow()
    val filteredAppointments: StateFlow<List<Appointment>> = _filteredAppointments.asStateFlow()
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    val error: StateFlow<String?> = _error.asStateFlow()
    val filterState: StateFlow<FilterState> = _filterState.asStateFlow()

    private val zone = ZoneId.of("Europe/Warsaw")

    init {
        fetchAppointments()
        viewModelScope.launch {
            _filterState.collect { filter ->
                applyFilters()
            }
        }
    }

    fun onFilterChange(newState: FilterState) {
        _filterState.value = newState
    }

    private fun fetchAppointments() {
        _isLoading.value = true
        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            _error.value = "Not authenticated"
            _isLoading.value = false
            return
        }
        val db = FirebaseFirestore.getInstance()
        db.collection("appointments")
            .whereEqualTo("doctorId", user.uid)
            .get()
            .addOnSuccessListener { result ->
                val appts = result.documents.mapNotNull { it.toObject(Appointment::class.java)?.copy(id = it.id) }
                _appointments.value = appts
                applyFilters()
                _isLoading.value = false
            }
            .addOnFailureListener { e ->
                _error.value = e.message
                _isLoading.value = false
            }
    }

    private fun applyFilters() {
        val filter = _filterState.value
        val filtered = _appointments.value.filter { appt ->
            val patientMatch = filter.patientQuery.isBlank() ||
                (appt.patientFirstName + " " + appt.patientLastName).contains(filter.patientQuery, ignoreCase = true)
            val date = appt.date.toDate().toInstant().atZone(zone).toLocalDate()
            val startMatch = filter.startDate == null || !date.isBefore(filter.startDate)
            val endMatch = filter.endDate == null || !date.isAfter(filter.endDate)
            val statusMatch = filter.statuses.contains(appt.status)
            patientMatch && startMatch && endMatch && statusMatch
        }
        _filteredAppointments.value = filtered
    }
}

