package com.example.zoomatch.ui.homeScreen.pets

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.OpenableColumns
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.zoomatch.R
import com.example.zoomatch.data.Result
import com.example.zoomatch.data.homeScreen.profile.ImageUtils
import com.example.zoomatch.databinding.ActivityEditPetBinding
import com.example.zoomatch.ui.applySystemBarsPadding
import com.example.zoomatch.ui.homeScreen.HomeViewModelFactory
import com.example.zoomatch.ui.location.LocationPickerActivity
import com.yandex.mapkit.Animation
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.map.InputListener
import com.yandex.mapkit.map.Map
import com.yandex.mapkit.map.PlacemarkMapObject
import com.yandex.runtime.image.ImageProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody


class EditPetActivity : AppCompatActivity() {
  private var _binding: ActivityEditPetBinding? = null
  private val binding get() = _binding!!
  private val handler = Handler(Looper.getMainLooper())
  private val debounceDelay = 400L
  private val uploadScope = kotlinx.coroutines.CoroutineScope(
    kotlinx.coroutines.Dispatchers.IO + kotlinx.coroutines.SupervisorJob()
  )

  private val viewModel: EditPetViewModel by viewModels {
    HomeViewModelFactory(application)
  }
  private var petId: Int? = null
  private var mapPlacemark: PlacemarkMapObject? = null
  private var _suppressTextWatchers = false
  private var _isSelectingFromResults = false

  private val mapInputListener = object : InputListener {
    override fun onMapTap(map: Map, point: Point) {
      updateMapPlacemark(point)
      viewModel.setCoordinates(point.latitude, point.longitude)
    }
    override fun onMapLongTap(map: Map, point: Point) {}
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    MapKitFactory.initialize(this)
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    _binding = ActivityEditPetBinding.inflate(layoutInflater)
    setContentView(binding.root)
    binding.root.applySystemBarsPadding()

    petId = intent.getIntExtra("PET_ID", -1).takeIf { it != -1 }

    setupUI()
    setupMap()
    observeData()
    if (petId != null) {
      _suppressTextWatchers = true
      viewModel.loadPet(petId)
      binding.root.postDelayed({ _suppressTextWatchers = false }, 300)
    }
  }

  private fun scrollToField(view: View) {
    binding.scrollView.post {
      val scrollViewLocation = IntArray(2)
      binding.scrollView.getLocationOnScreen(scrollViewLocation)
      val scrollViewY = scrollViewLocation[1]

      val viewLocation = IntArray(2)
      view.getLocationOnScreen(viewLocation)
      val viewY = viewLocation[1]

      val viewPositionInScroll = viewY - scrollViewY
      val screenHeight = binding.scrollView.height
      val targetOffset = (screenHeight * 0.25).toInt()
      val scrollTo = binding.scrollView.scrollY + viewPositionInScroll - targetOffset

      binding.scrollView.smoothScrollTo(0, scrollTo.coerceAtLeast(0))
    }
  }

  private fun sanitizeInput(text: String): String {
    val trimmed = text.trim()
    if (trimmed.isEmpty()) return trimmed
    val filtered = trimmed.replace(Regex("[^a-zA-Zа-яА-ЯёЁ\\s-]"), "")
    return filtered.replaceFirstChar { it.uppercase() }
  }

