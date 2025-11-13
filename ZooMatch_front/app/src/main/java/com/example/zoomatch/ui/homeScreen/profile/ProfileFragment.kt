package com.example.zoomatch.ui.homeScreen.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.zoomatch.R
import com.example.zoomatch.databinding.HomeFragmentProfileBinding
import com.example.zoomatch.ui.homeScreen.profile.fragments.ReviewsFragment
import com.example.zoomatch.ui.homeScreen.profile.fragments.StatisticFragment
import com.google.android.material.tabs.TabLayoutMediator

class ProfileFragment : Fragment() {
  private var _binding: HomeFragmentProfileBinding? = null

  // This property is only valid between onCreateView and
  // onDestroyView.
  private val binding get() = _binding!!

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View {
    val profileViewModel = ViewModelProvider(this)[ProfileViewModel::class.java]
    _binding = HomeFragmentProfileBinding.inflate(inflater, container, false)
    val root: View = binding.root
    binding.userAvatar.setImageResource(R.drawable.test_avatar)
    return root
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    val pager = binding.profileFragments
    val tabs = binding.profileSummary

    pager.adapter = ProfilePagerAdapter(this)

    TabLayoutMediator(tabs, pager) { tab, position ->
      tab.text = when (position) {
        0 -> "Statistic"
        1 -> "Reviews"
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