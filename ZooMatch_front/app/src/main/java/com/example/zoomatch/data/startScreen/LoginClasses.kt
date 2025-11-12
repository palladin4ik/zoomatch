package com.example.zoomatch.data.startScreen

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

data class LoggedInUser(
  val userId: String,
  val displayName: String
)