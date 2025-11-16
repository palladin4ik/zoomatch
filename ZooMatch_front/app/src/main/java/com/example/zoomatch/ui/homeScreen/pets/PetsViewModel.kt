package com.example.zoomatch.ui.homeScreen.pets

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.zoomatch.data.homeScreen.pets.PetUI
import com.example.zoomatch.data.homeScreen.pets.PetsRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class PetsViewModel(
  repository: PetsRepository
) : ViewModel() {

  val pets = repository.petsFlow
    .map { list ->
      list.map { pet ->
        PetUI(
          id = pet.id.toString(),
          name = pet.name,
          breed = pet.breed_id?.let { "Порода $it" } ?: "—",
          age = pet.age,
          status = if (pet.is_active) "В активном поиске" else "Не ищет пару",
          avatar = pet.avatar
        )
      }
    }
    .stateIn(
      scope = viewModelScope,
      started = SharingStarted.WhileSubscribed(5000),
      initialValue = emptyList()
    )

  private val _openAddPet = Channel<Unit>(Channel.CONFLATED)
  val openAddPet = _openAddPet.receiveAsFlow()

  fun onAddPetClick() = viewModelScope.launch {
    _openAddPet.send(Unit)
  }

  private val _openEditPet = Channel<Int>(Channel.CONFLATED)
  val openEditPet = _openEditPet.receiveAsFlow()

  fun onPetClick(petId: Int) = viewModelScope.launch {
    _openEditPet.send(petId)
  }
}