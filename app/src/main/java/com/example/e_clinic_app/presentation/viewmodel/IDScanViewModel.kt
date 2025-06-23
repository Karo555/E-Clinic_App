package com.example.e_clinic_app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.e_clinic_app.util.IDTextParser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * UI state for the doctor profile form
 */
data class DoctorFormState(
    val firstName: String = "",
    val lastName: String = "",
    val specialization: String = "",
    val experienceYears: String = "",
    val licenseNumber: String = "",
    val isProcessingImage: Boolean = false
)

/**
 * ViewModel for handling ID scanning and form auto-fill functionality
 */
class IDScanViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(DoctorFormState())
    val uiState: StateFlow<DoctorFormState> = _uiState.asStateFlow()

    /**
     * Update form field
     */
    fun updateField(field: String, value: String) {
        viewModelScope.launch {
            when (field) {
                "firstName" -> _uiState.update { it.copy(firstName = value) }
                "lastName" -> _uiState.update { it.copy(lastName = value) }
                "specialization" -> _uiState.update { it.copy(specialization = value) }
                "experienceYears" -> _uiState.update { it.copy(experienceYears = value) }
                "licenseNumber" -> _uiState.update { it.copy(licenseNumber = value) }
            }
        }
    }

    /**
     * Set loading state for image processing
     */
    fun setProcessingImage(isProcessing: Boolean) {
        viewModelScope.launch {
            _uiState.update { it.copy(isProcessingImage = isProcessing) }
        }
    }

    /**
     * Parse OCR text and update form fields with extracted information
     *
     * @param text Raw text from OCR processing
     */
    fun parseAndFill(text: String) {
        viewModelScope.launch {
            val extractedData = IDTextParser.parseIDText(text)

            // Extract and split name into first and last name
            extractedData["name"]?.let { fullName ->
                val nameParts = fullName.trim().split("\\s+".toRegex(), 2)
                if (nameParts.isNotEmpty()) {
                    _uiState.update { it.copy(firstName = nameParts[0]) }
                }
                if (nameParts.size > 1) {
                    _uiState.update { it.copy(lastName = nameParts[1]) }
                }
            }

            // Update other fields
            extractedData["specialization"]?.let { spec ->
                _uiState.update { it.copy(specialization = spec) }
            }

            extractedData["licenseNumber"]?.let { license ->
                _uiState.update { it.copy(licenseNumber = license) }
            }

            extractedData["experienceYears"]?.let { years ->
                _uiState.update { it.copy(experienceYears = years) }
            }

            // Processing complete
            _uiState.update { it.copy(isProcessingImage = false) }
        }
    }
}
