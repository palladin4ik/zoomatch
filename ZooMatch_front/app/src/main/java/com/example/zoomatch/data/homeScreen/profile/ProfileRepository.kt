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
  val userFlow: Flow<UserEntity?> = userDao.getCurrentUserFlow()
    .flowOn(Dispatchers.IO)

  suspend fun updateProfile(
    firstname: String,
    lastname: String,
    description: String?,
    email: String,
    phoneNumber: String?
  ): Result<String> {
    return when (
      val result = dataSource.updateProfile(
        tokenManager.getAccessToken(),
        firstname,
        lastname,
        description,
        email,
        phoneNumber,
      )
    ) {
      is Result.Success -> {
        val fullName = "${result.data.firstname} ${result.data.lastname}".trim()
        userDao.update(
          id = result.data.id,
          name = fullName,
          firstname = result.data.firstname,
          lastname = result.data.lastname,
          location = result.data.location ?: "",
          email = result.data.email,
          phoneNumber = result.data.phone_number,
          avatar = result.data.avatar,
          status = result.data.description,
          organization = result.data.organization
        )
        Result.Success("Данные обновлены")
      }

      is Result.Error -> result
    }
  }
}
