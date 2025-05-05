//package com.bibek.cropfit.core
//
//import android.content.Context
//import androidx.datastore.core.DataStore
//import androidx.datastore.preferences.core.Preferences
//import androidx.datastore.preferences.core.edit
//import androidx.datastore.preferences.core.stringPreferencesKey
//import androidx.datastore.preferences.preferencesDataStore
//import kotlinx.coroutines.flow.Flow
//import kotlinx.coroutines.flow.map
//
//val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_prefs")
//
//object DataStoreManager {
//    private val USER_TOKEN_KEY = stringPreferencesKey("user_token")
//    private val USER_ID_KEY = stringPreferencesKey("user_id")
//    private val USER_EMAIL_KEY = stringPreferencesKey("user_email")
//
//    // Save data into DataStore
//    suspend fun saveUserData(context: Context, token: String, userId: String, email: String) {
//        context.dataStore.edit { preferences ->
//            preferences[USER_TOKEN_KEY] = token
//            preferences[USER_ID_KEY] = userId
//            preferences[USER_EMAIL_KEY] = email
//        }
//    }
//
//    // Retrieve the stored token
//    fun getToken(context: Context): Flow<String?> {
//        return context.dataStore.data.map { preferences ->
//            preferences[USER_TOKEN_KEY]
//        }
//    }
//
//    // Retrieve the stored user ID
//    fun getUserId(context: Context): Flow<String?> {
//        return context.dataStore.data.map { preferences ->
//            preferences[USER_ID_KEY]
//        }
//    }
//
//    // Retrieve the stored email
//    fun getUserEmail(context: Context): Flow<String?> {
//        return context.dataStore.data.map { preferences ->
//            preferences[USER_EMAIL_KEY]
//        }
//    }
//
//    // Clear stored data
//    suspend fun clearUserData(context: Context) {
//        context.dataStore.edit { preferences ->
//            preferences.clear()
//        }
//    }
//}
