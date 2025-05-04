package com.example.e_clinic_app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.e_clinic_app.data.model.Drug
import com.example.e_clinic_app.data.repository.DrugRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MedicationsViewModel(
    private val repository: DrugRepository = DrugRepository()
) : ViewModel() {

    private val _drugs = MutableStateFlow<List<Drug>>(emptyList())
    val drugs: StateFlow<List<Drug>> = _drugs.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        loadDrugs()
    }

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
     * Refreshes the drug catalog from Firestore.
     */
    fun refreshDrugs() = loadDrugs()
}
