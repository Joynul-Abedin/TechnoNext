package com.shokal.technonext.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.userDataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

@Singleton
class UserPreferences @Inject constructor(
    private val context: Context
) {
    
    private val isLoggedInKey = booleanPreferencesKey("is_logged_in")
    private val userEmailKey = stringPreferencesKey("user_email")
    private val userNameKey = stringPreferencesKey("user_name")
    
    val isLoggedIn: Flow<Boolean> = context.userDataStore.data.map { preferences ->
        preferences[isLoggedInKey] ?: false
    }
    
    val userEmail: Flow<String?> = context.userDataStore.data.map { preferences ->
        preferences[userEmailKey]
    }
    
    val userName: Flow<String?> = context.userDataStore.data.map { preferences ->
        preferences[userNameKey]
    }
    
    suspend fun setLoggedIn(isLoggedIn: Boolean, email: String? = null, name: String? = null) {
        context.userDataStore.edit { preferences ->
            preferences[isLoggedInKey] = isLoggedIn
            email?.let { preferences[userEmailKey] = it }
            name?.let { preferences[userNameKey] = it }
        }
    }
    
    suspend fun logout() {
        context.userDataStore.edit { preferences ->
            preferences[isLoggedInKey] = false
            preferences.remove(userEmailKey)
            preferences.remove(userNameKey)
        }
    }
}
