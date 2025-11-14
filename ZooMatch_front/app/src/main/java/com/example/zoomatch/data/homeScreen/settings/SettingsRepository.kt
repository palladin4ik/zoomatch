package com.example.zoomatch.data.homeScreen.settings

import com.example.zoomatch.data.db.TokenManager
import com.example.zoomatch.data.db.UserDao

class SettingsRepository(
  private val tokenManager: TokenManager,
  private val userDao: UserDao
) {
  suspend fun logout() {
    tokenManager.clearTokens()
    userDao.clear()
  }
}