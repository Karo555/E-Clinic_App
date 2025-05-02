package com.example.e_clinic_app.data.users.admin

import com.example.e_clinic_app.data.users.Role
import com.example.e_clinic_app.data.users.UsersEntity
import java.util.UUID

data class Admin(
    override val id: UUID,
    override val firstName: String,
    override val lastName: String,
    override val email: String,
    override val role: Role = Role.ADMIN,
    override val phoneNumber: String?,
    val clinicsId:UUID

): UsersEntity()
