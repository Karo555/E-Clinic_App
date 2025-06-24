package com.example.e_clinic_app.ui.navigation

import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.e_clinic_app.data.repository.PatientReminderRepository
import com.example.e_clinic_app.ui.screens.patient.PatientReminderDetailScreen
import com.example.e_clinic_app.ui.screens.patient.PatientRemindersScreen
import com.example.e_clinic_app.viewmodel.PatientReminderViewModel
import com.example.e_clinic_app.viewmodel.PatientReminderViewModelFactory

/**
 * Navigation routes for patient reminders.
 */
object PatientReminderRoutes {
    const val REMINDERS_LIST = "patient_reminders"
    const val REMINDER_DETAIL = "patient_reminders/{reminderId}"

    fun reminderDetail(reminderId: String) = "patient_reminders/$reminderId"
}

/**
 * Adds the patient reminder navigation graph to the NavGraphBuilder.
 *
 * @param navController The NavController to use for navigation.
 */
fun NavGraphBuilder.patientReminderNavGraph(navController: NavController) {
    composable(PatientReminderRoutes.REMINDERS_LIST) {
        val repository = remember { PatientReminderRepository() }
        val viewModelFactory = remember { PatientReminderViewModelFactory(repository) }
        val viewModel: PatientReminderViewModel = viewModel(factory = viewModelFactory)

        PatientRemindersScreen(
            viewModel = viewModel,
            onNavigateToDetail = { reminderId ->
                navController.navigate(PatientReminderRoutes.reminderDetail(reminderId))
            }
        )
    }

    composable(
        route = PatientReminderRoutes.REMINDER_DETAIL,
        arguments = listOf(
            navArgument("reminderId") {
                type = NavType.StringType
            }
        )
    ) { backStackEntry ->
        val reminderId = backStackEntry.arguments?.getString("reminderId") ?: ""

        PatientReminderDetailScreen(
            reminderId = reminderId,
            onBackClick = {
                navController.popBackStack()
            }
        )
    }
}
