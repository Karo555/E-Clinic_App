package com.example.e_clinic_app.presentation.viewmodel
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import androidx.lifecycle.viewModelScope
import com.example.e_clinic_app.data.model.Doctor
import com.example.e_clinic_app.data.repository.DoctorRepository
/**
 * ViewModel for managing the list of doctors in the e-clinic application.
 *
 * This ViewModel fetches the list of available doctors from the repository
 * and exposes the UI state, including loading, success, and error states.
 */
class DoctorsViewModel : ViewModel() {
    private val _uiState = MutableStateFlow<UiState>(UiState.Loading)
    /** A state flow containing the current UI state of the doctors list screen. */
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    init { load() }
    /**
     * Loads the list of available doctors from the repository.
     *
     * This method updates the UI state based on the success or failure of the operation.
     */
    private fun load() = viewModelScope.launch {
        runCatching { DoctorRepository.fetchAvailableDoctors() }
            .onSuccess { _uiState.value = UiState.Success(it) }
            .onFailure { _uiState.value = UiState.Error(it) }
    }
    /**
     * Represents the UI state of the doctors list screen.
     */
    sealed interface UiState {
        /** Indicates that the data is currently being loaded. */
        object Loading : UiState
        /**
         * Indicates that the data has been successfully loaded.
         *
         * @property doctors The list of available doctors.
         */
        data class Success(val doctors: List<Doctor>) : UiState
        /**
         * Indicates that an error occurred while loading the data.
         *
         * @property throwable The exception that caused the error.
         */
        data class Error(val throwable: Throwable) : UiState
    }
}
