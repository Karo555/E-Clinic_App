package com.example.e_clinic_app.ui.navigation

object Routes {
    //auth and onboarding
    const val AUTH = "auth"
    const val MEDICAL_INTRO = "medical_intro"
    const val FIRST_LOGIN = "first_login"
    const val DOCTOR_FIRST_LOGIN = "doctor_first_login"

    //post log in
    const val HOME = "home"
    const val EDIT_MEDICAL_INFO = "edit_medical_info"

    //reset
    const val RESET_PASSWORD = "reset_password"

    //admin dashboards
    const val GLOBAL_ADMIN_DASHBOARD = "global_admin_dashboard"
    const val INSTITUTION_ADMIN_DASHBOARD = "institution_admin_dashboard"

    //patient dashboard
    const val PATIENT_DASHBOARD = "patient_dashboard"

    //doctor dashboard
    const val DOCTOR_DASHBOARD = "doctor_dashboard"

    // doctor detail screen
    const val DOCTOR_DETAIL = "doctor_detail"

    //tabs for nested navigation
    const val CHAT_TAB = "chat_tab"
    const val VISITS_TAB = "visits_tab"
    const val SETTINGS_TAB = "settings_tab"

    //setting availability
    const val SET_AVAILABILITY = "set_availability"

    // Patient & doctor visits screen
    const val VISITS = "visits"

    // chat
    const val CHAT_LIST   = "chat_list"
    const val CHAT_DETAIL = "chat_detail"

}
