package com.shokal.technonext.data.dao

import androidx.room.*
import com.shokal.technonext.data.model.Favorite
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteDao {
    
    @Query("SELECT * FROM favorites WHERE userId = :userEmail ORDER BY postId DESC")
    fun getFavoritesByUser(userEmail: String): Flow<List<Favorite>>
    
    @Query("SELECT * FROM favorites WHERE postId = :postId AND userId = :userEmail")
    suspend fun getFavorite(postId: Int, userEmail: String): Favorite?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(favorite: Favorite)
    
    @Delete
    suspend fun deleteFavorite(favorite: Favorite)
    
    @Query("DELETE FROM favorites WHERE postId = :postId AND userId = :userEmail")
    suspend fun deleteFavoriteById(postId: Int, userEmail: String)
    
    @Query("DELETE FROM favorites WHERE userId = :userEmail")
    suspend fun deleteAllFavoritesForUser(userEmail: String)
}
