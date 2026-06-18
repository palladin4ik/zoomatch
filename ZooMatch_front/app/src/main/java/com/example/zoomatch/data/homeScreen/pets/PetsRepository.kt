package com.example.zoomatch.data.homeScreen.pets

import android.util.Log
import com.example.zoomatch.data.Result
import com.example.zoomatch.data.db.AnimalTypeDao
import com.example.zoomatch.data.db.BreedDao
import com.example.zoomatch.data.db.PetDao
import com.example.zoomatch.data.db.PetEntity
import com.example.zoomatch.data.db.TokenManager
import com.example.zoomatch.data.db.UserDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import okhttp3.MultipartBody

class PetsRepository(
  private val dataSource: PetsDataSource,
  private val tokenManager: TokenManager,
  private val petDao: PetDao,
  private val animalTypeDao: AnimalTypeDao,
  private val breedDao: BreedDao,
  private val userDao: UserDao
) {
  val breedsFlow = breedDao.getAllFlow().flowOn(Dispatchers.IO)
  val animalTypesFlow = animalTypeDao.getAllFlow().flowOn(Dispatchers.IO)
  val petsFlow = petDao.getPetsFlow().flowOn(Dispatchers.IO)

  suspend fun createPet(
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
    tags: List<Int>,
    description: String?,
    is_active: Boolean
  ): Result<Int> {
    return when (
      val result = dataSource.createPet(
        tokenManager.getAccessToken(),
        name, animal_type, animal_type_custom, breed, breed_custom,
        is_male, age, address, latitude, longitude, has_pedigree,
        awards, tags.map { it.toString() },
        description, is_active
      )
    ) {
      is Result.Success -> {
        val pet = PetEntity(
          id = result.data.id,
          name = result.data.name,
          animal_type_id = result.data.animal_type,
          breed_id = result.data.breed,
          is_male = result.data.is_male,
          age = result.data.age,
          owner_id = userDao.getCurrentUserFlow().first()?.id ?: 0,
          avatar = null,
          location = result.data.location,
          has_pedigree = result.data.has_pedigree,
          pedigree_documents = null,
          awards = result.data.awards,
          description = result.data.description,
          is_active = result.data.is_active,
          animal_type_custom = animal_type_custom,
          breed_custom = breed_custom,
          moderation_status = if (animal_type_custom != null || breed_custom != null) "pending" else null
        )
        petDao.insert(pet)
        Result.Success(result.data.id)
      }

      is Result.Error -> result
    }
  }

  suspend fun updatePet(
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
    tags: List<Int>?,
    description: String?,
    is_active: Boolean?
  ): Result<String> = withContext(Dispatchers.IO) {
    when (
      val result = dataSource.updatePet(
        tokenManager.getAccessToken(),
        id,
        name, animal_type, animal_type_custom, breed, breed_custom,
        is_male, age, address, latitude, longitude, has_pedigree,
        awards, tags?.map { it.toString() },
        description, is_active
      )
    ) {
      is Result.Success -> {
        withContext(Dispatchers.IO) {
          val current = petDao.getPetById(id)
            ?: return@withContext Result.Error("Питомец не найден")

          val updated = current.copy(
            name = name ?: current.name,
            animal_type_id = animal_type ?: current.animal_type_id,
            breed_id = breed ?: current.breed_id,
            is_male = is_male ?: current.is_male,
            age = age ?: current.age,
            location = address ?: current.location,
            has_pedigree = has_pedigree ?: current.has_pedigree,
            awards = awards ?: current.awards,
            description = description ?: current.description,
            is_active = is_active ?: current.is_active,
            animal_type_custom = animal_type_custom ?: current.animal_type_custom,
            breed_custom = breed_custom ?: current.breed_custom,
            moderation_status = current.moderation_status
          )
          Log.d("UPDATE", "Before: ${petDao.getPetById(id)}")
          petDao.insert(updated)
          Log.d("UPDATE", "After: ${petDao.getPetById(id)}")
        }
        Result.Success("Питомец обновлён")
      }

      is Result.Error -> result
    }
  }

  suspend fun deletePet(id: Int): Result<String> {
    return when (val result = dataSource.deletePet(tokenManager.getAccessToken(), id)) {
      is Result.Success -> {
        petDao.deleteById(id)
        Result.Success("Питомец удалён")
      }

      is Result.Error -> result
    }
  }

  suspend fun uploadPedigreeDocuments(petId: Int, documents: MultipartBody.Part): Result<String> {
    return when (val result = dataSource.uploadPedigreeDocuments(tokenManager.getAccessToken(), petId, documents)) {
      is Result.Success -> {
        val docsUrl = result.data
        if (docsUrl.startsWith("/")) {
          petDao.updatePedigreeDocuments(petId, docsUrl)
        }
        result
      }
      is Result.Error -> result
    }
  }

  suspend fun uploadPetAvatar(petId: Int, avatar: MultipartBody.Part): Result<String> {
    return when (val result = dataSource.uploadPetAvatar(tokenManager.getAccessToken(), petId, avatar)) {
      is Result.Success -> {
        val avatarUrl = result.data
        if (avatarUrl.startsWith("/")) {
          petDao.updateAvatar(petId, avatarUrl)
        }
        result
      }
      is Result.Error -> result
    }
  }

  suspend fun deletePedigreeDocuments(petId: Int): Result<String> {
    return dataSource.deletePedigreeDocuments(tokenManager.getAccessToken(), petId)
  }
}
