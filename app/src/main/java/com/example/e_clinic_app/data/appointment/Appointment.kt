package com.example.e_clinic_app.data.appointment

import com.google.firebase.Timestamp

data class Appointment(
    val id: String = "",
    val doctorId: String = "",
    val patientId: String = "",
    val date: Timestamp = Timestamp.now(),
    val status: AppointmentStatus = AppointmentStatus.PENDING,
    val doctorFirstName: String = "",
    val doctorLastName: String = "",
    val patientFirstName: String = "",
    val patientLastName: String = ""
)