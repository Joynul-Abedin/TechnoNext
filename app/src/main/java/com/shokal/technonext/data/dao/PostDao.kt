package com.shokal.technonext.data.dao

import androidx.room.*
import com.shokal.technonext.data.model.Post
import kotlinx.coroutines.flow.Flow

@Dao
interface PostDao {
    
    @Query("SELECT * FROM posts ORDER BY id DESC")
    fun getAllPosts(): Flow<List<Post>>
    
    @Query("SELECT * FROM posts WHERE isFavorite = 1 ORDER BY id DESC")
    fun getFavoritePosts(): Flow<List<Post>>
    
    @Query("SELECT * FROM posts WHERE LOWER(title) LIKE LOWER('%' || :query || '%') OR LOWER(body) LIKE LOWER('%' || :query || '%') ORDER BY id DESC")
    fun searchPosts(query: String): Flow<List<Post>>
    
    @Query("SELECT * FROM posts WHERE LOWER(title) LIKE LOWER('%' || :query || '%') ORDER BY id DESC")
    fun searchPostsByTitle(query: String): Flow<List<Post>>
    
    @Query("SELECT * FROM posts WHERE LOWER(body) LIKE LOWER('%' || :query || '%') ORDER BY id DESC")
    fun searchPostsByBody(query: String): Flow<List<Post>>
    
    @Query("SELECT * FROM posts WHERE userId = :userId ORDER BY id DESC")
    fun searchPostsByUserId(userId: Int): Flow<List<Post>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPosts(posts: List<Post>)
    
    @Update
    suspend fun updatePost(post: Post)
    
    @Query("UPDATE posts SET isFavorite = :isFavorite WHERE id = :postId")
    suspend fun updateFavoriteStatus(postId: Int, isFavorite: Boolean)
    
    @Query("DELETE FROM posts")
    suspend fun clearAllPosts()
}