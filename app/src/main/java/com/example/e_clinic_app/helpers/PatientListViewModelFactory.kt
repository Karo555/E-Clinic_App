package com.example.e_clinic_app.helpers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.e_clinic_app.presentation.viewmodel.PatientListViewModel

/**
 * Factory class for creating instances of `PatientListViewModel`.
 *
 * This factory is used to provide the required dependencies (e.g., `doctorId`)
 * to the `PatientListViewModel` when it is instantiated.
 *
 * @property doctorId The unique identifier of the doctor, used to fetch the list of patients.
 */
class PatientListViewModelFactory(
    private val doctorId: String
) : ViewModelProvider.Factory {
    /**
     * Creates a new instance of the specified `ViewModel` class.
     *
     * This method checks if the requested `ViewModel` class is `PatientListViewModel`.
     * If so, it creates and returns an instance of `PatientListViewModel` with the provided `doctorId`.
     * Otherwise, it throws an `IllegalArgumentException`.
     *
     * @param modelClass The class of the `ViewModel` to create.
     * @return An instance of the requested `ViewModel` class.
     * @throws IllegalArgumentException If the requested `ViewModel` class is not `PatientListViewModel`.
     */
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PatientListViewModel::class.java)) {
            return PatientListViewModel(doctorId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
