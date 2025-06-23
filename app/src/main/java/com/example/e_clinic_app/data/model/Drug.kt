package com.example.e_clinic_app.data.model

import java.time.Instant

/**
 * Represents a drug definition in the e-clinic application.
 *
 * This data class contains details about a drug, including its name, formulation,
 * available dosage units, common dosages, default frequency, and metadata such as
 * creation and update timestamps.
 *
 * @property id The unique identifier for the drug.
 * @property name The name of the drug.
 * @property formulation The formulation of the drug (e.g., tablet, syrup).
 * @property availableUnits A list of dosage units in which the drug is available.
 * @property commonDosages A map of dosage units to a list of common dosage values.
 * @property defaultFrequency The default frequency for administering the drug.
 * @property searchableNames A list of alternative names or keywords for searching the drug.
 * @property createdAt The timestamp indicating when the drug was created.
 */
data class Drug(
    val id: String,
    val name: String,
    val formulation: String,
    val availableUnits: List<DosageUnit>,
    val commonDosages: Map<DosageUnit, List<Double>>,
    val defaultFrequency: Frequency,
    val searchableNames: List<String> = emptyList(),
    val createdAt: Instant? = null
)

fun Drug.toFirestoreMap(): Map<String, Any?> {
    return mapOf(
        "id" to id,
        "name" to name,
        "formulation" to formulation,
        "availableUnits" to availableUnits.map { it.name },
        "commonDosages" to commonDosages.mapKeys { it.key.name },
        "defaultFrequency" to defaultFrequency.name,
        "searchableNames" to searchableNames,
        "createdAt" to createdAt?.toEpochMilli()
    )
}
