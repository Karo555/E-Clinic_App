package com.example.e_clinic_app.data.model
import com.google.firebase.Timestamp


data class DocumentMeta(
    val id: String,
    val name: String,
    val storagePath: String,
    val type: String,
    val uploadedAt: Timestamp
)
