package com.example.e_clinic_app.data.model
import com.google.firebase.Timestamp

/**
 * Represents metadata for a document in the e-clinic application.
 *
 * This data class contains details about a document, including its unique identifier,
 * name, storage path, type, and the timestamp of when it was uploaded.
 *
 * @property id The unique identifier for the document.
 * @property name The name of the document.
 * @property storagePath The storage path where the document is saved.
 * @property type The type or format of the document (e.g., PDF, image).
 * @property uploadedAt The timestamp indicating when the document was uploaded.
 */
data class DocumentMeta(
    val id: String,
    val name: String,
    val storagePath: String,
    val type: String,
    val uploadedAt: Timestamp
)
