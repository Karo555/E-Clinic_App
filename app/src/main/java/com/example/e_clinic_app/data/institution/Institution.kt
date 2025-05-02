package com.example.e_clinic_app.data.institution

import android.provider.ContactsContract.CommonDataKinds.Email
import java.util.UUID

data class Institution (
    val id:UUID,
    val name: String,
    val city: String,
    val address: String,
    val phone: String,
    val email: Email
)