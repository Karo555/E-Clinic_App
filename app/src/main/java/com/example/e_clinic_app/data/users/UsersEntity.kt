package com.example.e_clinic_app.data.users

import java.util.UUID

abstract class UsersEntity {
    abstract val id: UUID
    abstract val firstName: String
    abstract val lastName: String
    abstract val email: String
    abstract val role:Role
    abstract val phoneNumber: String?


}