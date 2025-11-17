package com.example.zoomatch.ui.homeScreen.pets

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.zoomatch.R
import com.example.zoomatch.data.Result
import com.example.zoomatch.data.db.AnimalTypeEntity
import com.example.zoomatch.data.db.BreedEntity
import com.example.zoomatch.data.homeScreen.pets.EditPetFormState
import com.example.zoomatch.data.homeScreen.pets.PetEditUI
import com.example.zoomatch.data.homeScreen.pets.PetsRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class EditPetViewModel(
  private val repository: PetsRepository
) : ViewModel() {

  private val _pet = MutableStateFlow<PetEditUI?>(null)
  val pet: StateFlow<PetEditUI?> = _pet

  private val _avatarBase64 = MutableStateFlow<String?>(null)
  val avatarBase64: StateFlow<String?> = _avatarBase64
  fun setAvatarBase64(base64: String) {
    _avatarBase64.value = base64
  }

  private val _selectedAnimalType = MutableStateFlow<AnimalTypeEntity?>(null)
  val selectedAnimalType: StateFlow<AnimalTypeEntity?> = _selectedAnimalType
  fun selectAnimalType(position: Int) {
    _selectedAnimalType.value = animalTypes.value.getOrNull(position)
    _selectedBreed.value = null
  }

  private val _selectedBreed = MutableStateFlow<BreedEntity?>(null)
  val selectedBreed: StateFlow<BreedEntity?> = _selectedBreed
  fun selectBreed(position: Int) {
    _selectedBreed.value = breeds.value.getOrNull(position)
  }

  private val _isMale = MutableStateFlow(true)
  val isMale: StateFlow<Boolean> = _isMale
  fun setIsMale(isMale: Boolean) {
    _isMale.value = isMale
  }

  val animalTypes = repository.animalTypesFlow
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val breeds = combine(repository.breedsFlow, _selectedAnimalType) { allBreeds, selectedType ->
    selectedType?.id?.let { typeId -> allBreeds.filter { it.animal_type == typeId } } ?: emptyList()
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  private val _formState = MutableStateFlow(EditPetFormState())
  val formState: StateFlow<EditPetFormState> = _formState

  // TODO: валидация
  fun validate(name: String, age: String, location: String) {
    val nameError = if (name.isBlank()) R.string.invalid_name else null
    val ageError = if (age.toIntOrNull() == null || age.toInt() <= 0) R.string.invalid_age else null
    val typeError = if (_selectedAnimalType.value == null) R.string.invalid_type else null
    val breedError = if (_selectedBreed.value == null) R.string.invalid_breed else null
    val locationError = if (location.isBlank()) R.string.invalid_location else null
    val isValid =
      ((nameError == null) && (ageError == null) && (typeError == null) && (breedError == null) && (locationError == null))

    _formState.value =
      EditPetFormState(nameError, ageError, typeError, breedError, locationError, isValid)
  }

  private val _saveResult = Channel<Result<String>>(Channel.CONFLATED)
  val saveResult = _saveResult.receiveAsFlow()

  fun save(
    id: Int?,
    avatar: String?,
    name: String,
    animalTypeId: Int?,
    breedId: Int?,
    isMale: Boolean,
    age: Int,
    location: String?,
    hasPedigree: Boolean,
    pedigreeDocs: String?,
    awards: String?,
    description: String?,
    isActive: Boolean
  ) {
    viewModelScope.launch {
      val result = if (id == null) {
        repository.createPet(
          name = name,
          animal_type = animalTypeId ?: return@launch,
          breed = breedId ?: return@launch,
//          animal_type = animalTypeId,
//          breed = breedId,
          is_male = isMale,
          age = age,
          avatar = avatar,
          location = location,
          has_pedigree = hasPedigree,
          pedigree_documents = pedigreeDocs,
          awards = awards,
          tags = emptyList(),
          description = description,
          is_active = isActive
        )
      } else {
        repository.updatePet(
          id = id,
          name = name,
          animal_type = animalTypeId,
          breed = breedId,
          is_male = isMale,
          age = age,
          avatar = avatar,
          location = location,
          has_pedigree = hasPedigree,
          pedigree_documents = pedigreeDocs,
          awards = awards,
          tags = null,
          description = description,
          is_active = isActive
        )
      }
      _saveResult.send(result)
    }
  }

  private val _deleteResult = Channel<Result<String>>(Channel.CONFLATED)
  val deleteResult = _deleteResult.receiveAsFlow()

  fun deletePet(id: Int) {
    viewModelScope.launch {
      _deleteResult.send(repository.deletePet(id))
    }
  }

  fun loadPet(petId: Int?) {
    if (petId == null) return
    viewModelScope.launch {
      val pet = repository.petsFlow.firstOrNull()?.find { it.id == petId } ?: return@launch

      _pet.value = PetEditUI(
        avatar = pet.avatar,
        name = pet.name,
        location = pet.location,
        description = pet.description,
        has_pedigree = pet.has_pedigree,
        pedigree_documents = pet.pedigree_documents,
        awards = pet.awards,
        is_active = pet.is_active,
        age = pet.age
      )

      val types = repository.animalTypesFlow.first()
      _selectedAnimalType.value = types.find { it.id == pet.animal_type_id }

      val breeds = repository.breedsFlow.first()
      _selectedBreed.value = breeds.find { it.id == pet.breed_id }

      _isMale.value = pet.is_male
    }
  }
}