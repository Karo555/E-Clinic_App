package com.example.e_clinic_app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.e_clinic_app.data.model.Drug
import com.example.e_clinic_app.data.repository.DrugRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import android.util.Log
import androidx.lifecycle.ViewModelProvider

class MedicationsViewModel(
    private val drugRepository: DrugRepository
) : ViewModel() {

    private val _drugs = MutableStateFlow<List<Drug>>(emptyList())
    val drugs: StateFlow<List<Drug>> = _drugs

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    init {
        fetchDrugs()
    }

    private fun fetchDrugs() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            Log.d("MedicationsViewModel", "Fetching drugs from Firestore...")

            try {
                val fetchedDrugs = drugRepository.getAllDrugs()
                Log.d("MedicationsViewModel", "Fetched drugs: $fetchedDrugs")
                _drugs.value = fetchedDrugs
            } catch (e: Exception) {
                Log.e("MedicationsViewModel", "Error fetching drugs: ${e.message}", e)
                _error.value = e.message
            } finally {
                _isLoading.value = false
                Log.d("MedicationsViewModel", "Fetching drugs completed.")
            }
        }
    }
}
class MedicationsViewModelFactory : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MedicationsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MedicationsViewModel(DrugRepository()) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}