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
  val firstname: String,
  val lastname: String,
  val email: String,
  val avatar: String?,
  val location: String?,
  val description: String?,
  val phone_number: String?,
  val role: Int,
  val organization: String?,
  val last_seen: String?,
  val is_active: Boolean,
  val pets: List<PetShortResponse>
)

data class PetShortResponse(
  val id: Int,
  val name: String,
  val is_male: Boolean,
  val age: Int,
  val avatar: String?,
  val is_active: Boolean,
  val breed: IdNameResponse?,
  val animal_type: IdNameResponse?,
  val animal_type_custom: String?,
  val breed_custom: String?,
  val moderation_status: String?,
  val location: String? = null
)

data class IdNameResponse(
  val id: Int,
  val name: String
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
