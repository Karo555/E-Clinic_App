package com.example.e_clinic_app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.e_clinic_app.data.model.Doctor
import com.example.e_clinic_app.data.repository.DoctorRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.launch
/**
 * ViewModel for browsing and filtering doctors in the e-clinic application.
 *
 * This ViewModel manages the state of the doctor list, search query, and filtering options,
 * and provides a filtered list of doctors based on the selected criteria.
 */
class BrowseDoctorsViewModel : ViewModel() {
    private val _allDoctors = MutableStateFlow<List<Doctor>>(emptyList())
    /** A state flow containing the complete list of available doctors. */
    val allDoctors: StateFlow<List<Doctor>> = _allDoctors.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    /** A state flow containing the current search query for filtering doctors by name. */
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedSpecialisation = MutableStateFlow<String?>(null)
    /** A state flow containing the selected specialisation for filtering doctors. */
    val selectedSpecialisation: StateFlow<String?> = _selectedSpecialisation.asStateFlow()

    private val _selectedCity = MutableStateFlow<String?>(null)
    /** A state flow containing the selected city for filtering doctors. */
    val selectedCity: StateFlow<String?> = _selectedCity.asStateFlow()

    private val _minExperience = MutableStateFlow(0)
    /** A state flow containing the minimum years of experience for filtering doctors. */
    val minExperience: StateFlow<Int> = _minExperience.asStateFlow()
    /**
     * A state flow containing the filtered list of doctors based on the search query,
     * selected specialisation, city, and minimum years of experience.
     */
    val filteredDoctors = combine(
        _allDoctors,
        _searchQuery,
        _selectedSpecialisation,
        _selectedCity,
        _minExperience
    ) { doctors, query, spec, city, minExp ->
        doctors.filter {
            val matchesName = query.isBlank() ||
                    "${it.firstName} ${it.lastName}".contains(query, ignoreCase = true)
            val matchesSpec = spec.isNullOrBlank() ||
                    it.specialisation.equals(spec, ignoreCase = true)
            val matchesCity = city.isNullOrBlank() ||
                    it.institutionName.contains(city, ignoreCase = true)
            val matchesExp = it.experienceYears >= minExp
            matchesName && matchesSpec && matchesCity && matchesExp
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    init {
        viewModelScope.launch {
            _allDoctors.value = DoctorRepository.fetchAvailableDoctors()
        }
    }
    /**
     * Updates the search query for filtering doctors.
     *
     * @param q The new search query.
     */
    fun onSearchQueryChange(q: String) { _searchQuery.value = q }
    /**
     * Updates the selected specialisation for filtering doctors.
     *
     * @param s The new specialisation, or null to clear the filter.
     */
    fun onSpecialisationChange(s: String?) { _selectedSpecialisation.value = s }
    /**
     * Updates the selected city for filtering doctors.
     *
     * @param c The new city, or null to clear the filter.
     */
    fun onCityChange(c: String?) { _selectedCity.value = c }
    /**
     * Updates the minimum years of experience for filtering doctors.
     *
     * @param e The new minimum years of experience.
     */
    fun onMinExperienceChange(e: Int) { _minExperience.value = e }
}