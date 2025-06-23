package com.example.e_clinic_app.data.model

import com.google.firebase.Timestamp

/**
 * Represents a follow-up reminder for a patient in the e-clinic application.
 *
 * This data class contains details about a follow-up reminder, including its unique identifier,
 * the associated patient and doctor, due date, description, status, and creation date.
 *
 * @property id The unique identifier for the follow-up reminder.
 * @property patientId The unique identifier of the patient associated with the reminder.
 * @property doctorId The unique identifier of the doctor who created the reminder.
 * @property dueDate The date by which the follow-up should occur.
 * @property description A description of what needs to be followed up on.
 * @property status The current status of the follow-up reminder (e.g., PENDING, COMPLETED).
 * @property createdAt The date and time when the reminder was created.
 * @property completedAt The date and time when the reminder was marked as completed (if applicable).
 */
data class FollowUpReminder(
    val id: String = "",
    val patientId: String = "",
    val doctorId: String = "",
    val dueDate: Timestamp = Timestamp.now(),
    val description: String = "",
    val status: FollowUpStatus = FollowUpStatus.PENDING,
    val createdAt: Timestamp = Timestamp.now(),
    val completedAt: Timestamp? = null
)

/**
 * Represents the status of a follow-up reminder.
 */
enum class FollowUpStatus {
    PENDING,
    COMPLETED,
    CANCELLED
}

/**
 * Represents a care gap identified for a patient.
 *
 * @property id The unique identifier for the care gap.
 * @property patientId The unique identifier of the patient with the care gap.
 * @property doctorId The unique identifier of the doctor who identified the care gap.
 * @property description A description of the care gap.
 * @property recommendedAction The recommended action to address the care gap.
 * @property priority The priority level of addressing the care gap.
 * @property createdAt The date and time when the care gap was identified.
 * @property resolvedAt The date and time when the care gap was resolved (if applicable).
 * @property status The current status of the care gap.
 */
data class CareGap(
    val id: String = "",
    val patientId: String = "",
    val doctorId: String = "",
    val description: String = "",
    val recommendedAction: String = "",
    val priority: CareGapPriority = CareGapPriority.MEDIUM,
    val createdAt: Timestamp = Timestamp.now(),
    val resolvedAt: Timestamp? = null,
    val status: CareGapStatus = CareGapStatus.OPEN
)

/**
 * Represents the priority level of a care gap.
 */
enum class CareGapPriority {
    LOW,
    MEDIUM,
    HIGH
}

/**
 * Represents the status of a care gap.
 */
enum class CareGapStatus {
    OPEN,
    RESOLVED,
    IN_PROGRESS
}
