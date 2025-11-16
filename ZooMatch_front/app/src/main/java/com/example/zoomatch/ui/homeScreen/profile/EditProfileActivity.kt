package com.example.zoomatch.ui.homeScreen.profile

import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.zoomatch.R.drawable
import com.example.zoomatch.data.Result
import com.example.zoomatch.data.homeScreen.profile.ImageUtils
import com.example.zoomatch.databinding.ActivityEditProfileBinding
import com.example.zoomatch.ui.homeScreen.HomeViewModelFactory
import com.example.zoomatch.ui.startScreen.afterTextChanged
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class EditProfileActivity : AppCompatActivity() {

  private var _binding: ActivityEditProfileBinding? = null
  private val binding get() = _binding!!
  private val handler = Handler(Looper.getMainLooper())
  private val debounceDelay = 400L
  private val viewModel: EditProfileViewModel by viewModels {
    HomeViewModelFactory(application)
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    _binding = ActivityEditProfileBinding.inflate(layoutInflater)
    setContentView(binding.root)

    setupUI()
    observeData()
  }

  private fun setupUI() {
    binding.backButton.setOnClickListener {
      finish()
    }
    binding.uploadImageButton.setOnClickListener {
      pickImageLauncher.launch("image/*")
    }
    binding.saveButton.setOnClickListener {
      viewModel.update(
        avatar = viewModel.avatarBase64.value,
        name = binding.nameField.text.toString(),
        location = binding.locationField.text.toString(),
        email = binding.emailField.text.toString(),
        phoneNumber = binding.phoneNumberField.text.toString()
      )
    }

    binding.nameField.afterTextChanged { debounceValidate() }
    binding.emailField.afterTextChanged { debounceValidate() }
    binding.phoneNumberField.afterTextChanged { debounceValidate() }
  }

  private fun debounceValidate() {
    handler.removeCallbacksAndMessages(null)
    handler.postDelayed({
      viewModel.validate(
        name = binding.nameField.text.toString(),
        email = binding.emailField.text.toString(),
        phone = binding.phoneNumberField.text.toString()
      )
    }, debounceDelay)
  }
  private fun observeData() {
    lifecycleScope.launch {
      repeatOnLifecycle(Lifecycle.State.STARTED) {
        launch {
          viewModel.user.collect { user ->
            ImageUtils.base64ToBitmap(user.avatar)?.let { bitmap ->
              binding.profileImage.setImageBitmap(bitmap)
            } ?: binding.profileImage.setImageResource(drawable.test_avatar)

            binding.nameField.setText(user.name)
            binding.locationField.setText(user.location)
//            binding.descriptionField.setText(user.description)

            binding.emailField.setText(user.email)
            binding.phoneNumberField.setText(user.phone_number)
          }
        }
        launch {
          viewModel.formState.collect { state ->
            binding.saveButton.isEnabled = state.isDataValid
            binding.nameField.error = state.nameError?.let { getString(it) }
            binding.emailField.error = state.emailError?.let { getString(it) }
            binding.phoneNumberField.error = state.phoneError?.let { getString(it) }
          }
        }
        launch {
          viewModel.updateResult.collect { result ->
            when (result) {
              is Result.Success -> {
                Toast.makeText(this@EditProfileActivity, result.data, Toast.LENGTH_SHORT).show()
                finish()
              }

              is Result.Error -> {
                Toast.makeText(this@EditProfileActivity, result.message, Toast.LENGTH_LONG).show()
                Log.e("error", result.message)
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
      val bitmap = contentResolver.openInputStream(uri)?.use { stream ->
        BitmapFactory.decodeStream(stream)
      } ?: return@launch

      val base64 = ImageUtils.bitmapToBase64(bitmap)

      withContext(Dispatchers.Main) {
        binding.profileImage.setImageBitmap(bitmap)
        viewModel.setAvatarBase64(base64)
      }
    }
  }
}
