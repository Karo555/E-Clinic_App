package com.example.e_clinic_app.data.users.patient

import com.example.e_clinic_app.data.users.Role
import com.example.e_clinic_app.data.users.UsersEntity
import java.util.Date
import java.util.UUID

data class Patient(
    override val id: UUID,
    override val firstName: String,
    override val lastName: String,
    override val email: String,
    override val role: Role = Role.PATIENT,
    override val phoneNumber: String?,
    val dateOfBirth:Date
) : UsersEntity()