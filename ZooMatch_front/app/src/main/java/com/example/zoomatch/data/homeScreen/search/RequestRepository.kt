package com.example.zoomatch.data.homeScreen.search

import com.example.zoomatch.data.Result
import com.example.zoomatch.data.db.TokenManager
import com.example.zoomatch.data.db.UserDao

class RequestRepository(
  private val dataSource: RequestDataSource,
  private val tokenManager: TokenManager,
  private val userDao: UserDao
) {
  suspend fun loadMatchedPets(): Result<List<PetLongResponse>> {
    val token = tokenManager.getAccessToken() ?: return Result.Error("no token")
    val petIds = userDao.getCurrentUserPetIds()
    val result = mutableListOf<PetLongResponse>()
    for (fromId in petIds) {
      when (val matches = dataSource.getMatches(fromId, token)) {
        is Result.Success -> {
          for (m in matches.data.liked_by) {
            when (val pet = dataSource.getPet(m, token)) {
              is Result.Success -> result += pet.data
              else -> {}
            }
          }
        }

        else -> {}
      }
    }
    return Result.Success(result)
  }
}