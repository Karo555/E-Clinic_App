package com.example.e_clinic_app.ui.admin.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.e_clinic_app.data.model.MedicalCondition

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedicalConditionPicker(
    selectedConditions: List<MedicalCondition>,
    onSelectionChanged: (List<MedicalCondition>) -> Unit
) {
    val allConditions = listOf("Allergy", "Asthma", "Diabetes", "Other")

    val allergyTypes = listOf("Food", "Pollen", "Medication", "Other")

    var selectedAllergyType by remember { mutableStateOf<String?>(null) }

    val conditionSelections = remember { mutableStateMapOf<String, Boolean>() }

    allConditions.forEach {
        if (conditionSelections[it] == null) {
            conditionSelections[it] = selectedConditions.any { cond -> cond.category == it }
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Known Medical Conditions", style = MaterialTheme.typography.titleMedium)

        allConditions.forEach { condition ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = conditionSelections[condition] == true,
                    onCheckedChange = {
                        conditionSelections[condition] = it
                        if (!it && condition == "Allergy") {
                            selectedAllergyType = null
                        }
                        // Update the parent
                        val newList = conditionSelections
                            .filter { it.value }
                            .map {
                                if (it.key == "Allergy" && selectedAllergyType != null) {
                                    MedicalCondition("Allergy", selectedAllergyType)
                                } else {
                                    MedicalCondition(it.key)
                                }
                            }
                        onSelectionChanged(newList)
                    }
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(condition)
            }

            if (condition == "Allergy" && conditionSelections[condition] == true) {
                // Allergy type dropdown
                var expanded by remember { mutableStateOf(false) }

                Box {
                    OutlinedTextField(
                        value = selectedAllergyType ?: "",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Type of Allergy") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 24.dp)
                            .clickable { expanded = true },
                        isError = selectedAllergyType == null
                    )

                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        allergyTypes.forEach { type ->
                            DropdownMenuItem(
                                text = { Text(type) },
                                onClick = {
                                    selectedAllergyType = type
                                    expanded = false
                                    // Trigger update again
                                    val newList = conditionSelections
                                        .filter { it.value }
                                        .map {
                                            if (it.key == "Allergy" && selectedAllergyType != null) {
                                                MedicalCondition("Allergy", selectedAllergyType)
                                            } else {
                                                MedicalCondition(it.key)
                                            }
                                        }
                                    onSelectionChanged(newList)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
