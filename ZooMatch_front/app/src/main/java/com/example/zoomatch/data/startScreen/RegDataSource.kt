package com.example.zoomatch.data.startScreen

import java.io.IOException
import java.util.UUID

class RegDataSource {
  fun register(email: String, password: String, username: String): Result<RegUser> {
    try {
      val fakeUser = RegUser(UUID.randomUUID().toString(), email, username)
      return Result.Success(fakeUser)
    } catch (e: Throwable) {
      return Result.Error(IOException("Error logging in", e))
    }
  }
}