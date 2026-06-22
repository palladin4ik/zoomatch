package com.example.zoomatch.data.homeScreen.profile

import com.example.zoomatch.data.Result
import com.example.zoomatch.data.db.Network.zooMatchApi

class ProfileDataSource {

  suspend fun updateProfile(
    token: String?,
    firstname: String,
    lastname: String,
    description: String?,
    email: String,
    phoneNumber: String?
  ): Result<UserEditResponse> {
    return try {
      val response = zooMatchApi.updateProfile(
        "Bearer $token",
        UserEditUI(
          avatar = null,
          firstname = firstname,
          lastname = lastname,
          email = email,
          description = description,
          phone_number = phoneNumber,
          organization = null
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
