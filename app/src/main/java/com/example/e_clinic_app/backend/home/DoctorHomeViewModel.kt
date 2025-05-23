package com.example.e_clinic_app.backend.home


import android.util.Log
import com.example.e_clinic_app.data.appointment.Appointment
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

class DoctorHomeViewModel(
    override val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) : StandardDashboard() {

    private var registration: ListenerRegistration? = null

    fun startListeningAppointments() {
        // Remove any existing listener
        registration?.remove()

        registration = firestore.collection("appointments")
            .whereEqualTo("doctorId", userId)
            .whereIn("status", listOf("CONFIRMED", "PENDING"))
            .addSnapshotListener { snapshots, exception ->
                if (exception != null) {
                    Log.e("DoctorHomeVM", "Error listening appointments", exception)
                    return@addSnapshotListener
                }
                snapshots?.let { querySnap ->
                    val appointments = querySnap.toObjects(Appointment::class.java)
                    super.appointmentsList.clear()
                    super.appointmentsList.addAll(appointments)
                    // Notify observers or trigger recomposition
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