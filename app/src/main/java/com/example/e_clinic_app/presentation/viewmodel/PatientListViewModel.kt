package com.example.e_clinic_app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.e_clinic_app.data.model.PatientWithLastVisit
import com.example.e_clinic_app.data.repository.PatientRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class PatientListViewModel(
    private val doctorId: String
) : ViewModel() {

    private val _allPatients = MutableStateFlow<List<PatientWithLastVisit>>(emptyList())
    val allPatients: StateFlow<List<PatientWithLastVisit>> = _allPatients.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Patients filtered by search query
    val filteredPatients: StateFlow<List<PatientWithLastVisit>> = combine(
        _allPatients,
        _searchQuery
    ) { patients, query ->
        if (query.isBlank()) patients
        else {
            val q = query.trim().lowercase()
            patients.filter {
                val name = "${it.patient.firstName} ${it.patient.lastName}".lowercase()
                name.contains(q)
            }
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    init {
        viewModelScope.launch {
            val list = PatientRepository.fetchPatientsForDoctor(doctorId)
            _allPatients.value = list
        }
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }
}