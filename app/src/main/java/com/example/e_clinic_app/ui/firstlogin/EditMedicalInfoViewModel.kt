package com.example.e_clinic_app.ui.firstlogin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.e_clinic_app.data.model.DosageUnit
import com.example.e_clinic_app.data.model.Drug
import com.example.e_clinic_app.data.model.Frequency
import com.example.e_clinic_app.data.model.MedicalCondition
import com.example.e_clinic_app.data.model.Medication
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
/**
 * Represents the UI state for the Edit Medical Information screen.
 *
 * @property firstName The user's first name.
 * @property lastName The user's last name.
 * @property knownConditions A list of the user's known medical conditions.
 * @property isLoading A flag indicating whether the data is currently being loaded.
 * @property isSaving A flag indicating whether the data is currently being saved.
 * @property saveSuccess A flag indicating whether the save operation was successful.
 * @property errorMessage An error message, if any, encountered during data operations.
 * @property initialFirstName The initial value of the user's first name.
 * @property initialLastName The initial value of the user's last name.
 * @property initialConditions The initial list of the user's known medical conditions.
 * @property medications A list of the user's medications.
 * @property initialMedications The initial list of the user's medications.
 * @property hasMedications A flag indicating whether the user has medications.
 */
data class EditMedicalUiState(
    val firstName: String = "",
    val lastName: String = "",
    val knownConditions: List<MedicalCondition> = emptyList(),
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val saveSuccess: Boolean = false,
    val errorMessage: String? = null,
    val initialFirstName: String = "",
    val initialLastName: String = "",
    val initialConditions: List<MedicalCondition> = emptyList(),
    val medications: List<Medication> = emptyList(),
    val initialMedications: List<Medication> = emptyList(),
    val hasMedications: Boolean = false // Added field
)
/**
 * A `ViewModel` class that manages the state and logic for the Edit Medical Information screen
 * in the e-clinic application.
 *
 * This class handles loading, updating, and saving user medical information, including personal details,
 * known medical conditions, and medications. It interacts with Firestore to persist data and provides
 * state management for the UI.
 */
class EditMedicalInfoViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(EditMedicalUiState())
    val uiState: StateFlow<EditMedicalUiState> = _uiState.asStateFlow()

    private val db = FirebaseFirestore.getInstance()
    private val user = FirebaseAuth.getInstance().currentUser
    /**
     * Loads the user's basic medical information from Firestore.
     *
     * @param isEditing A flag indicating whether the screen is in edit mode.
     */
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
                val ln = doc.getString("lastName") ?: ""
                @Suppress("UNCHECKED_CAST")
                val conditions = (doc.get("knownConditions") as? List<Map<String, String>>)
                    ?.map { MedicalCondition(it["category"] ?: "", it["type"] ?: "") }
                    ?: emptyList()
                @Suppress("UNCHECKED_CAST")
                val meds = (doc.get("medications") as? List<Map<String, String>>)
                ?.map {
                        Medication(
                            Drug(
                                it["name"] as? String ?: "",
                                name = "aspirin",
                                formulation = "tablet",
                                availableUnits = listOf(DosageUnit.TAB),
                                commonDosages = mapOf(
                                    DosageUnit.MG to listOf(10.0, 20.0, 50.0),
                                    DosageUnit.ML to listOf(5.0, 10.0)
                                ),
                                defaultFrequency = Frequency.AS_NEEDED,
                                searchableNames = listOf("aspirin", "acetylsalicylic acid"),
                                createdAt = null,
                                updatedAt = null
                            ), // Assuming `Drug` takes a `String` as a parameter
                            it["dose"]?.toDoubleOrNull() ?: 0.0,
                            DosageUnit.valueOf(it["frequency"] as? String ?: "DEFAULT"),
                            frequency = TODO() // Assuming `DosageUnit` is an enum and has a default value
                        )
                    } ?: emptyList()                    ?: emptyList()

                _uiState.update {
                    it.copy(
                        firstName = fn,
                        lastName = ln,
                        knownConditions = conditions,
                        initialFirstName = fn,
                        initialLastName = ln,
                        initialConditions = conditions,
                        medications = meds,
                        initialMedications = meds,
                        hasMedications = meds.isNotEmpty(), // Set hasMedications
                        isLoading = false
                    )
                }
            } else {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }
    /**
     * Updates the user's first name in the UI state.
     *
     * @param fn The new first name.
     */
    fun onFirstNameChange(fn: String) {
        _uiState.update { it.copy(firstName = fn) }
    }
    /**
     * Updates the user's last name in the UI state.
     *
     * @param ln The new last name.
     */
    fun onLastNameChange(ln: String) {
        _uiState.update { it.copy(lastName = ln) }
    }
    /**
     * Updates the list of known medical conditions in the UI state.
     *
     * @param list The updated list of medical conditions.
     */
    fun onConditionsChange(list: List<MedicalCondition>) {
        _uiState.update { it.copy(knownConditions = list) }
    }
    /**
     * Updates the list of medications in the UI state.
     *
     * @param list The updated list of medications.
     */
    fun onMedicationsChange(list: List<Medication>) {
        _uiState.update { it.copy(medications = list) }
    }
    /**
     * Updates the flag indicating whether the user has medications.
     *
     * @param hasMedications The updated flag value.
     */
    fun onHasMedicationsChange(hasMedications: Boolean) { // Added function
        _uiState.update { it.copy(hasMedications = hasMedications) }
    }
    /**
     * Adds a new medication to the list of medications in the UI state.
     *
     * @param med The medication to add.
     */
    fun addMedication(med: Medication) {
        val newList = _uiState.value.medications + med
        onMedicationsChange(newList)
    }
    /**
     * Updates an existing medication in the list of medications in the UI state.
     *
     * @param index The index of the medication to update.
     * @param med The updated medication.
     */
    fun updateMedication(index: Int, med: Medication) {
        val list = _uiState.value.medications.toMutableList().apply { this[index] = med }
        onMedicationsChange(list)
    }
    /**
     * Removes a medication from the list of medications in the UI state.
     *
     * @param index The index of the medication to remove.
     */
    fun removeMedication(index: Int) {
        val list = _uiState.value.medications.toMutableList().apply { removeAt(index) }
        onMedicationsChange(list)
    }
    /**
     * Saves the user's basic medical information to Firestore.
     */
    fun saveBasicInfo() {
        val state = _uiState.value
        if (user == null) return

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null) }
            val data = mapOf(
                "firstName" to state.firstName,
                "lastName" to state.lastName,
                "knownConditions" to state.knownConditions.map {
                    mapOf("category" to it.category, "type" to it.type)
                },
                "medications" to state.medications.map {
                    mapOf("name" to it.drug.name, "dose" to it.drug.defaultFrequency, "frequency" to it.drug.defaultFrequency)
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
                            isSaving = false,
                            saveSuccess = true,
                            initialFirstName = state.firstName,
                            initialLastName = state.lastName,
                            initialConditions = state.knownConditions,
                            initialMedications = state.medications
                        )
                    }
                }
                .addOnFailureListener { e ->
                    _uiState.update { it.copy(isSaving = false, errorMessage = e.message) }
                }
        }
    }
    /**
     * Checks whether there are unsaved changes in the UI state.
     *
     * @return `true` if there are unsaved changes, `false` otherwise.
     */
    fun hasChanges(): Boolean {
        val s = _uiState.value
        return s.firstName != s.initialFirstName ||
                s.lastName != s.initialLastName ||
                s.knownConditions != s.initialConditions ||
                s.medications != s.initialMedications
    }

    /**
     * Clears the save success flag in the UI state.
     */
    fun clearSaveSuccess() {
        _uiState.update { it.copy(saveSuccess = false) }
    }
}