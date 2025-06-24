package com.example.e_clinic_app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.e_clinic_app.data.repository.PatientReminderRepository

/**
 * Factory for creating [PatientReminderViewModel] instances.
 *
 * @param repository The repository to be used by the ViewModel.
 */
class PatientReminderViewModelFactory(
    private val repository: PatientReminderRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PatientReminderViewModel::class.java)) {
            return PatientReminderViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
