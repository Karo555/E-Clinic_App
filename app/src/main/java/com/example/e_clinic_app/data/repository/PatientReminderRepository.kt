package com.example.e_clinic_app.data.repository

import com.example.e_clinic_app.data.model.FollowUpReminder
import com.example.e_clinic_app.data.model.FollowUpStatus
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import com.google.firebase.Timestamp
import kotlinx.coroutines.channels.awaitClose

/**
 * Repository for managing patient follow-up reminders.
 *
 * This repository provides methods for retrieving, updating, and managing
 * follow-up reminders for patients.
 */
class PatientReminderRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val remindersCollection = firestore.collection("followUpReminders")

    /**
     * Gets the current user ID or null if the user is not authenticated.
     */
    private fun getCurrentUserId(): String? {
        return Firebase.auth.currentUser?.uid
    }

    /**
     * Gets a flow of follow-up reminders for the current patient.
     *
     * @return A flow that emits lists of follow-up reminders.
     */
    fun getPatientRemindersFlow(): Flow<List<FollowUpReminder>> = callbackFlow {
        val userId = getCurrentUserId() ?: return@callbackFlow

        val listenerRegistration = remindersCollection
            .whereEqualTo("patientId", userId)
            .orderBy("dueDate", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    return@addSnapshotListener
                }

                val reminders = snapshot?.documents?.mapNotNull { doc ->
                    val reminder = doc.toObject(FollowUpReminder::class.java)
                    if (reminder != null && reminder.id.isEmpty()) {
                        reminder.copy(id = doc.id)
                    } else {
                        reminder
                    }
                } ?: emptyList()

                trySend(reminders)
            }

        awaitClose {
            listenerRegistration.remove()
        }
    }

    /**
     * Gets all reminders for the current patient.
     *
     * @return A list of follow-up reminders.
     */
    suspend fun getPatientReminders(): List<FollowUpReminder> {
        val userId = getCurrentUserId() ?: return emptyList()

        return remindersCollection
            .whereEqualTo("patientId", userId)
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
    }

    /**
     * Marks a reminder as completed.
     *
     * @param reminderId The ID of the reminder to mark as completed.
     * @return True if the operation was successful, false otherwise.
     */
    suspend fun markReminderAsCompleted(reminderId: String): Boolean {
        return try {
            remindersCollection.document(reminderId)
                .update(
                    mapOf(
                        "status" to FollowUpStatus.COMPLETED.name,
                        "completedAt" to Timestamp.now()
                    )
                )
                .await()
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Gets a reminder by ID.
     *
     * @param reminderId The ID of the reminder to get.
     * @return The reminder, or null if not found.
     */
    suspend fun getReminderById(reminderId: String): FollowUpReminder? {
        return try {
            val doc = remindersCollection.document(reminderId).get().await()
            if (doc.exists()) {
                val reminder = doc.toObject(FollowUpReminder::class.java)
                if (reminder != null && reminder.id.isEmpty()) {
                    reminder.copy(id = doc.id)
                } else {
                    reminder
                }
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
}
