package com.example.e_clinic_app.presentation.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.e_clinic_app.data.model.DocumentMeta
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import com.google.firebase.storage.ktx.storage
import com.google.firebase.Timestamp

class MyDocumentsViewModel : ViewModel() {
    private val uid = FirebaseAuth.getInstance().currentUser!!.uid
    private val db = Firebase.firestore
    private val storage = Firebase.storage
    private val docsColl = db.collection("users")
        .document(uid)
        .collection("documents")

    private val _docs = MutableStateFlow<List<DocumentMeta>>(emptyList())
    val docs: StateFlow<List<DocumentMeta>> = _docs.asStateFlow()

    init {
        loadDocs()
    }

    fun loadDocs() = viewModelScope.launch {
        val snaps = docsColl.get().await()
        _docs.value = snaps.documents.map { d ->
            DocumentMeta(
                id = d.id,
                name = d.getString("name") ?: "",
                storagePath = d.getString("storagePath") ?: "",
                type = d.getString("type") ?: "",
                uploadedAt = d.getTimestamp("uploadedAt") ?: Timestamp.now()
            )
        }
    }

    fun uploadDocument(uri: Uri) = viewModelScope.launch {
        val docRef = docsColl.document()
        val path = "users/$uid/documents/${docRef.id}"
        // Upload to Storage
        storage.reference.child(path).putFile(uri).await()
        // Prepare metadata
        val name = uri.lastPathSegment ?: docRef.id
        val meta = mapOf(
            "name" to name,
            "storagePath" to path,
            "type" to "",
            "uploadedAt" to FieldValue.serverTimestamp()
        )
        // Save metadata to Firestore
        docRef.set(meta).await()
        loadDocs()
    }

    fun deleteDocument(meta: DocumentMeta) = viewModelScope.launch {
        // Delete from Storage
        storage.reference.child(meta.storagePath).delete().await()
        // Delete metadata
        docsColl.document(meta.id).delete().await()
        loadDocs()
    }
}