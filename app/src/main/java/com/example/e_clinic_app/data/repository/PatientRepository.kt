package com.example.e_clinic_app.data.repository

import com.example.e_clinic_app.data.model.Patient
import com.example.e_clinic_app.data.model.PatientWithLastVisit
import com.google.firebase.Timestamp
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.tasks.await

/**
 * Repository for managing patient-related data in the e-clinic application.
 *
 * This object provides methods to interact with the Firestore database to fetch
 * and manage patient information, including their profiles and appointment history.
 */
object PatientRepository {
    private val db = Firebase.firestore

    /**
     * Fetches patients treated by a specific doctor, sorted by their most recent appointment.
     *
     * This method retrieves all appointments for a given doctor, orders them by date in descending order,
     * and collects unique patient IDs. It then fetches the profiles of these patients in parallel and
     * combines them with the timestamp of their most recent visit.
     *
     * @param doctorId The unique identifier of the doctor whose patients are to be fetched.
     * @return A list of `PatientWithLastVisit` objects representing the patients and their most recent visit timestamps.
     * @throws Exception if there is an error during the Firestore operation.
     */
    suspend fun fetchPatientsForDoctor(doctorId: String): List<PatientWithLastVisit> = coroutineScope {
        // Query appointments for this doctor, ordered by date desc
        val apptSnaps = db.collection("appointments")
            .whereEqualTo("doctorId", doctorId)
            .orderBy("date", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .get().await()

        // Collect unique patient IDs preserving order
        val seen = linkedSetOf<String>()
        val visits = mutableListOf<Pair<String, Timestamp>>()
        for (doc in apptSnaps.documents) {
            val pid = doc.getString("patientId") ?: continue
            val date = doc.getTimestamp("date") ?: continue
            if (seen.add(pid)) {
                visits.add(pid to date)
            }
        }

        // Parallel-fetch patient profiles
        visits.map { (pid, ts) -> async {
            val profileSnap = db.collection("users")
                .document(pid)
                .collection("profile")
                .document("basicInfo")
                .get().await()
            val patient = Patient(
                id = pid,
                firstName = profileSnap.getString("firstName") ?: "",
                lastName = profileSnap.getString("lastName") ?: ""
            )
            PatientWithLastVisit(patient, ts)
        }}.awaitAll()
    }

}