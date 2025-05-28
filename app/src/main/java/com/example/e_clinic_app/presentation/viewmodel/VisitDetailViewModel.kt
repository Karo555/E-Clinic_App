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
 * ViewModel for managing the details of a specific visit (appointment) in the e-clinic application.
 *
 * This ViewModel fetches the details of an appointment from Firestore and exposes it as a state flow.
 *
 * @property firestore The Firestore instance used for database operations.
 * @property appointmentId The unique identifier of the appointment to be loaded.
 */
class VisitDetailViewModel(
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
     *
     * This method fetches the appointment data using the provided appointment ID
     * and updates the state flow with the retrieved data.
     */
    private fun loadAppointment() = viewModelScope.launch {
        val snap = firestore.collection("appointments")
            .document(appointmentId)
            .get().await()
        _appointment.value = snap.toObject(Appointment::class.java)
    }

    companion object {
        /**
         * Provides a factory for creating instances of `VisitDetailViewModel`.
         *
         * @param firestore The Firestore instance to use.
         * @param appointmentId The unique identifier of the appointment.
         * @return A factory for creating `VisitDetailViewModel` instances.
         */
        fun provideFactory(
            firestore: FirebaseFirestore,
            appointmentId: String
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return VisitDetailViewModel(firestore, appointmentId) as T
            }
        }
    }
}