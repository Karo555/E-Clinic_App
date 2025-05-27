package com.example.e_clinic_app.data.model

/**
 * Represents a medical condition in the e-clinic application.
 *
 * This data class contains details about a medical condition, including its category
 * and an optional type for further classification.
 *
 * @property category The category of the medical condition (e.g., chronic, acute).
 * @property type An optional type providing additional classification of the medical condition.
 */
data class MedicalCondition(
    val category: String,
    val type: String? = null
)
