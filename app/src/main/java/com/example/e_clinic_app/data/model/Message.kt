package com.example.e_clinic_app.data.model
import com.google.firebase.Timestamp

data class Message(
    val id: String = "",
    val senderId: String = "",
    val text: String = "",
    val timestamp: Timestamp = Timestamp.now(),
    val attachmentUrl: String? = null,
    val attachmentName: String? = null,
    val attachmentType: String? = null,
    val caption: String? = null
)