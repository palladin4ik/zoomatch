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

data class LoginResponse(
  val id: Int,
  val name: String,
  val email: String,
  val avatar: String?,
  val location: String,
  val status: String?,
  val phone_number: String?,
  val role: Int,
  val pets: List<PetShortResponse>
)

data class PetShortResponse(
  val id: Int,
  val name: String,
  val is_male: Boolean,
  val age: Int,
  val avatar: String?,
  val is_active: Boolean
)

data class AnimalTypeResponse(
  val id: Int,
  val name: String
)

data class BreedResponse(
  val id: Int,
  val name: String,
  val animal_type: AnimalTypeResponse
)

data class JwtResponse(
  @SerializedName("access") val access: String,
  @SerializedName("refresh") val refresh: String
)