package com.example.e_clinic_app.data.model
/**
 * Enum representing the units of dosage for medications in the e-clinic application.
 *
 * This enum is used to specify the measurement units for medication dosages.
 */
enum class DosageUnit {
    /** Milligrams of active ingredient. */
    MG,
    /** Milliliters of liquid formulation. */
    ML,
    /** Tablet count. */
    TAB,
    /** Drops (e.g., for eye or ear medications). */
    DROP,
    /** Inhaler puffs. */
    PUFF
}