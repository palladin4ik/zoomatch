package com.example.zoomatch.data.startScreen

import com.example.zoomatch.data.Result
import com.example.zoomatch.data.db.Network.zooMatchApi
import com.example.zoomatch.data.db.PetEntity
import com.example.zoomatch.data.db.UserEntity

class LoginDataSource {
  suspend fun login(email: String, password: String): Result<JwtResponse> {
    return try {
      val response = zooMatchApi.loginUser(LoginRequest(email, password))
      if (response.isSuccessful && response.body() != null) {
        Result.Success(response.body()!!)
      } else {
        Result.Error(response.message() ?: "Login failed")
      }
    } catch (e: Exception) {
      Result.Error(e.message ?: "Network error")
    }
  }

  suspend fun getUserInfo(token: String): Result<Pair<UserEntity, List<PetEntity>>> {
    return try {
      val response = zooMatchApi.getProfile("Bearer $token")
      if (response.isSuccessful && response.body() != null) {
        val data = response.body()!!
        val fullName = "${data.firstname} ${data.lastname}".trim()
        val user = UserEntity(
          id = data.id,
          name = fullName,
          firstname = data.firstname,
          lastname = data.lastname,
          email = data.email,
          avatar = data.avatar,
          location = data.location,
          status = data.description,
          phone_number = data.phone_number,
          role = data.role,
          organization = data.organization
        )
        val pets = data.pets.map { pet ->
          PetEntity(
            id = pet.id,
            name = pet.name,
            animal_type_id = pet.animal_type?.id,
            breed_id = pet.breed?.id,
            is_male = pet.is_male,
            age = pet.age,
            owner_id = data.id,
            avatar = pet.avatar,
            location = null,
            has_pedigree = false,
            pedigree_documents = null,
            awards = null,
            description = null,
            is_active = pet.is_active,
            animal_type_custom = pet.animal_type_custom,
            breed_custom = pet.breed_custom,
            moderation_status = pet.moderation_status
          )
        }
        Result.Success(user to pets)
      } else {
        Result.Error(response.message() ?: "Failed to fetch profile")
      }
    } catch (e: Exception) {
      Result.Error(e.message ?: "Network error")
    }
  }

  suspend fun getAnimalTypes(token: String): Result<List<AnimalTypeResponse>> {
    return try {
      val response = zooMatchApi.getAnimalTypes("Bearer $token")
      if (response.isSuccessful && response.body() != null) {
        Result.Success(response.body()!!)
      } else {
        Result.Error(response.message() ?: "Failed to fetch animal types")
      }
    } catch (e: Exception) {
      Result.Error(e.message ?: "Network error")
    }
  }

  suspend fun getBreeds(token: String): Result<List<BreedResponse>> {
    return try {
      val response = zooMatchApi.getBreeds("Bearer $token")
      if (response.isSuccessful && response.body() != null) {
        Result.Success(response.body()!!)
      } else {
        Result.Error(response.message() ?: "Failed to fetch breeds")
      }
    } catch (e: Exception) {
      Result.Error(e.message ?: "Network error")
    }
  }
}
