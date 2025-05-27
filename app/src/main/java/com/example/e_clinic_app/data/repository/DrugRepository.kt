package com.example.e_clinic_app.data.repository

import com.example.e_clinic_app.data.model.DosageUnit
import com.example.e_clinic_app.data.model.Drug
import com.example.e_clinic_app.data.model.Frequency
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.tasks.await
import java.time.Instant

/**
 * Repository for managing drug-related data in the e-clinic application.
 *
 * This class provides methods to interact with the Firestore database to fetch,
 * add, update, and delete drug information. It also maintains a state flow of
 * the current list of drugs for reactive data updates.
 *
 * @property firestore The Firestore instance used to interact with the database.
 */
class DrugRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private val _drugs = MutableStateFlow<List<Drug>>(emptyList())
    val drugs: Flow<List<Drug>> = _drugs

    /**
     * Loads all drugs from the Firestore `drugs` collection.
     *
     * This method fetches all drug documents from the Firestore database, maps them
     * to `Drug` objects, and updates the state flow with the resulting list.
     *
     * @throws Exception if there is an error during the Firestore operation.
     */
    suspend fun loadDrugs() {
        val snapshot = firestore.collection("drugs").get().await()
        val list = snapshot.documents.mapNotNull { doc ->
            try {
                val id = doc.id
                val name = doc.getString("name") ?: return@mapNotNull null
                val formulation = doc.getString("formulation") ?: ""

                // availableUnits stored as list of strings
                val units = doc.get("availableUnits") as? List<String> ?: emptyList()
                val availableUnits = units.mapNotNull { u ->
                    DosageUnit.values().find { it.name == u }
                }

                // commonDosages stored as map of unit -> list of numbers
                val cdRaw = doc.get("commonDosages") as? Map<String, List<Double>> ?: emptyMap()
                val commonDosages = cdRaw.mapNotNull { (unitStr, values) ->
                    val unit = DosageUnit.values().find { it.name == unitStr } ?: return@mapNotNull null
                    unit to values
                }.toMap()

                // frequency stored as string
                val freqStr = doc.getString("defaultFrequency") ?: Frequency.ONCE_DAILY.name
                val defaultFrequency = Frequency.values().find { it.name == freqStr } ?: Frequency.ONCE_DAILY

                // optional fields
                val searchableNames = (doc.get("searchableNames") as? List<String>) ?: emptyList()
                val createdAt = doc.getTimestamp("createdAt")?.toDate()?.toInstant()
                val updatedAt = doc.getTimestamp("updatedAt")?.toDate()?.toInstant()

                Drug(
                    id = id,
                    name = name,
                    formulation = formulation,
                    availableUnits = availableUnits,
                    commonDosages = commonDosages,
                    defaultFrequency = defaultFrequency,
                    searchableNames = searchableNames,
                    createdAt = createdAt,
                    updatedAt = updatedAt
                )
            } catch (e: Exception) {
                null
            }
        }
        _drugs.value = list
    }

    /**
     * Adds or updates a drug document in the Firestore `drugs` collection.
     *
     * This method creates or updates a drug document in the Firestore database
     * with the provided `Drug` object.
     *
     * @param drug The `Drug` object to be added or updated in the database.
     * @throws Exception if there is an error during the Firestore operation.
     */
    suspend fun upsertDrug(drug: Drug) {
        val data = mapOf(
            "name" to drug.name,
            "formulation" to drug.formulation,
            "availableUnits" to drug.availableUnits.map { it.name },
            "commonDosages" to drug.commonDosages.mapKeys { it.key.name },
            "defaultFrequency" to drug.defaultFrequency.name,
            "searchableNames" to drug.searchableNames,
            "createdAt" to drug.createdAt,
            "updatedAt" to Instant.now()
        )
        firestore.collection("drugs").document(drug.id).set(data).await()
    }

    /**
     * Deletes a drug document from the Firestore `drugs` collection by its ID.
     *
     * This method removes the drug document with the specified ID from the Firestore database.
     *
     * @param drugId The unique identifier of the drug to be deleted.
     * @throws Exception if there is an error during the Firestore operation.
     */
    suspend fun deleteDrug(drugId: String) {
        firestore.collection("drugs").document(drugId).delete().await()
    }
}