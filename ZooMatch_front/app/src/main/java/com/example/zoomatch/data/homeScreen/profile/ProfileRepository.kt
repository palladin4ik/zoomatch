package com.example.zoomatch.data.homeScreen.profile

import com.example.zoomatch.data.Result
import com.example.zoomatch.data.db.TokenManager
import com.example.zoomatch.data.db.UserDao
import com.example.zoomatch.data.db.UserEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn

class ProfileRepository(
  private val dataSource: ProfileDataSource,
  private val tokenManager: TokenManager,
  private val userDao: UserDao
) {
  val userFlow: Flow<UserEntity> = userDao.getCurrentUserFlow()
    .flowOn(Dispatchers.IO)

  suspend fun updateProfile(
    avatar: String?,
    name: String,
    location: String,
    status: String,
    email: String,
    phoneNumber: String
  ): Result<String> {
    return when (
      val result = dataSource.updateProfile(
        tokenManager.getAccessToken(),
        avatar,
        name,
        location,
        status,
        email,
        phoneNumber,
      )
    ) {
      is Result.Success -> {
        userDao.update(
          id = result.data.id,
          name = result.data.name,
          location = result.data.location,
          email = result.data.email,
          status = result.data.status,
          phoneNumber = result.data.phone_number,
          avatar = result.data.avatar
        )
        Result.Success("Данные обновлены")
      }

      is Result.Error -> result
    }
  }
}