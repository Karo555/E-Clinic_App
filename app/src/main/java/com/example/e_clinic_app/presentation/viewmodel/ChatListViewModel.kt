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
 * ViewModel for the chat list screen (patient side).
 */
class ChatListViewModel(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) : ViewModel() {

    private val _threads = MutableStateFlow<List<ChatThread>>(emptyList())
    val threads: StateFlow<List<ChatThread>> = _threads.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val currentUserId: String?
        get() = Firebase.auth.currentUser?.uid

    private var registration: ListenerRegistration? = null

    init {
        startListening()
    }

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

    override fun onCleared() {
        super.onCleared()
        registration?.remove()
    }
}

/**
 * Represents one chat thread between the current user (patient) and a doctor.
 */
data class ChatThread(
    val pairId: String,
    val doctor: Doctor
)