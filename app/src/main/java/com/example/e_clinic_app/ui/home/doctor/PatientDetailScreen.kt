package com.example.e_clinic_app.ui.home.doctor
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.e_clinic_app.presentation.viewmodel.PatientDetailState
import com.example.e_clinic_app.presentation.viewmodel.PatientDetailViewModel
import com.google.firebase.firestore.FirebaseFirestore
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatientDetailScreen(
    navController: NavController,
    patientId: String
) {
    // Obtain the ViewModel with patientId
    val vm: PatientDetailViewModel = viewModel(
        factory = PatientDetailViewModel.provideFactory(
            firestore = FirebaseFirestore.getInstance(),
            patientId = patientId
        )
    )

    val uiState = vm.uiState.collectAsState().value

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Patient Details") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            when (uiState) {
                is PatientDetailState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is PatientDetailState.Error -> {
                    Text(
                        text = uiState.message,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                is PatientDetailState.Success -> {
                    val profile = uiState.profile
                    Column(
                        Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "${profile.firstName} ${profile.lastName}",
                            style = MaterialTheme.typography.headlineSmall
                        )
                        Text(
                            text = "Email: ${profile.email}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = profile.bio,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    }
}