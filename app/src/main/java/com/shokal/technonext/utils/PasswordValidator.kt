package com.shokal.technonext.utils

import java.util.regex.Pattern

object PasswordValidator {
    
    private const val MIN_LENGTH = 8
    private const val MAX_LENGTH = 128
    
    // At least one uppercase letter
    private val UPPERCASE_PATTERN = Pattern.compile("[A-Z]")
    
    // At least one lowercase letter
    private val LOWERCASE_PATTERN = Pattern.compile("[a-z]")
    
    // At least one digit
    private val DIGIT_PATTERN = Pattern.compile("[0-9]")
    
    // At least one special character
    private val SPECIAL_CHAR_PATTERN = Pattern.compile("[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?]")
    
    // No spaces allowed
    private val NO_SPACES_PATTERN = Pattern.compile("^\\S*$")
    
    data class ValidationResult(
        val isValid: Boolean,
        val errors: List<String> = emptyList()
    )
    
    fun validatePassword(password: String): ValidationResult {
        val errors = mutableListOf<String>()
        
        if (password.length < MIN_LENGTH) {
            errors.add("Password must be at least $MIN_LENGTH characters long")
        }
        
        if (password.length > MAX_LENGTH) {
            errors.add("Password must be no more than $MAX_LENGTH characters long")
        }
        
        if (!UPPERCASE_PATTERN.matcher(password).find()) {
            errors.add("Password must contain at least one uppercase letter")
        }
        
        if (!LOWERCASE_PATTERN.matcher(password).find()) {
            errors.add("Password must contain at least one lowercase letter")
        }
        
        if (!DIGIT_PATTERN.matcher(password).find()) {
            errors.add("Password must contain at least one number")
        }
        
        if (!SPECIAL_CHAR_PATTERN.matcher(password).find()) {
            errors.add("Password must contain at least one special character (!@#$%^&*()_+-=[]{}|;':\",./<>?)")
        }
        
        if (!NO_SPACES_PATTERN.matcher(password).matches()) {
            errors.add("Password cannot contain spaces")
        }
        
        // Check for common weak patterns
        if (isCommonWeakPassword(password)) {
            errors.add("Password is too common or weak. Please choose a stronger password")
        }
        
        return ValidationResult(
            isValid = errors.isEmpty(),
            errors = errors
        )
    }
    
    private fun isCommonWeakPassword(password: String): Boolean {
        val commonPasswords = listOf(
            "password", "123456", "123456789", "qwerty", "abc123", "password123",
            "admin", "letmein", "welcome", "monkey", "1234567890", "password1",
            "qwerty123", "dragon", "master", "hello", "freedom", "whatever",
            "qazwsx", "trustno1", "jordan", "jennifer", "zxcvbnm", "asdfgh",
            "hunter", "buster", "soccer", "harley", "batman", "andrew",
            "tigger", "sunshine", "iloveyou", "2000", "charlie", "robert",
            "thomas", "hockey", "ranger", "daniel", "starwars", "klaster",
            "112233", "george", "computer", "michelle", "jessica", "pepper",
            "1234", "zoey", "12345", "liverpool", "david", "password",
            "jordan23", "1991", "michael", "jennifer", "jordan", "superman",
            "harley", "1234567", "qwerty", "1234567890", "mustang", "freedom",
            "whatever", "qazwsx", "trustno1", "jordan", "jennifer", "zxcvbnm"
        )
        
        return commonPasswords.contains(password.lowercase())
    }
    
    fun getPasswordStrength(password: String): PasswordStrength {
        val validation = validatePassword(password)
        if (!validation.isValid) {
            return PasswordStrength.WEAK
        }
        
        var score = 0
        
        // Length bonus
        when {
            password.length >= 12 -> score += 2
            password.length >= 10 -> score += 1
        }
        
        // Character variety bonus
        if (UPPERCASE_PATTERN.matcher(password).find()) score++
        if (LOWERCASE_PATTERN.matcher(password).find()) score++
        if (DIGIT_PATTERN.matcher(password).find()) score++
        if (SPECIAL_CHAR_PATTERN.matcher(password).find()) score++
        
        return when {
            score >= 6 -> PasswordStrength.STRONG
            score >= 4 -> PasswordStrength.MEDIUM
            else -> PasswordStrength.WEAK
        }
    }
    
    enum class PasswordStrength {
        WEAK, MEDIUM, STRONG
    }
}
