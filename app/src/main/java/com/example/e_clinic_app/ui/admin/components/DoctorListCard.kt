package com.example.e_clinic_app.ui.admin.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * A composable function that displays a card representing a doctor's information.
 *
 * This card includes the doctor's full name and specialization, and it is clickable
 * to perform an action when selected.
 *
 * @param fullName The full name of the doctor to display.
 * @param specialization The specialization of the doctor to display.
 * @param onClick A lambda function to execute when the card is clicked. Defaults to an empty action.
 */
@Composable
fun DoctorListCard(
    fullName: String,
    specialization: String,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(text = fullName, style = MaterialTheme.typography.titleMedium)
            Text(text = specialization, style = MaterialTheme.typography.bodyMedium)
        }
    }
}