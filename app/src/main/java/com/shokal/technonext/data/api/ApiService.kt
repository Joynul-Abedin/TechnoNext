package com.shokal.technonext.data.api

import com.shokal.technonext.data.model.Comment
import com.shokal.technonext.data.model.Post
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {
    
    @GET("posts")
    suspend fun getPosts(
        @Query("_page") page: Int = 1,
        @Query("_limit") limit: Int = 20
    ): Response<List<Post>>
    
    @GET("posts")
    suspend fun getAllPosts(): Response<List<Post>>
    
    @GET("posts/{postId}/comments")
    suspend fun getCommentsForPost(@Path("postId") postId: Int): Response<List<Comment>>
}