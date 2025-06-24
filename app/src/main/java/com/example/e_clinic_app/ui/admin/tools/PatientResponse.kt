package com.example.e_clinic_app.ui.admin.tools


import android.util.Log
import com.example.e_clinic_app.data.users.Role
import com.example.e_clinic_app.data.users.patient.Patient
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.tasks.await
import java.util.Date

object PatientResponse {
    /**
     * Fetches a list of patients from the Firestore database.
     *
     * This method retrieves all users with the role of "Patient" and fetches their
     * profile sub-document. Patients are included regardless of verification or ban status,
     * as these fields are part of the data class and can be filtered by the caller if needed.
     *
     * @return A list of `Patient` objects representing patients.
     */
    suspend fun fetchPatients(): List<Patient> = coroutineScope {
        Log.d("PatientRepo", "Starting fetchPatients()")
        val db = FirebaseFirestore.getInstance()
        val patientsSnap = try {
            db.collection("users")
                .whereEqualTo("role", "Patient")
                .get().await()
                .also { Log.d("PatientRepo", "Fetched ${it.size()} user docs") }
        } catch (e: Exception) {
            Log.e("PatientRepo", "Error fetching user docs: ${e.message}")
            return@coroutineScope emptyList<Patient>()
        }

        // Parallel-fetch each profile sub-document (only 1 expected per patient)
        patientsSnap.documents.mapNotNull { userDoc ->
            async {
                val profileSnap = userDoc.reference
                    .collection("profile")
                    .limit(1)
                    .get().await()
                    .documents
                    .firstOrNull()
                val patientOrNull = profileSnap?.let { p ->
                    Patient(
                        id = userDoc.id,
                        firstName = p.getString("firstName") ?: "",
                        lastName = p.getString("lastName") ?: "",
                        email = p.getString("email") ?: "",
                        role = Role.PATIENT,
                        phoneNumber = p.getString("phoneNumber"),
                        dateOfBirth = p.getTimestamp("dateOfBirth")?.toDate() ?: Date(),
                        isVerified = p.getBoolean("isVerified") ?: false,
                        isBaned = p.getBoolean("isBaned") ?: false
                    )
                } ?: run {
                    Log.d("PatientRepo", "Skipped patient ${userDoc.id}: no profile")
                    null
                }
                patientOrNull
            }
        }.awaitAll().filterNotNull()
    }
}