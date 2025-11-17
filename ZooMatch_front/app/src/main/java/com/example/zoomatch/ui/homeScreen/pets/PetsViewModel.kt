package com.example.zoomatch.ui.homeScreen.pets

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.zoomatch.data.homeScreen.pets.PetUI
import com.example.zoomatch.data.homeScreen.pets.PetsRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class PetsViewModel(
  repository: PetsRepository
) : ViewModel() {

  val breeds = repository.breedsFlow
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val petTypes = repository.animalTypesFlow
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val searchQuery = MutableStateFlow("")

  val selectedTypeId = MutableStateFlow<Int?>(null)

  val filteredPets = combine(
    repository.petsFlow, breeds, searchQuery, selectedTypeId
  ) { petList, breedList, query, typeId ->
    petList.filter {
      it.name.contains(query, ignoreCase = true) &&
          (typeId == null || it.animal_type_id == typeId)
    }.map { pet ->
      val breedName = pet.breed_id?.let { id ->
        breedList.find { it.id == id }?.name ?: "Порода $id"
      } ?: "—"
      PetUI(
        id = pet.id.toString(),
        name = pet.name,
        breed = breedName,
        age = pet.age,
        status = if (pet.is_active) "В активном поиске" else "Не ищет пару",
        avatar = pet.avatar
      )
    }
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

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