package com.example.e_clinic_app.data.model

data class Doctor(
    val id: String,
    val firstName: String = "",
    val lastName: String = "",
    val specialisation: String = "",
    val institutionName: String = "",
    val experienceYears: Int = 0,
    val licenseNumber: String = "",
    val bio: String = "",
    val availability: Boolean = false,
    val weeklySchedule: Map<String, List<String>> = emptyMap()
)


