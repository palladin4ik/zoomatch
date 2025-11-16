package com.example.zoomatch.data.homeScreen.settings

data class EditPassFormState(
  val oldPassError: Int? = null,
  val passError: Int? = null,
  val passConfError: Int? = null,
  val isDataValid: Boolean = false
)

data class EditPassResponse(
  val res: String?
)

data class EditPassRequest(
  val old_password: String,
  val new_password: String
)

data class deleteAccResponse(
  val res: String?
)