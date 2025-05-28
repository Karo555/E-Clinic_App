package com.example.e_clinic_app.helpers
/**
 * Builds a unique chat ID for two users based on their unique identifiers.
 *
 * This function takes two user IDs, sorts them lexicographically, and joins them
 * with an underscore to create a consistent and unique chat ID.
 *
 * @param uidA The unique identifier of the first user.
 * @param uidB The unique identifier of the second user.
 * @return A unique chat ID in the format "uidA_uidB" (sorted lexicographically).
 */
fun buildChatId(uidA: String, uidB: String): String {
    return listOf(uidA, uidB).sorted().joinToString("_")
}
