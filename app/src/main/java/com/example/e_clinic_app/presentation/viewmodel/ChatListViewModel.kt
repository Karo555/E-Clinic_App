package com.example.e_clinic_app.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.tasks.await
import com.example.e_clinic_app.data.model.Doctor

/**
 * ViewModel for managing the chat list screen (patient side) in the e-clinic application.
 *
 * This ViewModel listens for real-time updates to the list of chat threads for the current patient
 * and fetches the associated doctor profiles for display.
 *
 * @property firestore The Firestore instance used for database operations.
 */
class ChatListViewModel(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) : ViewModel() {

    private val _threads = MutableStateFlow<List<ChatThread>>(emptyList())
    /** A state flow containing the list of chat threads for the current patient. */
    val threads: StateFlow<List<ChatThread>> = _threads.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    /** A state flow containing any error messages encountered during operations. */
    val error: StateFlow<String?> = _error.asStateFlow()

    private val currentUserId: String?
        /** The unique identifier of the currently authenticated user (patient). */
        get() = Firebase.auth.currentUser?.uid

    private var registration: ListenerRegistration? = null

    init {
        startListening()
    }
    /**
     * Starts listening for real-time updates to the chat threads for the current patient.
     *
     * This method fetches the list of chat threads where the patient is a participant
     * and retrieves the associated doctor profiles for each thread.
     */
    private fun startListening() {
        val uid = currentUserId ?: return
        registration = firestore.collection("chats")
            .whereEqualTo("patientId", uid)
            .addSnapshotListener { snapshots, ex ->
                if (ex != null) {
                    Log.e("ChatListVM", "Listener error", ex)
                    _error.value = ex.message
                    return@addSnapshotListener
                }
                viewModelScope.launch {
                    val list = snapshots?.documents.orEmpty().mapNotNull { doc ->
                        val pairId = doc.id
                        val doctorUid = doc.getString("doctorId") ?: return@mapNotNull null
                        try {
                            // Fetch doctor profile
                            val profileSnap = firestore.collection("users")
                                .document(doctorUid)
                                .collection("profile")
                                .limit(1)
                                .get().await()
                                .documents.firstOrNull()
                                ?: return@mapNotNull null
                            val doctor = Doctor(
                                id = doctorUid,
                                firstName = profileSnap.getString("firstName") ?: "",
                                lastName  = profileSnap.getString("lastName")  ?: "",
                                specialisation  = "",
                                institutionName = "",
                                experienceYears = 0,
                                availability    = false,
                                weeklySchedule  = emptyMap()
                            )
                            ChatThread(pairId, doctor)
                        } catch (e: Exception) {
                            Log.e("ChatListVM", "Error fetching doctor profile", e)
                            null
                        }
                    }
                    _threads.value = list
                }
                Log.d("ChatListVM", "Got snapshot with ${snapshots?.size()} docs")
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

/**
 * Represents a single chat thread between the current user (patient) and a doctor.
 *
 * @property pairId The unique identifier for the chat thread.
 * @property doctor The doctor associated with the chat thread.
 */
data class ChatThread(
    val pairId: String,
    val doctor: Doctor
)