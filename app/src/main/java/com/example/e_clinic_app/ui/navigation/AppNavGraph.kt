package com.example.e_clinic_app.ui.navigation

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.e_clinic_app.ui.admin.GlobalAdminDashboardScreen
import com.example.e_clinic_app.ui.admin.InstitutionAdminDashboardScreen
import com.example.e_clinic_app.ui.auth.AuthScreen
import com.example.e_clinic_app.ui.firstlogin.DoctorFirstLoginScreen
import com.example.e_clinic_app.ui.firstlogin.FirstLoginScreen
import com.example.e_clinic_app.ui.home.MainScreen
import com.example.e_clinic_app.ui.onboarding.MedicalIntroScreen

@Composable
fun AppNavGraph(navController: NavHostController, startDestination: String) {
    Log.d("AppNavGraph", "Initializing NavHost with startDestination = $startDestination")

    NavHost(navController = navController, startDestination = startDestination) {

        composable(Routes.AUTH) {
            AuthScreen(
                onNavigateToFirstLogin = {
                    navController.navigate(Routes.MEDICAL_INTRO) {
                        popUpTo(Routes.AUTH) { inclusive = true }
                    }
                },
                onNavigateToDoctorFirstLogin = {
                    navController.navigate(Routes.DOCTOR_FIRST_LOGIN) {
                        popUpTo(Routes.AUTH) { inclusive = true }
                    }
                },
                onNavigateToHome = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.AUTH) { inclusive = true }
                    }
                },
                onNavigateToGlobalAdminDashboard = {
                    navController.navigate(Routes.GLOBAL_ADMIN_DASHBOARD) {
                        popUpTo(Routes.AUTH) { inclusive = true }
                    }
                },
                onNavigateToInstitutionAdminDashboard = {
                    navController.navigate(Routes.INSTITUTION_ADMIN_DASHBOARD) {
                        popUpTo(Routes.AUTH) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.MEDICAL_INTRO) {
            MedicalIntroScreen(navController = navController)
        }

        composable(Routes.FIRST_LOGIN) {
            FirstLoginScreen(
                onSubmitSuccess = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.FIRST_LOGIN) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.DOCTOR_FIRST_LOGIN) {
            DoctorFirstLoginScreen(
                onSubmitSuccess = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.DOCTOR_FIRST_LOGIN) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.HOME) {
            MainScreen(navController = navController)
        }

        composable(Routes.EDIT_MEDICAL_INFO) {
            FirstLoginScreen(
                isEditing = true,
                onSubmitSuccess = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.EDIT_MEDICAL_INFO) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.GLOBAL_ADMIN_DASHBOARD) {
            GlobalAdminDashboardScreen(navController = navController)
        }

        composable(Routes.INSTITUTION_ADMIN_DASHBOARD) {
            InstitutionAdminDashboardScreen(navController = navController)
        }
    }
}