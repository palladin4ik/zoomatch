package com.example.zoomatch.data.startScreen

import com.example.zoomatch.data.Result
import com.example.zoomatch.data.db.Network.zooMatchApi
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException

class RegDataSource {
  companion object {
    const val EMAIL_EXISTS = "EMAIL_EXISTS"
  }

  suspend fun register(firstname: String, lastname: String, email: String, password: String): Result<RegUserResponse> {
    return try {
      val response = zooMatchApi.registerUser(RegUser(firstname, lastname, email, password))
      if (response.isSuccessful && response.body() != null) {
        Result.Success(response.body()!!)
      } else {
        val errorMsg = parseRegistrationError(response.errorBody()?.string())
        Result.Error(errorMsg)
      }
    } catch (e: Exception) {
      Result.Error(e.message ?: "Network error")
    }
  }

  private fun parseRegistrationError(errorBody: String?): String {
    if (errorBody == null) return "Registration failed"
    return try {
      val json = Gson().fromJson(errorBody, Map::class.java)
      val emailErrors = json?.get("email") as? List<*>
      if (emailErrors != null && emailErrors.isNotEmpty()) {
        EMAIL_EXISTS
      } else {
        "Registration failed"
      }
    } catch (e: JsonSyntaxException) {
      "Registration failed"
    }
  }
}
