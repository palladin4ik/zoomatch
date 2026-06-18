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
  val animal_type: AnimalTypeResponse? = null
)

data class UserResponse(
  val id: Int,
  val firstname: String,
  val lastname: String,
  val email: String,
  val avatar: String?,
  val location: String?,
  val description: String?,
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
  val animal_type: AnimalTypeResponse?,
  val breed: AnimalTypeResponse?,
  val distance_km: Double?,
  val location: String?,
  val animal_type_custom: String?,
  val breed_custom: String?,
  val moderation_status: String?
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
  val pet_from_data: MatchPetData?,
  val pet_to_data: MatchPetData?,
  val status: Int,
  val created_at: String
)

data class MatchPetData(
  val id: Int,
  val name: String,
  val animal_type: AnimalTypeResponse?,
  val breed: BreedResponse?,
  val is_male: Boolean,
  val age: Int,
  val avatar: String?,
  val location: String?,
  val is_active: Boolean,
  val description: String?,
  val owner: MatchOwnerData?
)

data class MatchOwnerData(
  val id: Int,
  val firstname: String?,
  val lastname: String?,
  val avatar: String?
)

data class MatchesListResponse(
  val liked_by: List<Int>
)

data class PetShortRecommendation(
  val id: Int,
  val name: String,
  @com.google.gson.annotations.SerializedName("is_male") val isMale: Boolean,
  val age: Int,
  val avatar: String?,
  @com.google.gson.annotations.SerializedName("is_active") val isActive: Boolean,
  val animal_type: AnimalTypeResponse?,
  val breed: AnimalTypeResponse?,
  @com.google.gson.annotations.SerializedName("distance_km") val distanceKm: Double?,
  val location: String?,
  @com.google.gson.annotations.SerializedName("animal_type_custom") val animalTypeCustom: String?,
  @com.google.gson.annotations.SerializedName("breed_custom") val breedCustom: String?,
  @com.google.gson.annotations.SerializedName("moderation_status") val moderationStatus: String?,
  val owner: PetShortOwner?
)

data class PetShortOwner(
  val id: Int,
  val firstname: String?,
  val lastname: String?,
  val avatar: String?
)

data class SearchFilterParams(
  @com.google.gson.annotations.SerializedName("radius_km") val radiusKm: Int? = null,
  @com.google.gson.annotations.SerializedName("requires_pedigree") val requiresPedigree: Boolean = false,
  @com.google.gson.annotations.SerializedName("min_age") val minAge: Int? = null,
  @com.google.gson.annotations.SerializedName("max_age") val maxAge: Int? = null,
  @com.google.gson.annotations.SerializedName("max_months_since_mating") val maxMonthsSinceMating: Int? = null
)
