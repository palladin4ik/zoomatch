package com.example.zoomatch.ui.homeScreen.settings

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.zoomatch.databinding.HomeFragmentSettingsBinding
import com.example.zoomatch.ui.startScreen.StartActivity

class SettingsFragment : Fragment() {

  private var _binding: HomeFragmentSettingsBinding? = null
  private val binding get() = _binding!!

  private val viewModel: SettingsViewModel by viewModels {
    SettingsViewModelFactory(requireActivity().application)
  }

  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
  ): View {
    _binding = HomeFragmentSettingsBinding.inflate(inflater, container, false)

    binding.logOutLayout.setOnClickListener {
      viewModel.onLogoutClicked()
    }

    viewModel.logoutEvent.observe(viewLifecycleOwner) {
      requireActivity().finish()
      startActivity(Intent(requireContext(), StartActivity::class.java))
    }

    return binding.root
  }

  override fun onDestroyView() {
    super.onDestroyView()
    _binding = null
  }
}