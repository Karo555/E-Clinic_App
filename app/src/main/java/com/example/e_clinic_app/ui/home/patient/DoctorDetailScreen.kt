package com.example.e_clinic_app.ui.home.patient

import java.time.format.DateTimeFormatter
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.navigation.NavController
import androidx.compose.foundation.lazy.items
import com.example.e_clinic_app.presentation.viewmodel.DoctorDetailViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DoctorDetailScreen(
    navController: NavController,
    viewModel: DoctorDetailViewModel
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            when (uiState) {
                is DoctorDetailViewModel.UiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is DoctorDetailViewModel.UiState.Error -> {
                    Text(
                        text = (uiState as DoctorDetailViewModel.UiState.Error)
                            .throwable.message
                            ?: "Error loading profile",
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                is DoctorDetailViewModel.UiState.Success -> {
                    val successState = uiState as DoctorDetailViewModel.UiState.Success
                    val doctor = successState.doctor
                    val slots = successState.slots

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        // Doctor header
                        Text(
                            text = "${doctor.firstName} ${doctor.lastName}",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = if (doctor.availability) "Available" else "Unavailable",
                            style = MaterialTheme.typography.bodyLarge,
                            color = if (doctor.availability) Color.Green else Color.Gray
                        )
                        Spacer(Modifier.height(16.dp))

                        // Slots list
                        Text(
                            text = "Available Slots:",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(Modifier.height(8.dp))

                        if (slots.isEmpty()) {
                            Text("No slots available", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        } else {
                            val grouped = slots.groupBy { it.toLocalDate() }
                            LazyColumn(
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                grouped.forEach { (date, dateSlots) ->
                                    // Day header
                                    item {
                                        Text(
                                            text = date.format(DateTimeFormatter.ofPattern("EEE, MMM d")),
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.SemiBold,
                                            modifier = Modifier.padding(vertical = 4.dp)
                                        )
                                    }
                                    // Each slot
                                    items(dateSlots) { slot ->
                                        ElevatedCard(
                                            onClick = { viewModel.bookAppointment(slot) },
                                            modifier = Modifier
                                                .fillMaxWidth(),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Text(
                                                text = slot.format(DateTimeFormatter.ofPattern("HH:mm")),
                                                modifier = Modifier.padding(12.dp),
                                                style = MaterialTheme.typography.bodyMedium,
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
