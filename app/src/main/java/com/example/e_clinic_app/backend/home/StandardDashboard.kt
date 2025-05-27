package com.example.e_clinic_app.backend.home

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import com.example.e_clinic_app.data.appointment.Appointment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

/**
 * Abstract base class for managing dashboard-related functionality.
 *
 * This class provides common properties and methods for managing user-specific
 * data, such as appointments, and interacting with Firebase Firestore.
 * It is intended to be extended by specific dashboard ViewModels.
 */
abstract class StandardDashboard : ViewModel() {

    /** The Firestore instance used for database operations. Defaults to [FirebaseFirestore.getInstance]. */
    open val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    /** The currently authenticated user. */
    val user = FirebaseAuth.getInstance().currentUser
    /** The unique identifier (UID) of the currently authenticated user. */
    val userId = user?.uid
    /** A mutable list of appointments associated with the user. */
    val appointmentsList = mutableStateListOf<Appointment>()
    /**
     * Abstract method for fetching appointments from Firestore.
     *
     * This method must be implemented by subclasses to define the logic
     * for retrieving appointments specific to the dashboard's requirements.
     *
     * @param firestore The Firestore instance used for database operations.
     */
    abstract fun fetchAppointments(firestore: FirebaseFirestore)
}