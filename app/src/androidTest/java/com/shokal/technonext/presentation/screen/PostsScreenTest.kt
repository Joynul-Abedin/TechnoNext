package com.shokal.technonext.presentation.screen

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.shokal.technonext.data.model.Post
import com.shokal.technonext.presentation.viewmodel.PostsViewModel
import com.shokal.technonext.utils.TestDataFactory
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PostsScreenTest {
    
    @get:Rule
    val composeTestRule = createComposeRule()
    
    @Test
    fun postsScreen_displaysTopAppBar() {
        // Given
        val mockViewModel = createMockPostsViewModel()
        
        // When
        composeTestRule.setContent {
            PostsScreen(
                viewModel = mockViewModel,
                onNavigateToSettings = {},
                onNavigateToFavorites = {},
                onNavigateToPostDetails = {}
            )
        }
        
        // Then
        composeTestRule.onNodeWithText("Posts").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("View Favorites").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Settings").assertIsDisplayed()
    }
    
    @Test
    fun postsScreen_displaysSearchBar() {
        // Given
        val mockViewModel = createMockPostsViewModel()
        
        // When
        composeTestRule.setContent {
            PostsScreen(
                viewModel = mockViewModel,
                onNavigateToSettings = {},
                onNavigateToFavorites = {},
                onNavigateToPostDetails = {}
            )
        }
        
        // Then
        composeTestRule.onNodeWithText("Search posts...").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Search").assertIsDisplayed()
    }
    
    @Test
    fun postsScreen_displaysPostsWhenAvailable() {
        // Given
        val posts = TestDataFactory.createPostList(3)
        val mockViewModel = createMockPostsViewModel(posts = posts)
        
        // When
        composeTestRule.setContent {
            PostsScreen(
                viewModel = mockViewModel,
                onNavigateToSettings = {},
                onNavigateToFavorites = {},
                onNavigateToPostDetails = {}
            )
        }
        
        // Then
        posts.forEach { post ->
            composeTestRule.onNodeWithText(post.title.toTitleCase()).assertIsDisplayed()
            composeTestRule.onNodeWithText(post.body.toTitleCase()).assertIsDisplayed()
        }
    }
    
    @Test
    fun postsScreen_displaysEmptyStateWhenNoPosts() {
        // Given
        val mockViewModel = createMockPostsViewModel(posts = emptyList())
        
        // When
        composeTestRule.setContent {
            PostsScreen(
                viewModel = mockViewModel,
                onNavigateToSettings = {},
                onNavigateToFavorites = {},
                onNavigateToPostDetails = {}
            )
        }
        
        // Then
        composeTestRule.onNodeWithText("No posts found").assertIsDisplayed()
        composeTestRule.onNodeWithText("Retry").assertIsDisplayed()
    }
    
    @Test
    fun postsScreen_displaysLoadingIndicatorWhenLoading() {
        // Given
        val mockViewModel = createMockPostsViewModel(isLoading = true)
        
        // When
        composeTestRule.setContent {
            PostsScreen(
                viewModel = mockViewModel,
                onNavigateToSettings = {},
                onNavigateToFavorites = {},
                onNavigateToPostDetails = {}
            )
        }
        
        // Then
        composeTestRule.onNodeWithTag("CircularProgressIndicator").assertIsDisplayed()
    }
    
    @Test
    fun postsScreen_displaysErrorWhenErrorOccurs() {
        // Given
        val errorMessage = "Failed to load posts"
        val mockViewModel = createMockPostsViewModel(errorMessage = errorMessage)
        
        // When
        composeTestRule.setContent {
            PostsScreen(
                viewModel = mockViewModel,
                onNavigateToSettings = {},
                onNavigateToFavorites = {},
                onNavigateToPostDetails = {}
            )
        }
        
        // Then
        composeTestRule.onNodeWithText(errorMessage).assertIsDisplayed()
        composeTestRule.onNodeWithText("Dismiss").assertIsDisplayed()
    }
    
    @Test
    fun postsScreen_searchFunctionalityWorks() {
        // Given
        val posts = listOf(
            TestDataFactory.createPost(1, title = "Android Development", body = "Learn Android"),
            TestDataFactory.createPost(2, title = "iOS Development", body = "Learn iOS")
        )
        val mockViewModel = createMockPostsViewModel(posts = posts)
        
        // When
        composeTestRule.setContent {
            PostsScreen(
                viewModel = mockViewModel,
                onNavigateToSettings = {},
                onNavigateToFavorites = {},
                onNavigateToPostDetails = {}
            )
        }
        
        // Search for "Android"
        composeTestRule.onNodeWithText("Search posts...").performTextInput("Android")
        
        // Then
        composeTestRule.onNodeWithText("Android Development").assertIsDisplayed()
        composeTestRule.onNodeWithText("iOS Development").assertDoesNotExist()
    }
    
    @Test
    fun postsScreen_clearSearchButtonWorks() {
        // Given
        val posts = TestDataFactory.createPostList(2)
        val mockViewModel = createMockPostsViewModel(posts = posts)
        
        // When
        composeTestRule.setContent {
            PostsScreen(
                viewModel = mockViewModel,
                onNavigateToSettings = {},
                onNavigateToFavorites = {},
                onNavigateToPostDetails = {}
            )
        }
        
        // Enter search text
        composeTestRule.onNodeWithText("Search posts...").performTextInput("test")
        
        // Clear search
        composeTestRule.onNodeWithContentDescription("Clear Search").performClick()
        
        // Then
        composeTestRule.onNodeWithText("Search posts...").assertTextEquals("")
    }
    
    @Test
    fun postsScreen_displaysLoadMoreButtonWhenHasMorePosts() {
        // Given
        val posts = TestDataFactory.createPostList(10)
        val mockViewModel = createMockPostsViewModel(
            posts = posts,
            hasMorePosts = true,
            isLoading = false
        )
        
        // When
        composeTestRule.setContent {
            PostsScreen(
                viewModel = mockViewModel,
                onNavigateToSettings = {},
                onNavigateToFavorites = {},
                onNavigateToPostDetails = {}
            )
        }
        
        // Then
        composeTestRule.onNodeWithText("Load More Posts").assertIsDisplayed()
    }
    
    @Test
    fun postsScreen_displaysLoadingMoreState() {
        // Given
        val posts = TestDataFactory.createPostList(10)
        val mockViewModel = createMockPostsViewModel(
            posts = posts,
            hasMorePosts = true,
            isLoadingMore = true
        )
        
        // When
        composeTestRule.setContent {
            PostsScreen(
                viewModel = mockViewModel,
                onNavigateToSettings = {},
                onNavigateToFavorites = {},
                onNavigateToPostDetails = {}
            )
        }
        
        // Then
        composeTestRule.onNodeWithText("Loading...").assertIsDisplayed()
    }
    
    @Test
    fun postsScreen_favoriteButtonTogglesCorrectly() {
        // Given
        val posts = TestDataFactory.createPostList(1)
        val mockViewModel = createMockPostsViewModel(
            posts = posts,
            favoritePostIds = emptySet()
        )
        
        // When
        composeTestRule.setContent {
            PostsScreen(
                viewModel = mockViewModel,
                onNavigateToSettings = {},
                onNavigateToFavorites = {},
                onNavigateToPostDetails = {}
            )
        }
        
        // Then
        composeTestRule.onNodeWithContentDescription("Add to favorites").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Remove from favorites").assertDoesNotExist()
    }
    
    @Test
    fun postsScreen_favoriteButtonShowsFilledWhenFavorited() {
        // Given
        val posts = TestDataFactory.createPostList(1)
        val mockViewModel = createMockPostsViewModel(
            posts = posts,
            favoritePostIds = setOf(posts.first().id)
        )
        
        // When
        composeTestRule.setContent {
            PostsScreen(
                viewModel = mockViewModel,
                onNavigateToSettings = {},
                onNavigateToFavorites = {},
                onNavigateToPostDetails = {}
            )
        }
        
        // Then
        composeTestRule.onNodeWithContentDescription("Remove from favorites").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Add to favorites").assertDoesNotExist()
    }
    
    @Test
    fun postsScreen_navigationButtonsWork() {
        // Given
        var settingsClicked = false
        var favoritesClicked = false
        var postDetailsClicked = false
        val posts = TestDataFactory.createPostList(1)
        val mockViewModel = createMockPostsViewModel(posts = posts)
        
        // When
        composeTestRule.setContent {
            PostsScreen(
                viewModel = mockViewModel,
                onNavigateToSettings = { settingsClicked = true },
                onNavigateToFavorites = { favoritesClicked = true },
                onNavigateToPostDetails = { postDetailsClicked = true }
            )
        }
        
        // Click settings button
        composeTestRule.onNodeWithContentDescription("Settings").performClick()
        
        // Click favorites button
        composeTestRule.onNodeWithContentDescription("View Favorites").performClick()
        
        // Click on a post
        composeTestRule.onNodeWithText(posts.first().title.toTitleCase()).performClick()
        
        // Then
        assertTrue("Settings should be clicked", settingsClicked)
        assertTrue("Favorites should be clicked", favoritesClicked)
        assertTrue("Post details should be clicked", postDetailsClicked)
    }
    
    @Test
    fun postsScreen_displaysSearchStatusWhenSearching() {
        // Given
        val posts = TestDataFactory.createPostList(2)
        val mockViewModel = createMockPostsViewModel(
            posts = posts,
            isSearching = true
        )
        
        // When
        composeTestRule.setContent {
            PostsScreen(
                viewModel = mockViewModel,
                onNavigateToSettings = {},
                onNavigateToFavorites = {},
                onNavigateToPostDetails = {}
            )
        }
        
        // Enter search text
        composeTestRule.onNodeWithText("Search posts...").performTextInput("test")
        
        // Then
        composeTestRule.onNodeWithText("Searching for: \"test\"").assertIsDisplayed()
        composeTestRule.onNodeWithText("Found 2 results").assertIsDisplayed()
    }
    
    private fun createMockPostsViewModel(
        posts: List<Post> = emptyList(),
        isLoading: Boolean = false,
        isLoadingMore: Boolean = false,
        errorMessage: String? = null,
        hasMorePosts: Boolean = true,
        favoritePostIds: Set<Int> = emptySet(),
        isSearching: Boolean = false
    ): PostsViewModel {
        val mockViewModel = mockk<PostsViewModel>()
        
        val uiState = com.shokal.technonext.presentation.viewmodel.PostsUiState(
            posts = posts,
            allLoadedPosts = posts,
            isLoading = isLoading,
            isLoadingMore = isLoadingMore,
            errorMessage = errorMessage,
            searchQuery = "",
            showFavoritesOnly = false,
            favoritePostIds = favoritePostIds,
            hasMorePosts = hasMorePosts,
            currentPage = 1,
            isSearching = isSearching
        )
        
        every { mockViewModel.uiState } returns MutableStateFlow(uiState)
        
        return mockViewModel
    }
    
    // Extension function to convert text to Title Case (copied from PostsScreen)
    private fun String.toTitleCase(): String {
        return this.split(" ")
            .joinToString(" ") { word ->
                word.lowercase().replaceFirstChar {
                    if (it.isLowerCase()) it.titlecase() else it.toString()
                }
            }
    }
}
