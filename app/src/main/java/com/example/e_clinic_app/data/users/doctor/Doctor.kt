package com.example.e_clinic_app.data.users.doctor

import com.example.e_clinic_app.data.users.Role
import com.example.e_clinic_app.data.users.UsersEntity
import java.util.UUID

/**
 * Represents a doctor user in the e-clinic application.
 *
 * This data class extends the `UsersEntity` class and provides additional properties
 * specific to doctor users, such as their specialization and rating.
 *
 * @property id The unique identifier for the doctor user.
 * @property firstName The first name of the doctor user.
 * @property lastName The last name of the doctor user.
 * @property email The email address of the doctor user.
 * @property role The role of the user, which is set to `Role.DOCTOR` by default.
 * @property phoneNumber The optional phone number of the doctor user.
 * @property specialization The specialization of the doctor.
 * @property rating The rating of the doctor, represented as a double value.
 */
data class Doctor(
    override val id: UUID,
    override val firstName: String,
    override val lastName: String,
    override val email: String,
    override val role: Role = Role.DOCTOR,
    override val phoneNumber: String?,
    val specialization:Specialization,
    var rating:Double,

):UsersEntity()