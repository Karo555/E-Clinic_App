package com.example.e_clinic_app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.e_clinic_app.data.appointment.Appointment
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

/**
 * ViewModel for managing the details of a specific appointment (doctor's POV) in the e-clinic application.
 *
 * This ViewModel fetches the details of an appointment from Firestore and exposes it as a state flow.
 *
 * @property firestore The Firestore instance used for database operations.
 * @property appointmentId The unique identifier of the appointment to be loaded.
 */
class AppointmentDetailViewModel(
    private val firestore: FirebaseFirestore,
    private val appointmentId: String
) : ViewModel() {
    private val _appointment = MutableStateFlow<Appointment?>(null)
    /** A state flow containing the details of the appointment. */
    val appointment: StateFlow<Appointment?> = _appointment.asStateFlow()

    init {
        loadAppointment()
    }
    /**
     * Loads the details of the appointment from Firestore.
     */
    private fun loadAppointment() = viewModelScope.launch {
        val snap = firestore.collection("appointments")
            .document(appointmentId)
            .get().await()
        _appointment.value = snap.toObject(Appointment::class.java)
    }

    /**
     * Adds a new prescription to the appointment and updates the Firestore document.
     *
     * @param appointmentId The ID of the appointment to update.
     * @param prescription The prescription data to add.
     */
    fun addPrescription(appointmentId: String, prescription: com.example.e_clinic_app.data.model.Prescription) = viewModelScope.launch {
        val docRef = firestore.collection("appointments").document(appointmentId)
        val snap = docRef.get().await()
        val current = snap.toObject(com.example.e_clinic_app.data.appointment.Appointment::class.java)
        val updatedPrescriptions = (current?.prescriptions ?: emptyList()) + prescription
        docRef.update("prescriptions", updatedPrescriptions).await()
        // Refresh local state
        loadAppointment()
    }

    companion object {
        fun provideFactory(
            firestore: FirebaseFirestore,
            appointmentId: String
        ): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    @Suppress("UNCHECKED_CAST")
                    return AppointmentDetailViewModel(firestore, appointmentId) as T
                }
            }
    }
}
