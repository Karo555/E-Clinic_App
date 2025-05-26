package com.example.e_clinic_app.helpers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.e_clinic_app.presentation.viewmodel.PatientListViewModel


class PatientListViewModelFactory(
    private val doctorId: String
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PatientListViewModel::class.java)) {
            return PatientListViewModel(doctorId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
