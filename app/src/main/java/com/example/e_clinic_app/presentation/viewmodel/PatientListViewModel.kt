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
/**
 * ViewModel for managing the list of patients associated with a doctor in the e-clinic application.
 *
 * This ViewModel fetches the list of patients for a specific doctor, supports search functionality,
 * and exposes the UI state for displaying all patients or filtered patients based on a search query.
 *
 * @property doctorId The unique identifier of the doctor whose patients are being managed.
 */
class PatientListViewModel(
    private val doctorId: String
) : ViewModel() {

    private val _allPatients = MutableStateFlow<List<PatientWithLastVisit>>(emptyList())
    /** A state flow containing the complete list of patients for the doctor. */
    val allPatients: StateFlow<List<PatientWithLastVisit>> = _allPatients.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    /** A state flow containing the current search query entered by the user. */
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    /**
     * A state flow containing the list of patients filtered by the current search query.
     *
     * If the search query is blank, the complete list of patients is returned.
     * Otherwise, the list is filtered to include only patients whose names match the query.
     */
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
    /**
     * Updates the search query used to filter the list of patients.
     *
     * @param query The new search query entered by the user.
     */
    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }
}