package com.example.e_clinic_app.data.appointment

/**
 * Enum representing the possible statuses of an appointment.
 *
 * This enum is used to track the current state of an appointment in the e-clinic application.
 */
enum class AppointmentStatus {
    /** The appointment is pending and awaiting confirmation. */
    PENDING,
    /** The appointment has been confirmed by the doctor or clinic. */
    CONFIRMED,
    /** The appointment has been cancelled by the patient or doctor. */
    CANCELLED,
    /** The appointment has been completed successfully. */
    COMPLETED;
}