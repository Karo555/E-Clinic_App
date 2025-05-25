package com.example.e_clinic_app.data.model
import com.google.firebase.Timestamp

/** Combines a Patient with their most recent visit timestamp */
data class PatientWithLastVisit(
    val patient: Patient,
    val lastVisit: Timestamp
)