  private fun setupUI() {
    binding.backButton.setOnClickListener { finish() }

    binding.uploadImageButton.setOnClickListener {
      pickImageLauncher.launch("image/*")
    }

    binding.photoUploadArea.setOnClickListener {
      pickImageLauncher.launch("image/*")
    }

    binding.titleText.text = if (petId != null) "Редактирование питомца" else getString(R.string.new_pet)
    binding.saveButton.text = if (petId != null) "Сохранить" else getString(R.string.save_profile)

    binding.saveButton.setOnClickListener {
      val isMale = binding.genderToggleGroup.checkedButtonId == R.id.maleButton

      viewModel.save(
        id = petId,
        avatar = viewModel.avatarBase64.value,
        name = sanitizeInput(binding.nameField.text.toString()),
        isMale = isMale,
        age = binding.ageField.text.toString().toIntOrNull() ?: 0,
        address = binding.addressField.text.toString(),
        description = binding.descriptionField.text.toString().takeIf { it.isNotBlank() },
        isActive = binding.activeCheckBox.isChecked,
        hasPedigree = binding.pedigreeCheckBox.isChecked
      )
    }

    binding.nameField.setOnFocusChangeListener { _, hasFocus ->
      if (hasFocus) scrollToField(binding.nameField)
    }
    binding.nameField.afterTextChanged {
      viewModel.onFieldEdited("name")
      debounceValidate()
    }

    binding.ageField.setOnFocusChangeListener { _, hasFocus ->
      if (hasFocus) scrollToField(binding.ageField)
    }
    binding.ageField.afterTextChanged {
      viewModel.onFieldEdited("age")
      debounceValidate()
    }

    binding.animalTypeField.setOnFocusChangeListener { _, hasFocus ->
      if (hasFocus) scrollToField(binding.animalTypeField)
    }
    binding.animalTypeField.addTextChangedListener(object : android.text.TextWatcher {
      override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
      override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
      override fun afterTextChanged(s: android.text.Editable?) {
        if (_suppressTextWatchers || _isSelectingFromResults) return
        val query = s?.toString() ?: ""
        val hasInput = query.isNotBlank()
        viewModel.setAnimalTypeInputActive(hasInput)
        viewModel.onAnimalTypeQueryChanged(query)
        viewModel.onFieldEdited("type")
        debounceValidate()
        if (hasInput) scrollToField(binding.animalTypeField)
      }
    })

    binding.breedField.setOnFocusChangeListener { _, hasFocus ->
      if (hasFocus) scrollToField(binding.breedField)
    }
    binding.breedField.addTextChangedListener(object : android.text.TextWatcher {
      override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
      override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
      override fun afterTextChanged(s: android.text.Editable?) {
        if (_suppressTextWatchers || _isSelectingFromResults) return
        val query = s?.toString() ?: ""
        viewModel.onBreedQueryChanged(query)
        viewModel.onFieldEdited("breed")
        debounceValidate()
        if (query.isNotBlank()) scrollToField(binding.breedField)
      }
    })

    binding.addressField.setOnFocusChangeListener { _, hasFocus ->
      if (hasFocus) scrollToField(binding.addressField)
    }
    binding.addressField.afterTextChanged {
      viewModel.onFieldEdited("address")
      viewModel.setAddressText(it)
      debounceValidate()
    }

    binding.openMapButton.setOnClickListener {
      val intent = android.content.Intent(this, LocationPickerActivity::class.java)
      val lat = viewModel.latitude.value
      val lon = viewModel.longitude.value
      if (lat != null && lon != null) {
        intent.putExtra("LATITUDE", lat)
        intent.putExtra("LONGITUDE", lon)
      }
      locationPickerLauncher.launch(intent)
    }

    binding.genderToggleGroup.check(R.id.maleButton)
    viewModel.setIsMale(true)

    binding.genderToggleGroup.addOnButtonCheckedListener { _, checkedId, isChecked ->
      if (isChecked) {
        when (checkedId) {
          R.id.maleButton -> viewModel.setIsMale(true)
          R.id.femaleButton -> viewModel.setIsMale(false)
        }
      }
    }

    if (petId != null) {
      binding.deleteButton.visibility = View.VISIBLE
      binding.deleteButton.setOnClickListener {
        viewModel.deletePet(petId!!)
      }
    } else {
      binding.deleteButton.visibility = View.GONE
    }

    binding.pedigreeUploadArea.setOnClickListener {
      pickPedigreeLauncher.launch("application/pdf")
    }
  }

