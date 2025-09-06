package com.shokal.technonext.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shokal.technonext.data.preferences.UserPreferences
import com.shokal.technonext.data.repository.AuthRepository
import com.shokal.technonext.utils.PasswordValidator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AuthUiState(
    val isLoading: Boolean = false,
    val isLoggedIn: Boolean = false,
    val isInitialized: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val currentUserEmail: String? = null,
    val passwordValidationErrors: List<String> = emptyList()
)

@HiltViewModel
open class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val userPreferences: UserPreferences
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()
    
    fun register(email: String, password: String, confirmPassword: String) {
        if (!isValidEmail(email)) {
            _uiState.value = _uiState.value.copy(errorMessage = "Please enter a valid email")
            return
        }
        
        val passwordValidation = PasswordValidator.validatePassword(password)
        if (!passwordValidation.isValid) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "Password validation failed",
                passwordValidationErrors = passwordValidation.errors
            )
            return
        }
        
        if (password != confirmPassword) {
            _uiState.value = _uiState.value.copy(errorMessage = "Passwords do not match")
            return
        }
        
        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null, passwordValidationErrors = emptyList())
        
        viewModelScope.launch {
            authRepository.register(email, password)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        successMessage = "Registration successful! Please login."
                    )
                }
                .onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = exception.message
                    )
                }
        }
    }
    
    fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _uiState.value = _uiState.value.copy(errorMessage = "Please fill in all fields")
            return
        }
        
        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
        
        viewModelScope.launch {
            authRepository.login(email, password)
                .onSuccess { user ->
                    // Save login state to preferences
                    userPreferences.setLoggedIn(true, email, email.split("@")[0])
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isLoggedIn = true,
                        currentUserEmail = email
                    )
                }
                .onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = exception.message
                    )
                }
        }
    }
    
    fun clearMessages() {
        _uiState.value = _uiState.value.copy(errorMessage = null, successMessage = null)
    }
    
    fun checkLoginState() {
        viewModelScope.launch {
            val isLoggedIn = userPreferences.isLoggedIn.first()
            val userEmail = userPreferences.userEmail.first()
            
            _uiState.value = _uiState.value.copy(
                isLoggedIn = isLoggedIn,
                currentUserEmail = userEmail,
                isInitialized = true
            )
        }
    }
    
    fun logout() {
        _uiState.value = _uiState.value.copy(isLoading = true)
        viewModelScope.launch {
            userPreferences.logout()
            _uiState.value = AuthUiState(
                isLoggedIn = false,
                isInitialized = true,
                currentUserEmail = null,
                isLoading = false
            )
        }
    }
    
    fun validatePassword(password: String): PasswordValidator.ValidationResult {
        return PasswordValidator.validatePassword(password)
    }
    
    fun getPasswordStrength(password: String): PasswordValidator.PasswordStrength {
        return PasswordValidator.getPasswordStrength(password)
    }
    
    open fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
}