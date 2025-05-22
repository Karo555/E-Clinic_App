package com.example.e_clinic_app.data.appointment

import com.google.firebase.Timestamp

data class Appointment(
    val id: String,
    val doctorId: String,
    val patientId: String,
    val date:Timestamp,
    val status:AppointmentStatus,
    val extraData: Map<String, Any>
)
