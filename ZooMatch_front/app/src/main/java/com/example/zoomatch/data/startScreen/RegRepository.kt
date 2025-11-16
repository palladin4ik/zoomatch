package com.example.zoomatch.data.startScreen

import com.example.zoomatch.data.Result
import com.example.zoomatch.data.db.TokenManager
import com.example.zoomatch.data.db.UserDao


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
          val (user, pets) = user.data
          userDao.insert(user)
          userDao.insertAllPets(pets)
          return Result.Success(user.name)
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