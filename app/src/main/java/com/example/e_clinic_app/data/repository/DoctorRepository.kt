package com.example.e_clinic_app.data.repository

import com.example.e_clinic_app.data.model.Doctor
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.tasks.await
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.awaitAll
import android.util.Log

/**
 * Repository for managing doctor-related data in the e-clinic application.
 *
 * This object provides methods to interact with the Firestore database to fetch
 * and manage doctor information, including their availability and profiles.
 */
object DoctorRepository {
    /**
     * Fetches a list of available doctors from the Firestore database.
     *
     * This method retrieves all users with the role of "Doctor" and checks their
     * availability by fetching their profile sub-document. Only doctors with
     * availability set to `true` are included in the result.
     *
     * @return A list of `Doctor` objects representing available doctors.
     */
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

        // Parallel-fetch each profile sub-document (only 1 expected per doctor)
        doctorsSnap.documents.mapNotNull { userDoc ->
            async {
                val profileSnap = userDoc.reference
                    .collection("profile")
                    .limit(1)
                    .get().await()
                    .documents
                    .firstOrNull()
                val doctorOrNull = profileSnap
                    ?.takeIf { it.getBoolean("availability") == true }
                    ?.let { p ->
                        Doctor(
                            id = userDoc.id,
                            firstName       = p.getString("firstName") ?: "",
                            lastName        = p.getString("lastName") ?: "",
                            specialisation  = p.getString("specialisation") ?: "",
                            institutionName = p.getString("institutionName") ?: "",
                            experienceYears = p.getLong("experienceYears")?.toInt() ?: 0,
                            licenseNumber   = p.getString("licenseNumber") ?: "",
                            bio             = p.getString("bio") ?: "",
                            availability    = true,
                            weeklySchedule  = p.get("weeklySchedule") as? Map<String, List<String>> ?: emptyMap()
                        )
                    }
                    ?: run {
                    Log.d("DoctorRepo", "Skipped doctor ${userDoc.id}: no profile or not available")
                    null
                }
                doctorOrNull
            }
        }.awaitAll().filterNotNull()
    }
}
