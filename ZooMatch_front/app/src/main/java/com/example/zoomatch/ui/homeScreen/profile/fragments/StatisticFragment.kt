package com.example.zoomatch.ui.homeScreen.profile.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.zoomatch.data.db.AppDatabase
import com.example.zoomatch.data.db.UserDao
import com.example.zoomatch.databinding.ProfileFragmentStatisticBinding
import kotlinx.coroutines.launch

class StatisticFragment : Fragment() {
  private var _binding: ProfileFragmentStatisticBinding? = null
  private val binding get() = _binding!!
  private val userDao: UserDao by lazy {
    AppDatabase.getDatabase(requireContext()).userDao()
  }

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View {
    _binding = ProfileFragmentStatisticBinding.inflate(inflater, container, false)
    observePetCount()
    return binding.root
  }

  private fun observePetCount() {
    viewLifecycleOwner.lifecycleScope.launch {
      repeatOnLifecycle(Lifecycle.State.STARTED) {
        userDao.getPetCountForCurrentUser().collect { count ->
          binding.petsCountText.text = count.toString()
        }
      }
    }
  }

  override fun onDestroyView() {
    super.onDestroyView()
    _binding = null
  }
}