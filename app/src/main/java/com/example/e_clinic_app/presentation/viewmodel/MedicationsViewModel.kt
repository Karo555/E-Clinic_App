package com.example.e_clinic_app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.e_clinic_app.data.model.Drug
import com.example.e_clinic_app.data.repository.DrugRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
/**
 * ViewModel for managing the medications catalog in the e-clinic application.
 *
 * This ViewModel handles loading, refreshing, and exposing the list of drugs
 * from the repository, along with loading and error states.
 *
 * @property repository The repository used to fetch and manage drug data.
 */
class MedicationsViewModel(
    private val repository: DrugRepository = DrugRepository()
) : ViewModel() {

    private val _drugs = MutableStateFlow<List<Drug>>(emptyList())
    /** A state flow containing the list of drugs in the catalog. */
    val drugs: StateFlow<List<Drug>> = _drugs.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    /** A state flow indicating whether the drug data is currently being loaded. */
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    /** A state flow containing any error messages encountered during operations. */
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        loadDrugs()
    }
    /**
     * Loads the list of drugs from the repository.
     *
     * This method fetches the drug data and updates the state flows
     * for the drug list, loading state, and error state.
     */
    private fun loadDrugs() {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                repository.loadDrugs()
                repository.drugs.collect { list ->
                    _drugs.value = list
                }
                _error.value = null
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Refreshes the drug catalog by reloading the data from the repository.
     */
    fun refreshDrugs() = loadDrugs()
}
