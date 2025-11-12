package com.example.zoomatch.data.startScreen

data class RegFormState(
  val emailError: Int? = null,
  val passwordError: Int? = null,
  val usernameError: Int? = null,
  val isDataValid: Boolean = false
)

data class RegResult(
  val success: RegUserView? = null,
  val error: Int? = null
)

data class RegUserView(
  override val displayName: String
) : UserDisplay

data class RegUser(
  val userId: String,
  val email: String,
  val displayName: String
)