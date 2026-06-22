package com.example.zoomatch.ui.homeScreen.profile

import android.content.Intent
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
import com.bumptech.glide.Glide
import com.example.zoomatch.R
import com.example.zoomatch.databinding.HomeFragmentProfileBinding
import com.example.zoomatch.ui.homeScreen.HomeViewModelFactory
import com.example.zoomatch.ui.homeScreen.settings.SettingsActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ProfileFragment : Fragment() {
  private var _binding: HomeFragmentProfileBinding? = null
  private val binding get() = _binding!!
  private val viewModel: ProfileViewModel by viewModels {
    HomeViewModelFactory(requireActivity().application)
  }

  private val pickImageLauncher =
    registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
      uri?.let {
        lifecycleScope.launch(Dispatchers.Main) {
          binding.userAvatar.setImageURI(it)
          viewModel.uploadAvatar(it)
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
            val avatarUrl = when {
              user.avatar.isNullOrBlank() -> null
              user.avatar.startsWith("http") -> user.avatar
              else -> "https://zoomatch.ru${user.avatar}"
            }
            Glide.with(this@ProfileFragment)
              .load(avatarUrl)
              .placeholder(R.drawable.test_avatar)
              .error(R.drawable.test_avatar)
              .circleCrop()
              .into(binding.userAvatar)
          }
        }
        launch {
          viewModel.petCount.collect { count ->
            binding.petsCount.text = count.toString()
          }
        }
        launch {
          viewModel.matchCount.collect { count ->
            binding.matingsCount.text = count.toString()
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
