package com.example.e_clinic_app.backend.home

import android.util.Log
import com.example.e_clinic_app.data.appointment.Appointment
import com.google.firebase.firestore.FirebaseFirestore

class PatientDashboardViewModel :StandardDashboard() {

    override fun fetchAppointments(firestore: FirebaseFirestore) {
        firestore.collection("appointments")
            .whereEqualTo("patientId", userId)
            .whereIn("status", listOf("CONFIRMED", "PENDING"))
            .get()
            .addOnSuccessListener { documents ->
                val appointments = documents.toObjects(Appointment::class.java)
                super.appointmentsList.clear()
                super.appointmentsList.addAll(appointments)
                // Notify observers or update UI as needed0
            }
            .addOnFailureListener { exception ->
                Log.e("PatientDashboardViewModel", "Error fetching appointments", exception)
            }
    }
}