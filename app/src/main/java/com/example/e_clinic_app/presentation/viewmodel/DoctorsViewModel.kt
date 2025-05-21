package com.example.e_clinic_app.presentation.viewmodel
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import androidx.lifecycle.viewModelScope
import com.example.e_clinic_app.data.model.Doctor
import com.example.e_clinic_app.data.repository.DoctorRepository

class DoctorsViewModel : ViewModel() {
    private val _uiState = MutableStateFlow<UiState>(UiState.Loading)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    init { load() }

    private fun load() = viewModelScope.launch {
        runCatching { DoctorRepository.fetchAvailableDoctors() }
            .onSuccess { _uiState.value = UiState.Success(it) }
            .onFailure { _uiState.value = UiState.Error(it) }
    }

    sealed interface UiState {
        object Loading : UiState
        data class Success(val doctors: List<Doctor>) : UiState
        data class Error(val throwable: Throwable) : UiState
    }
}
