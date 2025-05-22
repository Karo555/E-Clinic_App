package com.example.e_clinic_app.ui.home.doctor

import android.app.TimePickerDialog
import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.e_clinic_app.presentation.viewmodel.DoctorAvailabilityViewModel
import kotlinx.coroutines.launch
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetAvailabilityScreen(
    navController: NavController,
    viewModel: DoctorAvailabilityViewModel
) {
    val schedule by viewModel.schedule.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    val daysOfWeek = listOf(
        "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"
    )
    val formatter = DateTimeFormatter.ofPattern("HH:mm")
    val ranges = remember { mutableStateMapOf<String, Pair<LocalTime?, LocalTime?>>() }

    LaunchedEffect(schedule) {
        daysOfWeek.forEach { day ->
            val times = schedule[day].orEmpty()
            ranges[day] = if (times.isNotEmpty()) {
                val start = LocalTime.parse(times.first(), formatter)
                val end = LocalTime.parse(times.last(), formatter)
                Pair(start, end)
            } else Pair(null, null)
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Set Weekly Availability") }) },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else {
                Column(
                    modifier = Modifier
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    error?.let { Text(it, color = MaterialTheme.colorScheme.error) }

                    daysOfWeek.forEach { day ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(day, modifier = Modifier.width(80.dp))
                            val (currentStart, currentEnd) = ranges[day] ?: Pair(null, null)

                            // Start Time Picker
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable {
                                        Log.d("SetAvailabilityScreen", "Tapped Start for $day")
                                        val now = LocalTime.now()
                                        TimePickerDialog(
                                            context,
                                            { _, h, m ->
                                                ranges[day] = Pair(LocalTime.of(h, m), currentEnd)
                                            },
                                            currentStart?.hour ?: now.hour,
                                            currentStart?.minute ?: now.minute,
                                            true
                                        ).show()
                                    }
                            ) {
                                OutlinedTextField(
                                    value = currentStart?.format(formatter) ?: "",
                                    onValueChange = {},
                                    label = { Text("Start") },
                                    modifier = Modifier.fillMaxWidth(),
                                    readOnly = true,
                                    enabled = false
                                )
                            }

                            // End Time Picker
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable {
                                        Log.d("SetAvailabilityScreen", "Tapped End for $day")
                                        val now = LocalTime.now()
                                        TimePickerDialog(
                                            context,
                                            { _, h, m ->
                                                ranges[day] =
                                                    Pair(ranges[day]?.first, LocalTime.of(h, m))
                                            },
                                            currentEnd?.hour ?: now.hour,
                                            currentEnd?.minute ?: now.minute,
                                            true
                                        ).show()
                                    }
                            ) {
                                OutlinedTextField(
                                    value = currentEnd?.format(formatter) ?: "",
                                    onValueChange = {},
                                    label = { Text("End") },
                                    modifier = Modifier.fillMaxWidth(),
                                    readOnly = true,
                                    enabled = false
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(24.dp))
                    Button(
                        onClick = {
                            val updated = daysOfWeek.mapNotNull { day ->
                                val (s, e) = ranges[day] ?: Pair(null, null)
                                if (s != null && e != null && s < e) {
                                    val slots = mutableListOf<String>()
                                    var cur = s
                                    while (cur != null && e != null && cur < e) {
                                        slots += cur.format(formatter)
                                        cur = cur.plusMinutes(30)
                                    }
                                    day to slots
                                } else null
                            }.toMap()
                            viewModel.saveSchedule(updated)
                            coroutineScope.launch {
                                navController.popBackStack()
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Save & Continue")
                    }

                    if (ranges.values.all { it.first == null && it.second == null }) {
                        Text(
                            "No available slots",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}