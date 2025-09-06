package com.shokal.technonext.data.repository

import com.shokal.technonext.data.dao.FavoriteDao
import com.shokal.technonext.data.model.Favorite
import com.shokal.technonext.data.model.Post
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FavoriteRepository @Inject constructor(
    private val favoriteDao: FavoriteDao
) {
    
    fun getFavoritesByUser(userEmail: String): Flow<List<Favorite>> {
        return favoriteDao.getFavoritesByUser(userEmail)
    }
    
    suspend fun addToFavorites(post: Post, userEmail: String): Result<Unit> {
        return try {
            val favorite = Favorite(
                postId = post.id,
                userId = userEmail,
                title = post.title,
                body = post.body,
                originalUserId = post.userId
            )
            favoriteDao.insertFavorite(favorite)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun removeFromFavorites(postId: Int, userEmail: String): Result<Unit> {
        return try {
            favoriteDao.deleteFavoriteById(postId, userEmail)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun isFavorite(postId: Int, userEmail: String): Boolean {
        return favoriteDao.getFavorite(postId, userEmail) != null
    }
    
    suspend fun clearAllFavoritesForUser(userEmail: String): Result<Unit> {
        return try {
            favoriteDao.deleteAllFavoritesForUser(userEmail)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
