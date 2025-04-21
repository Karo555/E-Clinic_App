package com.example.e_clinic_app.ui.firstlogin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.e_clinic_app.data.model.MedicalCondition
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class EditMedicalUiState(
    val firstName: String = "",
    val lastName: String = "",
    val knownConditions: List<MedicalCondition> = emptyList(),
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val saveSuccess: Boolean = false,
    val errorMessage: String? = null,
    // initial values for change detection:
    val initialFirstName: String = "",
    val initialLastName: String = "",
    val initialConditions: List<MedicalCondition> = emptyList()
)

class EditMedicalInfoViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(EditMedicalUiState())
    val uiState: StateFlow<EditMedicalUiState> = _uiState.asStateFlow()

    private val db = FirebaseFirestore.getInstance()
    private val user = FirebaseAuth.getInstance().currentUser

    /** Load existing data if editing */
    fun loadBasicInfo(isEditing: Boolean) {
        if (!isEditing || user == null) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val doc = db.collection("users")
                .document(user.uid)
                .collection("profile")
                .document("basicInfo")
                .get()
                .await()
            if (doc.exists()) {
                val fn = doc.getString("firstName") ?: ""
                val ln = doc.getString("lastName")  ?: ""
                @Suppress("UNCHECKED_CAST")
                val list = (doc.get("knownConditions") as? List<Map<String, String>>)
                    ?.map { MedicalCondition(it["category"] ?: "", it["type"] ?: "") }
                    ?: emptyList()

                _uiState.update {
                    it.copy(
                        firstName         = fn,
                        lastName          = ln,
                        knownConditions   = list,
                        initialFirstName  = fn,
                        initialLastName   = ln,
                        initialConditions = list,
                        isLoading         = false
                    )
                }
            } else {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun onFirstNameChange(fn: String) {
        _uiState.update { it.copy(firstName = fn) }
    }

    fun onLastNameChange(ln: String) {
        _uiState.update { it.copy(lastName = ln) }
    }

    fun onConditionsChange(list: List<MedicalCondition>) {
        _uiState.update { it.copy(knownConditions = list) }
    }

    /** Save to Firestore */
    fun saveBasicInfo() {
        val state = _uiState.value
        if (user == null) return

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null) }
            val data = mapOf(
                "firstName" to state.firstName,
                "lastName"  to state.lastName,
                "knownConditions" to state.knownConditions.map {
                    mapOf("category" to it.category, "type" to it.type)
                }
            )

            db.collection("users")
                .document(user.uid)
                .collection("profile")
                .document("basicInfo")
                .set(data)
                .addOnSuccessListener {
                    _uiState.update {
                        it.copy(
                            isSaving    = false,
                            saveSuccess = true,
                            // update initial values so hasChanges() resets
                            initialFirstName  = state.firstName,
                            initialLastName   = state.lastName,
                            initialConditions = state.knownConditions
                        )
                    }
                }
                .addOnFailureListener { e ->
                    _uiState.update { it.copy(isSaving = false, errorMessage = e.message) }
                }
        }
    }

    /** Whether current fields differ from what we loaded */
    fun hasChanges(): Boolean {
        val s = _uiState.value
        return s.firstName != s.initialFirstName ||
                s.lastName  != s.initialLastName ||
                s.knownConditions != s.initialConditions
    }

    /** Reset saveSuccess flag after handling */
    fun clearSaveSuccess() {
        _uiState.update { it.copy(saveSuccess = false) }
    }
}