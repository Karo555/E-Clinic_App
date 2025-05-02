package com.example.e_clinic_app.backend.home

import com.example.e_clinic_app.data.appointment.Appoinment
import com.google.firebase.firestore.FirebaseFirestore

class PatientDashboardViewModel :StandardDashboard() {

    override fun fetchAppointments(firestore: FirebaseFirestore) {
        firestore.collection("appointments")
            .whereEqualTo("patientId", userId)
            .whereIn("status", listOf("CONFIRMED", "PENDING"))
            .get()
            .addOnSuccessListener { documents ->
                val appointments = documents.toObjects(Appoinment::class.java)
                super.appointments.clear()
                super.appointments.addAll(appointments)
                // Notify observers or update UI as needed
            }
            .addOnFailureListener { exception ->
                // Log or handle the error appropriately
            }
    }
}