package com.example.zoomatch.ui.homeScreen.search

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.zoomatch.data.db.AppDatabase
import com.example.zoomatch.databinding.HomeFragmentMatchBinding
import com.example.zoomatch.ui.homeScreen.search.Fragments.RequestFragment
import com.example.zoomatch.ui.homeScreen.search.Fragments.SearchFragment
import com.example.zoomatch.ui.homeScreen.settings.SettingsActivity
import com.google.android.material.chip.Chip
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

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

    binding.settingsIcon.setOnClickListener {
      startActivity(Intent(requireContext(), SettingsActivity::class.java))
    }

    binding.viewPager.adapter = MatchPagerAdapter(this)

    TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
      tab.text = when (position) {
        0 -> "Поиск пары"
        1 -> "Ваши заявки"
        else -> ""
      }
    }.attach()

    setupFilterChips()
    setupSearchInput()
  }

  private fun setupFilterChips() {
    viewLifecycleOwner.lifecycleScope.launch {
      val db = AppDatabase.getDatabase(requireActivity().application)

      val pets = db.petDao().getPetsFlow().first()
      val distinctTypeIds = pets.mapNotNull { it.animal_type_id }.distinct()

      if (distinctTypeIds.size <= 1) {
        binding.filterScrollView.visibility = View.GONE
        return@launch
      }

      val animalTypes = db.animalTypeDao().getAllFlow().first()
      val typeNameMap = animalTypes.associate { it.id to it.name }

      binding.filterChipGroup.removeAllViews()

      val allChip = createFilterChip("Все", isChecked = true)
      allChip.tag = null
      binding.filterChipGroup.addView(allChip)

      for (typeId in distinctTypeIds) {
        val typeName = typeNameMap[typeId] ?: continue
        val chip = createFilterChip(typeName, isChecked = false)
        chip.tag = typeName
        binding.filterChipGroup.addView(chip)
      }

      binding.filterScrollView.visibility = View.VISIBLE

      binding.filterChipGroup.setOnCheckedStateChangeListener { _, checkedIds ->
        val chipId = checkedIds.firstOrNull()
        val chip = chipId?.let { binding.filterChipGroup.findViewById<Chip>(it) }
        val typeName = chip?.tag as? String
        applyTypeFilter(typeName)
      }
    }
  }

  private fun createFilterChip(text: String, isChecked: Boolean): Chip {
    return Chip(requireContext()).apply {
      this.text = text
      isCheckable = true
      this.isChecked = isChecked
      setTextColor(Color.parseColor("#1F2937"))
      chipBackgroundColor = android.content.res.ColorStateList.valueOf(Color.WHITE)
      val states = arrayOf(intArrayOf(android.R.attr.state_checked), intArrayOf(-android.R.attr.state_checked))
      val colors = intArrayOf(Color.parseColor("#7C3AED"), Color.parseColor("#D1D5DB"))
      chipStrokeColor = android.content.res.ColorStateList(states, colors)
      chipStrokeWidth = resources.displayMetrics.density * 1
      id = View.generateViewId()
    }
  }

  private fun setupSearchInput() {
    binding.searchInput.addTextChangedListener(object : TextWatcher {
      override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
      override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
      override fun afterTextChanged(s: Editable?) {
        applySearchQuery(s?.toString() ?: "")
      }
    })
  }

  private fun applyTypeFilter(typeName: String?) {
    val searchFragment = childFragmentManager.findFragmentByTag("f0") as? SearchFragment
    searchFragment?.filterByType(typeName)
  }

  private fun applySearchQuery(query: String) {
    val searchFragment = childFragmentManager.findFragmentByTag("f0") as? SearchFragment
    searchFragment?.filterByName(query)
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
