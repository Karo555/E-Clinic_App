package com.example.e_clinic_app.backend.home

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.example.e_clinic_app.data.appointment.Appointment
import com.google.firebase.Timestamp
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class DoctorHomeViewModel(
    override val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) : StandardDashboard() {

    private var registration: ListenerRegistration? = null

    // NEW: expose doctor's first name
    private val _doctorFirstName = MutableStateFlow("")
    val doctorFirstName: StateFlow<String> = _doctorFirstName.asStateFlow()

    init {
        loadDoctorName()
        startListeningAppointments()
    }

    private fun loadDoctorName() {
        val uid = Firebase.auth.currentUser?.uid ?: return
        viewModelScope.launch {
            try {
                val profileSnap = firestore.collection("users")
                    .document(uid)
                    .collection("profile")
                    .limit(1)
                    .get().await()
                    .documents
                    .firstOrNull()
                val firstName = profileSnap?.getString("firstName") ?: ""
                _doctorFirstName.value = firstName
            } catch (e: Exception) {
                Log.e("DoctorHomeVM", "Error loading doctor name", e)
            }
        }
    }

    fun startListeningAppointments() {
        registration?.remove()

        val nowTs = Timestamp.now()
        val uid = Firebase.auth.currentUser?.uid ?: return

        registration = firestore.collection("appointments")
            .whereEqualTo("doctorId", uid)
            .whereIn("status", listOf("CONFIRMED", "PENDING"))
            .whereGreaterThanOrEqualTo("date", nowTs)
            .orderBy("date", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshots, exception ->
                if (exception != null) {
                    Log.e("DoctorHomeVM", "Error listening appointments", exception)
                    return@addSnapshotListener
                }
                Log.d("DoctorHomeVM", "Raw snaps: ${snapshots?.size()} documents")
                val appts = snapshots?.toObjects(Appointment::class.java) ?: emptyList()
                super.appointmentsList.apply {
                    clear()
                    addAll(appts)
                }
            }
    }

    override fun fetchAppointments(firestore: FirebaseFirestore) {
        // no-op: real-time listener in init
    }

    override fun onCleared() {
        super.onCleared()
        registration?.remove()
    }
}