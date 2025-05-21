package com.example.e_clinic_app.data.repository

import com.example.e_clinic_app.data.model.Doctor
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.tasks.await
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.awaitAll

object DoctorRepository {
    suspend fun fetchAvailableDoctors(): List<Doctor> = coroutineScope {
        val db = Firebase.firestore
        val doctorsSnap = db.collection("users")
            .whereEqualTo("role", "doctor")
            .get().await()

        // Parallel-fetch each profile sub-doc (only 1 expected per doctor)
        doctorsSnap.documents.mapNotNull { userDoc ->
            async {
                val profileSnap = userDoc.reference
                    .collection("profile")
                    .limit(1)      // safeguard
                    .get().await()
                    .documents.firstOrNull()

                profileSnap?.let { p ->
                    if (p.getBoolean("availability") == true) {
                        Doctor(
                            id = userDoc.id,
                            firstName       = p.getString("firstName") ?: "",
                            lastName        = p.getString("lastName") ?: "",
                            specialisation  = p.getString("specialisation") ?: "",
                            institutionName = p.getString("institutionName") ?: "",
                            experienceYears = p.getLong("experienceYears")?.toInt() ?: 0,
                            availability    = true
                        )
                    } else null
                }
            }
        }.awaitAll().filterNotNull()
    }
}
