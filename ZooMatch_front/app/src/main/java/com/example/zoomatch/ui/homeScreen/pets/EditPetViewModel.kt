package com.example.zoomatch.ui.homeScreen.pets

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.zoomatch.R
import com.example.zoomatch.data.Result
import com.example.zoomatch.data.db.AnimalTypeDao
import com.example.zoomatch.data.db.AnimalTypeEntity
import com.example.zoomatch.data.db.BreedDao
import com.example.zoomatch.data.db.BreedEntity
import com.example.zoomatch.data.homeScreen.pets.PetEditUI
import com.example.zoomatch.data.homeScreen.pets.PetsRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class EditPetViewModel(
  private val repository: PetsRepository,
  private val animalTypeDao: AnimalTypeDao,
  private val breedDao: BreedDao
) : ViewModel() {

  private val _pet = MutableStateFlow<PetEditUI?>(null)
  val pet: StateFlow<PetEditUI?> = _pet

  private val _avatarBase64 = MutableStateFlow<String?>(null)
  val avatarBase64: StateFlow<String?> = _avatarBase64
  fun setAvatarBase64(base64: String) {
    _avatarBase64.value = base64
  }

  private val _avatarUri = MutableStateFlow<android.net.Uri?>(null)
  val avatarUri: StateFlow<android.net.Uri?> = _avatarUri
  fun setAvatarUri(uri: android.net.Uri?) {
    _avatarUri.value = uri
  }

  private val _selectedAnimalType = MutableStateFlow<AnimalTypeEntity?>(null)
  val selectedAnimalType: StateFlow<AnimalTypeEntity?> = _selectedAnimalType

  private val _customAnimalType = MutableStateFlow<String?>(null)
  val customAnimalType: StateFlow<String?> = _customAnimalType

  private val _animalTypeSearchResults = MutableStateFlow<List<AnimalTypeEntity>>(emptyList())
  val animalTypeSearchResults: StateFlow<List<AnimalTypeEntity>> = _animalTypeSearchResults

  private val _animalTypeQuery = MutableStateFlow("")
  val animalTypeQuery: StateFlow<String> = _animalTypeQuery

  private var animalTypeSearchJob: Job? = null

  private var _isLoadingPet = false

  fun onAnimalTypeQueryChanged(query: String) {
    if (_isLoadingPet) return
    _animalTypeQuery.value = query
    _customAnimalType.value = null
    _selectedAnimalType.value = null
    animalTypeSearchJob?.cancel()
    animalTypeSearchJob = viewModelScope.launch {
      delay(300)
      if (query.isBlank()) {
        _animalTypeSearchResults.value = emptyList()
        return@launch
      }
      val results = animalTypeDao.search(query.trim())
      _animalTypeSearchResults.value = results

      val exact = animalTypeDao.findExact(query.trim())
      if (exact != null) {
        selectAnimalType(exact)
      }
    }
  }

  fun selectAnimalType(type: AnimalTypeEntity) {
    _selectedAnimalType.value = type
    _customAnimalType.value = null
    _animalTypeQuery.value = type.name
    _animalTypeSearchResults.value = emptyList()
    _selectedBreed.value = null
    _customBreed.value = null
    _breedQuery.value = ""
    _breedSearchResults.value = emptyList()
  }

  fun setCustomAnimalType(name: String) {
    _selectedAnimalType.value = null
    _customAnimalType.value = name
    _animalTypeSearchResults.value = emptyList()
    _selectedBreed.value = null
    _customBreed.value = null
    _breedQuery.value = ""
    _breedSearchResults.value = emptyList()
  }

  private val _selectedBreed = MutableStateFlow<BreedEntity?>(null)
  val selectedBreed: StateFlow<BreedEntity?> = _selectedBreed

  private val _customBreed = MutableStateFlow<String?>(null)
  val customBreed: StateFlow<String?> = _customBreed

  private val _breedSearchResults = MutableStateFlow<List<BreedEntity>>(emptyList())
  val breedSearchResults: StateFlow<List<BreedEntity>> = _breedSearchResults

  private val _breedQuery = MutableStateFlow("")
  val breedQuery: StateFlow<String> = _breedQuery

  private var breedSearchJob: Job? = null

  fun onBreedQueryChanged(query: String) {
    if (_isLoadingPet) return
    _breedQuery.value = query
    _customBreed.value = null
    _selectedBreed.value = null
    breedSearchJob?.cancel()
    breedSearchJob = viewModelScope.launch {
      delay(300)
      val typeId = _selectedAnimalType.value?.id
      if (query.isBlank() || typeId == null) {
        _breedSearchResults.value = emptyList()
        return@launch
      }
      val results = breedDao.search(typeId, query.trim())
      _breedSearchResults.value = results

      val exact = breedDao.findExact(typeId, query.trim())
      if (exact != null) {
        selectBreed(exact)
      }
    }
  }

  fun selectBreed(breed: BreedEntity) {
    _selectedBreed.value = breed
    _customBreed.value = null
    _breedQuery.value = breed.name
    _breedSearchResults.value = emptyList()
  }

  fun setCustomBreed(name: String) {
    _selectedBreed.value = null
    _customBreed.value = name
    _breedSearchResults.value = emptyList()
  }

  private val _isMale = MutableStateFlow(true)
  val isMale: StateFlow<Boolean> = _isMale

  private val _latitude = MutableStateFlow<Double?>(null)
  val latitude: StateFlow<Double?> = _latitude
  private val _longitude = MutableStateFlow<Double?>(null)
  val longitude: StateFlow<Double?> = _longitude

  fun setCoordinates(lat: Double, lon: Double) {
    _latitude.value = lat
    _longitude.value = lon
  }

  fun setIsMale(isMale: Boolean) {
    _isMale.value = isMale
  }

  fun setAddressText(text: String) {
    _addressText.value = text
  }

  private val _addressText = MutableStateFlow("")
  val addressText: StateFlow<String> = _addressText

  fun setAnimalTypeInputActive(active: Boolean) {
    _animalTypeInputActive.value = active
  }

  private val _animalTypeInputActive = MutableStateFlow(false)
  val animalTypeInputActive: StateFlow<Boolean> = _animalTypeInputActive

  private var lastEditedField: String? = null

  data class FieldErrors(
    val nameError: Int? = null,
    val ageError: Int? = null,
    val typeError: Int? = null,
    val breedError: Int? = null,
    val addressError: Int? = null
  )

  private val _fieldErrors = MutableStateFlow(FieldErrors())
  val fieldErrors: StateFlow<FieldErrors> = _fieldErrors

  private val _isFormValid = MutableStateFlow(false)
  val isFormValid: StateFlow<Boolean> = _isFormValid

  fun onFieldEdited(field: String) {
    lastEditedField = field
  }

  fun validate(name: String, age: String, address: String, animalTypeText: String, breedText: String) {
    val nameError = if (name.isBlank()) R.string.invalid_name else null
    val ageError = if (age.isBlank() || age.toIntOrNull() == null || (age.toIntOrNull() ?: 0) <= 0) R.string.invalid_age else null
    val addressError = if (address.isBlank() && _latitude.value == null) R.string.invalid_location else null

    val hasTypeSelection = _selectedAnimalType.value != null
    val hasTypeCustom = !_customAnimalType.value.isNullOrBlank()
    val typeError = if (!hasTypeSelection && !hasTypeCustom && animalTypeText.isBlank()) R.string.invalid_type else null

    val hasBreedSelection = _selectedBreed.value != null
    val hasBreedCustom = !_customBreed.value.isNullOrBlank()
    val typeIsSelected = hasTypeSelection || hasTypeCustom || animalTypeText.isNotBlank()
    val breedError = if (!hasBreedSelection && !hasBreedCustom && breedText.isBlank()) {
      if (!typeIsSelected) R.string.breed_requires_type else R.string.invalid_breed
    } else null

    val errors = when (lastEditedField) {
      "name" -> FieldErrors(nameError = nameError)
      "age" -> FieldErrors(ageError = ageError)
      "type" -> FieldErrors(typeError = typeError)
      "breed" -> FieldErrors(breedError = breedError)
      "address" -> FieldErrors(addressError = addressError)
      else -> FieldErrors()
    }
    _fieldErrors.value = errors

    _isFormValid.value = (nameError == null && ageError == null && typeError == null
        && breedError == null && addressError == null)
  }

  private val _saveResult = Channel<Result<String>>(Channel.CONFLATED)
  val saveResult = _saveResult.receiveAsFlow()

  fun save(
    id: Int?,
    avatar: String?,
    name: String,
    isMale: Boolean,
    age: Int,
    address: String?,
    description: String?,
    isActive: Boolean
  ) {
    var animalTypeId = _selectedAnimalType.value?.id
    var animalTypeCustom = _customAnimalType.value

    if (animalTypeId == null && animalTypeCustom.isNullOrBlank()) {
      val query = _animalTypeQuery.value.trim()
      if (query.isNotBlank()) {
        animalTypeCustom = query
      }
    }

    var breedId = _selectedBreed.value?.id
    var breedCustom = _customBreed.value

    if (breedId == null && breedCustom.isNullOrBlank()) {
      val query = _breedQuery.value.trim()
      if (query.isNotBlank()) {
        breedCustom = query
      }
    }

    if (!breedCustom.isNullOrBlank() && animalTypeId == null && animalTypeCustom.isNullOrBlank()) {
      animalTypeCustom = _animalTypeQuery.value.trim().ifBlank { "Неизвестный вид" }
    }

    viewModelScope.launch {
      val addressToSend = if (_latitude.value != null && _longitude.value != null) null else address
      val result = if (id == null) {
        val createResult = repository.createPet(
          name = name,
          animal_type = animalTypeId,
          animal_type_custom = animalTypeCustom,
          breed = breedId,
          breed_custom = breedCustom,
          is_male = isMale,
          age = age,
          address = addressToSend,
          latitude = _latitude.value,
          longitude = _longitude.value,
          has_pedigree = false,
          awards = null,
          tags = emptyList(),
          description = description,
          is_active = isActive
        )
        if (createResult is Result.Success) {
          _createdPetId.value = createResult.data
        }
        when (createResult) {
          is Result.Success -> Result.Success("Питомец добавлен")
          is Result.Error -> createResult
        }
      } else {
        repository.updatePet(
          id = id,
          name = name,
          animal_type = animalTypeId,
          animal_type_custom = animalTypeCustom,
          breed = breedId,
          breed_custom = breedCustom,
          is_male = isMale,
          age = age,
          address = addressToSend,
          latitude = _latitude.value,
          longitude = _longitude.value,
          has_pedigree = false,
          awards = null,
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

  private val _pedigreeDocumentsUri = MutableStateFlow<Uri?>(null)
  val pedigreeDocumentsUri: StateFlow<Uri?> = _pedigreeDocumentsUri

  private val _pedigreeOriginalFilename = MutableStateFlow<String?>(null)
  val pedigreeOriginalFilename: StateFlow<String?> = _pedigreeOriginalFilename

  fun setPedigreeDocumentsUri(uri: Uri?) {
    _pedigreeDocumentsUri.value = uri
  }

  fun setPedigreeOriginalFilename(filename: String?) {
    _pedigreeOriginalFilename.value = filename
  }

  fun deletePedigreeDocuments(petId: Int) {
    viewModelScope.launch {
      _pedigreeDocumentsResult.send(repository.deletePedigreeDocuments(petId))
    }
  }

  private val _pedigreeDocumentsResult = Channel<Result<String>>(Channel.CONFLATED)
  val pedigreeDocumentsResult = _pedigreeDocumentsResult.receiveAsFlow()

  fun uploadPedigreeDocuments(petId: Int, documents: okhttp3.MultipartBody.Part) {
    viewModelScope.launch {
      _pedigreeDocumentsResult.send(repository.uploadPedigreeDocuments(petId, documents))
    }
  }

  private val _petAvatarResult = Channel<Result<String>>(Channel.CONFLATED)
  val petAvatarResult = _petAvatarResult.receiveAsFlow()

  fun uploadPetAvatar(petId: Int, avatarPart: okhttp3.MultipartBody.Part) {
    viewModelScope.launch {
      _petAvatarResult.send(repository.uploadPetAvatar(petId, avatarPart))
    }
  }

  suspend fun uploadPetAvatarSuspend(petId: Int, avatarPart: okhttp3.MultipartBody.Part): Result<String> {
    return repository.uploadPetAvatar(petId, avatarPart)
  }

  suspend fun uploadPedigreeDocumentsSuspend(petId: Int, documents: okhttp3.MultipartBody.Part): Result<String> {
    return repository.uploadPedigreeDocuments(petId, documents)
  }

  private val _createdPetId = MutableStateFlow<Int?>(null)
  val createdPetId: StateFlow<Int?> = _createdPetId

  fun setCreatedPetId(id: Int) {
    _createdPetId.value = id
  }

  fun deletePet(id: Int) {
    viewModelScope.launch {
      _deleteResult.send(repository.deletePet(id))
    }
  }

  fun loadPet(petId: Int?) {
    if (petId == null) return
    viewModelScope.launch {
      _isLoadingPet = true
      try {
        val pets = repository.petsFlow.first()
        val pet = pets.firstOrNull { it.id == petId } ?: return@launch

        _pet.value = PetEditUI(
          avatar = pet.avatar,
          name = pet.name,
          address = pet.location,
          description = pet.description,
          has_pedigree = pet.has_pedigree,
          pedigree_documents = pet.pedigree_documents,
          awards = pet.awards,
          is_active = pet.is_active,
          age = pet.age,
          is_male = pet.is_male
        )

        val types = animalTypeDao.getAllFlow().first()
        types.find { it.id == pet.animal_type_id }?.let { type ->
          _selectedAnimalType.value = type
          _animalTypeQuery.value = type.name
        } ?: run {
          pet.animal_type_custom?.let { custom ->
            _customAnimalType.value = custom
            _animalTypeQuery.value = custom
          }
        }

        val breeds = breedDao.getAllFlow().first()
        breeds.find { it.id == pet.breed_id }?.let { breed ->
          _selectedBreed.value = breed
          _breedQuery.value = breed.name
        } ?: run {
          pet.breed_custom?.let { custom ->
            _customBreed.value = custom
            _breedQuery.value = custom
          }
        }

        _isMale.value = pet.is_male

        val location = pet.location
        if (!location.isNullOrBlank() && location.contains(",")) {
          val parts = location.split(",", limit = 2)
          val lat = parts[0].trim().toDoubleOrNull()
          val lon = parts[1].trim().toDoubleOrNull()
          if (lat != null && lon != null) {
            _latitude.value = lat
            _longitude.value = lon
          }
        }
      } finally {
        _isLoadingPet = false
      }
    }
  }
}
