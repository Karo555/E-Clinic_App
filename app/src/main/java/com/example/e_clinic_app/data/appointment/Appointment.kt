package com.example.e_clinic_app.data.appointment

import com.google.firebase.Timestamp
/**
 * Represents an appointment in the e-clinic application.
 *
 * This data class contains details about an appointment, including the associated
 * doctor, patient, date, status, and any additional preparation requirements.
 *
 * @property id The unique identifier for the appointment.
 * @property doctorId The unique identifier of the doctor associated with the appointment.
 * @property patientId The unique identifier of the patient associated with the appointment.
 * @property date The date and time of the appointment as a [Timestamp].
 * @property status The current status of the appointment (e.g., PENDING, CONFIRMED).
 * @property doctorFirstName The first name of the doctor associated with the appointment.
 * @property doctorLastName The last name of the doctor associated with the appointment.
 * @property patientFirstName The first name of the patient associated with the appointment.
 * @property patientLastName The last name of the patient associated with the appointment.
 * @property fastingRequired Indicates whether fasting is required for the appointment.
 * @property additionalPrep Any additional preparation instructions for the appointment.
 */
data class Appointment(
    val id: String = "",
    val doctorId: String = "",
    val patientId: String = "",
    val date: Timestamp = Timestamp.now(),
    val status: AppointmentStatus = AppointmentStatus.PENDING,
    val doctorFirstName: String = "",
    val doctorLastName: String = "",
    val patientFirstName: String = "",
    val patientLastName: String = "",
    val fastingRequired: Boolean = false,
    val additionalPrep: String = "",
    val prescriptions: List<com.example.e_clinic_app.data.model.Prescription> = emptyList()
)