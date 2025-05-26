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

object PatientRepository {
    private val db = Firebase.firestore

    /**
     * Fetches patients treated by a specific doctor, sorted by most recent appointment.
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