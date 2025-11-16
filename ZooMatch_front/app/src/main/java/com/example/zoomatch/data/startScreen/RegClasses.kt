package com.example.zoomatch.data.startScreen

import com.example.zoomatch.data.UserDisplay

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
  val email: String,
  val password: String,
  val name: String
)

data class RegUserResponse(
  val email: String,
  val name: String
)