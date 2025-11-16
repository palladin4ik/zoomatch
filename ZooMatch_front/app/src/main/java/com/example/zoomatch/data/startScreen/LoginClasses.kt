package com.example.zoomatch.data.startScreen

import com.example.zoomatch.data.UserDisplay
import com.google.gson.annotations.SerializedName

data class LoginFormState(
  val emailError: Int? = null,
  val passwordError: Int? = null,
  val isDataValid: Boolean = false
)

data class LoginResult(
  val success: LoggedInUserView? = null,
  val error: Int? = null
)

data class LoggedInUserView(
  override val displayName: String
) : UserDisplay

data class LoginRequest(
  val email: String,
  val password: String
)

data class JwtResponse(
  @SerializedName("access") val access: String,
  @SerializedName("refresh") val refresh: String
)