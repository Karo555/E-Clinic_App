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

class BrowseDoctorsViewModel : ViewModel() {
    private val _allDoctors = MutableStateFlow<List<Doctor>>(emptyList())
    val allDoctors: StateFlow<List<Doctor>> = _allDoctors.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedSpecialisation = MutableStateFlow<String?>(null)
    val selectedSpecialisation: StateFlow<String?> = _selectedSpecialisation.asStateFlow()

    private val _selectedCity = MutableStateFlow<String?>(null)
    val selectedCity: StateFlow<String?> = _selectedCity.asStateFlow()

    private val _minExperience = MutableStateFlow(0)
    val minExperience: StateFlow<Int> = _minExperience.asStateFlow()

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

    fun onSearchQueryChange(q: String) { _searchQuery.value = q }
    fun onSpecialisationChange(s: String?) { _selectedSpecialisation.value = s }
    fun onCityChange(c: String?) { _selectedCity.value = c }
    fun onMinExperienceChange(e: Int) { _minExperience.value = e }
}