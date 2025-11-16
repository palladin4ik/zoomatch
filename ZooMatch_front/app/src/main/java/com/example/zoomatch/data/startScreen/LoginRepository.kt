package com.example.zoomatch.data.startScreen

import com.example.zoomatch.data.Result
import com.example.zoomatch.data.db.TokenManager
import com.example.zoomatch.data.db.UserDao
import com.example.zoomatch.data.db.UserEntity

class LoginRepository(
  private val dataSource: LoginDataSource,
  private val tokenManager: TokenManager,
  private val userDao: UserDao
) {

  suspend fun login(email: String, password: String): Result<String> {
    val result = dataSource.login(email, password)
    if (result is Result.Success) {
      tokenManager.saveTokens(result.data.access, result.data.refresh)
      val user = dataSource.getUserInfo(result.data.access)
      if (user is Result.Success) {
        userDao.insert(
          UserEntity(
            user.data.id,
            user.data.name,
            user.data.email,
            user.data.avatar,
            user.data.location,
            user.data.status,
            user.data.phone_number,
            user.data.role,
          )
        )
        return Result.Success(user.data.name)
      }
    }
    return result as Result.Error
  }
}