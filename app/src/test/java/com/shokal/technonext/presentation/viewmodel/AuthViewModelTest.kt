package com.shokal.technonext.presentation.viewmodel

import app.cash.turbine.test
import com.shokal.technonext.data.preferences.UserPreferences
import com.shokal.technonext.data.repository.AuthRepository
import com.shokal.technonext.presentation.viewmodel.AuthViewModel
import com.shokal.technonext.utils.MockUtils
import com.shokal.technonext.utils.TestDataFactory
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.util.regex.Pattern

@OptIn(ExperimentalCoroutinesApi::class)
class AuthViewModelTest {
    
    private lateinit var authRepository: AuthRepository
    private lateinit var userPreferences: UserPreferences
    private lateinit var viewModel: AuthViewModel
    
    private val testDispatcher = StandardTestDispatcher()
    
    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        MockUtils.initMocks()
        
        authRepository = MockUtils.createMockAuthRepository()
        userPreferences = MockUtils.createMockUserPreferences()
        
        viewModel = TestAuthViewModel(authRepository, userPreferences)
    }
    
    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }
    
    @Test
    fun `initial state should be correct`() = runTest {
        val initialState = viewModel.uiState.value
        
        assertFalse("Initial loading state should be false", initialState.isLoading)
        assertFalse("Initial logged in state should be false", initialState.isLoggedIn)
        assertFalse("Initial initialized state should be false", initialState.isInitialized)
        assertNull("Initial error message should be null", initialState.errorMessage)
        assertNull("Initial success message should be null", initialState.successMessage)
        assertNull("Initial current user email should be null", initialState.currentUserEmail)
        assertTrue("Initial password validation errors should be empty", initialState.passwordValidationErrors.isEmpty())
    }
    
    @Test
    fun `register with valid data should succeed`() = runTest {
        // Given
        val email = "test@example.com"
        val password = "ValidPassword123!"
        val confirmPassword = "ValidPassword123!"
        
        MockUtils.setupAuthRepositorySuccess(authRepository)
        
        // When
        viewModel.register(email, password, confirmPassword)
        advanceUntilIdle()
        
        // Then
        val uiState = viewModel.uiState.value
        assertFalse("Loading should be false after success", uiState.isLoading)
        assertNull("Error message should be null after success", uiState.errorMessage)
        assertEquals("Success message should be set", "Registration successful! Please login.", uiState.successMessage)
        
        coVerify { authRepository.register(email, password) }
    }
    
    @Test
    fun `register with invalid email should show error`() = runTest {
        // Given
        val invalidEmail = "invalid-email"
        val password = "ValidPassword123!"
        val confirmPassword = "ValidPassword123!"
        
        // When
        viewModel.register(invalidEmail, password, confirmPassword)
        advanceUntilIdle()
        
        // Then
        val uiState = viewModel.uiState.value
        assertEquals("Should show email validation error", "Please enter a valid email", uiState.errorMessage)
        
        coVerify(exactly = 0) { authRepository.register(any(), any()) }
    }
    
    @Test
    fun `register with weak password should show validation errors`() = runTest {
        // Given
        val email = "test@example.com"
        val weakPassword = "123"
        val confirmPassword = "123"
        
        // When
        viewModel.register(email, weakPassword, confirmPassword)
        advanceUntilIdle()
        
        // Then
        val uiState = viewModel.uiState.value
        assertEquals("Should show password validation error", "Password validation failed", uiState.errorMessage)
        assertTrue("Should have password validation errors", uiState.passwordValidationErrors.isNotEmpty())
        
        coVerify(exactly = 0) { authRepository.register(any(), any()) }
    }
    
    @Test
    fun `register with mismatched passwords should show error`() = runTest {
        // Given
        val email = "test@example.com"
        val password = "ValidPassword123!"
        val confirmPassword = "DifferentPassword123!"
        
        // When
        viewModel.register(email, password, confirmPassword)
        advanceUntilIdle()
        
        // Then
        val uiState = viewModel.uiState.value
        assertEquals("Should show password mismatch error", "Passwords do not match", uiState.errorMessage)
        
        coVerify(exactly = 0) { authRepository.register(any(), any()) }
    }
    
    @Test
    fun `register with repository failure should show error`() = runTest {
        // Given
        val email = "test@example.com"
        val password = "ValidPassword123!"
        val confirmPassword = "ValidPassword123!"
        val errorMessage = "Registration failed"
        
        MockUtils.setupAuthRepositoryFailure(authRepository, errorMessage)
        
        // When
        viewModel.register(email, password, confirmPassword)
        advanceUntilIdle()
        
        // Then
        val uiState = viewModel.uiState.value
        assertFalse("Loading should be false after failure", uiState.isLoading)
        assertEquals("Should show repository error", errorMessage, uiState.errorMessage)
        assertNull("Success message should be null after failure", uiState.successMessage)
    }
    
    @Test
    fun `login with valid credentials should succeed`() = runTest {
        // Given
        val email = "test@example.com"
        val password = "password123"
        
        MockUtils.setupAuthRepositorySuccess(authRepository)
        MockUtils.setupUserPreferencesLoggedIn(userPreferences, email)
        
        // When
        viewModel.login(email, password)
        advanceUntilIdle()
        
        // Then
        val uiState = viewModel.uiState.value
        assertFalse("Loading should be false after success", uiState.isLoading)
        assertTrue("Should be logged in after success", uiState.isLoggedIn)
        assertEquals("Should set current user email", email, uiState.currentUserEmail)
        assertNull("Error message should be null after success", uiState.errorMessage)
        
        coVerify { authRepository.login(email, password) }
        coVerify { userPreferences.setLoggedIn(true, email, "test") }
    }
    
    @Test
    fun `login with empty fields should show error`() = runTest {
        // Given
        val email = ""
        val password = ""
        
        // When
        viewModel.login(email, password)
        advanceUntilIdle()
        
        // Then
        val uiState = viewModel.uiState.value
        assertEquals("Should show empty fields error", "Please fill in all fields", uiState.errorMessage)
        
        coVerify(exactly = 0) { authRepository.login(any(), any()) }
    }
    
    @Test
    fun `login with repository failure should show error`() = runTest {
        // Given
        val email = "test@example.com"
        val password = "password123"
        val errorMessage = "Login failed"
        
        MockUtils.setupAuthRepositoryFailure(authRepository, errorMessage)
        
        // When
        viewModel.login(email, password)
        advanceUntilIdle()
        
        // Then
        val uiState = viewModel.uiState.value
        assertFalse("Loading should be false after failure", uiState.isLoading)
        assertEquals("Should show repository error", errorMessage, uiState.errorMessage)
        assertFalse("Should not be logged in after failure", uiState.isLoggedIn)
    }
    
    @Test
    fun `checkLoginState should update state correctly`() = runTest {
        // Given
        val email = "test@example.com"
        MockUtils.setupUserPreferencesLoggedIn(userPreferences, email)
        
        // When
        viewModel.checkLoginState()
        advanceUntilIdle()
        
        // Then
        val uiState = viewModel.uiState.value
        assertTrue("Should be logged in", uiState.isLoggedIn)
        assertTrue("Should be initialized", uiState.isInitialized)
        assertEquals("Should set current user email", email, uiState.currentUserEmail)
    }
    
    @Test
    fun `logout should reset state correctly`() = runTest {
        // Given
        val email = "test@example.com"
        MockUtils.setupUserPreferencesLoggedIn(userPreferences, email)
        
        // When
        viewModel.logout()
        advanceUntilIdle()
        
        // Then
        val uiState = viewModel.uiState.value
        assertFalse("Should not be logged in after logout", uiState.isLoggedIn)
        assertTrue("Should be initialized after logout", uiState.isInitialized)
        assertNull("Should clear current user email", uiState.currentUserEmail)
        assertFalse("Should not be loading after logout", uiState.isLoading)
        
        coVerify { userPreferences.logout() }
    }
    
    @Test
    fun `clearMessages should clear error and success messages`() = runTest {
        // Given
        val email = "test@example.com"
        val password = "ValidPassword123!"
        val confirmPassword = "ValidPassword123!"
        
        MockUtils.setupAuthRepositorySuccess(authRepository)
        
        // Register to set success message
        viewModel.register(email, password, confirmPassword)
        advanceUntilIdle()
        
        // Verify success message is set
        assertNotNull("Success message should be set", viewModel.uiState.value.successMessage)
        
        // When
        viewModel.clearMessages()
        
        // Then
        val uiState = viewModel.uiState.value
        assertNull("Error message should be null after clear", uiState.errorMessage)
        assertNull("Success message should be null after clear", uiState.successMessage)
    }
    
    @Test
    fun `validatePassword should return correct validation result`() = runTest {
        // Given
        val weakPassword = "123"
        val strongPassword = "ValidPassword123!"
        
        // When
        val weakResult = viewModel.validatePassword(weakPassword)
        val strongResult = viewModel.validatePassword(strongPassword)
        
        // Then
        assertFalse("Weak password should be invalid", weakResult.isValid)
        assertTrue("Strong password should be valid", strongResult.isValid)
    }
    
    @Test
    fun `getPasswordStrength should return correct strength`() = runTest {
        // Given
        val weakPassword = "123"
        val mediumPassword = "MediumPass123" // Not in common weak passwords list
        val strongPassword = "ValidPassword123!"
        
        // When
        val weakStrength = viewModel.getPasswordStrength(weakPassword)
        val mediumStrength = viewModel.getPasswordStrength(mediumPassword)
        val strongStrength = viewModel.getPasswordStrength(strongPassword)
        
        // Then
        assertTrue("Weak password should have low strength", weakStrength.ordinal < 2)
        assertTrue("Medium password should have medium strength", mediumStrength.ordinal >= 1)
        assertTrue("Strong password should have high strength", strongStrength.ordinal >= 2)
    }
    
    @Test
    fun `uiState flow should emit correct values`() = runTest {
        // Given
        val email = "test@example.com"
        val password = "ValidPassword123!"
        val confirmPassword = "ValidPassword123!"
        
        MockUtils.setupAuthRepositorySuccess(authRepository)
        
        // When & Then
        viewModel.uiState.test {
            // Initial state
            val initialState = awaitItem()
            assertFalse("Initial loading should be false", initialState.isLoading)
            
            // Register
            viewModel.register(email, password, confirmPassword)
            advanceUntilIdle()
            
            // Should emit loading state, then success state
            val loadingState = awaitItem()
            assertTrue("Should be loading during registration", loadingState.isLoading)
            
            val successState = awaitItem()
            assertFalse("Should not be loading after success", successState.isLoading)
            assertNotNull("Should have success message", successState.successMessage)
        }
    }
}

// Test-specific AuthViewModel that overrides email validation to avoid Android Patterns
class TestAuthViewModel(
    authRepository: AuthRepository,
    userPreferences: UserPreferences
) : AuthViewModel(authRepository, userPreferences) {
    
    override fun isValidEmail(email: String): Boolean {
        val emailPattern = Pattern.compile("^[A-Za-z0-9+_.-]+@([A-Za-z0-9.-]+\\.[A-Za-z]{2,})$")
        return emailPattern.matcher(email).matches()
    }
}
