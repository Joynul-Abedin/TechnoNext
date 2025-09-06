package com.shokal.technonext

import com.shokal.technonext.data.api.ApiService
import com.shokal.technonext.data.preferences.UserPreferences
import com.shokal.technonext.data.repository.AuthRepository
import com.shokal.technonext.data.repository.FavoriteRepository
import com.shokal.technonext.data.repository.PostRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import io.mockk.mockk
import javax.inject.Singleton

@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = []
)
object TestModule {
    
    @Provides
    @Singleton
    fun provideMockApiService(): ApiService = mockk()
    
    @Provides
    @Singleton
    fun provideMockUserPreferences(): UserPreferences = mockk()
    
    @Provides
    @Singleton
    fun provideMockAuthRepository(): AuthRepository = mockk()
    
    @Provides
    @Singleton
    fun provideMockPostRepository(): PostRepository = mockk()
    
    @Provides
    @Singleton
    fun provideMockFavoriteRepository(): FavoriteRepository = mockk()
}
