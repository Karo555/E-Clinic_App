package com.example.e_clinic_app.presentation.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.firestore.FirebaseFirestore

/**
 * Factory for creating PatientRemindersViewModel instances.
 *
 * This factory is needed because PatientRemindersViewModel requires an Application parameter
 * for the notification service.
 */
class PatientRemindersViewModelFactory(
    private val application: Application,
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PatientRemindersViewModel::class.java)) {
            return PatientRemindersViewModel(application, firestore) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
