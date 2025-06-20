package com.example.e_clinic_app.data.model

/**
 * Represents a user's selected medication with dosage and schedule in the e-clinic application.
 *
 * This data class contains details about a medication, including the drug, dosage amount,
 * dosage unit, and the frequency of administration.
 *
 * @property drug The drug associated with the medication.
 * @property amount The dosage amount of the medication.
 * @property unit The unit of measurement for the dosage (e.g., MG, ML).
 * @property frequency The frequency of administration for the medication.
 */
data class Medication(
    val drug: Drug,
    val amount: Double,
    val unit: DosageUnit,
    val frequency: Frequency
)

