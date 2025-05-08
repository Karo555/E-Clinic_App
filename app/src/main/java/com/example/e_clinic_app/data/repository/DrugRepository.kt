package com.example.e_clinic_app.data.repository

import com.example.e_clinic_app.data.model.DosageUnit
import com.example.e_clinic_app.data.model.Drug
import com.example.e_clinic_app.data.model.Frequency
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.tasks.await
import java.time.Instant

class DrugRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private val _drugs = MutableStateFlow<List<Drug>>(emptyList())
    val drugs: Flow<List<Drug>> = _drugs

    /**
     * Fetch all drugs from Firestore 'drugs' collection, map to domain, and update the state flow.
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
     * Add or update a drug document in Firestore.
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
     * Delete a drug by ID.
     */
    suspend fun deleteDrug(drugId: String) {
        firestore.collection("drugs").document(drugId).delete().await()
    }
}