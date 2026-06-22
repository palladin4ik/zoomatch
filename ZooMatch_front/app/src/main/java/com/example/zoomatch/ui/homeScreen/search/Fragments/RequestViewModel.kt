package com.example.zoomatch.ui.homeScreen.search.Fragments

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.zoomatch.data.Result
import com.example.zoomatch.data.homeScreen.search.MatchResponse
import com.example.zoomatch.data.homeScreen.search.RequestRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class RequestViewModel(
  private val repo: RequestRepository
) : ViewModel() {
  private val _matches = MutableStateFlow<List<MatchResponse>>(emptyList())
  val matches = _matches.asStateFlow()

  private val _error = MutableStateFlow<String?>(null)
  val error = _error.asStateFlow()

  fun load() {
    viewModelScope.launch {
      when (val r = repo.loadReceivedMatches()) {
        is Result.Success -> _matches.value = r.data
        is Result.Error -> _error.value = r.message
        else -> {}
      }
    }
  }

  fun acceptMatch(matchId: Int) {
    viewModelScope.launch {
      when (val r = repo.updateMatchStatus(matchId, 1)) {
        is Result.Success -> load()
        is Result.Error -> _error.value = r.message
        else -> {}
      }
    }
  }

  fun rejectMatch(matchId: Int) {
    viewModelScope.launch {
      when (val r = repo.updateMatchStatus(matchId, 2)) {
        is Result.Success -> load()
        is Result.Error -> _error.value = r.message
        else -> {}
      }
    }
  }
}
