package com.example.e_clinic_app.data.model

import java.time.Instant

// Represents a full prescription
data class Prescription(
    val id: String,
    val patientId: String,
    val prescriberId: String,
    val medications: List<Medication>,
    val startDate: Instant,
    val endDate: Instant? = null,
    val notes: String? = null,
    val createdAt: Instant = Instant.now(),
    val updatedAt: Instant = Instant.now()
)