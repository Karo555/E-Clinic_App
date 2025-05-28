package com.example.e_clinic_app.data
/**
 * Represents a medical institution in the e-clinic application.
 *
 * @property id The unique identifier for the institution.
 * @property name The name of the institution.
 */
data class Institution(
    val id: String,
    val name: String
)
/**
 * A map of cities to their respective lists of medical institutions.
 *
 * This map associates city names with a list of `Institution` objects,
 * representing the medical institutions available in each city.
 */
val institutionsByCity = mapOf(
    "Kraków" to listOf(
        Institution(id = "clinic_krk_001", name = "MediPlus Kraków"),
        Institution(id = "clinic_krk_002", name = "HealthPro Clinic Kraków")
    ),
    "Warszawa" to listOf(
        Institution(id = "clinic_waw_001", name = "Centrum Medyczne Warszawa"),
        Institution(id = "clinic_waw_002", name = "City Hospital #3")
    ),
    "Wrocław" to listOf(
        Institution(id = "clinic_wro_001", name = "Wrocław General Hospital")
    )
)
