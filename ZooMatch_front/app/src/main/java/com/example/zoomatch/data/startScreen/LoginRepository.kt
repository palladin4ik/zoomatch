package com.example.zoomatch.data.startScreen

import com.example.zoomatch.data.Result
import com.example.zoomatch.data.db.AnimalTypeEntity
import com.example.zoomatch.data.db.BreedEntity
import com.example.zoomatch.data.db.TokenManager
import com.example.zoomatch.data.db.UserDao

class LoginRepository(
  private val dataSource: LoginDataSource,
  private val tokenManager: TokenManager,
  private val userDao: UserDao
) {

  suspend fun login(email: String, password: String): Result<String> {
    val result = dataSource.login(email, password)
    if (result is Result.Success) {
      tokenManager.saveTokens(result.data.access, result.data.refresh)

      val userResult = dataSource.getUserInfo(result.data.access)
      if (userResult !is Result.Success) return userResult as Result.Error
      val (user, incomingPets) = userResult.data

      val typesResult = dataSource.getAnimalTypes(result.data.access)
      if (typesResult !is Result.Success) return typesResult as Result.Error

      val breedsResult = dataSource.getBreeds(result.data.access)
      if (breedsResult !is Result.Success) return breedsResult as Result.Error

      val types = typesResult.data.map { AnimalTypeEntity(it.id, it.name) }
      val breeds = breedsResult.data.map { BreedEntity(it.id, it.name, it.animal_type.id) }

      userDao.clear()
      userDao.clearPets()
      userDao.clearAnimalTypes()
      userDao.clearBreeds()

      userDao.insertAllAnimalTypes(types)
      userDao.insertAllBreeds(breeds)
      userDao.insert(user)
      userDao.insertAllPets(incomingPets)

      return Result.Success(user.name)
    }
    return result as Result.Error
  }
}