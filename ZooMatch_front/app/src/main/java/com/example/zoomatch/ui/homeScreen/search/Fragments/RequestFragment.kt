package com.example.zoomatch.ui.homeScreen.search.Fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.zoomatch.R
import com.example.zoomatch.data.homeScreen.profile.ImageUtils
import com.example.zoomatch.data.homeScreen.search.PetLongResponse
import com.example.zoomatch.databinding.FragmentRequestBinding
import com.example.zoomatch.databinding.ItemPetBinding
import com.example.zoomatch.ui.homeScreen.HomeViewModelFactory
import kotlinx.coroutines.launch


class RequestFragment : Fragment() {
  private var _binding: FragmentRequestBinding? = null
  private val binding get() = _binding!!
  private val viewModel: RequestViewModel by viewModels { HomeViewModelFactory(requireActivity().application) }
  private lateinit var adapter: PetAdapter
  private lateinit var layoutManager: GridLayoutManager

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View {
    _binding = FragmentRequestBinding.inflate(inflater, container, false)
    adapter = PetAdapter()
    layoutManager = GridLayoutManager(requireContext(), 2)

    binding.requestsRecyclerView.apply {
      layoutManager = this@RequestFragment.layoutManager
      adapter = this@RequestFragment.adapter
    }
    return binding.root
  }

  @SuppressLint("UnsafeRepeatOnLifecycleDetector")
  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    viewModel.load()
    viewLifecycleOwner.lifecycleScope.launch {
      viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
        viewModel.pets.collect { list ->
          adapter.submitList(list)
        }
      }
    }

  }

  fun updateLayoutManager(spanCount: Int) {
    if (::layoutManager.isInitialized) {
      layoutManager.spanCount = spanCount
    }
  }

  override fun onDestroyView() {
    super.onDestroyView()
    _binding = null
  }
}


class PetAdapter : ListAdapter<PetLongResponse, PetViewHolder>(Diff) {
  object Diff : DiffUtil.ItemCallback<PetLongResponse>() {
    override fun areItemsTheSame(old: PetLongResponse, new: PetLongResponse) = old.id == new.id
    override fun areContentsTheSame(old: PetLongResponse, new: PetLongResponse) = old == new
  }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PetViewHolder {
    val bind = ItemPetBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    return PetViewHolder(bind)
  }

  override fun onBindViewHolder(holder: PetViewHolder, position: Int) {
    holder.bind(getItem(position))
  }
}

class PetViewHolder(private val binding: ItemPetBinding) : RecyclerView.ViewHolder(binding.root) {
  fun bind(pet: PetLongResponse) {
    binding.petName.text = pet.name
    binding.petInfo.text = "${pet.breed.name}, ${pet.age} лет"
    ImageUtils.base64ToBitmap(pet.avatar)?.let { binding.petPhoto.setImageBitmap(it) }
      ?: binding.petPhoto.setImageResource(R.drawable.test_avatar)
    binding.petStatus.text = ""
  }
}

