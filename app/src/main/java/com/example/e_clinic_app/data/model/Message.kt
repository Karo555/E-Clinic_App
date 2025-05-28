package com.example.e_clinic_app.data.model
import com.google.firebase.Timestamp

/**
 * Represents a message in the e-clinic application.
 *
 * This data class contains details about a message, including its sender, content,
 * timestamp, and optional attachment information.
 *
 * @property id The unique identifier for the message.
 * @property senderId The unique identifier of the sender of the message.
 * @property text The text content of the message.
 * @property timestamp The timestamp indicating when the message was sent.
 * @property attachmentUrl The URL of the attached file, if any.
 * @property attachmentName The name of the attached file, if any.
 * @property attachmentType The type of the attached file (e.g., image, document), if any.
 * @property caption An optional caption for the attachment, if any.
 */
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