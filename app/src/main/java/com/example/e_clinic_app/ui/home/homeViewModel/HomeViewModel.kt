//package com.example.e_clinic_app.ui.home.homeViewModel
//
//import androidx.lifecycle.ViewModel
//import com.google.firebase.auth.FirebaseAuth
//import com.google.firebase.firestore.FirebaseFirestore
//import kotlinx.coroutines.flow.MutableStateFlow
//import kotlinx.coroutines.tasks.await
//
//@HiltViewModel
//class HomeViewModel @Inject constructor() : ViewModel() {
//
//    private val _uiState = MutableStateFlow(HomeUiState())
//    val uiState: StateFlow<HomeUiState> = _uiState
//
//    init {
//        loadUserData()
//    }
//
//    private fun loadUserData() = viewModelScope.launch {
//        _uiState.value = _uiState.value.copy(isLoading = true)
//        try {
//            val user = FirebaseAuth.getInstance().currentUser ?: return@launch
//            val uid = user.uid
//            val db = FirebaseFirestore.getInstance()
//
//            val userDoc = db.collection("users").document(uid).get().await()
//            val role = userDoc.getString("role")
//
//            // Update profile based on role logic similar to your code...
//
//            _uiState.value = _uiState.value.copy(role = role, isLoading = false)
//        } catch (e: Exception) {
//            Log.e("HomeViewModel", "Error loading user: ${e.message}")
//            _uiState.value = _uiState.value.copy(isLoading = false)
//        }
//    }
//}
//
//data class HomeUiState(
//    val role: String? = null,
//    val isLoading: Boolean = false
//)