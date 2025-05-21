package com.example.e_clinic_app.helpers

fun buildChatId(uidA: String, uidB: String): String {
    return listOf(uidA, uidB).sorted().joinToString("_")
}
