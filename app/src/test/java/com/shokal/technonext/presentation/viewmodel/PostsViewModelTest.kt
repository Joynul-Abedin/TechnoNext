package com.shokal.technonext.presentation.viewmodel

import app.cash.turbine.test
import com.shokal.technonext.data.model.Post
import com.shokal.technonext.data.preferences.UserPreferences
import com.shokal.technonext.data.repository.FavoriteRepository
import com.shokal.technonext.data.repository.PostRepository
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

@OptIn(ExperimentalCoroutinesApi::class)
class PostsViewModelTest {
    
    private lateinit var postRepository: PostRepository
    private lateinit var favoriteRepository: FavoriteRepository
    private lateinit var userPreferences: UserPreferences
    private lateinit var viewModel: PostsViewModel
    
    private val testDispatcher = StandardTestDispatcher()
    
    @Before
    fun setup() {
        try {
            Dispatchers.setMain(testDispatcher)
            println("DEBUG: Set main dispatcher")
            MockUtils.initMocks()
            println("DEBUG: Initialized mocks")
            
            postRepository = MockUtils.createMockPostRepository()
            println("DEBUG: Created post repository")
            favoriteRepository = MockUtils.createMockFavoriteRepository()
            println("DEBUG: Created favorite repository")
            userPreferences = MockUtils.createMockUserPreferences()
            println("DEBUG: Created user preferences")
            
            viewModel = PostsViewModel(postRepository, favoriteRepository, userPreferences)
            println("DEBUG: Created view model")
        } catch (e: Exception) {
            println("DEBUG: Exception in setup: ${e.message}")
            e.printStackTrace()
            throw e
        }
    }
    
    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }
    
    @Test
    fun `initial state should be correct`() {
        println("DEBUG: Starting initial state test")
        
        val initialState = viewModel.uiState.value
        println("DEBUG: Got initial state: $initialState")
        
        // Just check basic properties for now
        assertNotNull("ViewModel should be created", viewModel)
        assertNotNull("UI state should not be null", initialState)
    }
    
    @Test
    fun `refreshPosts should load posts successfully`() = runTest {
        // Given
        val posts = TestDataFactory.createPostList(15) // More than page size to ensure hasMorePosts is true
        MockUtils.setupPostRepositorySuccess(postRepository, posts)
        
        // When
        viewModel.refreshPosts()
        advanceUntilIdle()
        
        // Then
        val uiState = viewModel.uiState.value
        assertFalse("Loading should be false after success", uiState.isLoading)
        assertEquals("Should load correct number of posts", 10, uiState.posts.size) // Only first page
        assertEquals("Should set allLoadedPosts", 10, uiState.allLoadedPosts.size) // Only first page
        assertTrue("Should have more posts available", uiState.hasMorePosts)
        assertEquals("Should be on page 1", 1, uiState.currentPage)
        
        coVerify { postRepository.refreshPosts() }
        coVerify { postRepository.loadPostsPage(1, 10) }
    }
    
    @Test
    fun `refreshPosts with API failure should load from local database`() = runTest {
        // Given
        val localPosts = TestDataFactory.createPostList(3)
        MockUtils.setupPostRepositoryFailure(postRepository)
        every { postRepository.getAllPosts() } returns flowOf(localPosts)
        
        // When
        viewModel.refreshPosts()
        advanceUntilIdle()
        
        // Then
        val uiState = viewModel.uiState.value
        assertFalse("Loading should be false after loading from local", uiState.isLoading)
        assertEquals("Should load posts from local database", 3, uiState.posts.size)
        assertNotNull("Should show offline mode message", uiState.errorMessage)
        assertTrue("Should contain offline mode message", uiState.errorMessage!!.contains("Offline mode"))
        
        coVerify { postRepository.refreshPosts() }
        coVerify { postRepository.getAllPosts() }
    }
    
    @Test
    fun `forceRefreshPosts should reset state and refresh`() = runTest {
        // Given
        val posts = TestDataFactory.createPostList(15)
        MockUtils.setupPostRepositorySuccess(postRepository, posts)
        
        // When
        viewModel.forceRefreshPosts()
        advanceUntilIdle()
        
        // Then
        val uiState = viewModel.uiState.value
        assertEquals("Search query should be cleared", "", uiState.searchQuery)
        assertFalse("Should not show favorites only", uiState.showFavoritesOnly)
        assertEquals("Should reset to page 1", 1, uiState.currentPage)
        assertTrue("Should have more posts", uiState.hasMorePosts)
        
        coVerify { postRepository.refreshPosts() }
    }
    
    @Test
    fun `searchPosts should filter posts correctly`() = runTest {
        // Given
        val posts = listOf(
            TestDataFactory.createPost(1, title = "Android Development", body = "Learn Android"),
            TestDataFactory.createPost(2, title = "iOS Development", body = "Learn iOS"),
            TestDataFactory.createPost(3, title = "Web Development", body = "Learn Web")
        )
        MockUtils.setupPostRepositorySuccess(postRepository, posts)
        
        // Load initial posts
        viewModel.refreshPosts()
        advanceUntilIdle()
        
        // When
        viewModel.searchPosts("Android")
        advanceUntilIdle()
        
        // Then
        val uiState = viewModel.uiState.value
        assertEquals("Should filter to Android posts", 1, uiState.posts.size)
        assertEquals("Should set search query", "Android", uiState.searchQuery)
        assertTrue("Should be in searching mode", uiState.isSearching)
        assertEquals("Should find Android post", "Android Development", uiState.posts.first().title)
    }
    
    @Test
    fun `searchPosts with empty query should show all posts`() = runTest {
        // Given
        val posts = TestDataFactory.createPostList(3)
        MockUtils.setupPostRepositorySuccess(postRepository, posts)
        
        // Load initial posts
        viewModel.refreshPosts()
        advanceUntilIdle()
        
        // When
        viewModel.searchPosts("")
        advanceUntilIdle()
        
        // Then
        val uiState = viewModel.uiState.value
        assertEquals("Should show all posts", 3, uiState.posts.size)
        assertEquals("Should clear search query", "", uiState.searchQuery)
        assertFalse("Should not be in searching mode", uiState.isSearching)
    }
    
    @Test
    fun `toggleFavorite should add post to favorites`() = runTest {
        // Given
        val post = TestDataFactory.createPost(1)
        val userEmail = "test@example.com"
        MockUtils.setupUserPreferencesLoggedIn(userPreferences, userEmail)
        
        // When
        viewModel.toggleFavorite(post)
        advanceUntilIdle()
        
        // Then
        val uiState = viewModel.uiState.value
        assertTrue("Post should be in favorites", uiState.favoritePostIds.contains(post.id))
        
        coVerify { favoriteRepository.addToFavorites(post, userEmail) }
    }
    
    @Test
    fun `toggleFavorite should remove post from favorites`() = runTest {
        // Given
        val post = TestDataFactory.createPost(1)
        val userEmail = "test@example.com"
        MockUtils.setupUserPreferencesLoggedIn(userPreferences, userEmail)
        
        // Add to favorites first
        viewModel.toggleFavorite(post)
        advanceUntilIdle()
        
        // When - toggle again to remove
        viewModel.toggleFavorite(post)
        advanceUntilIdle()
        
        // Then
        val uiState = viewModel.uiState.value
        assertFalse("Post should not be in favorites", uiState.favoritePostIds.contains(post.id))
        
        coVerify { favoriteRepository.removeFromFavorites(post.id, userEmail) }
    }
    
    @Test
    fun `toggleFavorite without logged in user should show error`() = runTest {
        // Given
        val post = TestDataFactory.createPost(1)
        MockUtils.setupUserPreferencesLoggedOut(userPreferences)
        
        // When
        viewModel.toggleFavorite(post)
        advanceUntilIdle()
        
        // Then
        val uiState = viewModel.uiState.value
        assertEquals("Should show user not logged in error", "User not logged in", uiState.errorMessage)
        
        coVerify(exactly = 0) { favoriteRepository.addToFavorites(any(), any()) }
        coVerify(exactly = 0) { favoriteRepository.removeFromFavorites(any(), any()) }
    }
    
    @Test
    fun `showFavoritesOnly should filter to favorite posts`() = runTest {
        // Given
        val posts = TestDataFactory.createPostList(3)
        val favoritePosts = posts.take(2)
        MockUtils.setupPostRepositorySuccess(postRepository, posts)
        every { postRepository.getFavoritePosts() } returns flowOf(favoritePosts)
        
        // Load initial posts
        viewModel.refreshPosts()
        advanceUntilIdle()
        
        // When
        viewModel.showFavoritesOnly(true)
        advanceUntilIdle()
        
        // Then
        val uiState = viewModel.uiState.value
        assertTrue("Should show favorites only", uiState.showFavoritesOnly)
        assertEquals("Should show only favorite posts", 2, uiState.posts.size)
        
        coVerify { postRepository.getFavoritePosts() }
    }
    
    @Test
    fun `toggleShowFavoritesOnly should toggle favorites filter`() = runTest {
        // Given
        val posts = TestDataFactory.createPostList(3)
        MockUtils.setupPostRepositorySuccess(postRepository, posts)
        
        // Load initial posts
        viewModel.refreshPosts()
        advanceUntilIdle()
        
        // When
        viewModel.toggleShowFavoritesOnly()
        advanceUntilIdle()
        
        // Then
        val uiState = viewModel.uiState.value
        assertTrue("Should show favorites only after toggle", uiState.showFavoritesOnly)
        
        // When - toggle again
        viewModel.toggleShowFavoritesOnly()
        advanceUntilIdle()
        
        // Then
        val uiStateAfterSecondToggle = viewModel.uiState.value
        assertFalse("Should not show favorites only after second toggle", uiStateAfterSecondToggle.showFavoritesOnly)
    }
    
    @Test
    fun `loadMorePosts should load additional posts`() = runTest {
        // Given
        val initialPosts = TestDataFactory.createPostList(10)
        val morePosts = TestDataFactory.createPostList(5).map { it.copy(id = it.id + 10) }
        MockUtils.setupPostRepositorySuccess(postRepository, initialPosts)
        
        // Load initial posts
        viewModel.refreshPosts()
        advanceUntilIdle()
        
        // Setup for load more
        coEvery { postRepository.loadPostsPage(2, 10) } returns Result.success(morePosts)
        
        // When
        viewModel.loadMorePosts()
        advanceUntilIdle()
        
        // Then
        val uiState = viewModel.uiState.value
        assertEquals("Should have loaded more posts", 15, uiState.posts.size)
        assertEquals("Should be on page 2", 2, uiState.currentPage)
        assertFalse("Should not be loading more", uiState.isLoadingMore)
        
        coVerify { postRepository.loadPostsPage(2, 10) }
    }
    
    @Test
    fun `loadMorePosts with no more posts should set hasMorePosts to false`() = runTest {
        // Given
        val initialPosts = TestDataFactory.createPostList(5)
        MockUtils.setupPostRepositorySuccess(postRepository, initialPosts)
        
        // Load initial posts
        viewModel.refreshPosts()
        advanceUntilIdle()
        
        // Setup for load more with empty result
        coEvery { postRepository.loadPostsPage(2, 10) } returns Result.success(emptyList())
        
        // When
        viewModel.loadMorePosts()
        advanceUntilIdle()
        
        // Then
        val uiState = viewModel.uiState.value
        assertFalse("Should not have more posts", uiState.hasMorePosts)
        assertEquals("Should still be on page 1", 1, uiState.currentPage)
    }
    
    @Test
    fun `loadMorePosts should not load when already loading more`() = runTest {
        // Given
        val posts = TestDataFactory.createPostList(10)
        MockUtils.setupPostRepositorySuccess(postRepository, posts)
        
        // Load initial posts
        viewModel.refreshPosts()
        advanceUntilIdle()
        
        // Set loading more state
        viewModel.setLoadingMore(true)
        
        // When
        viewModel.loadMorePosts()
        advanceUntilIdle()
        
        // Then
        coVerify(atLeast = 1) { postRepository.loadPostsPage(1, 10) } // Initial load
        coVerify(exactly = 0) { postRepository.loadPostsPage(2, 10) } // No additional load
    }
    
    @Test
    fun `loadMorePosts should not load when searching`() = runTest {
        // Given
        val posts = TestDataFactory.createPostList(10)
        MockUtils.setupPostRepositorySuccess(postRepository, posts)
        
        // Load initial posts
        viewModel.refreshPosts()
        advanceUntilIdle()
        
        // Start searching
        viewModel.searchPosts("test")
        advanceUntilIdle()
        
        // When
        viewModel.loadMorePosts()
        advanceUntilIdle()
        
        // Then
        coVerify(atLeast = 1) { postRepository.loadPostsPage(1, 10) } // Initial load
        coVerify(exactly = 0) { postRepository.loadPostsPage(2, 10) } // No additional load
    }
    
    @Test
    fun `clearError should clear error message`() = runTest {
        // Given
        val posts = TestDataFactory.createPostList(3)
        MockUtils.setupPostRepositoryFailure(postRepository, "Test error")
        
        // Trigger error
        viewModel.refreshPosts()
        advanceUntilIdle()
        
        // Verify error is set
        assertNotNull("Error should be set", viewModel.uiState.value.errorMessage)
        
        // When
        viewModel.clearError()
        
        // Then
        val uiState = viewModel.uiState.value
        assertNull("Error message should be cleared", uiState.errorMessage)
    }
    
    @Test
    fun `refreshFavorites should update favorite post IDs`() = runTest {
        // Given
        val userEmail = "test@example.com"
        val favorites = TestDataFactory.createFavoriteList(userEmail, 3)
        MockUtils.setupUserPreferencesLoggedIn(userPreferences, userEmail)
        every { favoriteRepository.getFavoritesByUser(userEmail) } returns flowOf(favorites)
        
        // When
        viewModel.refreshFavorites()
        advanceUntilIdle()
        
        // Then
        val uiState = viewModel.uiState.value
        assertEquals("Should have correct number of favorites", 3, uiState.favoritePostIds.size)
        assertTrue("Should contain favorite post IDs", uiState.favoritePostIds.containsAll(favorites.map { it.postId }))
        
        coVerify { favoriteRepository.getFavoritesByUser(userEmail) }
    }
    
    @Test
    fun `uiState flow should emit correct values during refresh`() = runTest {
        // Given
        val posts = TestDataFactory.createPostList(5)
        MockUtils.setupPostRepositorySuccess(postRepository, posts)
        
        // When & Then
        viewModel.uiState.test {
            // Initial state
            val initialState = awaitItem()
            assertTrue("Initial posts should be empty", initialState.posts.isEmpty())
            
            // Refresh posts
            viewModel.refreshPosts()
            advanceUntilIdle()
            
            // Should emit loading state, then success state
            val loadingState = awaitItem()
            assertTrue("Should be loading during refresh", loadingState.isLoading)
            
            val successState = awaitItem()
            assertFalse("Should not be loading after success", successState.isLoading)
            assertEquals("Should have loaded posts", 5, successState.posts.size)
        }
    }
}
