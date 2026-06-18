package com.example.zoomatch.data.startScreen

import com.example.zoomatch.data.UserDisplay

data class RegFormState(
  val emailError: Int? = null,
  val passwordError: Int? = null,
  val firstnameError: Int? = null,
  val lastnameError: Int? = null,
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
  val firstname: String,
  val lastname: String,
  val email: String,
  val password: String
)

data class RegUserResponse(
  val id: Int,
  val firstname: String,
  val lastname: String,
  val email: String
)
