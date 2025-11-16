package com.example.zoomatch.data.homeScreen.pets

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

class PetsRepository(
  private val dataSource: PetsDataSource,
  private val tokenManager: TokenManager,
  private val petDao: PetDao,
  private val animalTypeDao: AnimalTypeDao,
  private val breedDao: BreedDao,
  private val userDao: UserDao
) {
  val petsFlow = petDao.getPetsFlow().flowOn(Dispatchers.IO)
  val animalTypesFlow = animalTypeDao.getAllFlow().flowOn(Dispatchers.IO)
  val breedsFlow = breedDao.getAllFlow().flowOn(Dispatchers.IO)

  suspend fun createPet(
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
    tags: List<Int>,
    description: String?,
    is_active: Boolean
  ): Result<String> {
    return when (
      val result = dataSource.createPet(
        tokenManager.getAccessToken(),
        name, animal_type, breed, is_male, age,
        avatar, location, has_pedigree,
        pedigree_documents, awards, tags.map { it.toString() },
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
          avatar = result.data.avatar,
          location = result.data.location,
          has_pedigree = result.data.has_pedigree,
          pedigree_documents = result.data.pedigree_documents,
          awards = result.data.awards,
          description = result.data.description,
          is_active = result.data.is_active
        )
        petDao.insert(pet)
//        petDao.upsertPetWithTags(pet, tags)
        Result.Success("Питомец добавлен")
      }
      is Result.Error -> result
    }
  }

  suspend fun updatePet(
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
    tags: List<Int>?,
    description: String?,
    is_active: Boolean?
  ): Result<String> {
    return when (
      val result = dataSource.updatePet(
        tokenManager.getAccessToken(),
        id,
        name, animal_type, breed, is_male, age,
        avatar, location, has_pedigree,
        pedigree_documents, awards, tags?.map { it.toString() },
        description, is_active
      )
    ) {
      is Result.Success -> {
        val current = petDao.getPetsFlow().first().find { it.id == id }
          ?: return Result.Error("Питомец не найден")
        val updated = current.copy(
          name = name ?: current.name,
          animal_type_id = animal_type ?: current.animal_type_id,
          breed_id = breed ?: current.breed_id,
          is_male = is_male ?: current.is_male,
          age = age ?: current.age,
          avatar = avatar ?: current.avatar,
          location = location ?: current.location,
          has_pedigree = has_pedigree ?: current.has_pedigree,
          pedigree_documents = pedigree_documents ?: current.pedigree_documents,
          awards = awards ?: current.awards,
          description = description ?: current.description,
          is_active = is_active ?: current.is_active
        )
        petDao.insert(updated)
//        val tagList = tags ?: emptyList()
//        petDao.upsertPetWithTags(updated, tagList)
        Result.Success("Питомец обновлён")
      }
      is Result.Error -> result
    }
  }
}