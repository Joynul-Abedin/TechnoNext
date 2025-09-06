package com.shokal.technonext.di

import android.content.Context
import androidx.room.Room
import com.shokal.technonext.data.dao.CommentDao
import com.shokal.technonext.data.dao.FavoriteDao
import com.shokal.technonext.data.dao.PostDao
import com.shokal.technonext.data.dao.UserDao
import com.shokal.technonext.data.database.AppDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    
    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return AppDatabase.getDatabase(context)
    }
    
    @Provides
    fun providePostDao(database: AppDatabase): PostDao {
        return database.postDao()
    }
    
    @Provides
    fun provideUserDao(database: AppDatabase): UserDao {
        return database.userDao()
    }
    
    @Provides
    fun provideFavoriteDao(database: AppDatabase): FavoriteDao {
        return database.favoriteDao()
    }
    
    @Provides
    fun provideCommentDao(database: AppDatabase): CommentDao {
        return database.commentDao()
    }
}