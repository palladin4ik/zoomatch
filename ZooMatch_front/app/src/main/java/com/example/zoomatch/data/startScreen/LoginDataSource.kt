package com.example.zoomatch.data.startScreen

import com.example.zoomatch.data.Result
import com.example.zoomatch.data.db.Network.zooMatchApi
import com.example.zoomatch.data.db.UserEntity

class LoginDataSource {
  suspend fun login(email: String, password: String): Result<JwtResponse> {
    return try {
      val response = zooMatchApi.loginUser(LoginRequest(email, password))
      if (response.isSuccessful && response.body() != null) {
        Result.Success(response.body()!!)
      } else {
        Result.Error(response.message() ?: "Login failed")
      }
    } catch (e: Exception) {
      Result.Error(e.message ?: "Network error")
    }
  }

  suspend fun getUserInfo(token: String): Result<UserEntity> {
    return try {
      val response = zooMatchApi.getProfile("Bearer $token")
      if (response.isSuccessful && response.body() != null) {
        val dto = response.body()!!
        val entity = UserEntity(
          id = dto.id,
          name = dto.name,
          email = dto.email,
          avatar = dto.avatar,
          location = dto.location,
          status = dto.status,
          phone_number = dto.phone_number,
          role = dto.role
        )
        Result.Success(entity)
      } else {
        Result.Error(response.message() ?: "Failed to fetch profile info")
      }
    } catch (e: Exception) {
      Result.Error(e.message ?: "Network error")
    }
  }
}