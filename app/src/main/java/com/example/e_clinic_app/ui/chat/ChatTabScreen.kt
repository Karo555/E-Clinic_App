package com.example.e_clinic_app.ui.chat

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.e_clinic_app.ui.navigation.Routes
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.e_clinic_app.presentation.viewmodel.ChatListViewModel
import com.example.e_clinic_app.presentation.viewmodel.ChatThread
import androidx.compose.foundation.lazy.items
/**
 * A composable function that represents the Chat Tab screen in the e-clinic application.
 *
 * This screen displays a list of chat threads, allowing users to view and select a chat with a doctor.
 * It handles loading states, error messages, and empty states when no chats are available.
 * Each chat thread is displayed as a card, and clicking on a thread navigates to the Chat Detail screen.
 *
 * @param navController The `NavController` used for navigation to the Chat Detail screen.
 * @param viewModel The `ChatListViewModel` instance used to manage the list of chat threads. Defaults to a local `viewModel`.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatTabScreen(
    navController: NavController,
    viewModel: ChatListViewModel = viewModel()
) {
    val threads by viewModel.threads.collectAsState()
    val error by viewModel.error.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Chats") })
        },
        bottomBar = {
            com.example.e_clinic_app.ui.bottomNavBar.BottomNavigationBar(navController)
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            when {
                error != null -> Text(
                    text = error ?: "Error loading chats",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.align(Alignment.Center)
                )
                threads.isEmpty() -> Text(
                    text = "No chats yet. Start one from a doctor detail screen.",
                    modifier = Modifier.align(Alignment.Center)
                )
                else -> LazyColumn(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(threads) { thread: ChatThread ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    navController.navigate("${Routes.CHAT_DETAIL}/${thread.pairId}")
                                }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Dr. ${thread.doctor.firstName} ${thread.doctor.lastName}",
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
