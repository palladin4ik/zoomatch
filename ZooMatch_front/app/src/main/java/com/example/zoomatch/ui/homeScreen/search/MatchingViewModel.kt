package com.example.zoomatch.ui.homeScreen.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.zoomatch.data.Result
import com.example.zoomatch.data.homeScreen.search.MatchRepository
import com.example.zoomatch.data.homeScreen.search.PetLongResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MatchingViewModel(
  private val repository: MatchRepository
) : ViewModel() {

  private val _shuffledPets = MutableStateFlow<List<PetLongResponse>>(emptyList())
  val shuffledPets = _shuffledPets.asStateFlow()

  fun loadPets(animalTypeId: Int) {
    viewModelScope.launch {
      when (val result = repository.getActivePets(animalTypeId)) {
        is Result.Success -> _shuffledPets.value = result.data.shuffled()
        is Result.Error -> {}
      }
    }
  }

  fun createMatch(petFrom: Int, petTo: Int) {
    viewModelScope.launch {
      repository.createMatch(petFrom, petTo)
    }
  }

  fun getPetAt(index: Int): PetLongResponse? = shuffledPets.value.getOrNull(index)
}