package com.example.e_clinic_app.data.model
/**
 * Represents a doctor in the e-clinic application.
 *
 * This data class contains details about a doctor, including their personal information,
 * specialization, institution, experience, and availability.
 *
 * @property id The unique identifier for the doctor.
 * @property firstName The first name of the doctor.
 * @property lastName The last name of the doctor.
 * @property specialisation The area of specialization of the doctor (e.g., cardiology, dermatology).
 * @property institutionName The name of the institution where the doctor is affiliated.
 * @property experienceYears The number of years of experience the doctor has.
 * @property licenseNumber The professional license number of the doctor.
 * @property bio A brief biography or description of the doctor.
 * @property availability Indicates whether the doctor is currently available for appointments.
 * @property weeklySchedule A map representing the doctor's weekly schedule, where the key is the day of the week
 * and the value is a list of available time slots.
 */
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