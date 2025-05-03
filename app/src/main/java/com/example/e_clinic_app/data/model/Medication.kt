package com.example.e_clinic_app.data.model

// Represents a user's selected medication with dosage and schedule
data class Medication(
    val drug: Drug,
    val amount: Double,
    val unit: DosageUnit,
    val frequency: Frequency
)
