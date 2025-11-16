package com.example.zoomatch.data.homeScreen.pets

import com.example.zoomatch.data.Result
import com.example.zoomatch.data.db.Network.zooMatchApi

class PetsDataSource {

  suspend fun createPet(
    token: String?,
    name: String,
    animal_type: Int?,
    breed: Int?,
    is_male: Boolean,
    age: Int,
    avatar: String?,
    location: String?,
    has_pedigree: Boolean,
    pedigree_documents: String?,
    awards: String?,
    tags: List<String>,
    description: String?,
    is_active: Boolean
  ): Result<PetResponse> {
    return try {
      val response = zooMatchApi.createPet(
        "Bearer $token",
        PetCreateRequest(
          name = name,
          animal_type = animal_type,
          breed = breed,
          is_male = is_male,
          age = age,
          avatar = avatar,
          location = location,
          has_pedigree = has_pedigree,
          pedigree_documents = pedigree_documents,
          awards = awards,
          tags = tags,
          description = description,
          is_active = is_active
        )
      )
      if (response.isSuccessful && response.body() != null) {
        Result.Success(response.body()!!)
      } else {
        Result.Error(response.message() ?: "Invalid Data")
      }
    } catch (e: Exception) {
      Result.Error(e.message ?: "Network error")
    }
  }

  suspend fun updatePet(
    token: String?,
    id: Int,
    name: String?,
    animal_type: Int?,
    breed: Int?,
    is_male: Boolean?,
    age: Int?,
    avatar: String?,
    location: String?,
    has_pedigree: Boolean?,
    pedigree_documents: String?,
    awards: String?,
    tags: List<String>?,
    description: String?,
    is_active: Boolean?
  ): Result<PetResponse> {
    return try {
      val response = zooMatchApi.updatePet(
        "Bearer $token",
        id,
        PetUpdateRequest(
          name = name,
          animal_type = animal_type,
          breed = breed,
          is_male = is_male,
          age = age,
          avatar = avatar,
          location = location,
          has_pedigree = has_pedigree,
          pedigree_documents = pedigree_documents,
          awards = awards,
          tags = tags,
          description = description,
          is_active = is_active
        )
      )
      if (response.isSuccessful && response.body() != null) {
        Result.Success(response.body()!!)
      } else {
        Result.Error(response.message() ?: "Invalid Data")
      }
    } catch (e: Exception) {
      Result.Error(e.message ?: "Network error")
    }
  }
}