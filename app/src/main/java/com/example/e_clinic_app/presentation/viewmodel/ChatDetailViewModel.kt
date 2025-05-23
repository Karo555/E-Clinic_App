package com.example.e_clinic_app.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import com.example.e_clinic_app.data.model.Message

/**
 * ViewModel for a single chat thread between patient and doctor.
 */
class ChatDetailViewModel(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val pairId: String = checkNotNull(savedStateHandle.get<String>("pairId"))

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
            .orderBy("timestamp")
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
        val msg = mapOf(
            "senderId" to uid,
            "text" to text,
            "timestamp" to Timestamp.now()
        )
        viewModelScope.launch {
            try {
                firestore.collection("chats")
                    .document(pairId)
                    .collection("messages")
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
            savedStateHandle: SavedStateHandle
        ) = object : AbstractSavedStateViewModelFactory() {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(
                key: String,
                modelClass: Class<T>,
                handle: SavedStateHandle
            ): T = ChatDetailViewModel(firestore, handle) as T
        }
    }

}