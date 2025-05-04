package com.example.e_clinic_app.data.model
import com.example.e_clinic_app.data.repository.DrugRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.Instant

/**
 * Seeds the Firestore `drugs` collection with initial sample data.
 * Call this once (e.g., in Application.onCreate or a debug screen) to populate the catalog.
 */
object DrugSeeder {
    fun seed(repository: DrugRepository) {
        CoroutineScope(Dispatchers.IO).launch {
            val now = Instant.now()

            val drugs = listOf(
                Drug(
                    id = "amoxicillin",
                    name = "Amoxicillin",
                    formulation = "Tablet",
                    availableUnits = listOf(DosageUnit.MG, DosageUnit.TAB),
                    commonDosages = mapOf(
                        DosageUnit.MG to listOf(250.0, 500.0),
                        DosageUnit.TAB to listOf(1.0, 2.0)
                    ),
                    defaultFrequency = Frequency.TWICE_DAILY,
                    searchableNames = listOf("Amoxicillin", "Amox"),
                    createdAt = now,
                    updatedAt = now
                ),
                Drug(
                    id = "ibuprofen",
                    name = "Ibuprofen",
                    formulation = "Tablet",
                    availableUnits = listOf(DosageUnit.MG, DosageUnit.TAB),
                    commonDosages = mapOf(
                        DosageUnit.MG to listOf(200.0, 400.0, 600.0),
                        DosageUnit.TAB to listOf(1.0)
                    ),
                    defaultFrequency = Frequency.THREE_TIMES_DAILY,
                    searchableNames = listOf("Ibuprofen", "Advil", "Motrin"),
                    createdAt = now,
                    updatedAt = now
                ),
                Drug(
                    id = "cough_syrup",
                    name = "Cough Syrup",
                    formulation = "Suspension",
                    availableUnits = listOf(DosageUnit.ML),
                    commonDosages = mapOf(
                        DosageUnit.ML to listOf(5.0, 10.0)
                    ),
                    defaultFrequency = Frequency.THREE_TIMES_DAILY,
                    searchableNames = listOf("Cough Syrup", "Robitussin"),
                    createdAt = now,
                    updatedAt = now
                ),
                Drug(
                    id = "metformin",
                    name = "Metformin",
                    formulation = "Tablet",
                    availableUnits = listOf(DosageUnit.MG, DosageUnit.TAB),
                    commonDosages = mapOf(
                        DosageUnit.MG to listOf(500.0, 1000.0),
                        DosageUnit.TAB to listOf(1.0)
                    ),
                    defaultFrequency = Frequency.TWICE_DAILY,
                    searchableNames = listOf("Metformin", "Glucophage"),
                    createdAt = now,
                    updatedAt = now
                ),
                Drug(
                    id = "prednisone",
                    name = "Prednisone",
                    formulation = "Tablet",
                    availableUnits = listOf(DosageUnit.MG, DosageUnit.TAB),
                    commonDosages = mapOf(
                        DosageUnit.MG to listOf(5.0, 10.0),
                        DosageUnit.TAB to listOf(1.0)
                    ),
                    defaultFrequency = Frequency.ONCE_DAILY,
                    searchableNames = listOf("Prednisone"),
                    createdAt = now,
                    updatedAt = now
                )
            )

            drugs.forEach { drug ->
                try {
                    repository.upsertDrug(drug)
                } catch (e: Exception) {
                    // handle or log the error
                }
            }
        }
    }
}