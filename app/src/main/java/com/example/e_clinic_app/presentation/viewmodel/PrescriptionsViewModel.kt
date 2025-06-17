package com.example.e_clinic_app.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.e_clinic_app.data.model.Prescription
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow


class PrescriptionsViewModel : ViewModel() {
    private val _prescriptions = MutableStateFlow<List<Prescription>>(emptyList())
    val prescriptions = _prescriptions
    private val fs = FirebaseFirestore.getInstance()
    private val user = FirebaseAuth.getInstance().currentUser

    init {
        loadRoleAndPrescriptions()
    }

    private fun loadRoleAndPrescriptions() {
        fs.collection("users").document(user!!.uid)
            .get()
            .addOnSuccessListener { doc ->
                val fetchedRole = doc.getString("role") ?: "unknown"
                Log.d("PrescriptionsViewModel", "Fetched role: $fetchedRole")
                loadPrescriptions(fetchedRole)
            }
            .addOnFailureListener { exception ->
                Log.e("PrescriptionsViewModel", "Failed to fetch role.", exception)
                loadPrescriptions("unknown")
            }
    }

    private fun loadPrescriptions(fetchedRole: String) {
        Log.d("PrescriptionsViewModel", "Loading prescriptions for role: $fetchedRole")

        when (fetchedRole) {
            "Doctor" -> {
                fs.collection("prescriptions")
                    .whereEqualTo("authorId", user?.uid)
                    .get()
                    .addOnSuccessListener { snapshot ->
                        val prescriptionsList = snapshot.documents.mapNotNull { it.toObject(Prescription::class.java) }
                        _prescriptions.value = prescriptionsList
                        Log.d("PrescriptionsViewModel", "Loaded ${prescriptionsList.size} prescriptions for doctor.")
                    }
                    .addOnFailureListener { exception ->
                        Log.e("PrescriptionsViewModel", "Failed to load prescriptions for doctor.", exception)
                        _prescriptions.value = emptyList()
                    }
            }
            "Patient" -> {
                fs.collection("prescriptions")
                    .whereEqualTo("patientId", user?.uid)
                    .get()
                    .addOnSuccessListener { snapshot ->
                        val prescriptionsList = snapshot.documents.mapNotNull { it.toObject(Prescription::class.java) }
                        _prescriptions.value = prescriptionsList
                        Log.d("PrescriptionsViewModel", "Loaded ${prescriptionsList.size} prescriptions for patient.")
                    }
                    .addOnFailureListener { exception ->
                        Log.e("PrescriptionsViewModel", "Failed to load prescriptions for patient.", exception)
                        _prescriptions.value = emptyList()
                    }
            }
            else -> {
                Log.w("PrescriptionsViewModel", "Unknown role: $fetchedRole. No prescriptions loaded.")
                _prescriptions.value = emptyList()
            }
        }
    }
}

