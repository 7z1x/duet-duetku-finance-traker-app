package com.duetduetku.app.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

class UserPreferences @Inject constructor(private val context: Context) {

    companion object {
        val USER_NAME = stringPreferencesKey("user_name")
        val DAILY_LIMIT = doublePreferencesKey("daily_limit")
        val DAILY_REMINDER = booleanPreferencesKey("daily_reminder")
        val APP_THEME = androidx.datastore.preferences.core.intPreferencesKey("app_theme")
        val PROFILE_PHOTO_URI = stringPreferencesKey("profile_photo_uri")
    }

    val userName: Flow<String> = context.dataStore.data
        .map { preferences -> preferences[USER_NAME] ?: "User" }

    val dailyLimit: Flow<Double> = context.dataStore.data
        .map { preferences -> preferences[DAILY_LIMIT] ?: 100000.0 }


        
    val dailyReminder: Flow<Boolean> = context.dataStore.data
        .map { preferences -> preferences[DAILY_REMINDER] ?: true }

    val appTheme: Flow<Int> = context.dataStore.data
        .map { preferences -> preferences[APP_THEME] ?: 0 }

    val profilePhotoUri: Flow<String?> = context.dataStore.data
        .map { preferences -> preferences[PROFILE_PHOTO_URI] }

    suspend fun setUserName(name: String) {
        context.dataStore.edit { preferences -> preferences[USER_NAME] = name }
    }

    suspend fun setDailyLimit(limit: Double) {
        context.dataStore.edit { preferences -> preferences[DAILY_LIMIT] = limit }
    }
    

    
    suspend fun setDailyReminder(isEnabled: Boolean) {
        context.dataStore.edit { preferences -> preferences[DAILY_REMINDER] = isEnabled }
    }

    suspend fun setAppTheme(themeValues: Int) {
        context.dataStore.edit { preferences -> preferences[APP_THEME] = themeValues }
    }

    suspend fun setProfilePhotoUri(uri: String) {
        context.dataStore.edit { preferences -> preferences[PROFILE_PHOTO_URI] = uri }
    }
}
