package com.example.zoomatch.data.homeScreen.pets

data class PetUI(
  val id: String,
  val name: String,
  val breed: String,
  val age: Int,
  val status: String,
  val avatar: String?
)

data class PetCreateRequest(
  val name: String,
  val animal_type: Int?,
  val breed: Int?,
  val is_male: Boolean,
  val age: Int,
  val avatar: String? = null,
  val location: String? = null,
  val has_pedigree: Boolean = false,
  val pedigree_documents: String? = null,
  val awards: String? = null,
  val tags: List<String> = emptyList(),
  val description: String? = null,
  val is_active: Boolean = false
)

data class PetUpdateRequest(
  val name: String? = null,
  val animal_type: Int? = null,
  val breed: Int? = null,
  val is_male: Boolean? = null,
  val age: Int? = null,
  val avatar: String? = null,
  val location: String? = null,
  val has_pedigree: Boolean? = null,
  val pedigree_documents: String? = null,
  val awards: String? = null,
  val tags: List<String>? = null,
  val description: String? = null,
  val is_active: Boolean? = null
)


data class PetResponse(
  val id: Int,
  val name: String,
  val animal_type: Int,
  val breed: Int,
  val is_male: Boolean,
  val age: Int,
  val avatar: String?,
  val location: String?,
  val has_pedigree: Boolean,
  val pedigree_documents: String?,
  val awards: String?,
  val tags: List<Pair<Int,String>>,
  val description: String?,
  val is_active: Boolean
)

data class PetEditUI(
  val avatar: String?,
  val name: String,
  val location: String?,
  val description: String?,
  val has_pedigree: Boolean,
  val pedigree_documents: String?,
  val awards: String?,
  val is_active: Boolean,
  val age: Int
)

data class EditPetFormState(
  val nameError: Int? = null,
  val ageError: Int? = null,
  val isDataValid: Boolean = false
)
