package com.example.e_clinic_app.service

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.OneTimeWorkRequestBuilder
import com.example.e_clinic_app.worker.ReminderCheckWorker
import java.util.concurrent.TimeUnit

/**
 * Manager class for scheduling background work related to follow-up reminders.
 *
 * This class is responsible for scheduling periodic checks for upcoming reminders
 * and ensuring that patients receive notifications when reminders are due.
 */
class ReminderWorkManager(private val context: Context) {

    companion object {
        private const val REMINDER_CHECK_WORK_NAME = "reminder_check_work"

        /**
         * Singleton instance of the ReminderWorkManager.
         */
        @Volatile
        private var INSTANCE: ReminderWorkManager? = null

        /**
         * Gets the singleton instance of ReminderWorkManager.
         *
         * @param context Application context.
         * @return The singleton instance.
         */
        fun getInstance(context: Context): ReminderWorkManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: ReminderWorkManager(context.applicationContext).also {
                    INSTANCE = it
                }
            }
        }
    }

    /**
     * Schedules a periodic worker to check for upcoming reminders.
     *
     * This worker will run once per day to check for reminders that are due or overdue.
     */
    fun scheduleReminderChecks() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val reminderCheckRequest = PeriodicWorkRequestBuilder<ReminderCheckWorker>(
            24, TimeUnit.HOURS
        )
        .setConstraints(constraints)
        .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            REMINDER_CHECK_WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            reminderCheckRequest
        )
    }

    /**
     * Immediately checks for upcoming reminders.
     *
     * This can be called when the user logs in or when the app is launched.
     */
    fun checkRemindersNow() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val reminderCheckRequest = OneTimeWorkRequestBuilder<ReminderCheckWorker>()
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(context).enqueue(reminderCheckRequest)
    }

    /**
     * Cancels all scheduled reminder checks.
     *
     * This can be called when the user logs out.
     */
    fun cancelReminderChecks() {
        WorkManager.getInstance(context).cancelUniqueWork(REMINDER_CHECK_WORK_NAME)
    }
}