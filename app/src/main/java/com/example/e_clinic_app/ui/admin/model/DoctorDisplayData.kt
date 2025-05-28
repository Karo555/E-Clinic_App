package com.example.e_clinic_app.ui.admin.model
/**
 * A data class representing the display information of a doctor in the e-clinic application.
 *
 * This class is used to encapsulate the essential details of a doctor, such as their unique identifier,
 * full name, and specialization, for use in the user interface.
 *
 * @property uid The unique identifier of the doctor.
 * @property fullName The full name of the doctor.
 * @property specialization The specialization or field of expertise of the doctor.
 */
data class DoctorDisplayData(
    val uid: String,
    val fullName: String,
    val specialization: String
)