  private fun setupMap() {
    binding.mapPreview.map.addInputListener(mapInputListener)
    val defaultPoint = Point(55.7558, 37.6173)
    binding.mapPreview.map.move(
      CameraPosition(defaultPoint, 14f, 0f, 0f),
      Animation(Animation.Type.SMOOTH, 0f),
      null
    )
  }

  private fun vectorToBitmap(drawableRes: Int): Bitmap {
    val drawable = AppCompatResources.getDrawable(this, drawableRes)!!
    val size = 80
    val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    drawable.setBounds(0, 0, size, size)
    drawable.draw(canvas)
    return bitmap
  }

  private fun updateMapPlacemark(point: Point) {
    try {
      mapPlacemark?.let { binding.mapPreview.map.mapObjects.remove(it) }
      val bitmap = vectorToBitmap(R.drawable.ic_location_pin)
      mapPlacemark = binding.mapPreview.map.mapObjects.addPlacemark(
        point,
        ImageProvider.fromBitmap(bitmap)
      )
    } catch (_: Exception) {
      binding.mapPreview.postDelayed({
        try {
          mapPlacemark?.let { binding.mapPreview.map.mapObjects.remove(it) }
          val bitmap = vectorToBitmap(R.drawable.ic_location_pin)
          mapPlacemark = binding.mapPreview.map.mapObjects.addPlacemark(
            point,
            ImageProvider.fromBitmap(bitmap)
          )
        } catch (_: Exception) {}
      }, 500)
    }
  }

  private fun setBreedEnabled(enabled: Boolean) {
    binding.breedLayout.isEnabled = enabled
    binding.breedField.isEnabled = enabled
    binding.breedLabel.isEnabled = enabled
    if (enabled) {
      binding.breedLayout.helperText = "Введите название — если порода новая, питомец уйдёт на модерацию"
    } else {
      binding.breedLayout.helperText = "Сначала выберите вид животного"
      binding.breedField.setText("")
      binding.breedResultsList.visibility = View.GONE
    }
  }

  private fun reverseGeocodeAndSetAddress(lat: Double, lon: Double) {
    lifecycleScope.launch(Dispatchers.IO) {
      try {
        val geocoder = android.location.Geocoder(this@EditPetActivity, java.util.Locale("ru", "RU"))
        @Suppress("DEPRECATION")
        val addresses = geocoder.getFromLocation(lat, lon, 1)
        val addressText = if (!addresses.isNullOrEmpty()) {
          val addr = addresses[0]
          val parts = mutableListOf<String>()
          addr.thoroughfare?.let { parts.add(it) }
          addr.subThoroughfare?.let { parts.add(it) }
          addr.locality?.let { if (parts.isEmpty()) parts.add(it) }
          parts.joinToString(", ").ifBlank { "$lat, $lon" }
        } else {
          "$lat, $lon"
        }
        withContext(Dispatchers.Main) {
          if (binding.addressField.text.isNullOrBlank()) {
            binding.addressField.setText(addressText)
          }
        }
      } catch (_: Exception) {
        withContext(Dispatchers.Main) {
          if (binding.addressField.text.isNullOrBlank()) {
            binding.addressField.setText("$lat, $lon")
          }
        }
      }
    }
  }

  private fun debounceValidate() {
    handler.removeCallbacksAndMessages(null)
    handler.postDelayed({
      viewModel.validate(
        name = binding.nameField.text.toString(),
        age = binding.ageField.text.toString(),
        address = binding.addressField.text.toString(),
        animalTypeText = binding.animalTypeField.text.toString(),
        breedText = binding.breedField.text.toString()
      )
    }, debounceDelay)
  }

