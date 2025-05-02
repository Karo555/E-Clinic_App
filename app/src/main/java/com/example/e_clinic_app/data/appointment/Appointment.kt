package com.example.e_clinic_app.data.appointment

import com.example.e_clinic_app.data.users.doctor.Doctor
import com.example.e_clinic_app.data.users.patient.Patient
import com.google.firebase.Timestamp
import java.util.UUID

data class Appointment(
    val id: UUID,
    val doctor: Doctor,
    val patient: Patient,
    val date:Timestamp,
    val status:AppointmentStatus,
)
