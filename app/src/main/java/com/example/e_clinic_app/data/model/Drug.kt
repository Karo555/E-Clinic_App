package com.example.e_clinic_app.data.model

import java.time.Instant

// Represents a drug definition in the app and Firestore
data class Drug(
    val id: String,
    val name: String,
    val formulation: String,
    val availableUnits: List<DosageUnit>,
    val commonDosages: Map<DosageUnit, List<Double>>,
    val defaultFrequency: Frequency,
    val searchableNames: List<String> = emptyList(),
    val createdAt: Instant? = null,
    val updatedAt: Instant? = null
)
