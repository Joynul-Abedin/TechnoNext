package com.shokal.technonext.utils

import android.util.Patterns
import com.shokal.technonext.data.preferences.UserPreferences
import com.shokal.technonext.data.repository.AuthRepository
import com.shokal.technonext.data.repository.FavoriteRepository
import com.shokal.technonext.data.repository.PostRepository
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import java.util.regex.Pattern

object MockUtils {
    
    fun initMocks() {
        MockKAnnotations.init(this)
    }
    
    fun initMocksWithPatterns() {
        MockKAnnotations.init(this)
        // Note: We'll handle email validation differently in tests
        // by creating a test-specific version of the AuthViewModel
    }
    
    // Simple email validation for testing
    fun isValidEmail(email: String): Boolean {
        val emailPattern = Pattern.compile("^[A-Za-z0-9+_.-]+@([A-Za-z0-9.-]+\\.[A-Za-z]{2,})$")
        return emailPattern.matcher(email).matches()
    }
    
    fun createMockAuthRepository(): AuthRepository = mockk<AuthRepository>().apply {
        coEvery { register(any(), any()) } returns Result.success(Unit)
        coEvery { login(any(), any()) } returns Result.success(createMockUser())
    }
    
    fun createMockPostRepository(): PostRepository = mockk<PostRepository>().apply {
        every { getAllPosts() } returns flowOf(TestDataFactory.createPostList())
        every { getFavoritePosts() } returns flowOf(TestDataFactory.createPostList(2))
        coEvery { refreshPosts() } returns Result.success(Unit)
        coEvery { loadPostsPage(any(), any()) } returns Result.success(TestDataFactory.createPostList())
    }
    
    fun createMockFavoriteRepository(): FavoriteRepository = mockk<FavoriteRepository>().apply {
        every { getFavoritesByUser(any()) } returns flowOf(TestDataFactory.createFavoriteList("test@example.com"))
        coEvery { addToFavorites(any(), any()) } returns Result.success(Unit)
        coEvery { removeFromFavorites(any(), any()) } returns Result.success(Unit)
    }
    
    fun createMockUserPreferences(): UserPreferences = mockk<UserPreferences>().apply {
        every { isLoggedIn } returns flowOf(true)
        every { userEmail } returns flowOf("test@example.com")
        every { userName } returns flowOf("Test User")
        coEvery { setLoggedIn(any(), any(), any()) } returns Unit
        coEvery { logout() } returns Unit
    }
    
    private fun createMockUser() = TestDataFactory.createUser()
    
    // Helper functions for setting up mock behaviors
    fun setupAuthRepositorySuccess(authRepository: AuthRepository) {
        coEvery { authRepository.register(any(), any()) } returns Result.success(Unit)
        coEvery { authRepository.login(any(), any()) } returns Result.success(createMockUser())
    }
    
    fun setupAuthRepositoryFailure(authRepository: AuthRepository, errorMessage: String = "Test error") {
        coEvery { authRepository.register(any(), any()) } returns Result.failure(Exception(errorMessage))
        coEvery { authRepository.login(any(), any()) } returns Result.failure(Exception(errorMessage))
    }
    
    fun setupPostRepositorySuccess(postRepository: PostRepository, posts: List<com.shokal.technonext.data.model.Post> = TestDataFactory.createPostList()) {
        every { postRepository.getAllPosts() } returns flowOf(posts)
        every { postRepository.getFavoritePosts() } returns flowOf(posts.filter { it.isFavorite })
        coEvery { postRepository.refreshPosts() } returns Result.success(Unit)
        coEvery { postRepository.loadPostsPage(any(), any()) } answers {
            val page = firstArg<Int>()
            val pageSize = secondArg<Int>()
            val startIndex = (page - 1) * pageSize
            val endIndex = minOf(startIndex + pageSize, posts.size)
            val pagePosts = posts.subList(startIndex, endIndex)
            Result.success(pagePosts)
        }
    }
    
    fun setupPostRepositoryFailure(postRepository: PostRepository, errorMessage: String = "Test error") {
        every { postRepository.getAllPosts() } returns flowOf(emptyList())
        every { postRepository.getFavoritePosts() } returns flowOf(emptyList())
        coEvery { postRepository.refreshPosts() } returns Result.failure(Exception(errorMessage))
        coEvery { postRepository.loadPostsPage(any(), any()) } returns Result.failure(Exception(errorMessage))
    }
    
    fun setupUserPreferencesLoggedIn(userPreferences: UserPreferences, email: String = "test@example.com") {
        every { userPreferences.isLoggedIn } returns flowOf(true)
        every { userPreferences.userEmail } returns flowOf(email)
        every { userPreferences.userName } returns flowOf("Test User")
    }
    
    fun setupUserPreferencesLoggedOut(userPreferences: UserPreferences) {
        every { userPreferences.isLoggedIn } returns flowOf(false)
        every { userPreferences.userEmail } returns flowOf(null)
        every { userPreferences.userName } returns flowOf(null)
    }
}
