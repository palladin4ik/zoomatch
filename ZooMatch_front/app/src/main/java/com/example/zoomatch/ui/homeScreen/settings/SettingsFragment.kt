package com.example.zoomatch.ui.homeScreen.settings

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
import com.example.zoomatch.databinding.HomeFragmentSettingsBinding
import com.example.zoomatch.ui.LayoutWip
import com.example.zoomatch.ui.homeScreen.HomeViewModelFactory
import com.example.zoomatch.ui.homeScreen.settings.utils.EditPassActivity
import com.example.zoomatch.ui.startScreen.StartActivity
import kotlinx.coroutines.launch

class SettingsFragment : Fragment() {

  private var _binding: HomeFragmentSettingsBinding? = null
  private val binding get() = _binding!!

  private val viewModel: SettingsViewModel by viewModels {
    HomeViewModelFactory(requireActivity().application)
  }

  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
  ): View {
    _binding = HomeFragmentSettingsBinding.inflate(inflater, container, false)

    setupUI()
    observeData()

    return binding.root
  }

  private fun setupUI() {
    binding.logOutLayout.setOnClickListener { viewModel.logoutClick() }
    binding.privacyPolicyLayout.setOnClickListener { openWip() }
    binding.termsOfUseLayout.setOnClickListener { openWip() }
    binding.approveProfileLayout.setOnClickListener { openWip() }
    binding.editPasswordLayout.setOnClickListener { editPass() }
  }

  private fun observeData() {
    viewLifecycleOwner.lifecycleScope.launch {
      viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
        launch {
          viewModel.logout.collect {
            requireActivity().finish()
            startActivity(Intent(requireContext(), StartActivity::class.java))
          }
        }
      }
    }
  }

  private fun openWip() {
    startActivity(Intent(requireContext(), LayoutWip::class.java))
  }

  private fun editPass(){
    startActivity(Intent(requireContext(), EditPassActivity::class.java))
  }


  override fun onDestroyView() {
    super.onDestroyView()
    _binding = null
  }
}