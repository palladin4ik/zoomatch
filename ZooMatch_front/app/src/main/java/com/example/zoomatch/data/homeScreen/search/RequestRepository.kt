package com.example.zoomatch.data.homeScreen.search

import com.example.zoomatch.data.Result
import com.example.zoomatch.data.db.TokenManager

class RequestRepository(
  private val dataSource: RequestDataSource,
  private val tokenManager: TokenManager
) {
  suspend fun loadReceivedMatches(): Result<List<MatchResponse>> {
    val token = tokenManager.getAccessToken() ?: return Result.Error("no token")
    return dataSource.getReceivedMatches(token)
  }

  suspend fun loadSentMatches(): Result<List<MatchResponse>> {
    val token = tokenManager.getAccessToken() ?: return Result.Error("no token")
    return dataSource.getSentMatches(token)
  }

  suspend fun updateMatchStatus(
    matchId: Int,
    newStatus: Int
  ): Result<MatchResponse> {
    val token = tokenManager.getAccessToken() ?: return Result.Error("no token")
    return dataSource.updateMatchStatus(token, matchId, newStatus)
  }
}
