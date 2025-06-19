package com.example.e_clinic_app.ui.navigation

import AdminHomeTabScreen
import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.e_clinic_app.backend.home.DoctorHomeViewModel
import com.example.e_clinic_app.backend.home.PatientDashboardViewModel
import com.example.e_clinic_app.presentation.viewmodel.AppointmentDetailViewModel
import com.example.e_clinic_app.presentation.viewmodel.AppointmentsViewModel
import com.example.e_clinic_app.presentation.viewmodel.ChatDetailViewModel
import com.example.e_clinic_app.presentation.viewmodel.DoctorAvailabilityViewModel
import com.example.e_clinic_app.presentation.viewmodel.DoctorDetailViewModel
import com.example.e_clinic_app.presentation.viewmodel.PatientDetailViewModel
import com.example.e_clinic_app.presentation.viewmodel.PrescriptionsViewModel
import com.example.e_clinic_app.presentation.viewmodel.UserViewModel
import com.example.e_clinic_app.presentation.viewmodel.VisitDetailViewModel
import com.example.e_clinic_app.ui.admin.GlobalAdminDashboardScreen
import com.example.e_clinic_app.ui.admin.model.InstitutionAdminsScreen
import com.example.e_clinic_app.ui.auth.AuthScreen
import com.example.e_clinic_app.ui.auth.ResetPasswordScreen
import com.example.e_clinic_app.ui.chat.ChatDetailScreen
import com.example.e_clinic_app.ui.chat.ChatTabScreen
import com.example.e_clinic_app.ui.firstlogin.DoctorFirstLoginScreen
import com.example.e_clinic_app.ui.firstlogin.EditMedicalInfoScreen
import com.example.e_clinic_app.ui.home.doctor.AppointmentDetailScreen
import com.example.e_clinic_app.ui.home.doctor.DoctorAppointmentsScreen
import com.example.e_clinic_app.ui.home.doctor.DoctorHomeTabScreen
import com.example.e_clinic_app.ui.home.doctor.PatientDetailScreen
import com.example.e_clinic_app.ui.home.doctor.PatientsScreen
import com.example.e_clinic_app.ui.prescriptions.PrescriptionsScreen
import com.example.e_clinic_app.ui.home.doctor.SetAvailabilityScreen
import com.example.e_clinic_app.ui.home.patient.BrowseDoctorsScreen
import com.example.e_clinic_app.ui.home.patient.DoctorDetailScreen
import com.example.e_clinic_app.ui.home.patient.PatientHomeTabScreen
import com.example.e_clinic_app.ui.home.patient.VisitDetailScreen
import com.example.e_clinic_app.ui.home.patient.VisitsScreen
import com.example.e_clinic_app.ui.onboarding.MedicalFormStepperScreen
import com.example.e_clinic_app.ui.onboarding.MedicalIntroScreen
import com.example.e_clinic_app.ui.prescriptions.PrescriptionDetailScreen
import com.example.e_clinic_app.ui.settings.EditPublicProfileScreen
import com.example.e_clinic_app.ui.settings.MyDocumentsScreen
import com.example.e_clinic_app.ui.settings.SettingsTabScreen
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

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
 * @param patientDashboardViewModel The `PatientDashboardViewModel` instance for managing patient dashboard state.
 * @param doctorHomeViewModel The `DoctorHomeViewModel` instance for managing doctor dashboard state.
 */
@Composable
fun AppNavGraph(
    navController: NavHostController,
    startDestination: String,
    patientDashboardViewModel: PatientDashboardViewModel = viewModel(),
    doctorHomeViewModel: DoctorHomeViewModel = viewModel()
) {
    Log.d("AppNavGraph", "AppNavGraph COMPOSABLE recomposed")
    Log.d("AppNavGraph", "Initializing NavHost with startDestination = $startDestination")

    NavHost(navController = navController, startDestination = startDestination) {
        Log.d("AppNavGraph", "NavHost lambda recomposed")

        // Home
        composable(Routes.HOME) {
            // Dynamically show the correct dashboard based on user role
            val userViewModel: UserViewModel =
                viewModel()
            val currentUserRole by userViewModel.role.collectAsState()
            when (currentUserRole) {
                "Doctor" -> DoctorHomeTabScreen(
                    navController = navController,
                    viewModel = doctorHomeViewModel
                )

                "Patient" -> PatientHomeTabScreen(
                    navController = navController,
                    viewModel = patientDashboardViewModel
                )

                else -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
        }

        // Chat list (Chat tab)
        composable(Routes.CHAT_TAB) {
            ChatTabScreen(navController)
        }

        // Settings
        composable(Routes.SETTINGS_TAB) {
            Log.d("AppNavGraph", "Navigating to SettingsTabScreen")
            SettingsTabScreen(navController = navController)
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

        // Doctor's all appointments (historic and upcoming, grouped by month)
        composable(Routes.DOCTOR_APPOINTMENTS) {
            DoctorAppointmentsScreen(navController)
        }

        // Doctor appointment detail (doctor POV)
        composable(
            route = "${Routes.APPOINTMENT_DETAIL}/{appointmentId}",
            arguments = listOf(navArgument("appointmentId") { type = NavType.StringType })
        ) { backStackEntry ->
            val appointmentId = backStackEntry.arguments?.getString("appointmentId")!!
            val vm: AppointmentDetailViewModel =
                viewModel(
                    factory = AppointmentDetailViewModel.provideFactory(
                        firestore = FirebaseFirestore.getInstance(),
                        appointmentId = appointmentId
                    )
                )
            AppointmentDetailScreen(
                navController = navController,
                viewModel = vm,
            )
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
            arguments = listOf(navArgument("patientId") { type = NavType.StringType })
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
        // Prescription screen

        composable(Routes.PRESCRIPTIONS) {
            PrescriptionsScreen(navController, viewModel = PrescriptionsViewModel())

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

        composable(
            route = "prescriptionDetail/{prescriptionId}",
            arguments = listOf(navArgument("prescriptionId") { type = NavType.StringType })
        ) { backStackEntry ->
            val prescriptionId = backStackEntry.arguments?.getString("prescriptionId")
            val viewModel: PrescriptionsViewModel = viewModel()
            val prescription = viewModel.prescriptions.collectAsState().value
                .find { it.id == prescriptionId }

            if (prescription != null) {
                PrescriptionDetailScreen(
                    prescription = prescription,
                    navController = navController,
                    prescriptionsViewModel = viewModel
                )
            } else {
                // Handle missing prescription gracefully
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Prescription not found.")
                }
            }
        }
    }
}