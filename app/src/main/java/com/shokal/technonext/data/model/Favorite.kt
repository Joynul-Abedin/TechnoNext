package com.shokal.technonext.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorites")
data class Favorite(
    @PrimaryKey
    val postId: Int,
    val userId: String, // User email
    val title: String,
    val body: String,
    val originalUserId: Int // Original post userId from API
)
