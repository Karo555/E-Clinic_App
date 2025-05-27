package com.example.e_clinic_app.data.users

import java.util.UUID
/**
 * Abstract base class representing a user entity in the e-clinic application.
 *
 * This class defines the common properties that all user types (e.g., Patient, Doctor, Admin)
 * share in the system. It is intended to be extended by specific user types to provide
 * additional properties and functionality.
 *
 * @property id The unique identifier for the user.
 * @property firstName The first name of the user.
 * @property lastName The last name of the user.
 * @property email The email address of the user.
 * @property role The role of the user, represented by the `Role` enum.
 * @property phoneNumber The optional phone number of the user.
 */
abstract class UsersEntity {
    abstract val id: UUID
    abstract val firstName: String
    abstract val lastName: String
    abstract val email: String
    abstract val role:Role
    abstract val phoneNumber: String?


}