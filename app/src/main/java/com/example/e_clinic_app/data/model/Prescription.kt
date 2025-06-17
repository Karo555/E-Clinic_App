package com.example.e_clinic_app.data.model

import com.google.firebase.Timestamp


/**
 * Represents a medical prescription.
 *
 * This data class stores information about a prescription, including its unique identifier,
 * the ID of the authoring healthcare professional, the date it was issued, dosage instructions,
 * frequency of administration, the prescribed medication, any additional notes, and the ID of
 * the patient for whom it is intended.
 *
 * @property id The unique identifier for this prescription. Defaults to an empty string.
 * @property authorId The unique identifier of the healthcare professional who authored this prescription. Defaults to an empty string.
 * @property dateIssued The date and time when the prescription was issued. Uses Firebase `Timestamp`. Can be null if not yet issued or if the date is unknown.
 * @property dosage The prescribed dosage of the medication (e.g., "500mg", "1 tablet"). Defaults to an empty string.
 * @property frequency How often the medication should be taken (e.g., "Twice a day", "Every 4 hours"). Defaults to an empty string.
 * @property medication The name of the prescribed medication. Defaults to an empty string.
 * @property notes Any additional notes or instructions related to the prescription. Defaults to an empty string.
 * @property patientId The unique identifier of the patient for whom this prescription is intended. Defaults to an empty string.
 */
data class Prescription(
    val id: String = "",
    val authorId: String = "",
    val dateIssued: Timestamp? = null, // Use Timestamp
    val dosage: String = "",
    val frequency: String = "",
    val medication: String = "",
    val notes: String = "",
    val patientId: String = ""
) {
    // Extension function to convert Prescription to Firestore format
    companion object {
        fun fromFirestoreMap(map: Map<String, Any>): Prescription {
            return Prescription(
                id = map["id"] as? String ?: "",
                authorId = map["authorId"] as? String ?: "",
                dateIssued = map["dateIssued"] as? Timestamp,
                dosage = map["dosage"] as? String ?: "",
                frequency = map["frequency"] as? String ?: "",
                medication = map["medication"] as? String ?: "",
                notes = map["notes"] as? String ?: "",
                patientId = map["patientId"] as? String ?: ""
            )
        }
    }
}
fun Prescription.toFirestoreMap(): Map<String, Any?> {
    return mapOf(
        "id" to id,
        "authorId" to authorId,
        "dateIssued" to dateIssued,
        "dosage" to dosage,
        "frequency" to frequency,
        "medication" to medication,
        "notes" to notes,
        "patientId" to patientId
    )
}