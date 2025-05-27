package com.example.e_clinic_app.data.users.admin

import com.example.e_clinic_app.data.users.Role
import com.example.e_clinic_app.data.users.UsersEntity
import java.util.UUID

/**
 * Represents an admin user in the e-clinic application.
 *
 * This data class extends the `UsersEntity` class and provides additional properties
 * specific to admin users, such as the associated clinic ID.
 *
 * @property id The unique identifier for the admin user.
 * @property firstName The first name of the admin user.
 * @property lastName The last name of the admin user.
 * @property email The email address of the admin user.
 * @property role The role of the user, which is set to `Role.ADMIN` by default.
 * @property phoneNumber The optional phone number of the admin user.
 * @property clinicsId The unique identifier of the clinic associated with the admin user.
 */
data class Admin(
    override val id: UUID,
    override val firstName: String,
    override val lastName: String,
    override val email: String,
    override val role: Role = Role.ADMIN,
    override val phoneNumber: String?,
    val clinicsId:UUID

): UsersEntity()
