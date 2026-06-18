package com.example.zoomatch.data.homeScreen.search

import com.example.zoomatch.data.Result
import com.example.zoomatch.data.db.TokenManager

class MatchRepository(
  private val dataSource: MatchDataSource,
  private val tokenManager: TokenManager
) {

  suspend fun getActivePets(animalType: Int? = null): Result<List<PetLongResponse>> {
    val token = tokenManager.getAccessToken()
    return dataSource.getActivePets(token, animalType)
  }

  suspend fun createMatch(petFrom: Int, petTo: Int): Result<MatchResponse> {
    val token = tokenManager.getAccessToken()
    return dataSource.createMatch(token, petFrom, petTo)
  }

  suspend fun getRecommendations(
    petId: Int,
    params: SearchFilterParams = SearchFilterParams()
  ): Result<List<PetShortRecommendation>> {
    val token = tokenManager.getAccessToken()
    return dataSource.getRecommendations(token, petId, params)
  }
}