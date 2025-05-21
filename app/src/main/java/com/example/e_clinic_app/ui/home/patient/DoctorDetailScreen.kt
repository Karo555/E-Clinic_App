package com.example.e_clinic_app.ui.home.patient

import com.example.e_clinic_app.presentation.viewmodel.DoctorDetailViewModel
import kotlinx.coroutines.launch
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import java.time.format.DateTimeFormatter
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DoctorDetailScreen(
    navController: NavController,
    viewModel: DoctorDetailViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

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
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
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
                    val (doctor, slots) = uiState as DoctorDetailViewModel.UiState.Success
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
                            Text(
                                text = "No slots available",
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            val grouped = slots.groupBy { it.toLocalDate() }
                            LazyColumn(
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                grouped.forEach { (date, dateSlots) ->
                                    item {
                                        Text(
                                            text = date.format(DateTimeFormatter.ofPattern("EEE, MMM d")),
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.SemiBold,
                                            modifier = Modifier.padding(vertical = 4.dp)
                                        )
                                    }
                                    items(dateSlots) { slot ->
                                        ElevatedCard(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable {
                                                    viewModel.bookAppointment(slot)
                                                    coroutineScope.launch {
                                                        snackbarHostState.showSnackbar(
                                                            "Appointment booked for ${slot.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))}"
                                                        )
                                                        navController.popBackStack()
                                                    }
                                                },
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Text(
                                                text = slot.format(DateTimeFormatter.ofPattern("HH:mm")),
                                                modifier = Modifier.padding(12.dp),
                                                style = MaterialTheme.typography.bodyMedium
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