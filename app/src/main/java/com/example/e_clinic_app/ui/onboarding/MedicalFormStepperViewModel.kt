package com.example.e_clinic_app.ui.onboarding


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.e_clinic_app.data.model.MedicalCondition
import com.example.e_clinic_app.data.model.Medication
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class PersonalInfo(
    val firstName: String = "",
    val lastName: String = ""
)


data class MedicalFormState(
    val step: Int = 0,
    val personalInfo: PersonalInfo = PersonalInfo(),
    val conditions: List<MedicalCondition> = emptyList(),
    val medications: List<Medication> = emptyList(),
    val isSaving: Boolean = false,
    val saveSuccess: Boolean = false,
    val errorMessage: String? = null,
    val hasConditions: Boolean? = null,
    val selectedCategory: String = "",
    val selectedSubtype: String = "",
    val hasMedications: Boolean? = null
)

class MedicalFormStepperViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(MedicalFormState())
    val uiState: StateFlow<MedicalFormState> = _uiState

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    fun updatePersonalInfo(firstName: String, lastName: String) {
        _uiState.value = _uiState.value.copy(
            personalInfo = PersonalInfo(firstName, lastName)
        )
    }

    fun updateConditions(conditions: List<MedicalCondition>) {
        _uiState.value = _uiState.value.copy(conditions = conditions)
    }

    fun setHasConditions(hasConditions: Boolean) {
        _uiState.value = _uiState.value.copy(hasConditions = hasConditions)
    }

    fun applyConditionSelection() {
        if (_uiState.value.hasConditions == false) {
            updateConditions(emptyList())
        }
    }

    fun updateMedications(medications: List<Medication>) {
        _uiState.value = _uiState.value.copy(medications = medications)
    }

    fun goToNextStep() {
        if (_uiState.value.step < 3) {
            _uiState.value = _uiState.value.copy(step = _uiState.value.step + 1)
        }
    }

    fun goToPreviousStep() {
        if (_uiState.value.step > 0) {
            _uiState.value = _uiState.value.copy(step = _uiState.value.step - 1)
        }
    }

    fun updateConditionCategory(category: String) {
        _uiState.value = _uiState.value.copy(
            selectedCategory = category,
            selectedSubtype = "" // reset subtype
        )
    }

    fun updateConditionSubtype(subtype: String) {
        _uiState.value = _uiState.value.copy(selectedSubtype = subtype)

        // Immediately apply if both are selected
        val category = _uiState.value.selectedCategory
        if (category.isNotBlank() && subtype.isNotBlank()) {
            updateConditions(listOf(MedicalCondition(category, subtype)))
        }
    }


    fun submitForm() {
        val user = auth.currentUser ?: return
        val uid = user.uid
        val state = _uiState.value

        viewModelScope.launch {
            _uiState.value = state.copy(isSaving = true)

            val formData = mapOf(
                "firstName" to state.personalInfo.firstName,
                "lastName" to state.personalInfo.lastName,
                "knownConditions" to state.conditions.map {
                    mapOf("category" to it.category, "type" to it.type)
                },
                "medications" to state.medications.map {
                    mapOf("name" to it.drug.name, "dose" to it.drug.defaultFrequency, "frequency" to it.frequency)
                }
            )

            db.collection("users")
                .document(uid)
                .collection("profile")
                .document("basicInfo")
                .set(formData)
                .addOnSuccessListener {
                    _uiState.value = state.copy(isSaving = false, saveSuccess = true)
                }
                .addOnFailureListener { e ->
                    _uiState.value = state.copy(isSaving = false, errorMessage = e.message)
                }
        }
    }
    fun setHasMedications(has: Boolean) {
              _uiState.value = _uiState.value.copy(hasMedications = has)
           }
       fun applyMedicationSelection() {
               if (_uiState.value.hasMedications == false) {
                       updateMedications(emptyList())
                   }
           }
}
