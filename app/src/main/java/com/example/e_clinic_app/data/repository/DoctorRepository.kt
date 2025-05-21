package com.example.e_clinic_app.data.repository

import com.example.e_clinic_app.data.model.Doctor
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.tasks.await
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.awaitAll
import android.util.Log

object DoctorRepository {
    suspend fun fetchAvailableDoctors(): List<Doctor> = coroutineScope {
        Log.d("DoctorRepo", "Starting fetchAvailableDoctors()")
        val db = Firebase.firestore
        val doctorsSnap = try {db.collection("users")
            .whereEqualTo("role", "Doctor")
            .get().await()
            .also { Log.d("DoctorRepo", "Fetched ${it.size()} user docs") }}
        catch (e: Exception) {
            Log.e("DoctorRepo", "Error fetching user docs: ${e.message}")
            return@coroutineScope emptyList<Doctor>()
        }

        // Parallel-fetch each profile sub-doc (only 1 expected per doctor)
        doctorsSnap.documents.mapNotNull { userDoc ->
            async {
                val profileSnap = userDoc.reference
                    .collection("profile")
                    .limit(1)
                    .get().await()
                    .documents
                    .firstOrNull()
                val doctorOrNull = profileSnap?.takeIf { it.getBoolean("availability") == true }?.let { p ->
                    val doctor = Doctor(
                        id              = userDoc.id,
                        firstName       = p.getString("firstName") ?: "",
                        lastName        = p.getString("lastName") ?: "",
                        specialisation  = p.getString("specialisation") ?: "",
                        institutionName = p.getString("institutionName") ?: "",
                        experienceYears = p.getLong("experienceYears")?.toInt() ?: 0,
                        availability    = true
                    )
                    Log.d(
                        "DoctorRepo",
                        "Created Doctor object: id=${doctor.id}, name=${doctor.firstName} ${doctor.lastName}, " +
                                "specialisation=${doctor.specialisation}, institution=${doctor.institutionName}, " +
                                "experienceYears=${doctor.experienceYears}"
                    )
                    doctor
                } ?: run {
                    Log.d("DoctorRepo", "Skipped doctor ${userDoc.id}: no profile or not available")
                    null
                }
                doctorOrNull
            }
        }.awaitAll().filterNotNull()
    }
}
