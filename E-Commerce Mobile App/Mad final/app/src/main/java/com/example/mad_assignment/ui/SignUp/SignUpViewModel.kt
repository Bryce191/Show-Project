package com.example.mad_assignment.ui.SignUp

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SignUpUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isSignUpSuccess: Boolean = false
)

class SignUpViewModel : ViewModel() {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    private val _uiState = MutableStateFlow(SignUpUiState())
    val uiState: StateFlow<SignUpUiState> = _uiState.asStateFlow()

    fun signUp(email: String, password: String) {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener { authResult ->
                    val uid = authResult.user?.uid
                    if (uid != null) {
                        saveUserData(uid, email)
                    } else {
                        _uiState.update { it.copy(isLoading = false, errorMessage = "Failed to get user ID.") }
                    }
                }
                .addOnFailureListener { exception ->
                    _uiState.update { it.copy(isLoading = false, errorMessage = exception.localizedMessage) }
                }
        }
    }

    private fun saveUserData(uid: String, email: String) {
        val user = hashMapOf(
            "email" to email,
            "createdAt" to System.currentTimeMillis()
        )
        db.collection("users").document(uid).set(user)
            .addOnSuccessListener {
                _uiState.update { it.copy(isLoading = false, isSignUpSuccess = true) }
            }
            .addOnFailureListener { exception ->
                _uiState.update { it.copy(isLoading = false, errorMessage = exception.localizedMessage) }
            }
    }

    fun onDialogDismissed() {
        _uiState.update { it.copy(isSignUpSuccess = false) }
    }
}