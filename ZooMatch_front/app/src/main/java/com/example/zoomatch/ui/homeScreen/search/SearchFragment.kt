package com.example.zoomatch.ui.homeScreen.search

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.example.zoomatch.R
import com.example.zoomatch.databinding.HomeFragmentSearchBinding
import com.example.zoomatch.ui.homeScreen.pets.PetsAdapter

class SearchFragment : Fragment() {

  private var _binding: HomeFragmentSearchBinding? = null
  private val binding get() = _binding!!

  private lateinit var adapter: PetsAdapter
  private var isGrid = true // начальное состояние — сетка

  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
  ): View {
    _binding = HomeFragmentSearchBinding.inflate(inflater, container, false)
    return binding.root
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)


    // --- 3. Начальный LayoutManager (сетка 2 колонки) ---
    updateLayoutManager()

    // --- 4. Переключение вида ---
    binding.tabGroup.addOnButtonCheckedListener { _, checkedId, isChecked ->
      if (isChecked) {
        isGrid = checkedId == binding.viewGroupButton.id
        updateLayoutManager()
        updateButtonIcons()
      }
    }

    // Установим начальное состояние кнопок
    binding.viewGroupButton.isChecked = true
    updateButtonIcons()
  }

  private fun updateLayoutManager() {
    val spanCount = if (isGrid) 2 else 1
    binding.petsRecyclerView.layoutManager = GridLayoutManager(requireContext(), spanCount)
  }

  private fun updateButtonIcons() {
    val gridIcon = if (isGrid) R.drawable.test_avatar else R.drawable.test_avatar
    val listIcon = if (!isGrid) R.drawable.test_avatar else R.drawable.test_avatar

    binding.viewGroupButton.setIconResource(gridIcon)
    binding.viewListButton.setIconResource(listIcon)
  }

  override fun onDestroyView() {
    super.onDestroyView()
    _binding = null
  }
}