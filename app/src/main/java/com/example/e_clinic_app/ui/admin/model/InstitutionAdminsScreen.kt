package com.example.e_clinic_app.ui.admin.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.firestore.FirebaseFirestore

/**
 * A screen to manage Institution Admins - list, add, and remove.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InstitutionAdminsScreen(navController: NavController) {
    val firestore = FirebaseFirestore.getInstance()
    val institutionAdmins = remember { mutableStateListOf<InstitutionAdmin>() }

    var emailInput by remember { mutableStateOf("") }
    var institutionIdInput by remember { mutableStateOf("") }

    // Fetch institution admins on load
    LaunchedEffect(Unit) {
        firestore.collection("users")
            .whereEqualTo("role", "INSTITUTION_ADMIN")
            .get()
            .addOnSuccessListener { snapshot ->
                val admins = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(InstitutionAdmin::class.java)?.copy(id = doc.id)
                }
                institutionAdmins.clear()
                institutionAdmins.addAll(admins)
            }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Manage Institution Admins") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Add new institution admin
            OutlinedTextField(
                value = emailInput,
                onValueChange = { emailInput = it },
                label = { Text("Admin Email") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = institutionIdInput,
                onValueChange = { institutionIdInput = it },
                label = { Text("Institution ID") },
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = {
                    addInstitutionAdmin(emailInput.trim(), institutionIdInput.trim(), firestore) {
                        // Refresh list after add
                        firestore.collection("users")
                            .whereEqualTo("role", "INSTITUTION_ADMIN")
                            .get()
                            .addOnSuccessListener { snapshot ->
                                val admins = snapshot.documents.mapNotNull { doc ->
                                    doc.toObject(InstitutionAdmin::class.java)?.copy(id = doc.id)
                                }
                                institutionAdmins.clear()
                                institutionAdmins.addAll(admins)
                            }
                    }
                    emailInput = ""
                    institutionIdInput = ""
                },
                enabled = emailInput.isNotBlank() && institutionIdInput.isNotBlank(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Add Institution Admin")
            }

            Divider()

            // List of institution admins
            LazyColumn {
                items(institutionAdmins) { admin ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        elevation = CardDefaults.cardElevation()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text("Email: ${admin.email}")
                            Text("Institution ID: ${admin.institutionId}")
                            Button(
                                onClick = { removeInstitutionAdmin(admin.id, firestore) {
                                    institutionAdmins.remove(admin)
                                }},
                                modifier = Modifier.padding(top = 8.dp)
                            ) {
                                Text("Remove")
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun addInstitutionAdmin(email: String, institutionId: String, firestore: FirebaseFirestore, onComplete: () -> Unit) {
    val userData = hashMapOf(
        "email" to email,
        "institutionId" to institutionId,
        "role" to "INSTITUTION_ADMIN"
    )

    firestore.collection("users")
        .add(userData)
        .addOnSuccessListener {
            onComplete()
        }
}

private fun removeInstitutionAdmin(adminId: String, firestore: FirebaseFirestore, onComplete: () -> Unit) {
    firestore.collection("users")
        .document(adminId)
        .delete()
        .addOnSuccessListener {
            onComplete()
        }
}

/**
 * Model class for Institution Admin.
 */
data class InstitutionAdmin(
    val id: String = "",
    val email: String = "",
    val institutionId: String = ""
)
