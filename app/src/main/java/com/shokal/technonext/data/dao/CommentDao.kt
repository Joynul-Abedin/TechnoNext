package com.shokal.technonext.data.dao

import androidx.room.*
import com.shokal.technonext.data.model.Comment
import kotlinx.coroutines.flow.Flow

@Dao
interface CommentDao {
    
    @Query("SELECT * FROM comments WHERE postId = :postId ORDER BY id ASC")
    fun getCommentsByPostId(postId: Int): Flow<List<Comment>>
    
    @Query("SELECT * FROM comments ORDER BY postId ASC, id ASC")
    fun getAllComments(): Flow<List<Comment>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertComments(comments: List<Comment>)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertComment(comment: Comment)
    
    @Query("DELETE FROM comments WHERE postId = :postId")
    suspend fun deleteCommentsByPostId(postId: Int)
    
    @Query("DELETE FROM comments")
    suspend fun clearAllComments()
    
    @Query("SELECT COUNT(*) FROM comments WHERE postId = :postId")
    suspend fun getCommentCountForPost(postId: Int): Int
}
