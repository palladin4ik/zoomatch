package com.example.zoomatch.ui.homeScreen.pets

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.zoomatch.R
import com.example.zoomatch.data.homeScreen.pets.PetUI
import com.example.zoomatch.data.homeScreen.profile.ImageUtils
import com.example.zoomatch.databinding.HomeFragmentPetsBinding
import com.example.zoomatch.databinding.ItemPetBinding
import com.example.zoomatch.ui.homeScreen.HomeViewModelFactory
import kotlinx.coroutines.launch

class PetsFragment : Fragment() {

  private var _binding: HomeFragmentPetsBinding? = null
  private val binding get() = _binding!!

  private val viewModel: PetsViewModel by viewModels {
    HomeViewModelFactory(requireActivity().application)
  }

  private lateinit var adapter: PetsAdapter
  private var isGrid = true
  private var typeDialog: AlertDialog? = null

  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
  ): View {
    _binding = HomeFragmentPetsBinding.inflate(inflater, container, false)
    return binding.root
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    adapter = PetsAdapter(emptyList()) { petId ->
      viewModel.onPetClick(petId.toInt())
    }
    binding.petsRecyclerView.adapter = adapter

    updateLayoutManager()

    binding.viewChangeButton.setOnClickListener {
      isGrid = !isGrid
      updateLayoutManager()
      updateIcon()
    }

    binding.addPetButton.setOnClickListener {
      viewModel.onAddPetClick()
    }

    binding.searchEditText.addTextChangedListener(object : TextWatcher {
      override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
      override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
      override fun afterTextChanged(s: Editable?) {
        viewModel.searchQuery.value = s.toString()
      }
    })

    binding.filterButton.setOnClickListener {
      lifecycleScope.launch {
        viewModel.petTypes.collect { types ->
          val items = types.map { it.name }.toTypedArray()
          typeDialog?.dismiss()
          typeDialog = AlertDialog.Builder(requireContext())
            .setTitle("Фильтр по типу")
            .setItems(items) { _, which ->
              viewModel.selectedTypeId.value = types[which].id
            }
            .setNeutralButton("Сброс") { _, _ ->
              viewModel.selectedTypeId.value = null
            }
            .show()
        }
      }
    }

    observeData()
  }

  private fun observeData() {
    lifecycleScope.launch {
      repeatOnLifecycle(Lifecycle.State.STARTED) {
        launch {
          viewModel.filteredPets.collect { list ->
            adapter.update(list)
            binding.petsRecyclerView.visibility = if (list.isEmpty()) View.GONE else View.VISIBLE
            binding.emptyPetsText.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
          }
        }
        launch {
          viewModel.openAddPet.collect {
            startActivity(Intent(requireContext(), EditPetActivity::class.java))
          }
        }
        launch {
          viewModel.openEditPet.collect { petId ->
            val intent = Intent(requireContext(), EditPetActivity::class.java).apply {
              putExtra("PET_ID", petId)
            }
            startActivity(intent)
          }
        }
        launch {
          viewModel.selectedTypeId.collect { id ->
            val icon = if (id != null) R.drawable.test_avatar else R.drawable.filter_svgrepo_com
            binding.filterButton.setIconResource(icon)
          }
        }
      }
    }
  }

  private fun updateLayoutManager() {
    val spanCount = if (isGrid) 2 else 1
    binding.petsRecyclerView.layoutManager = GridLayoutManager(requireContext(), spanCount)
  }

  private fun updateIcon() {
    val icon = if (isGrid) R.drawable.grid_svgrepo_com else R.drawable.list_svgrepo_com
    binding.viewChangeButton.setIconResource(icon)
  }

  override fun onDestroyView() {
    super.onDestroyView()
    typeDialog?.dismiss()
    _binding = null
  }
}

class PetsAdapter(
  private var pets: List<PetUI>,
  private val onPetClick: (String) -> Unit
) : RecyclerView.Adapter<PetsAdapter.PetViewHolder>() {

  @SuppressLint("NotifyDataSetChanged")
  fun update(newList: List<PetUI>) {
    pets = newList
    notifyDataSetChanged()
  }

  inner class PetViewHolder(val binding: ItemPetBinding) : RecyclerView.ViewHolder(binding.root) {
    @SuppressLint("SetTextI18n")
    fun bind(pet: PetUI) {
      binding.petName.text = pet.name
      binding.petInfo.text = "${pet.breed}, ${pet.age} ${pluralYears(pet.age)}"
      binding.petStatus.text = pet.status
      binding.petStatus.setTextColor(getStatusColor(pet.status))

      ImageUtils.base64ToBitmap(pet.avatar)?.let { bitmap ->
        binding.petPhoto.setImageBitmap(bitmap)
      } ?: binding.petPhoto.setImageResource(R.drawable.test_avatar)

      binding.root.setOnClickListener {
        onPetClick(pet.id)
      }
    }

    private fun pluralYears(age: Int): String = when {
      age % 10 == 1 && age % 100 != 11 -> "год"
      age % 10 in 2..4 && age % 100 !in 12..14 -> "года"
      else -> "лет"
    }

    private fun getStatusColor(status: String): Int {
      val ctx = binding.root.context
      return when {
        status.contains("поиск") -> ContextCompat.getColor(ctx, R.color.green)
        else -> ContextCompat.getColor(ctx, R.color.gray)
      }
    }
  }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PetViewHolder {
    val binding = ItemPetBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    return PetViewHolder(binding)
  }

  override fun onBindViewHolder(holder: PetViewHolder, position: Int) {
    holder.bind(pets[position])
  }

  override fun getItemCount() = pets.size
}