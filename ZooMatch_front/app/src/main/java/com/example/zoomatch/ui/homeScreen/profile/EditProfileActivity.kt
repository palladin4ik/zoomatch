package com.example.zoomatch.ui.homeScreen.profile

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.zoomatch.data.Result
import com.example.zoomatch.databinding.ActivityEditProfileBinding
import com.example.zoomatch.ui.applySystemBarsPadding
import com.example.zoomatch.ui.homeScreen.HomeViewModelFactory
import com.example.zoomatch.ui.startScreen.afterTextChanged
import kotlinx.coroutines.launch

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
    binding.root.applySystemBarsPadding()

    setupUI()
    observeData()
  }

  private fun setupUI() {
    binding.backButton.setOnClickListener { finish() }
    binding.backButtonBottom.setOnClickListener { finish() }

    binding.saveButton.setOnClickListener {
      viewModel.update(
        firstname = binding.firstnameField.text.toString(),
        lastname = binding.lastnameField.text.toString(),
        description = binding.descriptionField.text.toString(),
        email = binding.emailField.text.toString(),
        phoneNumber = binding.phoneNumberField.text.toString()
      )
    }

    binding.firstnameField.afterTextChanged { debounceValidate() }
    binding.lastnameField.afterTextChanged { debounceValidate() }
    binding.emailField.afterTextChanged { debounceValidate() }
    binding.phoneNumberField.afterTextChanged { debounceValidate() }
  }

  private fun debounceValidate() {
    handler.removeCallbacksAndMessages(null)
    handler.postDelayed({
      viewModel.validate(
        firstname = binding.firstnameField.text.toString(),
        lastname = binding.lastnameField.text.toString(),
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
            binding.firstnameField.setText(user.firstname)
            binding.lastnameField.setText(user.lastname)
            binding.descriptionField.setText(user.description)
            binding.emailField.setText(user.email)
            binding.phoneNumberField.setText(user.phone_number)
          }
        }
        launch {
          viewModel.formState.collect { state ->
            binding.saveButton.isEnabled = state.isDataValid
            binding.firstnameField.error = state.firstnameError?.let { getString(it) }
            binding.lastnameField.error = state.lastnameError?.let { getString(it) }
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
}
