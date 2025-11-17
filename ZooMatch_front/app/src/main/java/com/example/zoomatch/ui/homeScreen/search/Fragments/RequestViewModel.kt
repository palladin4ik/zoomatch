package com.example.zoomatch.ui.homeScreen.search.Fragments

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.zoomatch.data.Result
import com.example.zoomatch.data.homeScreen.search.PetLongResponse
import com.example.zoomatch.data.homeScreen.search.RequestRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class RequestViewModel(
  private val repo: RequestRepository
) : ViewModel() {
  private val _pets = MutableStateFlow<List<PetLongResponse>>(emptyList())
  val pets = _pets.asStateFlow()

  fun load() {
    viewModelScope.launch {
      when (val r = repo.loadMatchedPets()) {
        is Result.Success -> _pets.value = r.data
        else -> {}
      }
    }
  }
}