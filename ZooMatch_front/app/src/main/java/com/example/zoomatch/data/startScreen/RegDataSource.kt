package com.example.zoomatch.data.startScreen

import com.example.zoomatch.data.Result
import com.example.zoomatch.data.db.Network.zooMatchApi

class RegDataSource {
  suspend fun register(email: String, password: String, name: String): Result<RegUserResponse> {
    return try {
      val response = zooMatchApi.registerUser(RegUser(email, password, name))
      if (response.isSuccessful && response.body() != null) {
        Result.Success(response.body()!!)
      } else {
        Result.Error(response.message() ?: "Registration failed")
      }
    } catch (e: Exception) {
      Result.Error(e.message ?: "Network error")
    }
  }
}