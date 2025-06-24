package com.example.e_clinic_app.data.users.patient

import com.example.e_clinic_app.data.users.Role
import com.example.e_clinic_app.data.users.UsersEntity
import java.util.Date

/**
 * Represents a patient user in the e-clinic application.
 *
 * This data class extends the `UsersEntity` class and provides additional properties
 * specific to patient users, such as their date of birth.
 *
 * @property id The unique identifier for the patient user.
 * @property firstName The first name of the patient user.
 * @property lastName The last name of the patient user.
 * @property email The email address of the patient user.
 * @property role The role of the user, which is set to `Role.PATIENT` by default.
 * @property phoneNumber The optional phone number of the patient user.
 * @property dateOfBirth The date of birth of the patient.
 */
data class Patient(
    override val id: String,
    override val firstName: String,
    override val lastName: String,
    override val email: String,
    override val role: Role = Role.PATIENT,
    override val phoneNumber: String?,
    val dateOfBirth:Date,
    val isVerified: Boolean = false,
    val isBaned: Boolean = false

) : UsersEntity()