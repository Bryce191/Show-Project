package com.example.mad_assignment.ui.Login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.mad_assignment.repository.UserPreferencesRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class LoginUiState(
    val isLoading: Boolean = false,
    val isLoginSuccess: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null
)

class LoginViewModel(private val userPreferencesRepository: UserPreferencesRepository) : ViewModel() {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState = _uiState.asStateFlow()

    val savedEmail = userPreferencesRepository.savedEmail

    fun resetState() {
        _uiState.value = LoginUiState()
    }

    fun clearMessages() {
        _uiState.update { it.copy(errorMessage = null, successMessage = null, isLoginSuccess = false) }
    }

    fun loginUser(email: String, pass: String, rememberMe: Boolean) {
        if (email.isBlank() || pass.isBlank()) {
            _uiState.value = LoginUiState(errorMessage = "Email and password cannot be empty.")
            return
        }
        viewModelScope.launch {
            _uiState.value = LoginUiState(isLoading = true)
            try {
                auth.signInWithEmailAndPassword(email, pass).await()
                if (rememberMe) {
                    userPreferencesRepository.saveUserEmail(email)
                } else {
                    userPreferencesRepository.clearUserEmail()
                }
                _uiState.value = LoginUiState(isLoginSuccess = true)
            } catch (e: FirebaseAuthInvalidUserException) {
                _uiState.value = LoginUiState(errorMessage = "User does not exist.")
            } catch (e: FirebaseAuthInvalidCredentialsException) {
                _uiState.value = LoginUiState(errorMessage = "Invalid password.")
            } catch (e: Exception) {
                _uiState.value = LoginUiState(errorMessage = e.message ?: "An unknown error occurred.")
            }
        }
    }

    fun sendPasswordResetEmail(email: String) {
        if (email.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Please enter your email to reset password.") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, successMessage = null) }
            try {
                auth.sendPasswordResetEmail(email).await()
                _uiState.update { it.copy(isLoading = false, successMessage = "Password reset email sent.") }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
            }
        }
    }
}

class LoginViewModelFactory(private val userPreferencesRepository: UserPreferencesRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LoginViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return LoginViewModel(userPreferencesRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}