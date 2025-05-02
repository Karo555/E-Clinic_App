package com.example.e_clinic_app.data.model

data class Appointment(
    val id: String,
    val date: String,
    val time: String,
    val status: String,
    val doctor: Doctor,
    val patient: Patient
)