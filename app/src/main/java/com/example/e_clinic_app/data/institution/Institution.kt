package com.example.e_clinic_app.data.institution

import android.provider.ContactsContract.CommonDataKinds.Email
import java.util.UUID
/**
 * Represents an institution in the e-clinic application.
 *
 * This data class contains details about an institution, including its name, location,
 * contact information, and additional metadata.
 *
 * @property id The unique identifier for the institution.
 * @property name The name of the institution.
 * @property city The city where the institution is located.
 * @property address The physical address of the institution.
 * @property phone The contact phone number of the institution.
 * @property email The contact email address of the institution.
 * @property domain The domain or specialization of the institution.
 * @property extraDetails A map containing additional details about the institution.
 */

data class Institution (
    val id:UUID,
    val name: String,
    val city: String,
    val address: String,
    val phone: String,
    val email: Email,
    val domain: String,
    val extraDetails: Map<String,Any>
)