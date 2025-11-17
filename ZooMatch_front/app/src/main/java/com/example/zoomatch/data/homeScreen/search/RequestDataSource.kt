package com.example.zoomatch.data.homeScreen.search

import com.example.zoomatch.data.Result
import com.example.zoomatch.data.db.Network

class RequestDataSource {
  private val api = Network.zooMatchApi

  suspend fun getMatches(petId: Int, token: String?): Result<MatchesListResponse> {
    return try {
      if (token == null) return Result.Error("no token")
      val resp = api.getMatches("Bearer $token", petId)
      if (resp.isSuccessful && resp.body() != null) Result.Success(resp.body()!!)
      else Result.Error(resp.message())
    } catch (e: Exception) {
      Result.Error(e.message ?: "net err")
    }
  }

  suspend fun getPet(id: Int, token: String?): Result<PetLongResponse> {
    return try {
      if (token == null) return Result.Error("no token")
      val resp = api.getPetById("Bearer $token", id)
      if (resp.isSuccessful && resp.body() != null) Result.Success(resp.body()!!)
      else Result.Error(resp.message())
    } catch (e: Exception) {
      Result.Error(e.message ?: "net err")
    }
  }
}