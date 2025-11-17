package com.example.zoomatch.ui.homeScreen.search

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.zoomatch.R
import com.example.zoomatch.databinding.HomeFragmentMatchBinding
import com.example.zoomatch.ui.homeScreen.search.Fragments.RequestFragment
import com.example.zoomatch.ui.homeScreen.search.Fragments.SearchFragment
import com.google.android.material.tabs.TabLayoutMediator

class MatchFragment : Fragment() {

  private var _binding: HomeFragmentMatchBinding? = null
  private val binding get() = _binding!!

  private var isGrid = true

  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
  ): View {
    _binding = HomeFragmentMatchBinding.inflate(inflater, container, false)
    return binding.root
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    binding.viewPager.adapter = MatchPagerAdapter(this)

    TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
      tab.text = when (position) {
        0 -> "Поиск пары"
        1 -> "Ваши заявки"
        else -> ""
      }
    }.attach()

//    updateLayoutManager()
    updateButtonIcons()

    binding.tabGroup.addOnButtonCheckedListener { _, checkedId, isChecked ->
      if (isChecked) {
        isGrid = checkedId == binding.viewGroupButton.id
        updateLayoutManager()
        updateButtonIcons()
      }
    }
    binding.root.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
      override fun onGlobalLayout() {
        binding.root.viewTreeObserver.removeOnGlobalLayoutListener(this)
        if (isGrid) {
          binding.viewGroupButton.isChecked = true
        } else {
          binding.viewListButton.isChecked = true
        }
      }
    })

//    binding.viewGroupButton.isChecked = true
  }

  private fun updateLayoutManager() {
    val spanCount = if (isGrid) 2 else 1
    (childFragmentManager.findFragmentByTag("f0") as? SearchFragment)?.updateLayoutManager(spanCount)
    (childFragmentManager.findFragmentByTag("f1") as? RequestFragment)?.updateLayoutManager(spanCount)
  }

  private fun updateButtonIcons() {
    binding.viewGroupButton.setIconResource(R.drawable.grid_svgrepo_com)
    binding.viewListButton.setIconResource(R.drawable.list_svgrepo_com)
  }
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    isGrid = savedInstanceState?.getBoolean("isGrid", true) ?: true
  }

  override fun onSaveInstanceState(outState: Bundle) {
    super.onSaveInstanceState(outState)
    outState.putBoolean("isGrid", isGrid)
  }

  override fun onDestroyView() {
    super.onDestroyView()
    _binding = null
  }
}

class MatchPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {
  override fun getItemCount() = 2
  override fun createFragment(position: Int): Fragment = when (position) {
    0 -> SearchFragment()
    1 -> RequestFragment()
    else -> SearchFragment()
  }
}