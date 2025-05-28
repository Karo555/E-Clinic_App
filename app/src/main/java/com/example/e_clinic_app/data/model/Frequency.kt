package com.example.e_clinic_app.data.model
/**
 * Enum representing the frequency of medication administration in the e-clinic application.
 *
 * This enum is used to specify how often a medication should be administered.
 */
enum class Frequency {
    /** Once daily administration. */
    ONCE_DAILY,
    /** Twice daily administration. */
    TWICE_DAILY,
    /** Three times daily administration. */
    THREE_TIMES_DAILY,
    /** Four times daily administration. */
    FOUR_TIMES_DAILY,
    /** Weekly administration. */
    WEEKLY,
    /** Administration as needed. */
    AS_NEEDED
}