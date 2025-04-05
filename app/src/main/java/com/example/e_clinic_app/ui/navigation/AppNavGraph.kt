package com.example.e_clinic_app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.e_clinic_app.ui.auth.AuthScreen
import com.example.e_clinic_app.ui.firstlogin.FirstLoginScreen
import com.example.e_clinic_app.ui.firstlogin.DoctorFirstLoginScreen
import com.example.e_clinic_app.ui.home.MainScreen


@Composable
fun AppNavGraph(navController: NavHostController, startDestination: String) {
    NavHost(navController = navController, startDestination = startDestination) {
        composable(Routes.AUTH) {
            AuthScreen(
                onNavigateToFirstLogin = {
                    navController.navigate(Routes.FIRST_LOGIN) {
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
                        popUpTo(0) { inclusive = true }  // clears everything
                        launchSingleTop = true
                    }
                }
            )
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
            MainScreen()
        }
    }
}