package com.example.zoomatch.data.db

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "auth_prefs")

class TokenManager(private val context: Context) {

  companion object {
    private val ACCESS_TOKEN_KEY = stringPreferencesKey("access_token")
    private val REFRESH_TOKEN_KEY = stringPreferencesKey("refresh_token")
  }

  suspend fun saveTokens(access: String, refresh: String) {
    context.dataStore.edit { prefs ->
      prefs[ACCESS_TOKEN_KEY] = access
      prefs[REFRESH_TOKEN_KEY] = refresh
    }
  }

  suspend fun getAccessToken(): String? {
    val prefs = context.dataStore.data.first()
    return prefs[ACCESS_TOKEN_KEY]
  }

  suspend fun getRefreshToken(): String? {
    val prefs = context.dataStore.data.first()
    return prefs[REFRESH_TOKEN_KEY]
  }

  suspend fun clearTokens() {
    context.dataStore.edit { it.clear() }
  }
}