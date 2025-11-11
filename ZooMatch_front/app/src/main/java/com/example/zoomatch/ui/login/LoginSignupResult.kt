package com.example.zoomatch.ui.login

/**
 * Authentication result : success (user details) or error message.
 */
data class LoginSignupResult(
  val success: LoggedInUserView? = null,
  val error: Int? = null
)