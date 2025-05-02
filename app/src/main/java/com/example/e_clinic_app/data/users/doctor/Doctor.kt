package com.example.e_clinic_app.data.users.doctor

import com.example.e_clinic_app.data.users.Role
import com.example.e_clinic_app.data.users.UsersEntity
import java.util.UUID

data class Doctor(
    override val id: UUID,
    override val firstName: String,
    override val lastName: String,
    override val email: String,
    override val role: Role = Role.DOCTOR,
    override val phoneNumber: String?,
    val specialization:Specialization,

):UsersEntity()