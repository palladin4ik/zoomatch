package com.example.zoomatch.ui.homeScreen.search.Fragments

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.zoomatch.data.homeScreen.pets.PetUI
import com.example.zoomatch.data.homeScreen.pets.PetsRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SearchViewModel(
  private val repository: PetsRepository
) : ViewModel() {

  val activePets = combine(
    repository.petsFlow,
    repository.breedsFlow
  ) { petList, breedList ->
    petList.filter { it.is_active }
      .map { pet ->
        val breedName = pet.breed_id?.let { id ->
          breedList.find { it.id == id }?.name ?: "—"
        } ?: "—"
        PetUI(
          id = pet.id.toString(),
          name = pet.name,
          breed = breedName,
          age = pet.age,
          status = "В активном поиске",
          avatar = pet.avatar
        )
      }
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  private val _openMatching = Channel<Pair<Int, Int>>(Channel.CONFLATED)
  val openMatching = _openMatching.receiveAsFlow()

  fun onPetClick(petId: Int) = viewModelScope.launch {
    val pet = activePets.value.find { it.id.toInt() == petId }
    val typeId = pet?.let { repository.petsFlow.first().find { p -> p.id == petId }?.animal_type_id ?: 0 } ?: 0
    _openMatching.send(Pair(petId, typeId))
  }
}