package com.example.e_clinic_app.ui.settings

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.e_clinic_app.data.model.DocumentMeta
import com.example.e_clinic_app.presentation.viewmodel.MyDocumentsViewModel
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
/**
 * A composable function that represents the My Documents screen in the e-clinic application.
 *
 * This screen allows users to manage their uploaded documents, such as medical reports or prescriptions.
 * Users can upload new documents, view existing ones, and delete documents they no longer need.
 *
 * The screen includes:
 * - A button to upload documents using the system file picker.
 * - A list of uploaded documents displayed in a scrollable list.
 * - Each document item includes options to view or delete the document.
 * - Integration with Firebase Storage for document management.
 *
 * @param navController The `NavController` used for navigating back to the previous screen.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyDocumentsScreen(
    navController: NavController
) {
    val vm: MyDocumentsViewModel = viewModel()
    val docs by vm.docs.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // File picker launcher
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? -> uri?.let { vm.uploadDocument(it) } }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Documents") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize()
        ) {
            Button(
                onClick = { launcher.launch(arrayOf("application/pdf", "image/*")) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Upload Document")
            }
            Spacer(Modifier.height(16.dp))
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(docs) { doc: DocumentMeta ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                // Open document by name click
                                scope.launch {
                                    val url = Firebase.storage
                                        .reference
                                        .child(doc.storagePath)
                                        .downloadUrl
                                        .await()
                                        .toString()
                                    context.startActivity(
                                        Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                    )
                                }
                            }
                    ) {
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = doc.name,
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(onClick = {
                                scope.launch {
                                    val url = Firebase.storage
                                        .reference
                                        .child(doc.storagePath)
                                        .downloadUrl
                                        .await()
                                        .toString()
                                    context.startActivity(
                                        Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                    )
                                }
                            }) {
                                Icon(Icons.Filled.Download, contentDescription = "Open")
                            }
                            IconButton(onClick = { vm.deleteDocument(doc) }) {
                                Icon(Icons.Filled.Delete, contentDescription = "Delete")
                            }
                        }
                    }
                }
            }
        }
    }
}