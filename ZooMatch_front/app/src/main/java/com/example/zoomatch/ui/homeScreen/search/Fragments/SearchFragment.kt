package com.example.zoomatch.ui.homeScreen.search.Fragments

import android.annotation.SuppressLint
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
import androidx.recyclerview.widget.GridLayoutManager
import com.example.zoomatch.databinding.FragmentSearchBinding
import com.example.zoomatch.ui.homeScreen.HomeViewModelFactory
import com.example.zoomatch.ui.homeScreen.pets.PetsAdapter
import com.example.zoomatch.ui.homeScreen.search.MatchingActivity
import kotlinx.coroutines.launch

class SearchFragment : Fragment() {

  private var _binding: FragmentSearchBinding? = null
  private val binding get() = _binding!!

  private val viewModel: SearchViewModel by viewModels {
    HomeViewModelFactory(requireActivity().application)
  }

  private lateinit var adapter: PetsAdapter
  private lateinit var layoutManager: GridLayoutManager

  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
  ): View {
    _binding = FragmentSearchBinding.inflate(inflater, container, false)
    layoutManager = GridLayoutManager(requireContext(), 2)
    binding.petsRecyclerView.layoutManager = layoutManager

    adapter = PetsAdapter(emptyList()) { petId ->
      viewModel.onPetClick(petId.toInt())
    }
    binding.petsRecyclerView.adapter = adapter
    return binding.root
  }

  @SuppressLint("UnsafeRepeatOnLifecycleDetector")
  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    lifecycleScope.launch {
      repeatOnLifecycle(Lifecycle.State.STARTED) {
        launch {
          viewModel.activePets.collect { list ->
            adapter.update(list)
          }
        }
        launch {
          viewModel.openMatching.collect { (petId, typeId) ->
            val intent = Intent(requireContext(), MatchingActivity::class.java).apply {
              putExtra("PET_ID", petId)
              putExtra("ANIMAL_TYPE_ID", typeId)
            }
            startActivity(intent)
          }
        }
      }
    }
  }

  fun filterByType(typeName: String?) {
    viewModel.setAnimalTypeFilter(typeName)
  }

  fun filterByName(query: String) {
    viewModel.setSearchQuery(query)
  }

  override fun onDestroyView() {
    super.onDestroyView()
    _binding = null
  }
}
