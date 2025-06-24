package com.example.e_clinic_app.service

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.e_clinic_app.MainActivity
import com.example.e_clinic_app.R
import com.example.e_clinic_app.data.model.FollowUpReminder
import com.example.e_clinic_app.data.model.FollowUpStatus
import java.util.Date

/**
 * Service for handling follow-up reminder notifications.
 *
 * This class is responsible for checking reminder due dates and sending notifications
 * to the patient when reminders are due or overdue.
 */
class ReminderNotificationService(private val application: Application) {

    companion object {
        private const val CHANNEL_ID = "follow_up_reminders"
        private const val CHANNEL_NAME = "Follow-up Reminders"
        private const val CHANNEL_DESCRIPTION = "Notifications for follow-up reminders"
    }

    init {
        createNotificationChannel()
    }

    /**
     * Creates the notification channel for Android 8.0 (API level 26) and higher.
     */
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = CHANNEL_DESCRIPTION
            }
            val notificationManager = application.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    /**
     * Checks a list of reminders and sends notifications for those that are due or overdue.
     *
     * @param reminders The list of reminders to check.
     */
    fun checkAndNotifyReminders(reminders: List<FollowUpReminder>) {
        val now = Date()

        reminders.forEach { reminder ->
            if (reminder.status == FollowUpStatus.PENDING) {
                val dueDate = reminder.dueDate.toDate()

                // Check if the reminder is due today or overdue
                if (dueDate.before(now) || isSameDay(dueDate, now)) {
                    val isOverdue = dueDate.before(now)
                    sendReminderNotification(reminder, isOverdue)
                }
            }
        }
    }

    /**
     * Sends a notification for a specific reminder.
     *
     * @param reminder The reminder to send a notification for.
     * @param isOverdue Whether the reminder is overdue.
     */
    private fun sendReminderNotification(reminder: FollowUpReminder, isOverdue: Boolean) {
        val intent = Intent(application, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("NAVIGATE_TO", "patient_reminders")
        }

        val pendingIntent = PendingIntent.getActivity(
            application,
            reminder.id.hashCode(),
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val title = if (isOverdue) "Overdue Follow-up Reminder" else "Follow-up Reminder Due"
        val textPrefix = if (isOverdue) "Your follow-up is overdue: " else "Your follow-up is due today: "

        val notification = NotificationCompat.Builder(application, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground) // Replace with an existing drawable resource
            .setContentTitle(title)
            .setContentText("$textPrefix${reminder.description}")
            .setStyle(NotificationCompat.BigTextStyle().bigText("$textPrefix${reminder.description}"))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        try {
            NotificationManagerCompat.from(application).notify(reminder.id.hashCode(), notification)
        } catch (e: SecurityException) {
            // Handle the case where notification permission is not granted
            // This can happen on Android 13+ if the app doesn't have POST_NOTIFICATIONS permission
        }
    }

    /**
     * Checks if two dates are on the same day.
     *
     * @param date1 The first date.
     * @param date2 The second date.
     * @return True if the dates are on the same day, false otherwise.
     */
    private fun isSameDay(date1: Date, date2: Date): Boolean {
        val cal1 = java.util.Calendar.getInstance().apply { time = date1 }
        val cal2 = java.util.Calendar.getInstance().apply { time = date2 }

        return cal1.get(java.util.Calendar.YEAR) == cal2.get(java.util.Calendar.YEAR) &&
               cal1.get(java.util.Calendar.DAY_OF_YEAR) == cal2.get(java.util.Calendar.DAY_OF_YEAR)
    }
}
