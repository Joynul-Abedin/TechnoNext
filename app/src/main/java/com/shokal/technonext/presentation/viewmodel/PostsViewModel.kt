package com.shokal.technonext.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shokal.technonext.data.model.Post
import com.shokal.technonext.data.preferences.UserPreferences
import com.shokal.technonext.data.repository.FavoriteRepository
import com.shokal.technonext.data.repository.PostRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PostsUiState(
    val posts: List<Post> = emptyList(),
    val allLoadedPosts: List<Post> = emptyList(), // All posts loaded so far
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val errorMessage: String? = null,
    val searchQuery: String = "",
    val showFavoritesOnly: Boolean = false,
    val favoritePostIds: Set<Int> = emptySet(),
    val hasMorePosts: Boolean = true,
    val currentPage: Int = 1,
    val isSearching: Boolean = false
)

@HiltViewModel
class PostsViewModel @Inject constructor(
    private val postRepository: PostRepository,
    private val favoriteRepository: FavoriteRepository,
    private val userPreferences: UserPreferences
) : ViewModel() {
    
    companion object {
        private const val POSTS_PER_PAGE = 10
    }
    
    private val _uiState = MutableStateFlow(PostsUiState())
    val uiState: StateFlow<PostsUiState> = _uiState.asStateFlow()
    
    private val _searchQuery = MutableStateFlow("")
    private val _showFavoritesOnly = MutableStateFlow(false)
    private val _favoritePostIds = MutableStateFlow<Set<Int>>(emptySet())
    private val _searchInTitle = MutableStateFlow(true)
    private val _searchInBody = MutableStateFlow(true)
    private val _allLoadedPosts = MutableStateFlow<List<Post>>(emptyList())
    private val _currentPage = MutableStateFlow(1)
    private val _hasMorePosts = MutableStateFlow(true)
    private var lastLoadTime = 0L
    
    init {
        observeFavorites()
        observePosts()
        loadInitialPostsIfNeeded()
    }
    
    private fun loadInitialPostsIfNeeded() {
        // Only load initial posts if we don't have any loaded posts yet
        if (_allLoadedPosts.value.isEmpty()) {
            refreshPosts()
        }
    }
    
    private fun observePosts() {
        viewModelScope.launch {
            combine(
                _searchQuery,
                _showFavoritesOnly,
                _favoritePostIds,
                _allLoadedPosts
            ) { query, showFavorites, favoriteIds, allLoadedPosts ->
                Quadruple(query, showFavorites, favoriteIds, allLoadedPosts)
            }.collect { (query, showFavorites, favoriteIds, allLoadedPosts) ->
                when {
                    showFavorites -> {
                        // Handle favorites - get from database
                        launch {
                            postRepository.getFavoritePosts().collect { favoritePosts ->
                                _uiState.value = _uiState.value.copy(
                                    posts = favoritePosts,
                                    searchQuery = query,
                                    showFavoritesOnly = showFavorites,
                                    favoritePostIds = favoriteIds,
                                    isSearching = false,
                                    isLoading = false
                                )
                            }
                        }
                    }
                    query.isNotBlank() -> {
                        // Search in loaded posts only
                        val filteredPosts = allLoadedPosts.filter { post ->
                            post.title.contains(query, ignoreCase = true) || 
                            post.body.contains(query, ignoreCase = true)
                        }
                        _uiState.value = _uiState.value.copy(
                            posts = filteredPosts,
                            searchQuery = query,
                            showFavoritesOnly = showFavorites,
                            favoritePostIds = favoriteIds,
                            isSearching = true,
                            isLoading = false,
                            allLoadedPosts = allLoadedPosts
                        )
                    }
                    else -> {
                        // Show all loaded posts
                        _uiState.value = _uiState.value.copy(
                            posts = allLoadedPosts,
                            searchQuery = query,
                            showFavoritesOnly = showFavorites,
                            favoritePostIds = favoriteIds,
                            isSearching = false,
                            isLoading = false,
                            allLoadedPosts = allLoadedPosts
                        )
                    }
                }
            }
        }
    }
    
    private data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
    
    
    private fun observeFavorites() {
        viewModelScope.launch {
            try {
                userPreferences.userEmail.collect { userEmail ->
                    if (userEmail != null) {
                        val favorites = favoriteRepository.getFavoritesByUser(userEmail).first()
                        val favoriteIds = favorites.map { it.postId }.toSet()
                        _favoritePostIds.value = favoriteIds
                        
                        // Also update the UI state directly
                        _uiState.value = _uiState.value.copy(
                            favoritePostIds = favoriteIds
                        )
                    } else {
                        // Clear favorites if user is not logged in
                        _favoritePostIds.value = emptySet()
                        _uiState.value = _uiState.value.copy(
                            favoritePostIds = emptySet()
                        )
                    }
                }
            } catch (e: Exception) {
                // Handle error silently for now
            }
        }
    }
    
    fun refreshPosts() {
        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
        
        viewModelScope.launch {
            try {
                // First refresh from API
                val result = postRepository.refreshPosts()
                result.onSuccess {
                    // After refreshing, load initial posts
                    loadInitialPosts()
                }.onFailure { exception ->
                    // If API fails, try to load from local database
                    loadFromLocalDatabase()
                }
            } catch (e: Exception) {
                loadFromLocalDatabase()
            }
        }
    }
    
    fun forceRefreshPosts() {
        // Reset pagination state and force refresh
        _allLoadedPosts.value = emptyList()
        _currentPage.value = 1
        _hasMorePosts.value = true
        _searchQuery.value = ""
        refreshPosts()
    }
    
    private fun loadInitialPosts() {
        viewModelScope.launch {
            try {
                val result = postRepository.loadPostsPage(1, POSTS_PER_PAGE)
                result.onSuccess { posts ->
                    _allLoadedPosts.value = posts
                    _currentPage.value = 1
                    _hasMorePosts.value = posts.size == POSTS_PER_PAGE
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        allLoadedPosts = posts,
                        posts = posts,
                        currentPage = 1,
                        hasMorePosts = posts.size == POSTS_PER_PAGE
                    )
                }.onFailure { exception ->
                    loadFromLocalDatabase()
                }
            } catch (e: Exception) {
                loadFromLocalDatabase()
            }
        }
    }
    
    private suspend fun loadFromLocalDatabase() {
        try {
            val localPosts = postRepository.getAllPosts().first()
            if (localPosts.isNotEmpty()) {
                val initialPosts = localPosts.take(POSTS_PER_PAGE)
                _allLoadedPosts.value = initialPosts
                _currentPage.value = 1
                _hasMorePosts.value = localPosts.size > POSTS_PER_PAGE
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    allLoadedPosts = initialPosts,
                    posts = initialPosts,
                    currentPage = 1,
                    hasMorePosts = localPosts.size > POSTS_PER_PAGE,
                    errorMessage = "Offline mode: Showing cached posts (${localPosts.size} available)"
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "No posts available. Please check your internet connection and try again."
                )
            }
        } catch (dbException: Exception) {
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                errorMessage = "Failed to load posts: ${dbException.message}"
            )
        }
    }
    
    fun searchPosts(query: String) {
        _searchQuery.value = query
    }
    
    fun searchPostsAdvanced(query: String, searchInTitle: Boolean = true, searchInBody: Boolean = true, userId: Int? = null) {
        _searchQuery.value = query
        _searchInTitle.value = searchInTitle
        _searchInBody.value = searchInBody
        // The actual search logic is handled in observePosts() based on the searchQuery
    }
    
    fun toggleFavorite(post: Post) {
        viewModelScope.launch {
            try {
                val userEmail = userPreferences.userEmail.first()
                if (userEmail != null) {
                    val isCurrentlyFavorite = _favoritePostIds.value.contains(post.id)
                    
                    // Update local state immediately for responsive UI
                    val newFavoriteIds = if (isCurrentlyFavorite) {
                        _favoritePostIds.value - post.id
                    } else {
                        _favoritePostIds.value + post.id
                    }
                    _favoritePostIds.value = newFavoriteIds
                    
                    // Also update the UI state immediately
                    _uiState.value = _uiState.value.copy(
                        favoritePostIds = newFavoriteIds
                    )
                    
                    // Update database
                    if (isCurrentlyFavorite) {
                        favoriteRepository.removeFromFavorites(post.id, userEmail)
                    } else {
                        favoriteRepository.addToFavorites(post, userEmail)
                    }
                } else {
                    _uiState.value = _uiState.value.copy(
                        errorMessage = "User not logged in"
                    )
                }
            } catch (e: Exception) {
                // Revert local state on error
                val isCurrentlyFavorite = _favoritePostIds.value.contains(post.id)
                val newFavoriteIds = if (isCurrentlyFavorite) {
                    _favoritePostIds.value - post.id
                } else {
                    _favoritePostIds.value + post.id
                }
                _favoritePostIds.value = newFavoriteIds
                _uiState.value = _uiState.value.copy(
                    favoritePostIds = newFavoriteIds,
                    errorMessage = e.message ?: "Failed to update favorite"
                )
            }
        }
    }
    
    fun showFavoritesOnly(show: Boolean) {
        _showFavoritesOnly.value = show
    }
    
    fun toggleShowFavoritesOnly() {
        _showFavoritesOnly.value = !_showFavoritesOnly.value
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
    
    fun refreshFavorites() {
        viewModelScope.launch {
            try {
                val userEmail = userPreferences.userEmail.first()
                if (userEmail != null) {
                    // Get the current favorites once
                    val favorites = favoriteRepository.getFavoritesByUser(userEmail).first()
                    val favoriteIds = favorites.map { it.postId }.toSet()
                    _favoritePostIds.value = favoriteIds
                    
                    // Also update the UI state directly
                    _uiState.value = _uiState.value.copy(
                        favoritePostIds = favoriteIds
                    )
                }
            } catch (e: Exception) {
                // Handle error silently for now
            }
        }
    }
    
    fun loadMorePosts() {
        val currentTime = System.currentTimeMillis()
        
        // Debounce: prevent rapid calls within 1 second
        if (currentTime - lastLoadTime < 1000) {
            return
        }
        
        if (_uiState.value.isLoadingMore || 
            !_uiState.value.hasMorePosts || 
            _uiState.value.isSearching || 
            _uiState.value.showFavoritesOnly) {
            return
        }
        
        lastLoadTime = currentTime
        _uiState.value = _uiState.value.copy(isLoadingMore = true)
        
        viewModelScope.launch {
            try {
                val nextPage = _currentPage.value + 1
                val result = postRepository.loadPostsPage(nextPage, POSTS_PER_PAGE)
                
                result.onSuccess { newPosts ->
                    if (newPosts.isEmpty()) {
                        _hasMorePosts.value = false
                    } else {
                        _currentPage.value = nextPage
                        val updatedPosts = _allLoadedPosts.value + newPosts
                        _allLoadedPosts.value = updatedPosts
                        _hasMorePosts.value = newPosts.size == POSTS_PER_PAGE
                        
                        // Update UI state with new posts
                        _uiState.value = _uiState.value.copy(
                            allLoadedPosts = updatedPosts,
                            posts = updatedPosts,
                            currentPage = nextPage,
                            hasMorePosts = _hasMorePosts.value,
                            isLoadingMore = false
                        )
                    }
                }.onFailure { exception ->
                    // Try to load more from local database in offline mode
                    try {
                        val localPosts = postRepository.getAllPosts().first()
                        val currentPosts = _allLoadedPosts.value
                        val remainingPosts = localPosts.drop(currentPosts.size)
                        if (remainingPosts.isNotEmpty()) {
                            val newPosts = remainingPosts.take(POSTS_PER_PAGE)
                            val updatedPosts = currentPosts + newPosts
                            _allLoadedPosts.value = updatedPosts
                            _currentPage.value = _currentPage.value + 1
                            _hasMorePosts.value = remainingPosts.size > POSTS_PER_PAGE
                            
                            _uiState.value = _uiState.value.copy(
                                allLoadedPosts = updatedPosts,
                                posts = updatedPosts,
                                currentPage = _currentPage.value,
                                hasMorePosts = _hasMorePosts.value,
                                isLoadingMore = false,
                                errorMessage = "Offline mode: Loaded more cached posts"
                            )
                        } else {
                            _hasMorePosts.value = false
                            _uiState.value = _uiState.value.copy(
                                isLoadingMore = false,
                                hasMorePosts = false,
                                errorMessage = "No more posts available"
                            )
                        }
                    } catch (dbException: Exception) {
                        _uiState.value = _uiState.value.copy(
                            isLoadingMore = false,
                            errorMessage = "Failed to load more posts: ${exception.message}"
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoadingMore = false,
                    errorMessage = "Failed to load more posts: ${e.message}"
                )
            }
        }
    }

    fun setLoadingMore(isLoading: Boolean) {
        _uiState.value = _uiState.value.copy(
            isLoadingMore = isLoading,
            hasMorePosts = isLoading
        )
    }
}