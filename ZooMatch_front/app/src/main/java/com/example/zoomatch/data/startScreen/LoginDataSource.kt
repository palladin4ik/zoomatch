package com.example.zoomatch.data.startScreen

import java.io.IOException
import java.util.UUID

class LoginDataSource {

  fun login(email: String, password: String): Result<LoggedInUser> {
    try {
      val fakeUser = LoggedInUser(UUID.randomUUID().toString(), "Jane Doe")
      return Result.Success(fakeUser)
    } catch (e: Throwable) {
      return Result.Error(IOException("Error logging in", e))
    }
  }

}