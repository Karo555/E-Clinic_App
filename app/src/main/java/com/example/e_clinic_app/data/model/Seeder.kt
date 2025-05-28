package com.example.e_clinic_app.data.model
import com.example.e_clinic_app.data.repository.DrugRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.Instant

/**
 * Provides functionality to seed the Firestore `drugs` collection with initial sample data.
 *
 * This object is responsible for populating the `drugs` collection in Firestore with predefined
 * sample data. It is intended to be used for testing or initializing the application with
 * default data. The seeding process is performed asynchronously using coroutines.
 */
object DrugSeeder {
    /**
     * Seeds the `drugs` collection with a predefined list of sample drugs.
     *
     * This method creates a list of sample `Drug` objects and inserts them into the Firestore
     * database using the provided `DrugRepository`. The operation is performed on a background
     * thread using the `Dispatchers.IO` coroutine dispatcher.
     *
     * @param repository The `DrugRepository` instance used to interact with the Firestore database.
     */
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