  private fun observeData() {
    lifecycleScope.launch {
      repeatOnLifecycle(Lifecycle.State.STARTED) {
        launch {
          viewModel.pet.collect { pet ->
            pet?.let {
              val avatarUrl = it.avatar
              if (!avatarUrl.isNullOrBlank()) {
                val fullUrl = if (avatarUrl.startsWith("http")) avatarUrl
                else "https://zoomatch.ru$avatarUrl"
                com.bumptech.glide.Glide.with(this@EditPetActivity)
                  .load(fullUrl)
                  .placeholder(R.drawable.ic_paw_print_light)
                  .error(R.drawable.ic_paw_print_light)
                  .centerCrop()
                  .into(binding.profileImage)
              } else {
                binding.profileImage.setImageResource(R.drawable.ic_paw_print_light)
              }

              binding.nameField.setText(it.name)
              binding.descriptionField.setText(it.description)
              binding.activeCheckBox.isChecked = it.is_active
              binding.ageField.setText(it.age.toString())

              if (!it.address.isNullOrBlank()) {
                if (it.address.contains(",")) {
                  val parts = it.address.split(",", limit = 2)
                  val lat = parts[0].trim().toDoubleOrNull()
                  val lon = parts[1].trim().toDoubleOrNull()
                  if (lat != null && lon != null) {
                    reverseGeocodeAndSetAddress(lat, lon)
                  } else {
                    binding.addressField.setText(it.address)
                  }
                } else {
                  binding.addressField.setText(it.address)
                }
              }

              if (it.is_male) {
                binding.genderToggleGroup.check(R.id.maleButton)
              } else {
                binding.genderToggleGroup.check(R.id.femaleButton)
              }

              val docsUrl = it.pedigree_documents
              if (!docsUrl.isNullOrBlank()) {
                val display = viewModel.pedigreeOriginalFilename.value
                  ?: "Документ загружен"
                binding.pedigreeFileName.text = display
                binding.pedigreeFileName.setTextColor(getColor(R.color.black))
              }
            }
          }
        }

        launch {
          combine(viewModel.latitude, viewModel.longitude) { lat, lon ->
            if (lat != null && lon != null) lat to lon else null
          }.collect { coords ->
            if (coords != null) {
              reverseGeocodeAndSetAddress(coords.first, coords.second)
            }
          }
        }

        launch {
          viewModel.selectedAnimalType.collect { type ->
            if (type != null) {
              val currentText = binding.animalTypeField.text?.toString() ?: ""
              if (!currentText.equals(type.name, ignoreCase = true)) {
                binding.animalTypeField.setText(type.name)
              }
            }
          }
        }

        launch {
          viewModel.selectedBreed.collect { breed ->
            if (breed != null) {
              val currentText = binding.breedField.text?.toString() ?: ""
              if (!currentText.equals(breed.name, ignoreCase = true)) {
                binding.breedField.setText(breed.name)
              }
            }
          }
        }

        launch {
          viewModel.customAnimalType.collect { custom ->
            if (custom != null) {
              val currentText = binding.animalTypeField.text?.toString() ?: ""
              if (!currentText.equals(custom, ignoreCase = true)) {
                binding.animalTypeField.setText(custom)
              }
            }
          }
        }

        launch {
          viewModel.customBreed.collect { custom ->
            if (custom != null) {
              val currentText = binding.breedField.text?.toString() ?: ""
              if (!currentText.equals(custom, ignoreCase = true)) {
                binding.breedField.setText(custom)
              }
            }
          }
        }

        launch {
          combine(
            viewModel.selectedAnimalType,
            viewModel.animalTypeInputActive,
            viewModel.customAnimalType
          ) { selected, hasInput, custom ->
            selected != null || hasInput || !custom.isNullOrBlank()
          }.collect { shouldEnable ->
            setBreedEnabled(shouldEnable)
          }
        }

        launch {
          combine(
            viewModel.animalTypeSearchResults,
            viewModel.animalTypeQuery,
            viewModel.selectedAnimalType
          ) { results, query, selected ->
            Triple(results, query, selected)
          }.collect { (results, query, selected) ->
            if (selected != null && selected.name.equals(query, ignoreCase = true)) {
              binding.animalTypeResultsList.visibility = View.GONE
              return@collect
            }
            if (results.isEmpty() || query.isBlank()) {
              binding.animalTypeResultsList.visibility = View.GONE
              return@collect
            }

            val items = mutableListOf<String>()
            val exactMatch = results.find { it.name.equals(query.trim(), ignoreCase = true) }
            if (exactMatch != null) {
              items.add(exactMatch.name)
            }
            results.filter { it.id != exactMatch?.id }.forEach { items.add(it.name) }
            items.add("«${query.trim()}» — свой вариант (модерация)")

            val adapter = ArrayAdapter(
              this@EditPetActivity,
              android.R.layout.simple_list_item_1,
              items
            )
            binding.animalTypeResultsList.adapter = adapter
            binding.animalTypeResultsList.visibility = View.VISIBLE
            binding.animalTypeResultsList.setOnItemClickListener { _, _, position, _ ->
              val clicked = items[position]
              _isSelectingFromResults = true
              try {
                if (position == items.lastIndex) {
                  viewModel.setCustomAnimalType(query.trim())
                  binding.animalTypeField.setText(query.trim())
                } else {
                  val match = results.find { it.name == clicked }
                  if (match != null) {
                    viewModel.selectAnimalType(match)
                    binding.animalTypeField.setText(match.name)
                  }
                }
                binding.animalTypeResultsList.visibility = View.GONE
              } finally {
                _isSelectingFromResults = false
              }
            }
          }
        }

        launch {
          combine(
            viewModel.breedSearchResults,
            viewModel.breedQuery,
            viewModel.selectedBreed
          ) { results, query, selected ->
            Triple(results, query, selected)
          }.collect { (results, query, selected) ->
            if (selected != null && selected.name.equals(query, ignoreCase = true)) {
              binding.breedResultsList.visibility = View.GONE
              return@collect
            }
            if (results.isEmpty() || query.isBlank()) {
              binding.breedResultsList.visibility = View.GONE
              return@collect
            }

            val items = mutableListOf<String>()
            val exactMatch = results.find { it.name.equals(query.trim(), ignoreCase = true) }
            if (exactMatch != null) {
              items.add(exactMatch.name)
            }
            results.filter { it.id != exactMatch?.id }.forEach { items.add(it.name) }
            items.add("«${query.trim()}» — свой вариант (модерация)")

            val adapter = ArrayAdapter(
              this@EditPetActivity,
              android.R.layout.simple_list_item_1,
              items
            )
            binding.breedResultsList.adapter = adapter
            binding.breedResultsList.visibility = View.VISIBLE
            binding.breedResultsList.setOnItemClickListener { _, _, position, _ ->
              val clicked = items[position]
              _isSelectingFromResults = true
              try {
                if (position == items.lastIndex) {
                  viewModel.setCustomBreed(query.trim())
                  binding.breedField.setText(query.trim())
                } else {
                  val match = results.find { it.name == clicked }
                  if (match != null) {
                    viewModel.selectBreed(match)
                    binding.breedField.setText(match.name)
                  }
                }
                binding.breedResultsList.visibility = View.GONE
              } finally {
                _isSelectingFromResults = false
              }
            }
          }
        }

        launch {
          viewModel.fieldErrors.collect { errors ->
            binding.nameField.error = errors.nameError?.let { getString(it) }
            binding.ageField.error = errors.ageError?.let { getString(it) }
            binding.animalTypeLayout.error = errors.typeError?.let { getString(it) }
            binding.breedLayout.error = errors.breedError?.let { getString(it) }
            binding.addressLayout.error = errors.addressError?.let { getString(it) }
          }
        }

        launch {
          viewModel.isFormValid.collect { isValid ->
            binding.saveButton.isEnabled = isValid
          }
        }

        launch {
          viewModel.saveResult.collect { result ->
            when (result) {
              is Result.Success -> {
                Toast.makeText(this@EditPetActivity, result.data, Toast.LENGTH_SHORT).show()
                val pendingUri = viewModel.pedigreeDocumentsUri.value
                val newPetId = viewModel.createdPetId.value
                if (pendingUri != null && newPetId != null) {
                  uploadPedigreeNow(newPetId, pendingUri)
                  viewModel.setPedigreeDocumentsUri(null)
                }
                val avatarUri = viewModel.avatarUri.value
                if (avatarUri != null && newPetId != null) {
                  uploadAvatarNow(newPetId, avatarUri)
                  viewModel.setAvatarUri(null)
                }
                Handler(Looper.getMainLooper()).postDelayed({ finish() }, 500)
              }
              is Result.Error -> {
                Toast.makeText(this@EditPetActivity, result.message, Toast.LENGTH_LONG).show()
              }
            }
          }
        }

        launch {
          viewModel.isMale.collect { isMale ->
            if (isMale) {
              binding.genderToggleGroup.check(R.id.maleButton)
            } else {
              binding.genderToggleGroup.check(R.id.femaleButton)
            }
          }
        }

        launch {
          viewModel.hasPedigree.collect { hasPedigree ->
            binding.pedigreeCheckBox.isChecked = hasPedigree
          }
        }

        launch {
          viewModel.deleteResult.collect { result ->
            when (result) {
              is Result.Success -> {
                Toast.makeText(this@EditPetActivity, "Питомец удалён", Toast.LENGTH_SHORT).show()
                finish()
              }
              is Result.Error -> {
                Toast.makeText(this@EditPetActivity, result.message, Toast.LENGTH_LONG).show()
              }
            }
          }
        }

        launch {
          viewModel.customAnimalType.collect { custom ->
            if (custom != null) {
              Toast.makeText(
                this@EditPetActivity,
                "Вид «$custom» будет отправлен на модерацию",
                Toast.LENGTH_SHORT
              ).show()
            }
          }
        }

        launch {
          viewModel.customBreed.collect { custom ->
            if (custom != null) {
              Toast.makeText(
                this@EditPetActivity,
                "Порода «$custom» будет отправлена на модерацию",
                Toast.LENGTH_SHORT
              ).show()
            }
          }
        }

        launch {
          viewModel.pedigreeDocumentsResult.collect { result ->
            when (result) {
              is Result.Success -> Toast.makeText(
                this@EditPetActivity, result.data, Toast.LENGTH_SHORT
              ).show()
              is Result.Error -> Toast.makeText(
                this@EditPetActivity, result.message, Toast.LENGTH_LONG
              ).show()
            }
          }
        }

        launch {
          viewModel.latitude.collect { lat ->
            val lon = viewModel.longitude.value
            if (lat != null && lon != null) {
              val point = Point(lat, lon)
              updateMapPlacemark(point)
              binding.mapPreview.map.move(
                CameraPosition(point, 14f, 0f, 0f),
                Animation(Animation.Type.SMOOTH, 0.3f),
                null
              )
            }
          }
        }
      }
    }
  }


