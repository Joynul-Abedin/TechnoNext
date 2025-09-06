package com.shokal.technonext.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shokal.technonext.data.model.Favorite
import com.shokal.technonext.data.preferences.UserPreferences
import com.shokal.technonext.data.repository.FavoriteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

data class FavoritesUiState(
    val isLoading: Boolean = false,
    val favorites: List<Favorite> = emptyList(),
    val errorMessage: String? = null
)

@HiltViewModel
class FavoritesViewModel @Inject constructor(
    private val favoriteRepository: FavoriteRepository,
    private val userPreferences: UserPreferences
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(FavoritesUiState())
    val uiState: StateFlow<FavoritesUiState> = _uiState.asStateFlow()
    
    fun loadFavorites() {
        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
        
        viewModelScope.launch {
            try {
                val userEmail = userPreferences.userEmail.first()
                if (userEmail != null) {
                    favoriteRepository.getFavoritesByUser(userEmail).collect { favorites ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            favorites = favorites
                        )
                    }
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "User not logged in"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Failed to load favorites"
                )
            }
        }
    }
    
    fun removeFromFavorites(postId: Int) {
        viewModelScope.launch {
            try {
                val userEmail = userPreferences.userEmail.first()
                if (userEmail != null) {
                    favoriteRepository.removeFromFavorites(postId, userEmail)
                        .onSuccess {
                            // Favorites will be updated automatically through the Flow
                        }
                        .onFailure { exception ->
                            _uiState.value = _uiState.value.copy(
                                errorMessage = exception.message ?: "Failed to remove from favorites"
                            )
                        }
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = e.message ?: "Failed to remove from favorites"
                )
            }
        }
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}
