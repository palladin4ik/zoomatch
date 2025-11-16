package com.example.zoomatch.ui.homeScreen.settings.utils

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
import com.example.zoomatch.databinding.ActivityEditPassBinding
import com.example.zoomatch.ui.homeScreen.HomeViewModelFactory
import com.example.zoomatch.ui.startScreen.afterTextChanged
import kotlinx.coroutines.launch

class EditPassActivity : AppCompatActivity() {
  private var _binding: ActivityEditPassBinding? = null
  private val binding get() = _binding!!
  private val handler = Handler(Looper.getMainLooper())
  private val debounceDelay = 300L
  private val viewModel: EditPassViewModel by viewModels {
    HomeViewModelFactory(application)
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    _binding = ActivityEditPassBinding.inflate(layoutInflater)

    setupUI()
    observeData()

    setContentView(binding.root)
  }

  private fun setupUI() {
    binding.backButton.setOnClickListener { finish() }
    binding.saveButton.setOnClickListener {
      viewModel.updatePass(
        binding.oldPassField.text.toString(),
        binding.passField.text.toString()
      )
    }
    binding.oldPassField.afterTextChanged { debounceValidate() }
    binding.passField.afterTextChanged { debounceValidate() }
    binding.passConfField.afterTextChanged { debounceValidate() }
  }

  private fun debounceValidate() {
    handler.removeCallbacksAndMessages(null)
    handler.postDelayed({
      viewModel.validate(
        oldPass = binding.oldPassField.text.toString(),
        newPass = binding.passField.text.toString(),
        confPass = binding.passConfField.text.toString()
      )
    }, debounceDelay)
  }

  private fun observeData() {
    lifecycleScope.launch {
      repeatOnLifecycle(Lifecycle.State.STARTED) {
        launch {
          viewModel.formState.collect { state ->
            binding.saveButton.isEnabled = state.isDataValid
            binding.oldPassField.error = state.oldPassError?.let { getString(it) }
            binding.passField.error = state.passError?.let { getString(it) }
            binding.passConfField.error = state.passConfError?.let { getString(it) }
          }
        }
        launch {
          viewModel.updateResult.collect { result ->
            when (result) {
              is Result.Success -> {
                Toast.makeText(this@EditPassActivity, result.data, Toast.LENGTH_SHORT).show()
                finish()
              }

              is Result.Error -> {
                Toast.makeText(this@EditPassActivity, result.message, Toast.LENGTH_LONG).show()
                Log.e("error", result.message)
              }
            }
          }
        }
      }
    }
  }
}