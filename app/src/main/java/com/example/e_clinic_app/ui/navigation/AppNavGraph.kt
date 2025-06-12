package com.example.e_clinic_app.ui.navigation

import com.example.e_clinic_app.ui.settings.SettingsTabScreen
import AdminHomeTabScreen
import com.example.e_clinic_app.ui.settings.EditPublicProfileScreen
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.NavType
import com.example.e_clinic_app.backend.home.DoctorHomeViewModel
import com.example.e_clinic_app.backend.home.PatientDashboardViewModel
import com.example.e_clinic_app.ui.home.doctor.PatientDetailScreen
import com.example.e_clinic_app.ui.admin.GlobalAdminDashboardScreen
import com.example.e_clinic_app.ui.auth.AuthScreen
import com.example.e_clinic_app.ui.auth.ResetPasswordScreen
import com.example.e_clinic_app.ui.chat.ChatTabScreen
import com.example.e_clinic_app.ui.chat.ChatDetailScreen
import com.example.e_clinic_app.ui.firstlogin.DoctorFirstLoginScreen
import com.example.e_clinic_app.ui.firstlogin.EditMedicalInfoScreen
import com.example.e_clinic_app.ui.home.HomeTabScreen
import com.example.e_clinic_app.ui.home.doctor.DoctorHomeTabScreen
import com.example.e_clinic_app.ui.home.doctor.SetAvailabilityScreen
import com.example.e_clinic_app.ui.home.patient.PatientHomeTabScreen
import com.example.e_clinic_app.ui.home.patient.VisitsScreen
import com.example.e_clinic_app.ui.onboarding.MedicalFormStepperScreen
import com.example.e_clinic_app.ui.onboarding.MedicalIntroScreen
import com.google.firebase.firestore.FirebaseFirestore
import com.example.e_clinic_app.presentation.viewmodel.AppointmentsViewModel
import com.example.e_clinic_app.presentation.viewmodel.ChatDetailViewModel
import com.example.e_clinic_app.presentation.viewmodel.DoctorAvailabilityViewModel
import com.example.e_clinic_app.presentation.viewmodel.DoctorDetailViewModel
import com.example.e_clinic_app.presentation.viewmodel.PatientDetailViewModel
import com.example.e_clinic_app.presentation.viewmodel.VisitDetailViewModel
import com.example.e_clinic_app.ui.admin.model.InstitutionAdminsScreen
import com.example.e_clinic_app.ui.home.doctor.PatientsScreen
import com.google.firebase.auth.FirebaseAuth
import com.example.e_clinic_app.ui.home.patient.BrowseDoctorsScreen
import com.example.e_clinic_app.ui.home.patient.DoctorDetailScreen
import com.example.e_clinic_app.ui.settings.MyDocumentsScreen
import com.example.e_clinic_app.ui.home.patient.VisitDetailScreen

/**
 * A composable function that defines the navigation graph for the e-clinic application.
 *
 * This function sets up the navigation structure of the app, including routes for authentication,
 * onboarding, dashboards, settings, chat, visits, and other features. It uses the `NavHost` to
 * manage navigation between different screens based on the user's role and actions.
 *
 * The navigation graph includes:
 * - Authentication flow (login, reset password, onboarding).
 * - Role-based dashboards for patients, doctors, and admins.
 * - Chat functionality, including chat list and chat details.
 * - Visits management for patients and doctors.
 * - Profile editing and settings screens.
 * - Doctor and patient-specific features, such as browsing doctors and viewing patient details.
 *
 * @param navController The `NavHostController` used to manage navigation between screens.
 * @param startDestination The initial route to display when the app starts.
 * @param currentUserRole The role of the currently logged-in user (e.g., "Doctor", "Patient", "Admin").
 * @param patientDashboardViewModel The `PatientDashboardViewModel` instance for managing patient dashboard state.
 * @param doctorHomeViewModel The `DoctorHomeViewModel` instance for managing doctor dashboard state.
 */
