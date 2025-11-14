package com.example.zoomatch.data.startScreen

import com.example.zoomatch.data.db.TokenManager
import com.example.zoomatch.data.db.UserDao
import com.example.zoomatch.data.db.UserEntity


class RegRepository(
  private val regDataSource: RegDataSource,
  private val loginDataSource: LoginDataSource,
  private val tokenManager: TokenManager,
  private val userDao: UserDao
) {

  suspend fun registerAndLogin(email: String, password: String, name: String): Result<String> {
    val regResult = regDataSource.register(email, password, name)
    if (regResult is Result.Success) {
      val loginResult = loginDataSource.login(email, password)
      if (loginResult is Result.Success) {
        tokenManager.saveTokens(loginResult.data.access, loginResult.data.refresh)
        val user = loginDataSource.getUserInfo(loginResult.data.access)
        if (user is Result.Success) {
          userDao.insert(
            UserEntity(
              user.data.id,
              user.data.email,
              user.data.name,
              user.data.avatar,
              user.data.location,
              user.data.phone_number,
              user.data.role,
              user.data.last_seen,
              user.data.is_active
            )
          )
          return Result.Success(regResult.data.name)
        } else {
          return Result.Error((user as Result.Error).message)
        }
      } else {
        return Result.Error((loginResult as Result.Error).message)
      }
    } else {
      return Result.Error((regResult as Result.Error).message)
    }
  }
}