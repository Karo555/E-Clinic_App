import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.e_clinic_app.ui.bottomNavBar.BottomNavigationBar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatTabScreen(navController: NavController) {
    val auth = FirebaseAuth.getInstance()
    val currentUserId = auth.currentUser?.uid
    val db = FirebaseFirestore.getInstance()

    var chats by remember { mutableStateOf<List<ChatPreview>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(currentUserId) {
        if (currentUserId != null) {
            val snapshot = db.collection("chats")
                .whereArrayContains("participants", currentUserId)
                .orderBy("lastMessageAt", Query.Direction.DESCENDING)
                .get()
                .await()

            chats = snapshot.documents.mapNotNull { doc ->
                val doctorId = doc.getString("doctorId") ?: return@mapNotNull null
                val patientId = doc.getString("patientId") ?: return@mapNotNull null
                val lastMessage = doc.getString("lastMessage") ?: ""
                val lastMessageAt = doc.getTimestamp("lastMessageAt")?.toDate() ?: Date()
                val chatId = doc.id

                ChatPreview(
                    chatId = chatId,
                    doctorId = doctorId,
                    patientId = patientId,
                    lastMessage = lastMessage,
                    lastMessageAt = lastMessageAt
                )
            }
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Messages",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            BottomNavigationBar(navController)
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                chats.isEmpty() -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ChatBubbleOutline,
                            contentDescription = "Chat Icon",
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "No chats yet 💬",
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "Your messages with doctors will appear here.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                        )
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(chats) { chat ->
                            ChatItem(chat = chat) {
                                navController.navigate("MessageScreen/${chat.chatId}")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ChatItem(chat: ChatPreview, onClick: () -> Unit) {
    val sdf = remember { SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()) }
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        tonalElevation = 2.dp,
        shape = MaterialTheme.shapes.medium
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Chat with ${chat.otherPartyName}",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = chat.lastMessage,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = sdf.format(chat.lastMessageAt),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    }
}

data class ChatPreview(
    val chatId: String,
    val doctorId: String,
    val patientId: String,
    val lastMessage: String,
    val lastMessageAt: Date
) {
    val otherPartyName: String
        get() = "Doctor/Patient" // Placeholder – you can fetch actual name via Firestore if needed
}


