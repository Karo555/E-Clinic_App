package com.example.e_clinic_app.ui.home.doctor

import android.app.TimePickerDialog
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.getSystemService
import androidx.navigation.NavController
import com.example.e_clinic_app.presentation.viewmodel.DoctorAvailabilityViewModel
import kotlinx.coroutines.launch
import java.time.LocalTime
import java.time.format.DateTimeFormatter

/**
 * A composable function that represents the Set Availability screen in the e-clinic application.
 *
 * This screen allows doctors to set their weekly availability by selecting start and end times for each day of the week.
 * It provides a user-friendly interface with time pickers for each day and validates the input to ensure proper time ranges.
 *
 * The screen includes:
 * - A vertical scrollable layout for all days of the week.
 * - Toggles to enable/disable availability for each day.
 * - Time pickers for selecting start and end times for each day.
 * - A button to save the availability schedule, which updates the backend and navigates back to the previous screen.
 * - Error handling and loading indicators for a smooth user experience.
 *
 * @param navController The `NavController` used for navigating back to the previous screen.
 * @param viewModel The `DoctorAvailabilityViewModel` instance used to manage the screen's state and data.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetAvailabilityScreen(
    navController: NavController,
    viewModel: DoctorAvailabilityViewModel
) {
    val schedule by viewModel.schedule.collectAsState()
    val sessionDuration by viewModel.sessionDuration.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    // Available session durations in minutes
    val availableDurations = listOf(15, 20, 30, 45, 60)

    val daysOfWeek = listOf(
        "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"
    )
    val formatter = DateTimeFormatter.ofPattern("HH:mm")
    val ranges = remember { mutableStateMapOf<String, Pair<LocalTime?, LocalTime?>>() }
    val enabledDays = remember { mutableStateMapOf<String, Boolean>() }
    val view = LocalView.current

    LaunchedEffect(schedule) {
        daysOfWeek.forEach { day ->
            val times = schedule[day].orEmpty()
            ranges[day] = if (times.isNotEmpty()) {
                val start = LocalTime.parse(times.first(), formatter)
                val end = LocalTime.parse(times.last(), formatter)
                Pair(start, end)
            } else Pair(null, null)

            // Initialize enabledDays map
            enabledDays[day] = times.isNotEmpty()
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
                    verticalArrangement = Arrangement.spacedBy(20.dp) // More spacing between sections
                ) {
                    error?.let { Text(it, color = MaterialTheme.colorScheme.error) }

                    daysOfWeek.forEach { day ->
                        val (currentStart, currentEnd) = ranges[day] ?: Pair(null, null)
                        val isEnabled = enabledDays[day] == true

                        // Section background and spacing
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (isEnabled)
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                                    else
                                        MaterialTheme.colorScheme.surfaceVariant
                                )
                                .border(
                                    width = if (isEnabled) 2.dp else 1.dp,
                                    color = if (isEnabled)
                                        MaterialTheme.colorScheme.primary
                                    else
                                        MaterialTheme.colorScheme.outlineVariant,
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .padding(16.dp)
                        ) {
                            // Day toggle row with visual hierarchy
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    day,
                                    fontSize = if (isEnabled) 20.sp else 18.sp,
                                    fontWeight = if (isEnabled) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isEnabled)
                                        MaterialTheme.colorScheme.primary
                                    else
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Switch(
                                    checked = isEnabled,
                                    onCheckedChange = { isChecked ->
                                        enabledDays[day] = isChecked
                                        if (!isChecked) {
                                            ranges[day] = Pair(null, null)
                                        }
                                        // Haptic feedback
                                        val vibrator = context.getSystemService<Vibrator>()
                                        vibrator?.let {
                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                                it.vibrate(
                                                    VibrationEffect.createOneShot(
                                                        40,
                                                        if (isChecked) VibrationEffect.DEFAULT_AMPLITUDE else 30
                                                    )
                                                )
                                            } else {
                                                @Suppress("DEPRECATION")
                                                it.vibrate(40)
                                            }
                                        }
                                    },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = MaterialTheme.colorScheme.primary,
                                        checkedTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                                        uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                                        uncheckedTrackColor = MaterialTheme.colorScheme.outlineVariant
                                    )
                                )
                            }

                            Spacer(Modifier.height(10.dp))

                            if (isEnabled) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    // Start Time Picker
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(MaterialTheme.colorScheme.surface)
                                            .border(
                                                1.dp,
                                                MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                                                RoundedCornerShape(8.dp)
                                            )
                                            .clickable {
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
                                            .padding(vertical = 8.dp, horizontal = 12.dp)
                                    ) {
                                        Text(
                                            text = currentStart?.format(formatter) ?: "Select start",
                                            color = if (currentStart != null)
                                                MaterialTheme.colorScheme.onSurface
                                            else
                                                MaterialTheme.colorScheme.onSurfaceVariant,
                                            fontSize = 16.sp,
                                            modifier = Modifier.fillMaxWidth(),
                                            textAlign = TextAlign.Start
                                        )
                                    }

                                    // End Time Picker
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(MaterialTheme.colorScheme.surface)
                                            .border(
                                                1.dp,
                                                MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                                                RoundedCornerShape(8.dp)
                                            )
                                            .clickable {
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
                                            .padding(vertical = 8.dp, horizontal = 12.dp)
                                    ) {
                                        Text(
                                            text = currentEnd?.format(formatter) ?: "Select end",
                                            color = if (currentEnd != null)
                                                MaterialTheme.colorScheme.onSurface
                                            else
                                                MaterialTheme.colorScheme.onSurfaceVariant,
                                            fontSize = 16.sp,
                                            modifier = Modifier.fillMaxWidth(),
                                            textAlign = TextAlign.Start
                                        )
                                    }
                                }
                            } else {
                                // Disabled state hint
                                Text(
                                    "Unavailable",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 14.sp,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                        }
                    }

                    // Session duration selector
                    Column(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            "Session Duration",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(Modifier.height(8.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            availableDurations.forEach { duration ->
                                val isSelected = sessionDuration == duration
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(
                                            if (isSelected)
                                                MaterialTheme.colorScheme.primary
                                            else
                                                MaterialTheme.colorScheme.surface
                                        )
                                        .border(
                                            1.dp,
                                            if (isSelected)
                                                MaterialTheme.colorScheme.primary
                                            else
                                                MaterialTheme.colorScheme.outlineVariant,
                                            RoundedCornerShape(8.dp)
                                        )
                                        .clickable {
                                            viewModel.setSessionDuration(duration)
                                        }
                                        .padding(12.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "$duration min",
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = if (isSelected)
                                            MaterialTheme.colorScheme.onPrimary
                                        else
                                            MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(24.dp))
                    Button(
                        onClick = {
                            val updated = daysOfWeek.mapNotNull { day ->
                                val (s, e) = ranges[day] ?: Pair(null, null)
                                if (enabledDays[day] == true && s != null && e != null && s < e) {
                                    val slots = mutableListOf<String>()
                                    var cur = s
                                    while (cur != null && e != null && cur < e) {
                                        slots += cur.format(formatter)
                                        cur = cur.plusMinutes(sessionDuration.toLong())
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