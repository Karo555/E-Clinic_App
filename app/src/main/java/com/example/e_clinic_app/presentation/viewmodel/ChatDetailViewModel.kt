package com.example.e_clinic_app.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.e_clinic_app.data.model.Message
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
import com.google.firebase.firestore.SetOptions

class ChatDetailViewModel(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val pairId: String
) : ViewModel() {

    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    val currentUserId: String?
        get() = Firebase.auth.currentUser?.uid

    private var registration: ListenerRegistration? = null

    init {
        startListening()
    }

    private fun startListening() {
        registration = firestore.collection("chats")
            .document(pairId)
            .collection("messages")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snaps, ex ->
                if (ex != null) {
                    Log.e("ChatDetailVM", "Listener error", ex)
                    _error.value = ex.message
                    return@addSnapshotListener
                }
                viewModelScope.launch {
                    val list = snaps?.documents.orEmpty().map { doc ->
                        Message(
                            id = doc.id,
                            senderId = doc.getString("senderId") ?: "",
                            text = doc.getString("text") ?: "",
                            timestamp = doc.getTimestamp("timestamp") ?: Timestamp.now()
                        )
                    }
                    _messages.value = list
                }
            }
    }

    fun sendMessage(text: String) {
        val uid = currentUserId ?: return
        val chatRef = firestore.collection("chats").document(pairId)
        viewModelScope.launch {
            try {
                // Ensure chat doc exists
                val parts = pairId.split("_")
                val doctorId = parts.first { it != uid }
                chatRef.set(
                    mapOf(
                        "patientId" to uid,
                        "doctorId" to doctorId
                    ),
                    SetOptions.merge()
                ).await()

                // Add message
                val msg = mapOf(
                    "senderId" to uid,
                    "text" to text,
                    "timestamp" to Timestamp.now()
                )
                chatRef.collection("messages")
                    .add(msg)
                    .await()
            } catch (e: Exception) {
                Log.e("ChatDetailVM", "Error sending message", e)
                _error.value = e.message
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        registration?.remove()
    }

    companion object {
        fun provideFactory(
            firestore: FirebaseFirestore,
            pairId: String
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return ChatDetailViewModel(firestore, pairId) as T
            }
        }
    }
}