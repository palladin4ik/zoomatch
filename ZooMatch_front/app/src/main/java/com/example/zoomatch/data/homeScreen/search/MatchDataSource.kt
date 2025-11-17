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

}