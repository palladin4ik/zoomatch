package com.example.zoomatch.ui.homeScreen.profile

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.zoomatch.R
import com.example.zoomatch.data.homeScreen.profile.ImageUtils
import com.example.zoomatch.databinding.HomeFragmentProfileBinding
import com.example.zoomatch.ui.homeScreen.HomeViewModelFactory
import com.example.zoomatch.ui.homeScreen.profile.fragments.ReviewsFragment
import com.example.zoomatch.ui.homeScreen.profile.fragments.StatisticFragment
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.coroutines.launch

class ProfileFragment : Fragment() {
  private var _binding: HomeFragmentProfileBinding? = null
  private val binding get() = _binding!!
  private val viewModel: ProfileViewModel by viewModels {
    HomeViewModelFactory(requireActivity().application)
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
    binding.editProfileButton.setOnClickListener {
      viewModel.onEditProfileClick()
    }
    binding.userAvatar.setImageResource(R.drawable.test_avatar) // временное решение
  }

  private fun observeData() {
    lifecycleScope.launch {
      repeatOnLifecycle(Lifecycle.State.STARTED) {
        launch {
          viewModel.user.collect { user ->
            binding.userName.text = user.name
            binding.userGeo.text = user.geo
            binding.userBio.text = user.status
            ImageUtils.base64ToBitmap(user.avatar)?.let { bitmap ->
              binding.userAvatar.setImageBitmap(bitmap)
            } ?: binding.userAvatar.setImageResource(R.drawable.test_avatar)
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

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    val pager = binding.profileFragments
    val tabs = binding.profileSummary

    pager.adapter = ProfilePagerAdapter(this)

    TabLayoutMediator(tabs, pager) { tab, position ->
      tab.text = when (position) {
        0 -> "Статистика"
        1 -> "Отзывы"
        else -> ""
      }
    }.attach()
  }

  override fun onDestroyView() {
    super.onDestroyView()
    _binding = null
  }
}

class ProfilePagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {
  override fun getItemCount() = 2

  override fun createFragment(position: Int): Fragment {
    return when (position) {
      0 -> StatisticFragment()
      1 -> ReviewsFragment()
      else -> StatisticFragment()
    }
  }
}