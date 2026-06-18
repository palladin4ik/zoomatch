package com.example.zoomatch.ui.homeScreen.profile

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.zoomatch.R
import com.example.zoomatch.data.homeScreen.profile.ImageUtils
import com.example.zoomatch.databinding.HomeFragmentProfileBinding
import com.example.zoomatch.ui.homeScreen.HomeViewModelFactory
import com.example.zoomatch.ui.homeScreen.settings.SettingsActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ProfileFragment : Fragment() {
  private var _binding: HomeFragmentProfileBinding? = null
  private val binding get() = _binding!!
  private val viewModel: ProfileViewModel by viewModels {
    HomeViewModelFactory(requireActivity().application)
  }

  private val pickImageLauncher =
    registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
      uri?.let {
        lifecycleScope.launch(Dispatchers.IO) {
          val bitmap = requireContext().contentResolver.openInputStream(it)?.use { stream ->
            BitmapFactory.decodeStream(stream)
          } ?: return@launch
          val base64 = ImageUtils.bitmapToBase64(bitmap)
          withContext(Dispatchers.Main) {
            binding.userAvatar.setImageBitmap(bitmap)
            viewModel.uploadAvatar(base64)
          }
        }
      }
    }

  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
  ): View {
    _binding = HomeFragmentProfileBinding.inflate(inflater, container, false)
    setupUI()
    observeData()
    return binding.root
  }

  private fun setupUI() {
    binding.editAvatarButton.setOnClickListener {
      pickImageLauncher.launch("image/*")
    }

    binding.editNameButton.setOnClickListener {
      startActivity(Intent(requireContext(), EditProfileActivity::class.java))
    }

    binding.settingsIcon.setOnClickListener {
      startActivity(Intent(requireContext(), SettingsActivity::class.java))
    }
  }

  private fun observeData() {
    lifecycleScope.launch {
      repeatOnLifecycle(Lifecycle.State.STARTED) {
        launch {
          viewModel.user.collect { user ->
            val displayName = listOfNotNull(
              user.firstname.ifBlank { null },
              user.lastname.ifBlank { null }
            ).joinToString(" ").ifBlank { "Имя не указано" }
            binding.userName.text = displayName
            binding.userBio.text = user.description
            ImageUtils.base64ToBitmap(user.avatar)?.let { bitmap ->
              binding.userAvatar.setImageBitmap(bitmap)
            } ?: binding.userAvatar.setImageResource(R.drawable.test_avatar)
          }
        }
        launch {
          viewModel.petCount.collect { count ->
            binding.petsCount.text = count.toString()
          }
        }
        launch {
          viewModel.openEditProfile.collect {
            startActivity(Intent(requireContext(), EditProfileActivity::class.java))
          }
        }
      }
    }
  }

  override fun onDestroyView() {
    super.onDestroyView()
    _binding = null
  }
}
