package com.shokal.technonext.data.repository

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.shokal.technonext.data.database.AppDatabase
import com.shokal.technonext.data.dao.PostDao
import com.shokal.technonext.data.model.Post
import com.shokal.technonext.utils.TestDataFactory
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PostRepositoryIntegrationTest {
    
    private lateinit var database: AppDatabase
    private lateinit var postDao: PostDao
    
    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).allowMainThreadQueries().build()
        
        postDao = database.postDao()
        // Note: This test focuses on DAO operations, not the full repository
        // For full repository testing, we would need to inject dependencies properly
    }
    
    @After
    fun tearDown() {
        database.close()
    }
    
    @Test
    fun insertAndRetrievePosts() = runTest {
        // Given
        val posts = TestDataFactory.createPostList(5)
        
        // When
        postDao.insertPosts(posts)
        val retrievedPosts = postDao.getAllPosts().first()
        
        // Then
        assertEquals("Should retrieve all inserted posts", 5, retrievedPosts.size)
        assertTrue("Should contain all inserted posts", retrievedPosts.containsAll(posts))
    }
    
    @Test
    fun searchPostsByTitle() = runTest {
        // Given
        val posts = listOf(
            TestDataFactory.createPost(1, title = "Android Development"),
            TestDataFactory.createPost(2, title = "iOS Development"),
            TestDataFactory.createPost(3, title = "Web Development")
        )
        postDao.insertPosts(posts)
        
        // When
        val searchResults = postDao.searchPostsByTitle("Android").first()
        
        // Then
        assertEquals("Should find 1 Android post", 1, searchResults.size)
        assertEquals("Should find the correct post", "Android Development", searchResults.first().title)
    }
    
    @Test
    fun searchPostsByBody() = runTest {
        // Given
        val posts = listOf(
            TestDataFactory.createPost(1, body = "Learn Android programming"),
            TestDataFactory.createPost(2, body = "Learn iOS programming"),
            TestDataFactory.createPost(3, body = "Learn Web programming")
        )
        postDao.insertPosts(posts)
        
        // When
        val searchResults = postDao.searchPostsByBody("Android").first()
        
        // Then
        assertEquals("Should find 1 Android post", 1, searchResults.size)
        assertTrue("Should contain Android in body", searchResults.first().body.contains("Android"))
    }
    
    @Test
    fun searchPostsByUserId() = runTest {
        // Given
        val posts = listOf(
            TestDataFactory.createPost(1, userId = 1),
            TestDataFactory.createPost(2, userId = 1),
            TestDataFactory.createPost(3, userId = 2)
        )
        postDao.insertPosts(posts)
        
        // When
        val searchResults = postDao.searchPostsByUserId(1).first()
        
        // Then
        assertEquals("Should find 2 posts for user 1", 2, searchResults.size)
        assertTrue("All posts should belong to user 1", searchResults.all { it.userId == 1 })
    }
    
    @Test
    fun updateFavoriteStatus() = runTest {
        // Given
        val post = TestDataFactory.createPost(1, isFavorite = false)
        postDao.insertPosts(listOf(post))
        
        // When
        postDao.updateFavoriteStatus(1, true)
        val updatedPost = postDao.getAllPosts().first().first()
        
        // Then
        assertTrue("Post should be marked as favorite", updatedPost.isFavorite)
    }
    
    @Test
    fun getFavoritePosts() = runTest {
        // Given
        val posts = listOf(
            TestDataFactory.createPost(1, isFavorite = true),
            TestDataFactory.createPost(2, isFavorite = false),
            TestDataFactory.createPost(3, isFavorite = true)
        )
        postDao.insertPosts(posts)
        
        // When
        val favoritePosts = postDao.getFavoritePosts().first()
        
        // Then
        assertEquals("Should find 2 favorite posts", 2, favoritePosts.size)
        assertTrue("All posts should be favorites", favoritePosts.all { it.isFavorite })
    }
    
    @Test
    fun clearAllPosts() = runTest {
        // Given
        val posts = TestDataFactory.createPostList(5)
        postDao.insertPosts(posts)
        
        // Verify posts are inserted
        assertEquals("Should have 5 posts", 5, postDao.getAllPosts().first().size)
        
        // When
        postDao.clearAllPosts()
        
        // Then
        val remainingPosts = postDao.getAllPosts().first()
        assertTrue("Should have no posts after clearing", remainingPosts.isEmpty())
    }
    
    @Test
    fun insertPostsWithConflictStrategy() = runTest {
        // Given
        val originalPost = TestDataFactory.createPost(1, title = "Original Title")
        val updatedPost = TestDataFactory.createPost(1, title = "Updated Title")
        
        // When
        postDao.insertPosts(listOf(originalPost))
        postDao.insertPosts(listOf(updatedPost))
        
        // Then
        val posts = postDao.getAllPosts().first()
        assertEquals("Should have only 1 post", 1, posts.size)
        assertEquals("Should have updated title", "Updated Title", posts.first().title)
    }
    
    @Test
    fun searchPostsCaseInsensitive() = runTest {
        // Given
        val posts = listOf(
            TestDataFactory.createPost(1, title = "Android Development"),
            TestDataFactory.createPost(2, title = "iOS Development")
        )
        postDao.insertPosts(posts)
        
        // When
        val searchResults = postDao.searchPosts("android").first()
        
        // Then
        assertEquals("Should find 1 Android post (case insensitive)", 1, searchResults.size)
        assertEquals("Should find the correct post", "Android Development", searchResults.first().title)
    }
    
    @Test
    fun searchPostsInTitleAndBody() = runTest {
        // Given
        val posts = listOf(
            TestDataFactory.createPost(1, title = "Android Development", body = "Learn mobile development"),
            TestDataFactory.createPost(2, title = "Web Development", body = "Learn Android web development"),
            TestDataFactory.createPost(3, title = "iOS Development", body = "Learn mobile development")
        )
        postDao.insertPosts(posts)
        
        // When
        val searchResults = postDao.searchPosts("Android").first()
        
        // Then
        assertEquals("Should find 2 posts containing Android", 2, searchResults.size)
        assertTrue("Should find posts with Android in title or body", 
            searchResults.any { it.title.contains("Android", ignoreCase = true) } ||
            searchResults.any { it.body.contains("Android", ignoreCase = true) })
    }
    
    @Test
    fun postsOrderedByIdDesc() = runTest {
        // Given
        val posts = listOf(
            TestDataFactory.createPost(1),
            TestDataFactory.createPost(3),
            TestDataFactory.createPost(2)
        )
        postDao.insertPosts(posts)
        
        // When
        val orderedPosts = postDao.getAllPosts().first()
        
        // Then
        assertEquals("Should have 3 posts", 3, orderedPosts.size)
        assertTrue("Posts should be ordered by ID descending", 
            orderedPosts[0].id >= orderedPosts[1].id &&
            orderedPosts[1].id >= orderedPosts[2].id)
    }
}
