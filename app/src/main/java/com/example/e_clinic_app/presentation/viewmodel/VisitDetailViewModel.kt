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

class VisitDetailViewModel(
    private val firestore: FirebaseFirestore,
    private val appointmentId: String
) : ViewModel() {
    private val _appointment = MutableStateFlow<Appointment?>(null)
    val appointment: StateFlow<Appointment?> = _appointment.asStateFlow()

    init {
        loadAppointment()
    }

    private fun loadAppointment() = viewModelScope.launch {
        val snap = firestore.collection("appointments")
            .document(appointmentId)
            .get().await()
        _appointment.value = snap.toObject(Appointment::class.java)
    }

    companion object {
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