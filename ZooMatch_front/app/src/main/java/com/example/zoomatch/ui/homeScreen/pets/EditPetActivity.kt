package com.example.zoomatch.ui.homeScreen.pets

import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.zoomatch.R
import com.example.zoomatch.data.Result
import com.example.zoomatch.data.homeScreen.profile.ImageUtils
import com.example.zoomatch.databinding.ActivityEditPetBinding
import com.example.zoomatch.ui.homeScreen.HomeViewModelFactory
import com.example.zoomatch.ui.startScreen.afterTextChanged
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class EditPetActivity : AppCompatActivity() {
  private var _binding: ActivityEditPetBinding? = null
  private val binding get() = _binding!!
  private val handler = Handler(Looper.getMainLooper())
  private val debounceDelay = 400L

  private val viewModel: EditPetViewModel by viewModels {
    HomeViewModelFactory(application)
  }
  private var petId: Int? = null

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    _binding = ActivityEditPetBinding.inflate(layoutInflater)
    setContentView(binding.root)

    petId = intent.getIntExtra("PET_ID", -1).takeIf { it != -1 }

    setupUI()
    observeData()
    viewModel.loadPet(petId)
  }

  private fun setupUI() {
    binding.backButton.setOnClickListener { finish() }

    binding.uploadImageButton.setOnClickListener {
      pickImageLauncher.launch("image/*")
    }

    binding.saveButton.text = if (petId != null) "Сохранить" else "Добавить"

    binding.saveButton.setOnClickListener {
      viewModel.save(
        id = petId,
        avatar = viewModel.avatarBase64.value,
        name = binding.nameField.text.toString(),
        animalTypeId = viewModel.selectedAnimalType.value?.id,
        breedId = viewModel.selectedBreed.value?.id,
        isMale = viewModel.isMale.value,
        age = binding.ageField.text.toString().toIntOrNull() ?: 0,
        location = binding.locationField.text.toString(),
        hasPedigree = binding.pedigreeCheckBox.isChecked,
//        pedigreeDocs = binding.pedigreeDocsField.text.toString().takeIf { it.isNotBlank() },
//        awards = binding.awardsField.text.toString().takeIf { it.isNotBlank() },
        pedigreeDocs = null,
        awards = null,
        description = binding.descriptionField.text.toString().takeIf { it.isNotBlank() },
        isActive = binding.activeCheckBox.isChecked
      )
    }

    binding.nameField.afterTextChanged { debounceValidate() }
    binding.ageField.afterTextChanged { debounceValidate() }

    binding.animalTypeField.setOnItemClickListener { _, _, position, _ ->
      viewModel.selectAnimalType(position)
      debounceValidate()
    }
    binding.breedField.setOnItemClickListener { _, _, position, _ ->
      viewModel.selectBreed(position)
      debounceValidate()
    }

    val genderAdapter = ArrayAdapter(
      this, android.R.layout.simple_dropdown_item_1line, listOf("Самец", "Самка")
    )
    binding.genderField.setAdapter(genderAdapter)

    binding.genderField.setOnItemClickListener { _, _, position, _ ->
      viewModel.setIsMale(position == 0)
    }

    if (petId != null) {
      binding.deleteButton.visibility = View.VISIBLE
      binding.deleteButton.setOnClickListener {
        viewModel.deletePet(petId!!)
      }
    } else {
      binding.deleteButton.visibility = View.GONE
    }
  }

  // TODO: валидация
  private fun debounceValidate() {
    handler.removeCallbacksAndMessages(null)
    handler.postDelayed({
      viewModel.validate(
        name = binding.nameField.text.toString(),
        age = binding.ageField.text.toString()
      )
    }, debounceDelay)
  }

  private fun observeData() {
    lifecycleScope.launch {
      repeatOnLifecycle(Lifecycle.State.STARTED) {
        launch {
          viewModel.pet.collect { pet ->
            pet?.let {
              ImageUtils.base64ToBitmap(it.avatar)?.let { bitmap ->
                binding.profileImage.setImageBitmap(bitmap)
              } ?: binding.profileImage.setImageResource(R.drawable.test_avatar)

              binding.nameField.setText(it.name)
              binding.locationField.setText(it.location)
              binding.descriptionField.setText(it.description)
              binding.pedigreeCheckBox.isChecked = it.has_pedigree
//              binding.pedigreeDocsField.setText(it.pedigree_documents)
//              binding.awardsField.setText(it.awards)
              binding.activeCheckBox.isChecked = it.is_active
              binding.ageField.setText(it.age.toString())
            }
          }
        }

        launch {
          combine(viewModel.animalTypes, viewModel.selectedAnimalType) { list, selected ->
            Pair(list, selected)
          }.collect { (list, selected) ->
            val adapter = ArrayAdapter(
              this@EditPetActivity,
              android.R.layout.simple_dropdown_item_1line,
              list.map { it.name }
            )
            binding.animalTypeField.setAdapter(adapter)
            if (selected != null) {
              val pos = list.indexOfFirst { it.id == selected.id }
              if (pos >= 0) binding.animalTypeField.setText(list[pos].name, false)
            } else {
              binding.animalTypeField.setText("", false)
            }
          }
        }

        launch {
          combine(viewModel.breeds, viewModel.selectedBreed) { list, selected ->
            Pair(list, selected)
          }.collect { (list, selected) ->
            val adapter = ArrayAdapter(
              this@EditPetActivity,
              android.R.layout.simple_dropdown_item_1line,
              list.map { it.name }
            )
            binding.breedField.setAdapter(adapter)
            if (selected != null) {
              val pos = list.indexOfFirst { it.id == selected.id }
              if (pos >= 0) binding.breedField.setText(list[pos].name, false)
            } else {
              binding.breedField.setText("", false)
            }
          }
        }


        launch {
          viewModel.formState.collect { state ->
            binding.saveButton.isEnabled = state.isDataValid
            binding.nameField.error = state.nameError?.let { getString(it) }
            binding.ageField.error = state.ageError?.let { getString(it) }
          }
        }

        launch {
          viewModel.saveResult.collect { result ->
            when (result) {
              is Result.Success -> {
                Toast.makeText(this@EditPetActivity, result.data, Toast.LENGTH_SHORT).show()
                Handler(Looper.getMainLooper()).postDelayed({
                  finish()
                }, 500)
              }
              is Result.Error -> {
                Toast.makeText(this@EditPetActivity, result.message, Toast.LENGTH_LONG).show()
              }
            }
          }
        }
        launch {
          viewModel.isMale.collect { isMale ->
            binding.genderField.setText(if (isMale) "Самец" else "Самка", false)
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
      }
    }
  }

  private val pickImageLauncher =
    registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
      uri?.let { processImage(it) }
    }

  private fun processImage(uri: Uri) {
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

  override fun onDestroy() {
    super.onDestroy()
    _binding = null
  }
}