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
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

/**
 * ViewModel for managing the doctor's home dashboard.
 *
 * This ViewModel handles loading the doctor's profile information and listening
 * to real-time updates for appointments from Firestore.
 *
 * @property firestore The Firestore instance used for database operations. Defaults to [FirebaseFirestore.getInstance].
 */
class DoctorHomeViewModel(
    override val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) : StandardDashboard() {

    private var registration: ListenerRegistration? = null
    private var initialized = false
    private val _isBanned = MutableStateFlow<Boolean?>(null)
    val isBanned: StateFlow<Boolean?> = _isBanned

    // Exposes the doctor's first name as a read-only StateFlow
    private val _doctorFirstName = MutableStateFlow("")
    val doctorFirstName: StateFlow<String> = _doctorFirstName.asStateFlow()

    fun initialize(userId: String) {
        if (initialized) return
        initialized = true

        loadDoctorName()
        startListeningAppointments()

        viewModelScope.launch {
            val result = loadBanStatus(userId, firestore)
            _isBanned.value = result.getOrNull()
            result.exceptionOrNull()?.let {
                Log.e("BanCheck", "Error checking ban status: ${it.message}")
            }
        }
    }
    /**
     * Loads the doctor's first name from Firestore and updates the [_doctorFirstName] state.
     *
     * This method fetches the doctor's profile information based on the current user's UID.
     * If the profile is not found or an error occurs, the first name remains empty.
     */
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
    /**
     * Starts a real-time listener for the doctor's appointments in Firestore.
     *
     * The listener fetches appointments that match the following criteria:
     * - The `doctorId` matches the current user's UID.
     * - The `status` is either "CONFIRMED" or "PENDING".
     * - The `date` is greater than or equal to the current timestamp.
     *
     * The appointments are ordered by date in ascending order and stored in the
     * [appointmentsList] property of the parent [StandardDashboard] class.
     */
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
    /**
     * Fetches appointments from Firestore.
     *
     * This method is overridden to perform no operation because real-time updates
     * are handled by the listener in [startListeningAppointments].
     *
     * @param firestore The Firestore instance used for database operations.
     */
    override fun fetchAppointments(firestore: FirebaseFirestore) {
        // no-op: real-time listener in init
    }

    override suspend fun loadBanStatus(
        doctorId: String?,
        db: FirebaseFirestore
    ): Result<Boolean> = suspendCoroutine { continuation ->
        if (doctorId != null) {
            db.collection("users")
                .document(doctorId)
                .collection("profile")
                .document("doctorInfo")
                .get()
                .addOnSuccessListener { document ->
                    val isBaned = document.getBoolean("isBaned")
                    if (document.exists() && isBaned != null) {
                        continuation.resume(Result.success(isBaned))
                    } else {
                        continuation.resume(Result.failure(Exception("Doctor data not found.")))
                    }
                }
                .addOnFailureListener { exception ->
                    continuation.resume(Result.failure(exception))
                }
        }
    }

    /**
     * Cleans up resources when the ViewModel is cleared.
     *
     * This method removes the Firestore listener to prevent memory leaks.
     */
    override fun onCleared() {
        super.onCleared()
        registration?.remove()
    }
}