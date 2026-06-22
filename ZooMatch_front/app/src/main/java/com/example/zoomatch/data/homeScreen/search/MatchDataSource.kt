package com.example.zoomatch.data.homeScreen.search

import com.example.zoomatch.data.Result
import com.example.zoomatch.data.db.Network

class MatchDataSource {

  suspend fun getActivePets(
    token: String?,
    animalType: Int? = null
  ): Result<List<PetLongResponse>> {
    return try {
      val response = Network.zooMatchApi.getActivePets(
        "Bearer $token",
        animalType
      )
      if (response.isSuccessful && response.body() != null) {
        Result.Success(response.body()!!)
      } else {
        Result.Error(response.message() ?: "Invalid data")
      }
    } catch (e: Exception) {
      Result.Error(e.message ?: "Network error")
    }
  }

  suspend fun createMatch(
    token: String?,
    petFrom: Int,
    petTo: Int
  ): Result<MatchResponse> {
    return try {
      val response = Network.zooMatchApi.createMatch(
        "Bearer $token",
        MatchRequest(petFrom, petTo)
      )
      if (response.isSuccessful && response.body() != null) {
        Result.Success(response.body()!!)
      } else {
        Result.Error(response.message() ?: "Invalid data")
      }
    } catch (e: Exception) {
      Result.Error(e.message ?: "Network error")
    }
  }

  suspend fun getRecommendations(
    token: String?,
    petId: Int,
    params: SearchFilterParams = SearchFilterParams()
  ): Result<List<PetShortRecommendation>> {
    return try {
      val response = Network.zooMatchApi.getRecommendations(
        auth = "Bearer $token",
        petId = petId,
        radiusKm = params.radiusKm,
        requiresPedigree = params.requiresPedigree.takeIf { it },
        minAge = params.minAge,
        maxAge = params.maxAge,
        maxMonthsSinceMating = params.maxMonthsSinceMating
      )
      if (response.isSuccessful && response.body() != null) {
        Result.Success(response.body()!!.results)
      } else {
        Result.Error(response.message() ?: "Ошибка загрузки рекомендаций")
      }
    } catch (e: Exception) {
      Result.Error(e.message ?: "Network error")
    }
  }
}