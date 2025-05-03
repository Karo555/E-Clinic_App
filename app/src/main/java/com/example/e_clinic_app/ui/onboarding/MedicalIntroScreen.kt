package com.example.e_clinic_app.ui.onboarding

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.e_clinic_app.ui.navigation.Routes

@Composable
fun MedicalIntroScreen(navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(48.dp))

        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Medical profile setup",
                style = MaterialTheme.typography.headlineMedium
            )

            Text(
                text = "To ensure your doctor has the most accurate info, we’ll guide you through a short form.",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )

            Text(
                text = "What to have ready:\n1) medical history\n2) medications\n3) known conditions\n " +
                        "\n",
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = "⏱ Estimated time: ~10 minutes\n",
                style = MaterialTheme.typography.bodyLarge
            )
        }

        Button(
            onClick = {
                navController.navigate(Routes.FIRST_LOGIN) {
                    popUpTo(Routes.MEDICAL_INTRO) { inclusive = true }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("I’m Ready")
        }
    }
}