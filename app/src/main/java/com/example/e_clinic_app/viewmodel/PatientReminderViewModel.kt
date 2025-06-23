package com.example.e_clinic_app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.e_clinic_app.data.model.FollowUpReminder
import com.example.e_clinic_app.data.model.FollowUpStatus
import com.example.e_clinic_app.data.repository.PatientReminderRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.util.Date

/**
 * ViewModel for managing patient follow-up reminders.
 *
 * This ViewModel provides the UI with data and operations related to follow-up reminders
 * for the patient.
 */
class PatientReminderViewModel(
    private val repository: PatientReminderRepository
) : ViewModel() {

    // UI states for reminders
    private val _uiState = MutableStateFlow<RemindersUiState>(RemindersUiState.Loading)
    val uiState: StateFlow<RemindersUiState> = _uiState

    // Status of reminder actions (marking as completed, etc.)
    private val _actionStatus = MutableStateFlow<ActionStatus>(ActionStatus.Idle)
    val actionStatus: StateFlow<ActionStatus> = _actionStatus

    init {
        loadReminders()
    }

    /**
     * Loads reminders from the repository.
     */
    fun loadReminders() {
        viewModelScope.launch {
            _uiState.value = RemindersUiState.Loading
            repository.getPatientRemindersFlow()
                .map { reminders ->
                    if (reminders.isEmpty()) {
                        RemindersUiState.Empty
                    } else {
                        RemindersUiState.Success(
                            activeReminders = reminders.filter { it.status == FollowUpStatus.PENDING },
                            completedReminders = reminders.filter { it.status == FollowUpStatus.COMPLETED },
                            overdueReminders = reminders.filter {
                                it.status == FollowUpStatus.PENDING &&
                                it.dueDate.toDate().before(Date())
                            }
                        )
                    }
                }
                .catch { e ->
                    _uiState.value = RemindersUiState.Error(e.message ?: "Unknown error")
                }
                .collect { state ->
                    _uiState.value = state
                }
        }
    }

    /**
     * Marks a reminder as completed.
     *
     * @param reminderId The ID of the reminder to mark as completed.
     */
    fun markReminderAsCompleted(reminderId: String) {
        viewModelScope.launch {
            _actionStatus.value = ActionStatus.Loading
            try {
                val success = repository.markReminderAsCompleted(reminderId)
                _actionStatus.value = if (success) {
                    ActionStatus.Success("Reminder marked as completed")
                } else {
                    ActionStatus.Error("Failed to mark reminder as completed")
                }
            } catch (e: Exception) {
                _actionStatus.value = ActionStatus.Error(e.message ?: "Unknown error")
            }
        }
    }

    /**
     * Resets the action status to idle.
     */
    fun resetActionStatus() {
        _actionStatus.value = ActionStatus.Idle
    }

    /**
     * Represents the UI state for reminders.
     */
    sealed class RemindersUiState {
        object Loading : RemindersUiState()
        object Empty : RemindersUiState()
        data class Success(
            val activeReminders: List<FollowUpReminder>,
            val completedReminders: List<FollowUpReminder>,
            val overdueReminders: List<FollowUpReminder>
        ) : RemindersUiState()
        data class Error(val message: String) : RemindersUiState()
    }

    /**
     * Represents the status of reminder actions.
     */
    sealed class ActionStatus {
        object Idle : ActionStatus()
        object Loading : ActionStatus()
        data class Success(val message: String) : ActionStatus()
        data class Error(val message: String) : ActionStatus()
    }
}
