package com.example.e_clinic_app.ui.home.doctor

import android.app.TimePickerDialog
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

    // Local editable ranges: day -> Pair<start,end>
    val daysOfWeek = listOf(
        "Monday","Tuesday","Wednesday","Thursday","Friday","Saturday","Sunday"
    )
    val formatter = DateTimeFormatter.ofPattern("HH:mm")
    val ranges = remember { mutableStateMapOf<String, Pair<LocalTime?,LocalTime?>>() }

    // Initialize from loaded schedule
    LaunchedEffect(schedule) {
        daysOfWeek.forEach { day ->
            val times = schedule[day].orEmpty()
            if (times.isNotEmpty()) {
                val start = LocalTime.parse(times.first(), formatter)
                val end = LocalTime.parse(times.last(), formatter)
                ranges[day] = Pair(start,end)
            } else {
                ranges[day] = Pair(null, null)
            }
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
                            val (currentStart, currentEnd) = ranges[day] ?: Pair(null,null)

                            // Start time picker
                            OutlinedTextField(
                                value = currentStart?.format(formatter) ?: "",
                                onValueChange = {},
                                label = { Text("Start") },
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable {
                                        val now = LocalTime.now()
                                        TimePickerDialog(
                                            context,
                                            { _, h, m ->
                                                ranges[day] = Pair(LocalTime.of(h,m), ranges[day]?.second)
                                            },
                                            currentStart?.hour ?: now.hour,
                                            currentStart?.minute ?: now.minute,
                                            true
                                        ).show()
                                    },
                                readOnly = true
                            )

                            // End time picker
                            OutlinedTextField(
                                value = currentEnd?.format(formatter) ?: "",
                                onValueChange = {},
                                label = { Text("End") },
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable {
                                        val now = LocalTime.now()
                                        TimePickerDialog(
                                            context,
                                            { _, h, m ->
                                                ranges[day] = Pair(ranges[day]?.first, LocalTime.of(h,m))
                                            },
                                            currentEnd?.hour ?: now.hour,
                                            currentEnd?.minute ?: now.minute,
                                            true
                                        ).show()
                                    },
                                readOnly = true
                            )
                        }
                    }

                    Spacer(Modifier.height(24.dp))
                    Button(
                        onClick = {
                            // Build weeklySchedule map
                            val updated = daysOfWeek.mapNotNull { day ->
                                val (s,e) = ranges[day] ?: Pair(null,null)
                                if (s != null && e != null && s < e) {
                                    // generate half-hour slots
                                    val slots = mutableListOf<String>()
                                    var cur = s
                                    while (cur != null && cur < e) {
                                        slots += cur.format(formatter)
                                        cur = cur.plusMinutes(30)
                                    }
                                    day to slots
                                } else null
                            }.toMap()
                            viewModel.saveSchedule(updated)
                            coroutineScope.launch {
                                // show confirmation, then back
                                // no Snackbar here for brevity
                                navController.popBackStack()
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Save & Continue")
                    }

                    if (ranges.values.all { it.first == null && it.second == null }) {
                        Text("No available slots", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}