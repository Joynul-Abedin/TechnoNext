package com.shokal.technonext.utils

import com.shokal.technonext.data.model.Comment
import com.shokal.technonext.data.model.Favorite
import com.shokal.technonext.data.model.Post
import com.shokal.technonext.data.model.User
import kotlin.random.Random

object TestDataFactory {
    
    fun createPost(
        id: Int = Random.nextInt(1, 1000),
        userId: Int = Random.nextInt(1, 10),
        title: String = "Test Post Title $id",
        body: String = "Test post body content for post $id",
        isFavorite: Boolean = false
    ): Post = Post(
        id = id,
        userId = userId,
        title = title,
        body = body,
        isFavorite = isFavorite
    )
    
    fun createUser(
        email: String = "test@example.com",
        password: String = "password123"
    ): User = User(
        email = email,
        password = password
    )
    
    fun createComment(
        id: Int = Random.nextInt(1, 1000),
        postId: Int = Random.nextInt(1, 100),
        name: String = "Test Commenter $id",
        email: String = "commenter$id@example.com",
        body: String = "Test comment body for comment $id"
    ): Comment = Comment(
        id = id,
        postId = postId,
        name = name,
        email = email,
        body = body
    )
    
    fun createFavorite(
        postId: Int = Random.nextInt(1, 100),
        userId: String = "test@example.com",
        title: String = "Favorite Post $postId",
        body: String = "Favorite post body $postId",
        originalUserId: Int = Random.nextInt(1, 10)
    ): Favorite = Favorite(
        postId = postId,
        userId = userId,
        title = title,
        body = body,
        originalUserId = originalUserId
    )
    
    fun createPostList(count: Int = 5): List<Post> = (1..count).map { createPost(id = it) }
    
    fun createUserList(count: Int = 3): List<User> = (1..count).map { createUser(email = "test$it@example.com") }
    
    fun createCommentList(postId: Int, count: Int = 3): List<Comment> = 
        (1..count).map { createComment(id = it, postId = postId) }
    
    fun createFavoriteList(userEmail: String, count: Int = 3): List<Favorite> = 
        (1..count).map { createFavorite(postId = it, userId = userEmail) }
}