@Composable
fun AppNavGraph(
    navController: NavHostController,
    startDestination: String,
    currentUserRole: String?,
    patientDashboardViewModel: PatientDashboardViewModel = viewModel(),
    doctorHomeViewModel: DoctorHomeViewModel = viewModel()
) {
    Log.d("AppNavGraph", "Initializing NavHost with startDestination = $startDestination and role = $currentUserRole")

    NavHost(navController = navController, startDestination = startDestination) {

        // Home
        composable(Routes.HOME) {
            HomeTabScreen(navController)
        }

        // Chat list (Chat tab)
        composable(Routes.CHAT_TAB) {
            Log.d("AppNavGraph", "currentUserRole passed to ChatTabScreen: $currentUserRole")
            ChatTabScreen(navController)
        }

        // Settings
        composable(Routes.SETTINGS_TAB) {
            Log.d("AppNavGraph", "currentUserRole passed to SettingsTabScreen: $currentUserRole")
            SettingsTabScreen(
                navController = navController,
                currentUserRole = currentUserRole
            )
        }

        // Auth flow
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
                },
                onNavigateToResetPassword = {
                    navController.navigate(Routes.RESET_PASSWORD)
                }
            )
        }

        // Reset Password
        composable(Routes.RESET_PASSWORD) {
            ResetPasswordScreen(onBackToLogin = { navController.popBackStack() })
        }

        // Onboarding
        composable(Routes.MEDICAL_INTRO) {
            MedicalIntroScreen(navController)
        }
        composable(Routes.FIRST_LOGIN) {
            MedicalFormStepperScreen(onFormCompleted = {
                navController.navigate(Routes.HOME) {
                    popUpTo(Routes.FIRST_LOGIN) { inclusive = true }
                }
            })
        }
        composable(Routes.DOCTOR_FIRST_LOGIN) {
            DoctorFirstLoginScreen(onSubmitSuccess = {
                navController.navigate(Routes.HOME) {
                    popUpTo(Routes.DOCTOR_FIRST_LOGIN) { inclusive = true }
                }
            })
        }
        composable(Routes.EDIT_MEDICAL_INFO) {
            EditMedicalInfoScreen(
                isEditing = true,
                onSubmitSuccess = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.EDIT_MEDICAL_INFO) { inclusive = true }
                    }
                },
                onCancel = { navController.popBackStack() }
            )
        }

        // Admin dashboards
        composable(Routes.GLOBAL_ADMIN_DASHBOARD) {
            GlobalAdminDashboardScreen(navController)
        }
        composable(Routes.INSTITUTION_ADMIN_DASHBOARD) {
            AdminHomeTabScreen(navController)
        }

        composable(Routes.MANAGE_INSTITUTION_ADMINS) {   // <-- New Composable
            InstitutionAdminsScreen(navController)
        }

        // Patient & Doctor dashboards
        composable(Routes.PATIENT_DASHBOARD) {
            PatientHomeTabScreen(navController, patientDashboardViewModel)
        }
        composable(Routes.DOCTOR_DASHBOARD) {
            DoctorHomeTabScreen(navController, doctorHomeViewModel)
        }

        // Doctor detail
        composable(
            route = "${Routes.DOCTOR_DETAIL}/{doctorId}",
            arguments = listOf(navArgument("doctorId") { type = NavType.StringType })
        ) { backStackEntry ->
            val vm: DoctorDetailViewModel = viewModel(
                viewModelStoreOwner = backStackEntry,
                factory = DoctorDetailViewModel.provideFactory(
                    firestore = FirebaseFirestore.getInstance(),
                    savedStateHandle = backStackEntry.savedStateHandle
                )
            )
            DoctorDetailScreen(
                navController = navController,
                viewModel = vm
            )
        }

        // Availability setup
        composable(Routes.SET_AVAILABILITY) {
            val availabilityVM: DoctorAvailabilityViewModel = viewModel()
            SetAvailabilityScreen(navController, availabilityVM)
        }

        // Visits list
        composable(Routes.VISITS) {
            val vm: AppointmentsViewModel = viewModel(
                factory = AppointmentsViewModel.factoryForPatient()
            )
            VisitsScreen(navController, vm)
        }

        // Doctor's own appointments
        composable(Routes.DOCTOR_APPOINTMENTS) {
            val vm: AppointmentsViewModel = viewModel(
                factory = AppointmentsViewModel.factoryForDoctor()
            )
            VisitsScreen(navController = navController, viewModel = vm)
        }

        // Chat detail
        composable(
            route = "${Routes.CHAT_DETAIL}/{pairId}",
            arguments = listOf(navArgument("pairId") { type = NavType.StringType })
        ) { backStackEntry ->
            val vm: ChatDetailViewModel = viewModel(
                viewModelStoreOwner = backStackEntry,
                factory = ChatDetailViewModel.provideFactory(
                    firestore = FirebaseFirestore.getInstance(),
                    pairId = backStackEntry.arguments?.getString("pairId") ?: ""
                )
            )
            ChatDetailScreen(navController, vm)
        }

        // patient details for doctor POV
        composable(
            route = "${Routes.PATIENT_DETAIL}/{patientId}",
            arguments = listOf(navArgument("patientId"){ type = NavType.StringType })
        ) { backStackEntry ->
            val patientId = backStackEntry.arguments!!.getString("patientId")!!
            val vm: PatientDetailViewModel = viewModel(
                factory = PatientDetailViewModel.provideFactory(
                    firestore = FirebaseFirestore.getInstance(),
                    patientId = patientId
                )
            )
            PatientDetailScreen(navController, patientId)
        }

        // Doctor edit profile
        composable(Routes.EDIT_PUBLIC_PROFILE) {
            Log.d("SettingsTabScreen", "currentUserRole: $currentUserRole")
            EditPublicProfileScreen(
                onSubmitSuccess = { navController.popBackStack() },
                onCancel = { navController.popBackStack() }
            )
        }

        composable(Routes.DOCTOR_PATIENTS) {
            PatientsScreen(
                navController = navController,
                doctorId = FirebaseAuth.getInstance().currentUser!!.uid
            )
        }

        // Browse Doctors (patient POV)
        composable(Routes.BROWSE_DOCTORS) {
            BrowseDoctorsScreen(navController)
        }

        composable(Routes.MY_DOCUMENTS) {
            MyDocumentsScreen(navController)
        }

        // Patient visit details
        composable(
            route = "${Routes.VISIT_DETAIL}/{appointmentId}",
            arguments = listOf(navArgument("appointmentId") { type = NavType.StringType })
        ) { backStackEntry ->
                        val appointmentId = backStackEntry.arguments?.getString("appointmentId")!!
                        val vm: VisitDetailViewModel = viewModel(
                                factory = VisitDetailViewModel.provideFactory(
                                        firestore = FirebaseFirestore.getInstance(),
                                        appointmentId = appointmentId
                                            )
                                    )
                        VisitDetailScreen(navController, vm)
                    }
    }
}