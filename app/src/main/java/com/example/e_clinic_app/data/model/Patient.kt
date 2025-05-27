package com.example.e_clinic_app.data.model

/**
 * Represents a patient in the e-clinic application.
 *
 * This data class contains details about a patient, including their unique identifier,
 * first name, and last name.
 *
 * @property id The unique identifier for the patient.
 * @property firstName The first name of the patient.
 * @property lastName The last name of the patient.
 */
data class Patient(
    val id: String,
    val firstName: String = "",
    val lastName: String = ""
)
