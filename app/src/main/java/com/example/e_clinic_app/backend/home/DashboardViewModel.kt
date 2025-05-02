package com.example.e_clinic_app.backend.home

import androidx.lifecycle.ViewModel
import com.example.e_clinic_app.data.model.Appointment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

abstract class StandardDashboard : ViewModel() {

    val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    val user = FirebaseAuth.getInstance().currentUser
    val userId = user?.uid

    val appointments = mutableListOf<Appointment>()

    abstract fun fetchAppointments(firestore: FirebaseFirestore)


}