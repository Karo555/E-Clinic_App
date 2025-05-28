package com.example.e_clinic_app.data.model

import java.time.Instant
/**
 * Represents a prescription in the e-clinic application.
 *
 * This data class contains details about a prescription, including the patient and prescriber IDs,
 * a list of medications, the start and optional end dates, notes, and timestamps for creation and updates.
 *
 * @property id The unique identifier for the prescription.
 * @property patientId The unique identifier of the patient associated with the prescription.
 * @property prescriberId The unique identifier of the prescriber who issued the prescription.
 * @property medications The list of medications included in the prescription.
 * @property startDate The start date of the prescription.
 * @property endDate The optional end date of the prescription.
 * @property notes Optional notes or instructions related to the prescription.
 * @property createdAt The timestamp indicating when the prescription was created.
 * @property updatedAt The timestamp indicating the last update to the prescription.
 */
data class Prescription(
    val id: String,
    val patientId: String,
    val prescriberId: String,
    val medications: List<Medication>,
    val startDate: Instant,
    val endDate: Instant? = null,
    val notes: String? = null,
    val createdAt: Instant = Instant.now(),
    val updatedAt: Instant = Instant.now()
)