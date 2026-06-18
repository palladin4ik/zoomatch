package com.example.zoomatch.ui.homeScreen.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.zoomatch.data.Result
import com.example.zoomatch.data.homeScreen.search.MatchRepository
import com.example.zoomatch.data.homeScreen.search.PetShortRecommendation
import com.example.zoomatch.data.homeScreen.search.SearchFilterParams
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MatchingViewModel(
  private val repository: MatchRepository
) : ViewModel() {

  private val _pets = MutableStateFlow<List<PetShortRecommendation>>(emptyList())
  val pets = _pets.asStateFlow()

  private val _isLoading = MutableStateFlow(false)
  val isLoading = _isLoading.asStateFlow()

  private val _error = MutableStateFlow<String?>(null)
  val error = _error.asStateFlow()

  private val _suggestExpand = MutableStateFlow(false)
  val suggestExpand = _suggestExpand.asStateFlow()

  private var currentPetId: Int = 0
  private var currentParams = SearchFilterParams()

  fun loadRecommendations(petId: Int, params: SearchFilterParams = SearchFilterParams()) {
    currentPetId = petId
    currentParams = params
    _isLoading.value = true
    _error.value = null
    viewModelScope.launch {
      when (val result = repository.getRecommendations(petId, params)) {
        is Result.Success -> {
          _pets.value = result.data.shuffled()
          _suggestExpand.value = false
        }
        is Result.Error -> {
          _error.value = result.message
        }
      }
      _isLoading.value = false
    }
  }

  fun applyFilter(params: SearchFilterParams, petId: Int = currentPetId) {
    if (petId != 0) {
      loadRecommendations(petId, params)
    }
  }

  fun createMatch(petFrom: Int, petTo: Int) {
    viewModelScope.launch {
      repository.createMatch(petFrom, petTo)
    }
  }

  fun getPetAt(index: Int): PetShortRecommendation? = _pets.value.getOrNull(index)
}
