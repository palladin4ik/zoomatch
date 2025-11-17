package com.example.zoomatch.data.homeScreen.search

data class PetLongResponse(
  val id: Int,
  val name: String,
  val animal_type: AnimalTypeResponse,
  val breed: BreedResponse,
  val is_male: Boolean,
  val age: Int,
  val owner: UserResponse,
  val avatar: String?,
  val location: String?,
  val has_pedigree: Boolean,
  val pedigree_documents: String?,
  val awards: String?,
  val tags: List<TagResponse>,
  val description: String?,
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

data class UserResponse(
  val id: Int,
  val name: String,
  val email: String,
  val avatar: String?,
  val location: String?,
  val status: String?,
  val phone_number: String?,
  val role: Int,
  val last_seen: String,
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
  val animal_type: Int,
  val breed: Int
)

data class TagResponse(
  val id: Int,
  val tag: String
)

data class MatchRequest(
  val pet_from: Int,
  val pet_to: Int
)

data class MatchResponse(
  val id: Int,
  val pet_from: Int,
  val pet_to: Int,
  val created_at: String
)

data class MatchesListResponse(
  val liked_by: List<Int>
)