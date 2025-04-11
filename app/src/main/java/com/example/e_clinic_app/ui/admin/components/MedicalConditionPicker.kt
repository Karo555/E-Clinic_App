package com.example.e_clinic_app.ui.admin.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.e_clinic_app.data.model.MedicalCondition


@Composable
fun MedicalConditionPicker(
    selectedConditions: List<MedicalCondition>,
    onSelectionChanged: (List<MedicalCondition>) -> Unit
) {
    val allConditions = listOf(
        "Allergy",
        "Asthma",
        "Diabetes",
        "Neurological",
        "Cardiac",
        "Autoimmune",
        "Other"
    )
    val conditionTypes = mapOf(
        "Allergy" to listOf(
            "Food",
            "Pollen",
            "Dust",
            "Mold",
            "Medication",
            "Insect Stings",
            "Latex",
            "Other"
        ),
        "Diabetes" to listOf("Type 1", "Type 2", "Gestational", "Other"),
        "Neurological" to listOf(
            "Epilepsy",
            "Multiple Sclerosis",
            "Parkinson's Disease",
            "Migraines",
            "Other"
        ),
        "Cardiac" to listOf(
            "High Blood Pressure",
            "Arrhythmia",
            "Coronary Artery Disease",
            "Heart Failure",
            "Other"
        ),
        "Autoimmune" to listOf(
            "Rheumatoid Arthritis",
            "Lupus",
            "Crohn’s Disease",
            "Celiac Disease",
            "Other"
        )
    )

    val conditionSelections = remember { mutableStateMapOf<String, Boolean>() }
    val typeSelections = remember { mutableStateMapOf<String, String?>() }

    allConditions.forEach { condition ->
        if (conditionSelections[condition] == null) {
            conditionSelections[condition] = selectedConditions.any { it.category == condition }
        }
        if (condition in conditionTypes && typeSelections[condition] == null) {
            val existing = selectedConditions.find { it.category == condition }?.type
            typeSelections[condition] = existing
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
                        if (!it) typeSelections[condition] = null

                        val newList = buildList {
                            conditionSelections.filter { it.value }.forEach { (category, _) ->
                                if (category in conditionTypes) {
                                    typeSelections[category]?.let { type ->
                                        add(MedicalCondition(category, type))
                                    }
                                } else {
                                    add(MedicalCondition(category))
                                }
                            }
                        }
                        onSelectionChanged(newList)
                    }
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(condition)
            }

            if (conditionSelections[condition] == true && condition in conditionTypes) {
                var expanded by remember { mutableStateOf(false) }
                val selectedType = typeSelections[condition] ?: ""

                Box {
                    OutlinedTextField(
                        value = selectedType,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Select type") },
                        trailingIcon = {
                            IconButton(onClick = { expanded = !expanded }) {
                                Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 24.dp)
                    )
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        conditionTypes[condition]?.forEach { type ->
                            DropdownMenuItem(
                                text = { Text(type) },
                                onClick = {
                                    typeSelections[condition] = type
                                    expanded = false

                                    val newList = buildList {
                                        conditionSelections.filter { it.value }
                                            .forEach { (category, _) ->
                                                if (category in conditionTypes) {
                                                    typeSelections[category]?.let { selected ->
                                                        add(MedicalCondition(category, selected))
                                                    }
                                                } else {
                                                    add(MedicalCondition(category))
                                                }
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