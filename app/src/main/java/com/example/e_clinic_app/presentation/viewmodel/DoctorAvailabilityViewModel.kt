package com.example.e_clinic_app.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

/**
 * ViewModel for managing a doctor's weekly availability schedule in the e-clinic application.
 *
 * This ViewModel provides functionality to load and save the weekly schedule of a doctor
 * from Firestore. The schedule is represented as a map of day names to a list of time slots.
 * Also manages the session duration and break time between appointments.
 *
 * @property firestore The Firestore instance used for database operations.
 */
class DoctorAvailabilityViewModel(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) : ViewModel() {

    private val _schedule = MutableStateFlow<Map<String, List<String>>>(emptyMap())
    /** A state flow containing the weekly schedule: day name (e.g., "Monday") to list of "HH:mm" slots. */
    val schedule: StateFlow<Map<String, List<String>>> = _schedule.asStateFlow()

    private val _sessionDuration = MutableStateFlow(30) // Default to 30 minutes
    /** A state flow containing the session duration in minutes. */
    val sessionDuration: StateFlow<Int> = _sessionDuration.asStateFlow()

    private val _breakDuration = MutableStateFlow(0) // Default to 0 minutes (no break)
    /** A state flow containing the break duration in minutes between appointments. */
    val breakDuration: StateFlow<Int> = _breakDuration.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    /** A state flow indicating whether the schedule is currently being loaded or saved. */
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    /** A state flow containing any error messages encountered during operations. */
    val error: StateFlow<String?> = _error.asStateFlow()

    private val currentUserId: String?
        /** The unique identifier of the currently authenticated user (doctor). */
        get() = Firebase.auth.currentUser?.uid

    /**
     * Sets the session duration for appointments.
     *
     * @param minutes The duration of each appointment slot in minutes.
     */
    fun setSessionDuration(minutes: Int) {
        _sessionDuration.value = minutes
    }

    /**
     * Sets the break duration between appointments.
     *
     * @param minutes The duration of break between appointment slots in minutes.
     */
    fun setBreakDuration(minutes: Int) {
        _breakDuration.value = minutes
    }

    /**
     * Loads the current weekly schedule, session duration, and break duration of the doctor from Firestore.
     *
     * The schedule is fetched from the `doctorInfo` document in the `profile` collection
     * of the authenticated user's Firestore document.
     */
    fun loadSchedule() {
        val uid = currentUserId ?: run {
            _error.value = "Not authenticated"
            return
        }
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val docSnap = firestore.collection("users")
                    .document(uid)
                    .collection("profile")
                    .document("doctorInfo")
                    .get().await()

                // Load session duration
                val duration = docSnap.getLong("sessionDuration")?.toInt() ?: 30
                _sessionDuration.value = duration

                // Load break duration
                val breakTime = docSnap.getLong("breakDuration")?.toInt() ?: 0
                _breakDuration.value = breakTime

                @Suppress("UNCHECKED_CAST")
                val raw = docSnap.get("weeklySchedule") as? Map<*, *>
                val parsed = raw?.mapNotNull { entry ->
                    val day = entry.key as? String
                    val times = (entry.value as? List<*>)
                        ?.filterIsInstance<String>()
                    if (day != null && times != null) day to times else null
                }?.toMap() ?: emptyMap()
                _schedule.value = parsed
            } catch (e: Exception) {
                Log.e("DocAvailVM", "Error loading schedule", e)
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Saves the updated weekly schedule, session duration, and break duration of the doctor to Firestore.
     *
     * The schedule is saved to the `doctorInfo` document in the `profile` collection
     * of the authenticated user's Firestore document. The availability flag is also updated
     * based on whether the schedule is empty or not.
     *
     * @param updated The updated weekly schedule to save.
     */
    fun saveSchedule(updated: Map<String, List<String>>) {
        val uid = currentUserId ?: run {
            _error.value = "Not authenticated"
            return
        }
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val profileRef = firestore.collection("users")
                    .document(uid)
                    .collection("profile")
                    .document("doctorInfo")
                // Update availability flag, session duration, break duration, and schedule map
                val availabilityFlag = updated.isNotEmpty()
                profileRef.update(
                    mapOf(
                        "availability" to availabilityFlag,
                        "sessionDuration" to _sessionDuration.value,
                        "breakDuration" to _breakDuration.value,
                        "weeklySchedule" to updated
                    )
                ).await()
                // Reflect back to state
                _schedule.value = updated
            } catch (e: Exception) {
                Log.e("DocAvailVM", "Error saving schedule", e)
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }
}