  private val pickImageLauncher =
    registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
      uri?.let { processImage(it) }
    }

  private val locationPickerLauncher =
    registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
      if (result.resultCode == RESULT_OK) {
        val lat = result.data?.getDoubleExtra("LATITUDE", 0.0) ?: 0.0
        val lon = result.data?.getDoubleExtra("LONGITUDE", 0.0) ?: 0.0
        val address = result.data?.getStringExtra("ADDRESS") ?: ""
        viewModel.setCoordinates(lat, lon)
        if (address.isNotBlank()) {
          binding.addressField.setText(address)
        }
      }
    }

  private val pickPedigreeLauncher =
    registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
      uri?.let { processPedigreeDocument(it) }
    }

  private fun processPedigreeDocument(uri: Uri) {
    val fileName = getFileName(uri)
    binding.pedigreeFileName.text = fileName ?: "Документ загружен"
    binding.pedigreeFileName.setTextColor(getColor(R.color.black))

    val currentPetId = petId
    if (currentPetId != null) {
      uploadPedigreeNow(currentPetId, uri, fileName)
    } else {
      viewModel.setPedigreeDocumentsUri(uri)
      viewModel.setPedigreeOriginalFilename(fileName)
      Toast.makeText(this, "Документ загрузится после сохранения питомца", Toast.LENGTH_SHORT).show()
    }
  }

  private fun uploadPedigreeNow(petId: Int, uri: Uri, fileName: String? = null) {
    val file = java.io.File(cacheDir, "pedigree_${petId}_${fileName ?: "doc"}.pdf")
    try {
      val opened = contentResolver.openInputStream(uri)?.use { input ->
        file.outputStream().use { output -> input.copyTo(output) }
      }
      if (opened == null) {
        handler.post { Toast.makeText(this, "Не удалось открыть файл документа", Toast.LENGTH_SHORT).show() }
        return
      }
    } catch (e: Exception) {
      handler.post { Toast.makeText(this, "Ошибка копирования документа: ${e.message}", Toast.LENGTH_SHORT).show() }
      return
    }

    val requestBody = file.asRequestBody("application/pdf".toMediaTypeOrNull())
    val part = MultipartBody.Part.createFormData("pedigree_documents", file.name, requestBody)
    uploadScope.launch {
      val result = viewModel.uploadPedigreeDocumentsSuspend(petId, part)
      if (result is Result.Error) {
        handler.post { Toast.makeText(this@EditPetActivity, "Ошибка загрузки документа: ${result.message}", Toast.LENGTH_LONG).show() }
      }
    }
  }

  private fun uploadAvatarNow(petId: Int, uri: Uri) {
    val file = java.io.File(cacheDir, "avatar_pet_${petId}.jpg")
    try {
      val opened = contentResolver.openInputStream(uri)?.use { input ->
        file.outputStream().use { output -> input.copyTo(output) }
      }
      if (opened == null) {
        handler.post { Toast.makeText(this, "Не удалось открыть файл аватара", Toast.LENGTH_SHORT).show() }
        return
      }
    } catch (e: Exception) {
      handler.post { Toast.makeText(this, "Ошибка копирования аватара: ${e.message}", Toast.LENGTH_SHORT).show() }
      return
    }

    val requestBody = file.asRequestBody("image/jpeg".toMediaTypeOrNull())
    val part = MultipartBody.Part.createFormData("avatar", file.name, requestBody)
    uploadScope.launch {
      val result = viewModel.uploadPetAvatarSuspend(petId, part)
      if (result is Result.Error) {
        handler.post { Toast.makeText(this@EditPetActivity, "Ошибка загрузки аватара: ${result.message}", Toast.LENGTH_LONG).show() }
      }
    }
  }

  private fun getFileName(uri: Uri): String? {
    if (uri.scheme == "content") {
      contentResolver.query(uri, null, null, null, null)?.use { cursor ->
        if (cursor.moveToFirst()) {
          val idx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
          if (idx >= 0) return cursor.getString(idx)
        }
      }
    }
    return uri.path?.substringAfterLast('/')
  }

  private fun processImage(uri: Uri) {
    viewModel.setAvatarUri(uri)
    lifecycleScope.launch(Dispatchers.IO) {
      val bitmap = contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it) }
        ?: return@launch
      val base64 = ImageUtils.bitmapToBase64(bitmap)
      withContext(Dispatchers.Main) {
        binding.profileImage.setImageBitmap(bitmap)
        viewModel.setAvatarBase64(base64)
      }
    }
  }

  override fun onStart() {
    super.onStart()
    MapKitFactory.getInstance().onStart()
    binding.mapPreview.onStart()
  }

  override fun onStop() {
    binding.mapPreview.onStop()
    MapKitFactory.getInstance().onStop()
    super.onStop()
  }

  override fun onDestroy() {
    super.onDestroy()
    _binding = null
  }
}

private fun android.widget.EditText.afterTextChanged(afterTextChanged: (String) -> Unit) {
  addTextChangedListener(object : android.text.TextWatcher {
    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
    override fun afterTextChanged(editable: android.text.Editable?) {
      afterTextChanged(editable?.toString() ?: "")
    }
  })
}
