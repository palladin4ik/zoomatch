package com.example.zoomatch.data.homeScreen.search

import com.example.zoomatch.data.Result
import com.example.zoomatch.data.db.Network

class RequestDataSource {
  private val api = Network.zooMatchApi

  suspend fun getReceivedMatches(token: String?): Result<List<MatchResponse>> {
    return try {
      if (token == null) return Result.Error("no token")
      val resp = api.getMatchesList("Bearer $token", type = "received")
      if (resp.isSuccessful && resp.body() != null) Result.Success(resp.body()!!)
      else Result.Error(resp.message() ?: "Failed to load matches")
    } catch (e: Exception) {
      Result.Error(e.message ?: "net err")
    }
  }

  suspend fun getSentMatches(token: String?): Result<List<MatchResponse>> {
    return try {
      if (token == null) return Result.Error("no token")
      val resp = api.getMatchesList("Bearer $token", type = "sent")
      if (resp.isSuccessful && resp.body() != null) Result.Success(resp.body()!!)
      else Result.Error(resp.message() ?: "Failed to load matches")
    } catch (e: Exception) {
      Result.Error(e.message ?: "net err")
    }
  }

  suspend fun updateMatchStatus(
    token: String?,
    matchId: Int,
    newStatus: Int
  ): Result<MatchResponse> {
    return try {
      if (token == null) return Result.Error("no token")
      val resp = api.updateMatchStatus(
        "Bearer $token",
        matchId,
        com.example.zoomatch.data.db.MatchStatusRequest(status = newStatus)
      )
      if (resp.isSuccessful && resp.body() != null) Result.Success(resp.body()!!)
      else Result.Error(resp.message() ?: "Failed to update match")
    } catch (e: Exception) {
      Result.Error(e.message ?: "net err")
    }
  }

  suspend fun getPet(id: Int, token: String?): Result<PetLongResponse> {
    return try {
      if (token == null) return Result.Error("no token")
      val resp = api.getPetById("Bearer $token", id)
      if (resp.isSuccessful && resp.body() != null) Result.Success(resp.body()!!)
      else Result.Error(resp.message() ?: "Failed to load pet")
    } catch (e: Exception) {
      Result.Error(e.message ?: "net err")
    }
  }
}
