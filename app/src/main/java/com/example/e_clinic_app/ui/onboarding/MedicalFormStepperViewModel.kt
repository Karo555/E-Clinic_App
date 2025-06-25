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
/**
 * A `ViewModel` class that manages the state and logic for the Medical Form Stepper screen in the e-clinic application.
 *
 * This ViewModel handles the multi-step form process, including updating personal information, medical conditions,
 * and medications. It also manages the form's submission to Firebase Firestore and provides error handling.
 *
 * The ViewModel includes:
 * - State management for the current step, personal information, conditions, and medications.
 * - Methods to update and validate form data.
 * - Firebase Firestore integration for saving the form data.
 * - Navigation between steps in the form.
 */
class MedicalFormStepperViewModel : ViewModel() {
    /**
     * A `StateFlow` that holds the current state of the medical form.
     */
    private val _uiState = MutableStateFlow(MedicalFormState())
    val uiState: StateFlow<MedicalFormState> = _uiState

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    /**
     * Updates the personal information in the form state.
     *
     * @param firstName The user's first name.
     * @param lastName The user's last name.
     */
    fun updatePersonalInfo(firstName: String, lastName: String) {
        _uiState.value = _uiState.value.copy(
            personalInfo = PersonalInfo(firstName, lastName)
        )
    }

    /**
     * Updates the list of medical conditions in the form state.
     *
     * @param conditions A list of `MedicalCondition` objects representing the user's conditions.
     */
    fun updateConditions(conditions: List<MedicalCondition>) {
        _uiState.value = _uiState.value.copy(conditions = conditions)
    }
    /**
     * Sets whether the user has medical conditions.
     *
     * @param hasConditions A boolean indicating if the user has conditions.
     */
    fun setHasConditions(hasConditions: Boolean) {
        _uiState.value = _uiState.value.copy(hasConditions = hasConditions)
    }
    /**
     * Applies the condition selection by clearing the conditions list if the user has no conditions.
     */
    fun applyConditionSelection() {
        if (_uiState.value.hasConditions == false) {
            updateConditions(emptyList())
        }
    }
    /**
     * Updates the list of medications in the form state.
     *
     * @param medications A list of `Medication` objects representing the user's medications.
     */
    fun updateMedications(medications: List<Medication>) {
        _uiState.value = _uiState.value.copy(medications = medications)
    }
    /**
     * Navigates to the next step in the form.
     */
    fun goToNextStep() {
        if (_uiState.value.step < 3) {
            _uiState.value = _uiState.value.copy(step = _uiState.value.step + 1)
        }
    }
    /**
     * Navigates to the previous step in the form.
     */
    fun goToPreviousStep() {
        if (_uiState.value.step > 0) {
            _uiState.value = _uiState.value.copy(step = _uiState.value.step - 1)
        }
    }
    /**
     * Updates the selected condition category in the form state.
     *
     * @param category The selected condition category.
     */
    fun updateConditionCategory(category: String) {
        _uiState.value = _uiState.value.copy(
            selectedCategory = category,
            selectedSubtype = "" // reset subtype
        )
    }
    /**
     * Updates the selected condition subtype in the form state.
     *
     * @param subtype The selected condition subtype.
     */
    fun updateConditionSubtype(subtype: String) {
        _uiState.value = _uiState.value.copy(selectedSubtype = subtype)

        // Immediately apply if both are selected
        val category = _uiState.value.selectedCategory
        if (category.isNotBlank() && subtype.isNotBlank()) {
            updateConditions(listOf(MedicalCondition(category, subtype)))
        }
    }

    /**
     * Submits the form data to Firebase Firestore.
     */
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
                    mapOf(
                        "name" to it.drug.name,
                        "dose" to it.amount,
                        "frequency" to it.frequency.name
                    )
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
    /**
     * Sets whether the user takes medications.
     *
     * @param has A boolean indicating if the user takes medications.
     */
    fun setHasMedications(has: Boolean) {
        _uiState.value = _uiState.value.copy(hasMedications = has)
    }
    /**
     * Applies the medication selection by clearing the medications list if the user has no medications.
     */
    fun applyMedicationSelection() {
        if (_uiState.value.hasMedications == false) {
            updateMedications(emptyList())
        }
    }
}
