package com.example.e_clinic_app.worker

import android.app.Application
import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.e_clinic_app.data.model.FollowUpReminder
import com.example.e_clinic_app.data.model.FollowUpStatus
import com.example.e_clinic_app.service.ReminderNotificationService
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await

/**
 * Worker that periodically checks for upcoming follow-up reminders and sends notifications.
 *
 * This worker is scheduled to run daily to check for reminders that are due today or overdue
 * and notify the patient.
 */
class ReminderCheckWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    private val firestore = FirebaseFirestore.getInstance()
    private val notificationService = ReminderNotificationService(appContext as Application)
    override suspend fun doWork(): Result {
        val currentUserId = Firebase.auth.currentUser?.uid ?: return Result.failure()

        return try {
            // Fetch upcoming and overdue reminders for the current patient
            val reminders = firestore.collection("followUpReminders")
                .whereEqualTo("patientId", currentUserId)
                .whereEqualTo("status", FollowUpStatus.PENDING.name)
                .orderBy("dueDate", Query.Direction.ASCENDING)
                .get()
                .await()
                .documents
                .mapNotNull { doc ->
                    val reminder = doc.toObject(FollowUpReminder::class.java)
                    if (reminder != null && reminder.id.isEmpty()) {
                        reminder.copy(id = doc.id)
                    } else {
                        reminder
                    }
                }

            // Check reminders and send notifications if needed
            notificationService.checkAndNotifyReminders(reminders)

            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}
