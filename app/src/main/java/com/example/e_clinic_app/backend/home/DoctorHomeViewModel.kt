package com.example.e_clinic_app.backend.home


import android.util.Log
import com.example.e_clinic_app.data.appointment.Appointment
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.firebase.Timestamp

class DoctorHomeViewModel(
    override val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) : StandardDashboard() {

    private var registration: ListenerRegistration? = null

    fun startListeningAppointments() {
        // Remove any existing listener
        registration?.remove()

        val nowTs = com.google.firebase.Timestamp.now()
        registration = firestore.collection("appointments")
            .whereEqualTo("doctorId", userId)
            .whereIn("status", listOf("CONFIRMED", "PENDING"))
            .whereGreaterThanOrEqualTo("date", nowTs)               // ← only upcoming
            .orderBy("date", com.google.firebase.firestore.Query.Direction.ASCENDING)  // ← sorted
            .addSnapshotListener { snapshots, exception ->
                if (exception != null) {
                    Log.e("DoctorHomeVM", "Error listening appointments", exception)
                    return@addSnapshotListener
                }
                Log.d("DoctorHomeVM", "Raw snaps: ${snapshots?.size()} documents")
                snapshots?.let { querySnap ->
                    val appointments = querySnap.toObjects(Appointment::class.java)
                    super.appointmentsList.clear()
                    super.appointmentsList.addAll(appointments)
                }
            }
    }

    override fun fetchAppointments(firestore: FirebaseFirestore) {
        // we no longer use one-time fetch
        startListeningAppointments()
    }

    override fun onCleared() {
        super.onCleared()
        registration?.remove()
    }
}