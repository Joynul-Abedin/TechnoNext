package com.shokal.technonext.data.repository

import com.shokal.technonext.data.api.ApiService
import com.shokal.technonext.data.dao.CommentDao
import com.shokal.technonext.data.model.Comment
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CommentRepository @Inject constructor(
    private val apiService: ApiService,
    private val commentDao: CommentDao
) {
    
    fun getCommentsByPostId(postId: Int): Flow<List<Comment>> = commentDao.getCommentsByPostId(postId)
    
    suspend fun loadCommentsForPost(postId: Int): Result<List<Comment>> {
        return try {
            val response = apiService.getCommentsForPost(postId)
            if (response.isSuccessful) {
                response.body()?.let { comments ->
                    // Store comments in database
                    commentDao.insertComments(comments)
                    Result.success(comments)
                } ?: Result.failure(Exception("Empty response body"))
            } else {
                // Try to get from local database
                val localComments = commentDao.getCommentsByPostId(postId).first()
                if (localComments.isNotEmpty()) {
                    Result.success(localComments)
                } else {
                    Result.failure(Exception("Failed to fetch comments: ${response.message()}"))
                }
            }
        } catch (e: Exception) {
            // Try to get from local database
            try {
                val localComments = commentDao.getCommentsByPostId(postId).first()
                if (localComments.isNotEmpty()) {
                    Result.success(localComments)
                } else {
                    Result.failure(e)
                }
            } catch (dbException: Exception) {
                Result.failure(e)
            }
        }
    }
    
    suspend fun getCommentCountForPost(postId: Int): Int = commentDao.getCommentCountForPost(postId)
    
    suspend fun clearCommentsForPost(postId: Int) = commentDao.deleteCommentsByPostId(postId)
}
