package com.example.e_clinic_app.presentation.viewmodel


import android.net.Uri
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
import com.google.firebase.firestore.SetOptions
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class ChatDetailViewModel(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val pairId: String
) : ViewModel() {

    private val messagesColl = firestore.collection("chats")
        .document(pairId)
        .collection("messages")

    private var registration: ListenerRegistration? = null

    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    // upload progress: null when idle, 0f..1f during upload
    private val _uploadProgress = MutableStateFlow<Float?>(null)
    val uploadProgress: StateFlow<Float?> = _uploadProgress.asStateFlow()

    val currentUserId: String?
        get() = Firebase.auth.currentUser?.uid

    init {
        startListening()
    }

    private fun startListening() {
        registration = messagesColl
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
                            timestamp = doc.getTimestamp("timestamp") ?: Timestamp.now(),
                            attachmentUrl = doc.getString("attachmentUrl"),
                            attachmentName = doc.getString("attachmentName"),
                            attachmentType = doc.getString("attachmentType"),
                            caption = doc.getString("caption")
                        )
                    }
                    _messages.value = list
                }
            }
    }

    fun sendMessage(text: String) {
        val uid = currentUserId ?: return
        viewModelScope.launch {
            try {
                // ensure chat doc exists with participants
                val parts = pairId.split("_")
                val otherId = parts.first { it != uid }
                firestore.collection("chats")
                    .document(pairId)
                    .set(
                        mapOf(
                            "patientId" to uid,
                            "doctorId" to otherId
                        ), SetOptions.merge()
                    )
                    .await()
                // send text message
                val msg = mapOf(
                    "senderId" to uid,
                    "text" to text,
                    "timestamp" to Timestamp.now()
                )
                messagesColl.add(msg).await()
            } catch (e: Exception) {
                Log.e("ChatDetailVM", "Error sending message", e)
                _error.value = e.message
            }
        }
    }

    /**
     * Upload file and send message with attachment
     */
    fun sendAttachment(uri: Uri, caption: String?) {
        val uid = currentUserId ?: return
        val msgRef = messagesColl.document()
        val ext = firestore.app.applicationContext.contentResolver
            .getType(uri)
            ?.let { android.webkit.MimeTypeMap
                .getSingleton()
                .getExtensionFromMimeType(it) }
            ?: "bin"
        val path = "chats/$pairId/attachments/${msgRef.id}.$ext"
        val storageRef = Firebase.storage.reference.child(path)

        viewModelScope.launch {
            try {
                // upload with progress listener
                storageRef.putFile(uri)
                    .addOnProgressListener { snap ->
                        _uploadProgress.value = snap.bytesTransferred.toFloat() / snap.totalByteCount
                    }
                    .await()
                val url = storageRef.downloadUrl.await().toString()
                _uploadProgress.value = null

                // send attachment message
                val msg = mapOf(
                    "senderId" to uid,
                    "text" to "",
                    "timestamp" to Timestamp.now(),
                    "attachmentUrl" to url,
                    "attachmentName" to uri.lastPathSegment,
                    "attachmentType" to firestore.app.applicationContext.contentResolver.getType(uri),
                    "caption" to caption
                )
                msgRef.set(msg).await()
            } catch (e: Exception) {
                Log.e("ChatDetailVM", "Error sending attachment", e)
                _error.value = e.message
                _uploadProgress.value = null
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