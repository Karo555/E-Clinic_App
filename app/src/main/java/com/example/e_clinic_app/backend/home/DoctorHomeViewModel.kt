package com.example.e_clinic_app.backend.home

import com.example.e_clinic_app.data.model.Appointment
import com.google.firebase.firestore.FirebaseFirestore

class DoctorHomeViewModel : StandrdDashboard() {

    override fun fetchAppointments(firestore: FirebaseFirestore) {
        firestore.collection("appointments")
            .whereEqualTo("doctorId", userId)
            .whereIn("status", listOf("CONFIRMED", "PENDING"))
            .get()
            .addOnSuccessListener { documents ->
                val appointments = documents.toObjects(Appointment::class.java)
                listOfAppointments.clear()
                listOfAppointments.addAll(appointments)
                // Notify observers or update UI as needed
            }
            .addOnFailureListener { exception ->
                // Log or handle the error appropriately
            }
    }



}