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
    const val PATIENT_HOME = "patient_home"
    const val DOCTOR_HOME = "doctor_home"
    const val ADMIN_HOME = "admin_home"

    //reset
    const val RESET_PASSWORD = "reset_password"

    //admin dashboards
    const val GLOBAL_ADMIN_DASHBOARD = "global_admin_dashboard"
    const val INSTITUTION_ADMIN_DASHBOARD = "institution_admin_dashboard"

    //tabs for nested navigation
    const val HOME_TAB = "home_tab"
    const val CHAT_TAB = "chat_tab"
    const val VISITS_TAB = "visits_tab"
    const val SETTINGS_TAB = "settings_tab"
}
