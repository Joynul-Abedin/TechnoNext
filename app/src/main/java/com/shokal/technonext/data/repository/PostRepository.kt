package com.shokal.technonext.data.repository

import com.shokal.technonext.data.api.ApiService
import com.shokal.technonext.data.dao.PostDao
import com.shokal.technonext.data.model.Post
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PostRepository @Inject constructor(
    private val apiService: ApiService,
    private val postDao: PostDao
) {
    
    fun getAllPosts(): Flow<List<Post>> = postDao.getAllPosts()
    
    fun getFavoritePosts(): Flow<List<Post>> = postDao.getFavoritePosts()
    
    fun searchPosts(query: String): Flow<List<Post>> = postDao.searchPosts(query)
    
    fun searchPostsByTitle(query: String): Flow<List<Post>> = postDao.searchPostsByTitle(query)
    
    fun searchPostsByBody(query: String): Flow<List<Post>> = postDao.searchPostsByBody(query)
    
    fun searchPostsByUserId(userId: Int): Flow<List<Post>> = postDao.searchPostsByUserId(userId)
    
    fun searchPostsAdvanced(query: String, searchInTitle: Boolean = true, searchInBody: Boolean = true, userId: Int? = null): Flow<List<Post>> {
        return when {
            userId != null -> postDao.searchPostsByUserId(userId)
            searchInTitle && searchInBody -> postDao.searchPosts(query)
            searchInTitle -> postDao.searchPostsByTitle(query)
            searchInBody -> postDao.searchPostsByBody(query)
            else -> postDao.getAllPosts()
        }
    }
    
    suspend fun loadPostsPage(page: Int, limit: Int): Result<List<Post>> {
        return try {
            val response = apiService.getPosts(page, limit)
            if (response.isSuccessful) {
                response.body()?.let { posts ->
                    // Store in database for offline access
                    postDao.insertPosts(posts)
                    Result.success(posts)
                } ?: Result.failure(Exception("Empty response body"))
            } else {
                Result.failure(Exception("Failed to fetch posts: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun refreshPosts(): Result<Unit> {
        return try {
            val response = apiService.getAllPosts()
            if (response.isSuccessful) {
                response.body()?.let { posts ->
                    postDao.insertPosts(posts)
                }
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to fetch posts: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun toggleFavorite(postId: Int, isFavorite: Boolean) {
        postDao.updateFavoriteStatus(postId, isFavorite)
    }
}