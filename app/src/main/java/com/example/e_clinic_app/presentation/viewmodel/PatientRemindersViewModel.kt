package com.example.e_clinic_app.presentation.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.e_clinic_app.data.model.FollowUpReminder
import com.example.e_clinic_app.data.model.FollowUpStatus
import com.example.e_clinic_app.service.ReminderNotificationService
import com.google.firebase.Timestamp
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

/**
 * ViewModel for managing a patient's follow-up reminders.
 *
 * This ViewModel provides functionality for patients to view, sort, and manage their
 * follow-up reminders created by doctors.
 */
class PatientRemindersViewModel(
    application: Application,
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) : AndroidViewModel(application) {

    private val _reminders = MutableStateFlow<List<FollowUpReminder>>(emptyList())
    /** A state flow containing the list of reminders for the patient. */
    val reminders: StateFlow<List<FollowUpReminder>> = _reminders.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    /** A state flow indicating whether data is currently being loaded. */
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    /** A state flow containing any error messages encountered during data loading. */
    val error: StateFlow<String?> = _error.asStateFlow()

    private val notificationService = ReminderNotificationService(application)

    private val currentPatientId: String?
        /** The unique identifier of the currently authenticated patient. */
        get() = Firebase.auth.currentUser?.uid

    private val _allReminders = MutableStateFlow<List<FollowUpReminder>>(emptyList())

    init {
        loadReminders()
    }

    /**
     * Loads follow-up reminders for the current patient from Firestore.
     *
     * This method fetches all reminders where the patientId matches the current user's ID,
     * sorted by due date in ascending order (most urgent first).
     */
    fun loadReminders() {
        val patientId = currentPatientId ?: return
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val remindersList = firestore.collection("followUpReminders")
                    .whereEqualTo("patientId", patientId)
                    .orderBy("dueDate", Query.Direction.ASCENDING)
                    .get()
                    .await()
                    .documents
                    .mapNotNull { doc ->
                        val reminder = doc.toObject(FollowUpReminder::class.java)
                        // Ensure the ID is set from the document ID
                        if (reminder != null && reminder.id.isEmpty()) {
                            reminder.copy(id = doc.id)
                        } else {
                            reminder
                        }
                    }

                _allReminders.value = remindersList
                _reminders.value = remindersList
                _isLoading.value = false

                // Check for reminders that need notifications
                notificationService.checkAndNotifyReminders(remindersList)
            } catch (e: Exception) {
                Log.e("PatientRemindersVM", "Error loading reminders", e)
                _error.value = "Failed to load reminders: ${e.message}"
                _isLoading.value = false
            }
        }
    }

    /**
     * Marks a follow-up reminder as completed.
     *
     * @param reminderId The ID of the reminder to mark as completed.
     */
    fun markReminderAsCompleted(reminderId: String) {
        viewModelScope.launch {
            try {
                val reminderRef = firestore.collection("followUpReminders").document(reminderId)

                // Update the reminder status to COMPLETED and set the completedAt timestamp
                reminderRef.update(
                    mapOf(
                        "status" to FollowUpStatus.COMPLETED.name,
                        "completedAt" to Timestamp.now()
                    )
                ).await()

                // Also update the local copy to reflect changes immediately
                val updatedList = _reminders.value.map { reminder ->
                    if (reminder.id == reminderId) {
                        reminder.copy(
                            status = FollowUpStatus.COMPLETED,
                            completedAt = Timestamp.now()
                        )
                    } else {
                        reminder
                    }
                }
                _reminders.value = updatedList

                // Reload reminders to ensure sync with server
                loadReminders()
            } catch (e: Exception) {
                Log.e("PatientRemindersVM", "Error marking reminder as completed", e)
                _error.value = "Failed to update reminder: ${e.message}"
            }
        }
    }

    /**
     * Filters reminders by status.
     *
     * @param status The status to filter by (null for all reminders).
     */
    fun filterRemindersByStatus(status: FollowUpStatus?) {
        if (status == null) {
            _reminders.value = _allReminders.value
            return
        }

        _reminders.value = _allReminders.value.filter { it.status == status }
    }

    /**
     * Creates a new follow-up reminder.
     *
     * @param description The description of the reminder.
     * @param dueDate The due date of the reminder.
     */
    fun createFollowUpReminder(description: String, dueDate: Timestamp) {
        viewModelScope.launch {
            try {
                val newReminder = FollowUpReminder(
                    id = "", // Let the backend/database assign the ID
                    description = description,
                    dueDate = dueDate,
                    status = FollowUpStatus.PENDING,
                    completedAt = null
                )
                firestore.collection("followUpReminders")
                    .add(newReminder)
                    .await()

                // Refresh the reminders list
                loadReminders()
            } catch (e: Exception) {
                _error.value = "Failed to create reminder: ${e.message}"
            }
        }
    }
}