package com.example.zoomatch.data.homeScreen.pets

import com.example.zoomatch.data.Result
import com.example.zoomatch.data.db.Network.zooMatchApi
import com.example.zoomatch.data.homeScreen.search.PetLongResponse
import com.google.gson.Gson
import com.google.gson.JsonObject
import okhttp3.MultipartBody

class PetsDataSource {

  private fun parseServerError(response: retrofit2.Response<*>): String {
    return try {
      val errorBody = response.errorBody()?.string() ?: return response.message() ?: "Unknown error"
      val json = Gson().fromJson(errorBody, JsonObject::class.java)
      val messages = mutableListOf<String>()
      for (key in json.keySet()) {
        val value = json.get(key)
        if (value.isJsonArray) {
          value.asJsonArray.forEach { messages.add("$key: ${it.asString}") }
        } else {
          messages.add("$key: ${value.asString}")
        }
      }
      if (messages.isNotEmpty()) messages.joinToString("\n") else response.message() ?: "Invalid Data"
    } catch (e: Exception) {
      response.message() ?: "Invalid Data"
    }
  }

  suspend fun createPet(
    token: String?,
    name: String,
    animal_type: Int?,
    animal_type_custom: String?,
    breed: Int?,
    breed_custom: String?,
    is_male: Boolean,
    age: Int,
    address: String?,
    latitude: Double?,
    longitude: Double?,
    has_pedigree: Boolean,
    awards: String?,
    tags: List<String>,
    description: String?,
    is_active: Boolean
  ): Result<PetCreateResponse> {
    return try {
      val response = zooMatchApi.createPet(
        "Bearer $token",
        PetCreateRequest(
          name = name,
          animal_type = animal_type,
          animal_type_custom = animal_type_custom,
          breed = breed,
          breed_custom = breed_custom,
          is_male = is_male,
          age = age,
          address = address,
          latitude = latitude,
          longitude = longitude,
          has_pedigree = has_pedigree,
          awards = awards,
          tags = tags,
          description = description,
          is_active = is_active
        )
      )
      if (response.isSuccessful) {
        val body = response.body()
        if (body != null) {
          Result.Success(body)
        } else {
          Result.Success(PetCreateResponse(
            id = 0, name = name, animal_type = animal_type, breed = breed,
            is_male = is_male, age = age, location = null,
            has_pedigree = has_pedigree, awards = awards, tags_list = null,
            description = description, is_active = is_active,
            animal_type_custom = animal_type_custom, breed_custom = breed_custom
          ))
        }
      } else {
        Result.Error(parseServerError(response))
      }
    } catch (e: com.google.gson.JsonSyntaxException) {
      Result.Error("Ошибка формата данных от сервера")
    } catch (e: java.lang.IllegalStateException) {
      Result.Error("Сервер вернул неожиданный ответ")
    } catch (e: Exception) {
      Result.Error(e.message ?: "Network error")
    }
  }

  suspend fun updatePet(
    token: String?,
    id: Int,
    name: String?,
    animal_type: Int?,
    animal_type_custom: String?,
    breed: Int?,
    breed_custom: String?,
    is_male: Boolean?,
    age: Int?,
    address: String?,
    latitude: Double?,
    longitude: Double?,
    has_pedigree: Boolean?,
    awards: String?,
    tags: List<String>?,
    description: String?,
    is_active: Boolean?
  ): Result<PetCreateResponse> {
    return try {
      val response = zooMatchApi.updatePet(
        "Bearer $token",
        id,
        PetUpdateRequest(
          name = name,
          animal_type = animal_type,
          animal_type_custom = animal_type_custom,
          breed = breed,
          breed_custom = breed_custom,
          is_male = is_male,
          age = age,
          address = address,
          latitude = latitude,
          longitude = longitude,
          has_pedigree = has_pedigree,
          awards = awards,
          tags = tags,
          description = description,
          is_active = is_active
        )
      )
      if (response.isSuccessful) {
        val body = response.body()
        if (body != null) {
          Result.Success(body)
        } else {
          Result.Success(PetCreateResponse(
            id = id, name = name ?: "", animal_type = animal_type, breed = breed,
            is_male = is_male ?: true, age = age ?: 0, location = null,
            has_pedigree = has_pedigree ?: false, awards = awards, tags_list = null,
            description = description, is_active = is_active ?: false,
            animal_type_custom = animal_type_custom, breed_custom = breed_custom
          ))
        }
      } else {
        Result.Error(parseServerError(response))
      }
    } catch (e: com.google.gson.JsonSyntaxException) {
      Result.Error("Ошибка формата данных от сервера")
    } catch (e: java.lang.IllegalStateException) {
      Result.Error("Сервер вернул неожиданный ответ")
    } catch (e: Exception) {
      Result.Error(e.message ?: "Network error")
    }
  }

  suspend fun deletePet(token: String?, id: Int): Result<String> {
    return try {
      val response = zooMatchApi.deletePet("Bearer $token", id)
      if (response.isSuccessful) {
        Result.Success("Питомец удалён")
      } else {
        Result.Error(response.message() ?: "Ошибка удаления")
      }
    } catch (e: Exception) {
      Result.Error(e.message ?: "Network error")
    }
  }

  suspend fun getPetById(token: String?, id: Int): Result<PetLongResponse> {
    return try {
      val response = zooMatchApi.getPetById("Bearer $token", id)
      if (response.isSuccessful && response.body() != null) {
        Result.Success(response.body()!!)
      } else {
        Result.Error(response.message() ?: "Failed to fetch pet")
      }
    } catch (e: Exception) {
      Result.Error(e.message ?: "Network error")
    }
  }

  suspend fun uploadPedigreeDocuments(
    token: String?,
    petId: Int,
    documents: MultipartBody.Part
  ): Result<String> {
    return try {
      val response = zooMatchApi.uploadPetDocuments("Bearer $token", petId, documents)
      if (response.isSuccessful) {
        val docsUrl = response.body()?.pedigree_documents
        if (docsUrl != null) {
          Result.Success(docsUrl)
        } else {
          Result.Success("Документы загружены")
        }
      } else {
        Result.Error(parseServerError(response))
      }
    } catch (e: Exception) {
      Result.Error(e.message ?: "Network error")
    }
  }

  suspend fun uploadPetAvatar(
    token: String?,
    petId: Int,
    avatar: MultipartBody.Part
  ): Result<String> {
    return try {
      val response = zooMatchApi.uploadPetAvatar("Bearer $token", petId, avatar)
      if (response.isSuccessful) {
        val avatarUrl = response.body()?.avatar
        if (avatarUrl != null) {
          Result.Success(avatarUrl)
        } else {
          Result.Success("Аватар загружен")
        }
      } else {
        Result.Error(parseServerError(response))
      }
    } catch (e: Exception) {
      Result.Error(e.message ?: "Network error")
    }
  }

  suspend fun deletePedigreeDocuments(token: String?, petId: Int): Result<String> {
    return try {
      val response = zooMatchApi.deletePetDocuments("Bearer $token", petId)
      if (response.isSuccessful) {
        Result.Success("Документы удалены")
      } else {
        Result.Error(response.message() ?: "Ошибка удаления документов")
      }
    } catch (e: Exception) {
      Result.Error(e.message ?: "Network error")
    }
  }
}
