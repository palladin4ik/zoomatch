package com.example.zoomatch.data.homeScreen.chats

import com.example.zoomatch.data.db.TokenManager
import com.example.zoomatch.data.db.UserDao
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CurrentUserProvider @Inject constructor(
  private val tokenManager: TokenManager,
  private val userDao: UserDao
) {

  suspend fun getCurrentUserId(): Int {
    return userDao.getCurrentUserFlow().firstOrNull()?.id ?: 0
  }

  suspend fun getAccessToken(): String? {
    return tokenManager.getAccessToken()
  }
}