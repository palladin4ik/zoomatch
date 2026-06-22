package com.example.zoomatch.data.homeScreen.home

data class Recommendation(
  val id: String,
  val name: String,
  val photoUrl: String?,
  val btnText: String,
  val type: RecType
)

enum class RecType {
  PROFILE_INCOMPLETE,
  NO_PETS
}