package com.example.zoomatch.data.startScreen

import com.example.zoomatch.data.Result
import com.example.zoomatch.data.db.TokenManager
import com.example.zoomatch.data.db.UserDao


class RegRepository(
  private val regDataSource: RegDataSource,
  private val loginDataSource: LoginDataSource,
  private val tokenManager: TokenManager,
  private val userDao: UserDao
) {

  suspend fun registerAndLogin(firstname: String, lastname: String, email: String, password: String): Result<String> {
    val regResult = regDataSource.register(firstname, lastname, email, password)
    if (regResult is Result.Success) {
      val loginResult = loginDataSource.login(email, password)
      if (loginResult is Result.Success) {
        tokenManager.saveTokens(loginResult.data.access, loginResult.data.refresh)
        val userResult = loginDataSource.getUserInfo(loginResult.data.access)
        if (userResult is Result.Success) {
          val (user, pets) = userResult.data

          userDao.clear()
          userDao.clearPets()

          val typesResult = loginDataSource.getAnimalTypes(loginResult.data.access)
          val breedsResult = loginDataSource.getBreeds(loginResult.data.access)

          if (typesResult is Result.Success) {
            userDao.clearAnimalTypes()
            userDao.insertAllAnimalTypes(typesResult.data.map { com.example.zoomatch.data.db.AnimalTypeEntity(it.id, it.name) })
          }
          if (breedsResult is Result.Success) {
            userDao.clearBreeds()
            userDao.insertAllBreeds(breedsResult.data.map { com.example.zoomatch.data.db.BreedEntity(it.id, it.name, it.animal_type.id) })
          }

          userDao.insert(user)
          userDao.insertAllPets(pets)
          return Result.Success(user.name)
        } else {
          return Result.Error((userResult as Result.Error).message)
        }
      } else {
        return Result.Error((loginResult as Result.Error).message)
      }
    } else {
      return Result.Error((regResult as Result.Error).message)
    }
  }
}
