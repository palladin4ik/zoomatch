package com.example.zoomatch.data.homeScreen.pets

import com.google.gson.annotations.SerializedName

data class PetUI(
  val id: String,
  val name: String,
  val breed: String,
  val age: Int,
  val status: String,
  val avatar: String?,
  val isMale: Boolean = true,
  val isActive: Boolean = false,
  val moderationStatus: String? = null
)

data class PetCreateRequest(
  val name: String,
  @SerializedName("animal_type") val animal_type: Int? = null,
  @SerializedName("animal_type_custom") val animal_type_custom: String? = null,
  @SerializedName("breed") val breed: Int? = null,
  @SerializedName("breed_custom") val breed_custom: String? = null,
  @SerializedName("is_male") val is_male: Boolean,
  val age: Int,
  @SerializedName("address") val address: String? = null,
  @SerializedName("latitude") val latitude: Double? = null,
  @SerializedName("longitude") val longitude: Double? = null,
  @SerializedName("has_pedigree") val has_pedigree: Boolean = false,
  val awards: String? = null,
  val tags: List<String> = emptyList(),
  val description: String? = null,
  @SerializedName("is_active") val is_active: Boolean = false
)

data class PetUpdateRequest(
  val name: String? = null,
  @SerializedName("animal_type") val animal_type: Int? = null,
  @SerializedName("animal_type_custom") val animal_type_custom: String? = null,
  @SerializedName("breed") val breed: Int? = null,
  @SerializedName("breed_custom") val breed_custom: String? = null,
  @SerializedName("is_male") val is_male: Boolean? = null,
  val age: Int? = null,
  @SerializedName("address") val address: String? = null,
  @SerializedName("latitude") val latitude: Double? = null,
  @SerializedName("longitude") val longitude: Double? = null,
  @SerializedName("has_pedigree") val has_pedigree: Boolean? = null,
  val awards: String? = null,
  val tags: List<String>? = null,
  val description: String? = null,
  @SerializedName("is_active") val is_active: Boolean? = null
)


data class PetResponse(
  val id: Int,
  val name: String,
  val animal_type: AnimalTypeResponse?,
  val breed: BreedResponse?,
  val is_male: Boolean,
  val age: Int,
  val avatar: String?,
  val location: String?,
  val has_pedigree: Boolean,
  val pedigree_documents: String?,
  val awards: String?,
  val tags: List<TagResponse>?,
  val description: String?,
  val is_active: Boolean
)

data class PetCreateResponse(
  val id: Int,
  val name: String,
  val animal_type: Int?,
  val breed: Int?,
  val is_male: Boolean,
  val age: Int,
  val location: String?,
  val has_pedigree: Boolean,
  val awards: String?,
  val tags_list: List<TagResponse>?,
  val description: String?,
  val is_active: Boolean,
  val animal_type_custom: String?,
  val breed_custom: String?
)

data class AnimalTypeResponse(
  val id: Int,
  val name: String
)

data class BreedResponse(
  val id: Int,
  val name: String
)

data class TagResponse(
  val id: Int,
  val tag: String
)

data class PetEditUI(
  val avatar: String?,
  val name: String,
  val address: String?,
  val description: String?,
  val has_pedigree: Boolean,
  val pedigree_documents: String?,
  val awards: String?,
  val is_active: Boolean,
  val age: Int,
  val is_male: Boolean = true
)

data class EditPetFormState(
  val nameError: Int? = null,
  val ageError: Int? = null,
  val typeError: Int? = null,
  val breedError: Int? = null,
  val addressError: Int? = null,
  val isDataValid: Boolean = false
)
