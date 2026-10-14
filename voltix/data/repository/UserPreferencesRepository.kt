package com.example.voltix.data.repository

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.userDataStore by preferencesDataStore(name = "user_prefs")

class UserPreferencesRepository(private val context: Context) {

    private object PreferencesKeys {
        val IS_LOGGED_IN = booleanPreferencesKey("is_logged_in")
        val USER_EMAIL = stringPreferencesKey("user_email")
        val USER_ROLE = stringPreferencesKey("user_role")
        val USER_AVATAR_URI = stringPreferencesKey("user_avatar_uri")
        val THEME_MODE = stringPreferencesKey("theme_mode")
    }

    val isLoggedIn: Flow<Boolean> = context.userDataStore.data.map { preferences ->
        preferences[PreferencesKeys.IS_LOGGED_IN] ?: false
    }

    val userEmail: Flow<String> = context.userDataStore.data.map { preferences ->
        preferences[PreferencesKeys.USER_EMAIL] ?: ""
    }

    val userRole: Flow<String> = context.userDataStore.data.map { preferences ->
        preferences[PreferencesKeys.USER_ROLE] ?: "BUYER"
    }

    // Выбранная тема приложения: SYSTEM / LIGHT / DARK — сохраняется между запусками
    val themeMode: Flow<String> = context.userDataStore.data.map { preferences ->
        preferences[PreferencesKeys.THEME_MODE] ?: "SYSTEM"
    }

    suspend fun saveThemeMode(mode: String) {
        context.userDataStore.edit { preferences ->
            preferences[PreferencesKeys.THEME_MODE] = mode
        }
    }

    suspend fun saveSession(email: String, role: String) {
        context.userDataStore.edit { preferences ->
            preferences[PreferencesKeys.IS_LOGGED_IN] = true
            preferences[PreferencesKeys.USER_EMAIL] = email
            preferences[PreferencesKeys.USER_ROLE] = role
        }
    }

    // Пользователь может выбрать своё фото профиля из галереи — URI сохраняется между запусками
    val userAvatarUri: Flow<String> = context.userDataStore.data.map { preferences ->
        preferences[PreferencesKeys.USER_AVATAR_URI] ?: ""
    }

    suspend fun saveAvatarUri(uri: String) {
        context.userDataStore.edit { preferences ->
            preferences[PreferencesKeys.USER_AVATAR_URI] = uri
        }
    }

    suspend fun clearSession() {
        context.userDataStore.edit { preferences ->
            preferences.clear()
        }
    }
}