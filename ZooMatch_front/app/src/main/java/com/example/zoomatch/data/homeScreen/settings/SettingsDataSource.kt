package com.example.zoomatch.data.homeScreen.settings

import com.example.zoomatch.data.Result
import com.example.zoomatch.data.db.Network.zooMatchApi

class SettingsDataSource {

  suspend fun updatePass(
    token: String?,
    old_pass: String,
    new_pass: String
  ): Result<EditPassResponse> {
    return try {
      val response = zooMatchApi.updatePass(
        "Bearer $token",
        EditPassRequest(
          old_pass,
          new_pass
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