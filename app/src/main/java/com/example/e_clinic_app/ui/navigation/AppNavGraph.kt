package com.example.e_clinic_app.ui.navigation

import AdminHomeTabScreen
import ChatTabScreen
import SettingsTabScreen
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.e_clinic_app.backend.home.DoctorHomeViewModel
import com.example.e_clinic_app.backend.home.PatientDashboardViewModel
import com.example.e_clinic_app.ui.admin.GlobalAdminDashboardScreen
import com.example.e_clinic_app.ui.auth.AuthViewModel
import com.example.e_clinic_app.ui.auth.LoginScreen
import com.example.e_clinic_app.ui.auth.RegisterScreen
import com.example.e_clinic_app.ui.auth.ResetPasswordScreen
import com.example.e_clinic_app.ui.firstlogin.DoctorFirstLoginScreen
import com.example.e_clinic_app.ui.firstlogin.EditMedicalInfoScreen
import com.example.e_clinic_app.ui.home.HomeTabScreen
import com.example.e_clinic_app.ui.home.doctor.DoctorHomeTabScreen
import com.example.e_clinic_app.ui.home.patient.PatientHomeTabScreen
import com.example.e_clinic_app.ui.onboarding.MedicalFormStepperScreen
import com.example.e_clinic_app.ui.onboarding.MedicalIntroScreen

/**
 * Configures and initializes the navigation graph for the application.
 *
 * @param navController The NavHostController used to control and manage navigation between composable screens.
 * @param startDestination A string representing the starting destination for the navigation graph.
 * @param patientDashboardViewModel An instance of PatientDashboardViewModel used for managing state related to the patient dashboard.
 * @param doctorHomeViewModel An instance of DoctorHomeViewModel used for managing state related to the doctor dashboard.
 */
@Composable
fun AppNavGraph(
    navController: NavHostController,
    patientDashboardViewModel: PatientDashboardViewModel = PatientDashboardViewModel(),
    doctorHomeViewModel: DoctorHomeViewModel = DoctorHomeViewModel()
) {
    val startDestination = Routes.HOME

    Log.d("AppNavGraph", "Initializing NavHost with startDestination  $startDestination")

    NavHost(navController = navController, startDestination = startDestination) {

        //Home screen
        composable(Routes.HOME) {
            HomeTabScreen(navController = navController)
        }
        // Chat screen
        composable(Routes.CHAT_TAB) {
            ChatTabScreen(navController = navController)
        }
        // Settings screen
        composable(Routes.SETTINGS_TAB) {
            SettingsTabScreen(navController = navController)
        }
        composable(Routes.REGISTER) {
            RegisterScreen(navController = navController, viewModel = AuthViewModel())
        }

        composable(Routes.LOGIN_SCREEN){
            LoginScreen(navController = navController, viewModel = AuthViewModel())
        }

        // Defines a composable function for the Reset Password screen and its navigation logic.
        composable(Routes.RESET_PASSWORD) {
            ResetPasswordScreen(
                navController = navController
            )
        }

        // Defines a composable function for the Medical Intro screen.
        composable(Routes.MEDICAL_INTRO) {
            MedicalIntroScreen(navController = navController)
        }

        // Defines a composable function for the First Login screen and its navigation logic.
        composable(Routes.FIRST_LOGIN) {
            MedicalFormStepperScreen(
                // Navigates to the Home screen and removes the First Login screen from the back stack.
                onFormCompleted = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.FIRST_LOGIN) { inclusive = true }
                    }
                }
            )
        }

        // Defines a composable function for the Doctor First Login screen and its navigation logic.
        composable(Routes.DOCTOR_FIRST_LOGIN) {
            DoctorFirstLoginScreen(
                // Navigates to the Home screen and removes the Doctor First Login screen from the back stack.
                onSubmitSuccess = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.DOCTOR_FIRST_LOGIN) { inclusive = true }
                    }
                }
            )
        }

        // Defines a composable function for the Edit Medical Info screen and its navigation logic.
        composable(Routes.EDIT_MEDICAL_INFO) {
            EditMedicalInfoScreen(
                isEditing = true,
                // Navigates to the Home screen and removes the Edit Medical Info screen from the back stack.
                onSubmitSuccess = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.EDIT_MEDICAL_INFO) { inclusive = true }
                    }
                },
                // Navigates back to the previous screen in the navigation stack.
                onCancel = {
                    navController.popBackStack()
                }
            )
        }

        // TODO  Defines a composable function for the Global Admin Dashboard screen.
        composable(Routes.GLOBAL_ADMIN_DASHBOARD) {
            GlobalAdminDashboardScreen(navController = navController)
        }

        // Defines a composable function for the Institution Admin Dashboard screen.
        composable(Routes.INSTITUTION_ADMIN_DASHBOARD) {
            AdminHomeTabScreen(navController = navController)
        }

        // Defines a composable function for the Patient Dashboard screen.
        composable(Routes.PATIENT_DASHBOARD) {
            PatientHomeTabScreen(navController, patientDashboardViewModel)
        }

        // Defines a composable function for the Doctor Dashboard screen.
        composable(Routes.DOCTOR_DASHBOARD) {
            DoctorHomeTabScreen(navController, doctorHomeViewModel)
        }

    }
}