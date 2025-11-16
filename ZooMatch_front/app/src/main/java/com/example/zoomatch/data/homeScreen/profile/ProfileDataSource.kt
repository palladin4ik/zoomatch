package com.example.zoomatch.data.homeScreen.profile

import com.example.zoomatch.data.Result
import com.example.zoomatch.data.db.Network.zooMatchApi

class ProfileDataSource {

  suspend fun updateProfile(
    token: String?,
    avatar: String?,
    name: String,
    location: String,
//    description: String,
    email: String,
    phoneNumber: String
  ): Result<UserEditResponse> {
    return try {
      val response = zooMatchApi.updateProfile(
        "Bearer $token",
        UserEditUI(
          avatar,
          name,
          location,
//          description,
          email,
          phoneNumber
        )
      )
      if (response.isSuccessful && response.body() != null) {
        Result.Success(response.body()!!)
      } else {
        Result.Error(response.message() ?: "Invalid Data")
      }
    } catch (e: Exception) {
      Result.Error(e.message ?: "Network error")
    }
  }
}