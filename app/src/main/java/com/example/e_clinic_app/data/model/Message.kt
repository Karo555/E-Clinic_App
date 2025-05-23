package com.example.e_clinic_app.data.model

data class Message(
    val id: String,
    val senderId: String,
    val text: String,
    val timestamp: com.google.firebase.Timestamp
)
