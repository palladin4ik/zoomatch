package com.example.zoomatch.data.homeScreen.settings

import com.example.zoomatch.data.Result
import com.example.zoomatch.data.db.TokenManager
import com.example.zoomatch.data.db.UserDao

class SettingsRepository(
  private val dataSource: SettingsDataSource,
  private val tokenManager: TokenManager,
  private val userDao: UserDao
) {

  suspend fun updatePass(old_pass: String, new_pass: String): Result<String>{
    return when(
      val result = dataSource.updatePass(
        tokenManager.getAccessToken(),
        old_pass, new_pass
      )
    ) {
      is Result.Error -> result
      is Result.Success -> Result.Success("Пароль изменён")
    }
  }
  suspend fun logout() {
    tokenManager.clearTokens()
    userDao.clear()
  }
}