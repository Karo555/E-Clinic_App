package com.example.e_clinic_app.ui.chat

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.text.SimpleDateFormat
import java.util.*
import android.util.Log

@Composable
fun MessageScreen(chatId: String) {
    val auth = FirebaseAuth.getInstance()
    val currentUserId = auth.currentUser?.uid ?: return
    Log.d("ChatDebug", "Current UID: $currentUserId")

    if (currentUserId == null) {
        Log.e("ChatDebug", "User is not authenticated!")
        return
    }
    val db = FirebaseFirestore.getInstance()

    var messages by remember { mutableStateOf<List<Message>>(emptyList()) }
    var input by remember { mutableStateOf(TextFieldValue("")) }

    val sdf = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }

    // Load messages in real-time
    LaunchedEffect(chatId) {
        db.collection("chats")
            .document(chatId)
            .collection("messages")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    messages = snapshot.documents.mapNotNull {
                        val senderId = it.getString("senderId") ?: return@mapNotNull null
                        val text = it.getString("text") ?: return@mapNotNull null
                        val timestamp = it.getTimestamp("timestamp")?.toDate() ?: Date()
                        Message(senderId, text, timestamp)
                    }
                }
            }
    }

    // UI
    Column(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.weight(1f).padding(16.dp),
            reverseLayout = false
        ) {
            items(messages) { message ->
                val isMe = message.senderId == currentUserId
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = if (isMe) Alignment.End else Alignment.Start
                ) {
                    Surface(
                        color = if (isMe) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondaryContainer,
                        shape = MaterialTheme.shapes.medium,
                        modifier = Modifier.padding(vertical = 4.dp)
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Text(text = message.text, color = MaterialTheme.colorScheme.onPrimary)
                            Text(
                                text = sdf.format(message.timestamp),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }
        }

        // Input bar
        Row(modifier = Modifier.padding(8.dp)) {
            TextField(
                value = input,
                onValueChange = { input = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Type a message") }
            )
            Spacer(modifier = Modifier.width(8.dp))
            Button(onClick = {
                val msg = input.text.trim()
                if (msg.isNotEmpty()) {
                    val message = mapOf(
                        "senderID" to currentUserId,
                        "text" to msg,
                        "timestamp" to Date()
                    )
                    val chatRef = db.collection("chats").document(chatId)
                    chatRef.collection("messages").add(message)
                    chatRef.update(
                        mapOf(
                            "lastMessage" to msg,
                            "lastMessageAt" to Date()
                        )
                    )
                    input = TextFieldValue("")
                }
            }) {
                Text("Send")
            }
        }
    }
}

data class Message(
    val senderId: String,
    val text: String,
    val timestamp: Date
)
