package com.example.e_clinic_app.data.model
import com.google.firebase.Timestamp

/**
 * Combines a patient with their most recent visit timestamp in the e-clinic application.
 *
 * This data class represents a relationship between a patient and the timestamp
 * of their last recorded visit.
 *
 * @property patient The patient associated with the last visit.
 * @property lastVisit The timestamp of the patient's most recent visit.
 */
data class PatientWithLastVisit(
    val patient: Patient,
    val lastVisit: Timestamp
)