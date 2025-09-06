package com.shokal.technonext.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shokal.technonext.data.model.Comment
import com.shokal.technonext.data.model.Post
import com.shokal.technonext.data.repository.CommentRepository
import com.shokal.technonext.data.repository.PostRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PostDetailsUiState(
    val post: Post? = null,
    val comments: List<Comment> = emptyList(),
    val isLoading: Boolean = false,
    val isLoadingComments: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class PostDetailsViewModel @Inject constructor(
    private val postRepository: PostRepository,
    private val commentRepository: CommentRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(PostDetailsUiState())
    val uiState: StateFlow<PostDetailsUiState> = _uiState.asStateFlow()
    
    fun loadPostDetails(postId: Int) {
        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
        
        viewModelScope.launch {
            try {
                // First, try to get the post from the loaded posts
                val allPosts = postRepository.getAllPosts().first()
                val post = allPosts.find { it.id == postId }
                
                if (post != null) {
                    _uiState.value = _uiState.value.copy(
                        post = post,
                        isLoading = false
                    )
                    
                    // Load comments for this post
                    loadComments(postId)
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "Post not found"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Failed to load post: ${e.message}"
                )
            }
        }
    }
    
    private fun loadComments(postId: Int) {
        _uiState.value = _uiState.value.copy(isLoadingComments = true)
        
        viewModelScope.launch {
            try {
                // First check if we have comments in the database
                val existingComments = commentRepository.getCommentsByPostId(postId).first()
                
                if (existingComments.isNotEmpty()) {
                    // Use existing comments from database
                    _uiState.value = _uiState.value.copy(
                        comments = existingComments,
                        isLoadingComments = false
                    )
                } else {
                    // Load comments from API
                    val result = commentRepository.loadCommentsForPost(postId)
                    result.onSuccess { comments ->
                        _uiState.value = _uiState.value.copy(
                            comments = comments,
                            isLoadingComments = false
                        )
                    }.onFailure { exception ->
                        _uiState.value = _uiState.value.copy(
                            isLoadingComments = false,
                            errorMessage = "Failed to load comments."
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoadingComments = false,
                    errorMessage = "Failed to load comments."
                )
            }
        }
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}
